package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.FriendsActivity;
import com.example.teamnovapersonalprojectprojecting.activity.project.EditChannelDialogFragment;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FriendList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemberAddDialogFragment extends DialogFragment {

    public static final String PROJECT_ID = "projectId";

    public static MemberAddDialogFragment Instance(int projectId){
        MemberAddDialogFragment dialogFragment = new MemberAddDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PROJECT_ID, projectId);
        dialogFragment.setArguments(bundle);
        return  dialogFragment;
    }

    private ImageButton copyLinkButton;
    private RecyclerView recyclerView;

    private int projectId;
    private DataAdapter adapter; // RecyclerView.Adapter
    private List<FriendsActivity.DataModel> dataModelList; // 데이터 목록
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_add_member, container, false);
        copyLinkButton = view.findViewById(R.id.copy_link_button);
        recyclerView = view.findViewById(R.id.friendRecyclerView);

        projectId = getArguments().getInt(PROJECT_ID);

        dataModelList = new ArrayList<>();
        dataModelList.addAll(LocalDBMain.GetTable(DB_FriendList.class).getFriendListUseDataModel());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new DataAdapter(projectId, dataModelList));


        copyLinkButton.setOnClickListener(this::onClickCopyLinkButton);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    public void onClickCopyLinkButton(View view){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId));
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN){
            @Override
            public boolean runOnce(JsonUtil jsonUtil) {
                String token = jsonUtil.getString(JsonUtil.Key.TOKEN, "");
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("token", getJoinLink(token));
                clipboardManager.setPrimaryClip(clip);

                DataManager.Instance().mainHandler.post(()->{
                    Toast.makeText(getContext(), "copy link", Toast.LENGTH_SHORT).show();
                });
                return false;
            }
        });
    }

    public static String getJoinLink(String token){
        return "http://" + SocketConnection.SERVER_ADDRESS + "/invite?token=" + token ;
    }


    public static class DataAdapter extends RecyclerView.Adapter<DataViewHolder> {
        public List<FriendsActivity.DataModel> dataModelList;
        public int projectId;
        public DataAdapter (int projectId, List<FriendsActivity.DataModel> dataList) {
            this.dataModelList = dataList;
            this.projectId = projectId;
        }

        @NonNull
        @Override
        public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new DataViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
            holder.setData(dataModelList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        private DataAdapter dataAdapter;
        FriendsActivity.DataModel dataModel;
        TextView nameTextView;
        ImageView friendProfileImage;
        Button friendButton;
        int userId;

        public DataViewHolder(@NonNull View itemView, DataAdapter dataAdapter) {
            super(itemView);
            this.dataAdapter = dataAdapter;
            nameTextView = itemView.findViewById(R.id.friendName);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
            friendButton = itemView.findViewById(R.id.friendButton);

            friendButton.setText("초대");

            friendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SocketConnection.sendMessage(false, new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN.toString())
                            .add(JsonUtil.Key.PROJECT_ID, dataAdapter.projectId));
                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_PROJECT_MEMBER_JOIN_TOKEN){
                        @Override
                        public boolean runOnce(JsonUtil jsonUtil) {
                            String token = jsonUtil.getString(JsonUtil.Key.TOKEN, "");
                            SocketConnection.sendMessage(false, new JsonUtil()
                                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SEND_MESSAGE_SPECIFIC_PERSON)
                                    .add(JsonUtil.Key.OTHER_ID, userId)
                                    .add(JsonUtil.Key.MESSAGE, getJoinLink(token)));
                            return false;
                        }
                    });
                }
            });
        }

        public void setData(FriendsActivity.DataModel dataModel){
            this.dataModel = dataModel;
            userId = dataModel.getUserId();

            LocalDBMain.GetTable(DB_UserList.class).getUser(userId).execute(new CursorReturn.Execute() {
                @Override
                public void run(Cursor cursor) {
                    if(cursor.moveToFirst()) {
                        nameTextView.setText(cursor.getString(1));
                    }
                }
            });
        }
    }
}
