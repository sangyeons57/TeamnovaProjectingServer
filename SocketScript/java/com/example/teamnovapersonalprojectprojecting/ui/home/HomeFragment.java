package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetProjectData;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private ImageButton dmButton;
    private RecyclerView contentRecyclerview;

    private DMAdapter dmAdapterMyAdapter;
    private ProjectAdapter projectAdapter;

    private RecyclerView projectListRecyclerView;
    private ProjectListAdapter projectListAdapter;
    private List<ProjectListAdapter.MyItem> projectListItemList;

    private SocketEventListener.EventListener reloadDmListListener;
    private SocketEventListener.EventListener reloadProjectListListener;
    private SocketEventListener.EventListener displayProjectEventListener;
    private SocketEventListener.EventListener getProjectDataEventListener;

    private FragmentManager fragmentManager;


    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        projectListItemList = new ArrayList<>();
        projectListAdapter = new ProjectListAdapter(projectListItemList);


        dmButton = view.findViewById(R.id.friendButton);
        contentRecyclerview = view.findViewById(R.id.content_recyclerview);
        contentRecyclerview.setLayoutManager(new LinearLayoutManager(view.getContext()));
        contentRecyclerview.setAdapter(setDMAdapterItemList());

        projectListRecyclerView = view.findViewById(R.id.project_list_recyclerview);
        projectListRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        projectListRecyclerView.setAdapter(projectListAdapter);

        fragmentManager = getChildFragmentManager();

        if(DataManager.Instance().userId != DataManager.NOT_SETUP_I){
            setHomeProjectList(view);
            setHomeContentDM(view);
        }

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.JOIN_PROJECT, (j)->{
            setHomeProjectList(view);
            return false;
        });

        dmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHomeContentDM(v);
            }
        });


        SocketEventListener.addAddEventQueue(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, displayProjectEventListener = (jsonUtil)-> {
            SocketEventListener.eType type = SocketEventListener.eType.toType(jsonUtil.getString(JsonUtil.Key.TYPE, ""));
            SocketConnection.LOG(type.toString());
            if (!type.equals(SocketEventListener.eType._RELOAD)) {
                DataManager.Instance().projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, 0);
                DataManager.Instance().projectName = jsonUtil.getString(JsonUtil.Key.PROJECT_NAME, "");
                setHomeContentProject(view);
            }
            DataManager.Instance().mainHandler.post(()->projectAdapter.notifyDataSetChanged());

            return false;
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_DATA, getProjectDataEventListener = (j)->{
            SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                    .add(JsonUtil.Key.PROJECT_ID, DataManager.Instance().projectId)
                    .add(JsonUtil.Key.PROJECT_NAME, DataManager.Instance().projectName));
            return false;
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.RELOAD_DM_LIST, reloadDmListListener = (j) -> {
            DataManager.Instance().mainHandler.post(()-> setDMAdapterItemList().notifyDataSetChanged() );
            return false;
        });

        reloadProjectListListener = (j)->{
            Log.d("ReloadProjectListListener", "reloadProjectListListener");
            DataManager.Instance().mainHandler.post(projectAdapter::notifyDataSetChanged);
            return false;
        };
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.EDIT_CATEGORY_NAME, reloadProjectListListener );
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.CREATE_CHANNEL, reloadProjectListListener);

        LocalDBMain.printAllRows(DB_UserList.class);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalDBMain.printAllRows(DB_Project.class);
        LocalDBMain.printAllRows(DB_FileList.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHomeProjectList(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.RELOAD_DM_LIST, reloadDmListListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.EDIT_CATEGORY_NAME, reloadProjectListListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.CREATE_CHANNEL, reloadProjectListListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, displayProjectEventListener);
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.GET_PROJECT_DATA, getProjectDataEventListener);
    }

    private void setHomeProjectList(View view) {

        new Retry(()->{
            try{
                LocalDBMain.GetTable(DB_Project.class)
                        .getDefaultDataCursor()
                        .execute(this::displayDataFromLocalDB);
                return true;
            } catch (IllegalStateException e){
                e.printStackTrace();
                return false;
            }
        }).setMaxRetries(5).setRetryInterval(100).execute();

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_ALL_PROJECT_USER_INCLUDED)
                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_ALL_PROJECT_USER_INCLUDED, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_ALL_PROJECT_USER_INCLUDED){
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                LocalDBMain.GetTable(DB_Project.class)
                        .getDefaultDataCursor()
                        .execute(this::display);
                return false;
            }

            private void display(Cursor cursor) {
                projectListItemList.clear();
                while (cursor.moveToNext()){
                    projectListItemList.add( new ProjectListAdapter.MyItem(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getInt(2)
                    ));
                }
                projectListItemList.add(new ProjectListAdapter.MyItem( DataManager.NOT_SETUP_I, "", DataManager.NOT_SETUP_I ));
                DataManager.Instance().mainHandler.post(projectListAdapter::notifyDataSetChanged);
            }
        });
    }

    private void displayDataFromLocalDB(Cursor cursor){
        projectListItemList.clear();
        while (cursor.moveToNext()){
            projectListItemList.add( new ProjectListAdapter.MyItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            ));
        }
        projectListItemList.add(new ProjectListAdapter.MyItem( 0, "", DataManager.NOT_SETUP_I ));
        DataManager.Instance().mainHandler.post(projectListAdapter::notifyDataSetChanged);
    }

    public void setHomeContentProject(View view){
        //retry 페턴 적용 필요 해당 util이나 기능 만들면될듯
        DataManager.Instance().mainHandler.post(()->{
            fragmentManager.beginTransaction()
                    .replace( R.id.top_section, new TopSectionProject())
                    .commitAllowingStateLoss();

            contentRecyclerview.setAdapter(setProjectItemList());
        });
    }

    private void setHomeContentDM(View view){
        //retry 페턴 적용 필요 해당 util이나 기능 만들면될듯
        fragmentManager.beginTransaction()
                .replace( R.id.top_section, new TopSectionDM())
                .commitAllowingStateLoss();
        contentRecyclerview.setAdapter(setDMAdapterItemList());
    }

    private DMAdapter setDMAdapterItemList(){

        //여기 계속해서 hasBeenCloased 에러 발생함
        new Retry(() ->{
            try {
                LocalDBMain.GetTable(DB_DMList.class).getAllOrderByLastTime().execute(new CursorReturn.Execute() {
                    @Override
                    public void run(Cursor cursor) {
                        DataManager.Instance().dmItemList.clear();
                        while (cursor.moveToNext()){

                            int channelId = cursor.getInt(0);
                            int otherId = cursor.getInt(1);
                            String lastTime = cursor.getString(2);
                            String otherUsername = cursor.getString(3);
                            int profileImageId = cursor.getInt(4);
                            LocalDBMain.LOG(channelId + " " + otherId + " " + lastTime + " " + otherUsername + " " + profileImageId);

                            DataManager.Instance().dmItemList
                                    .add(new DMAdapter.DataModel(otherUsername, channelId, profileImageId));
                        }
                    }
                });
                return true;
            } catch (IllegalStateException e){
                e.printStackTrace();
                return false;
            }
        }).setMaxRetries(5).setRetryInterval(0).execute();

        if (dmAdapterMyAdapter == null) {
            dmAdapterMyAdapter = new DMAdapter(DataManager.Instance().dmItemList);
        }
        return dmAdapterMyAdapter;
    }

    private ProjectAdapter setProjectItemList(){
        new Retry(()->{
            try {
                JSONObject structure = LocalDBMain.GetTable(DB_ProjectStructure.class).getStructureById(DataManager.Instance().projectId);
                DataManager.Instance().projectItemList = GetProjectData.getProjectItemListFromStructure(structure);
                projectAdapter = new ProjectAdapter(DataManager.Instance().projectItemList, getParentFragmentManager());
                return true;
            } catch (IllegalStateException e){
                e.printStackTrace();
            }
            return false;
        }).setMaxRetries(5).setRetryInterval(0).execute();;

        return projectAdapter;
    }
}