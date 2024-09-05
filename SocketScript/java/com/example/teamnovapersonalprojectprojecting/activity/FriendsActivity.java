package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FriendList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.FriendAddDialogFragment;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private Button acceptFriendButton;
    private ImageButton backButton;
    private TextView addFriendTextView;
    private RecyclerView recyclerView;

    SocketEventListener.EventListener eventListener;

    private FriendsActivity.DataAdapter adapter; // RecyclerView.Adapter
    private List<FriendsActivity.DataModel> dataModelList; // 데이터 목록

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        DataManager.Instance().currentContext = this;

        this.recyclerView = findViewById(R.id.recyclerview);
        acceptFriendButton = findViewById(R.id.acceptFriendButton);
        backButton = findViewById(R.id.backButton);
        addFriendTextView = findViewById(R.id.addFriedTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataModelList = new ArrayList<>();
        adapter = new FriendsActivity.DataAdapter(dataModelList);
        recyclerView.setAdapter(adapter);

        dataModelList.addAll(LocalDBMain.GetTable(DB_FriendList.class).getFriendListUseDataModel());

        acceptFriendButton.setOnClickListener(v -> {
            startActivity(new Intent(FriendsActivity.this, FriendsAcceptActivity.class));
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        addFriendTextView.setOnClickListener(v -> {
            FriendAddDialogFragment dialogFragment = new FriendAddDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "FriendAddDialogFragment");
        });


        eventListener = (jsonUtil)->{
            runOnUiThread(recyclerView.getAdapter()::notifyDataSetChanged);
            return false;
        };
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.UPDATE_FRIEND_LIST, eventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.UPDATE_FRIEND_LIST, eventListener);
    }
    public static class DataAdapter extends RecyclerView.Adapter<FriendsActivity.DataViewHolder> {
        public List<FriendsActivity.DataModel> dataModelList;
        public DataAdapter (List<FriendsActivity.DataModel> dataList) {
            this.dataModelList = dataList;
        }

        @NonNull
        @Override
        public FriendsActivity.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new FriendsActivity.DataViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsActivity.DataViewHolder holder, int position) {
            holder.setData(dataModelList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataModelList.size();
        }
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        private FriendsActivity.DataAdapter dataAdapter;
        FriendsActivity.DataModel dataModel;
        TextView nameTextView;
        ImageView friendProfileImage;
        int userId;
        int channelId;

        public DataViewHolder(@NonNull View itemView, FriendsActivity.DataAdapter dataAdapter) {
            super(itemView);
            this.dataAdapter = dataAdapter;
            nameTextView = itemView.findViewById(R.id.friendName);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);
            itemView.findViewById(R.id.friendButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.JOIN_CHANNEL)
                            .add(JsonUtil.Key.CHANNEL_ID, channelId));
                }
            });
        }

        public void setData(FriendsActivity.DataModel dataModel){
            this.dataModel = dataModel;
            userId = dataModel.getUserId();
            channelId = LocalDBMain.GetTable(DB_DMList.class).getChannelId(userId);

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

    public static class DataModel {
        private int userId;
        public DataModel(int userId) {
            this.userId = userId;
        }

        // Getters
        public int getUserId() { return userId; }
    }
}
