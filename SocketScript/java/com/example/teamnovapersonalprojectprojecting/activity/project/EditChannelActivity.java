package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class EditChannelActivity extends AppCompatActivity {
    private EditText channelNameEditText;
    private Button deleteChannelButton;
    private Button saveChannelButton;

    private int projectId;
    private int categoryId;
    private int channelId;
    private String categoryName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_edit_channel);
        DataManager.Instance().currentContext = this;

        channelNameEditText = findViewById(R.id.channelNameEditText);
        deleteChannelButton = findViewById(R.id.deleteChannelButton);
        saveChannelButton = findViewById(R.id.saveChannelButton);


        this.projectId = getIntent().getIntExtra(EditChannelDialogFragment.PROJECT_ID,0);
        this.categoryId = getIntent().getIntExtra(EditChannelDialogFragment.CATEGORY_ID, 0);
        this.channelId = getIntent().getIntExtra(EditChannelDialogFragment.CHANNEL_ID, 0);
        this.categoryName = getIntent().getStringExtra(EditChannelDialogFragment.CHANNEL_NAME);

        channelNameEditText.setText(categoryName);

        deleteChannelButton.setOnClickListener(this::onClickDeleteChannelButton);
        saveChannelButton.setOnClickListener(this::onClickSaveChannelButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    private void onClickDeleteChannelButton(View view){
        finish();
        JsonUtil data = new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.DELETE_CHANNEL)
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.CATEGORY_ID, categoryId)
                .add(JsonUtil.Key.CHANNEL_ID, channelId);
        SocketConnection.sendMessage(data);
        SocketEventListener.callEvent(SocketEventListener.eType.DELETE_CHANNEL, data);
        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );
    }

    private void onClickSaveChannelButton(View view){
        finish();
        String newName = channelNameEditText.getText().toString();
        DataManager.Instance().getCategoryItem(categoryId).getChannelItem(channelId).setName(newName);
        LocalDBMain.GetTable(DB_ProjectChannelList.class).setChannelName(channelId, newName);

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EDIT_CHANNEL_NAME.toString())
                .add(JsonUtil.Key.CHANNEL_ID, channelId)
                .add(JsonUtil.Key.NAME, newName)
        );

        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );
    }
}
