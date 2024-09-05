package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.ProjectAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class AddChannelActivity extends AppCompatActivity {
    private EditText channelNameTextView;
    private Button createChannelButton;
    private TextView warningTextView;

    private String channelName;

    private int projectId;
    private int categoryId;;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_create_channel);
        DataManager.Instance().currentContext = this;
        Intent intent = getIntent();

        projectId = intent.getIntExtra(EditCategoryDialogFragment.PROJECT_ID, 0);
        categoryId = intent.getIntExtra(EditCategoryDialogFragment.CATEGORY_ID, 0);

        channelNameTextView = findViewById(R.id.channelNameTextView);
        createChannelButton = findViewById(R.id.createChannelButton);
        warningTextView = findViewById(R.id.warningTextView);

        createChannelButton.setOnClickListener(this::createChannelOnClick);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataManager.Instance().currentContext = this;
    }
    public void createChannelOnClick(View view){
        channelName = channelNameTextView.getText().toString().trim();
        if(channelName.isEmpty()){
            warningTextView.setText("카테고리 이름을 입력해주세요.");
            return;
        } else {
            warningTextView.setText("");
        }

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.CREATE_CHANNEL.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.CATEGORY_ID, categoryId)
                .add(JsonUtil.Key.NAME, channelName));

        finish();
    }
}
