package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.MainActivity;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class AddProjectSetNameActivity extends AppCompatActivity {
    public static final String IS_PRIVATE = "isPrivate";

    private Button joinNewProjectButton;
    private EditText joinNewProjectInput;

    private Intent intent;
    private boolean isPrivate;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_project_name);
        DataManager.Instance().currentContext = this;

        joinNewProjectInput = findViewById(R.id.project_join_input);
        joinNewProjectButton = findViewById(R.id.project_join_button);

        intent = getIntent();
        isPrivate = intent.getBooleanExtra(IS_PRIVATE, false);

        joinNewProjectButton.setOnClickListener(v -> createNewProject());
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    public void createNewProject(){
        String projectName = joinNewProjectInput.getText().toString();

        if(projectName.trim().isEmpty()){
            return;
        }
        // 프로젝트 이름을 기반으로 소켓 서버에 프로젝트 생성 요청 보네기
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.CREATE_PROJECT)
                .add(JsonUtil.Key.PROJECT_NAME, projectName)
                .add(JsonUtil.Key.IS_PRIVATE, isPrivate));

        Intent intent = new Intent(this, MainActivity.class);

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.CREATE_PROJECT, new SocketEventListener.EventListener() {
            @Override
            public boolean run(JsonUtil jsonUtil) {

                startActivity(intent);
                SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.CREATE_PROJECT, this);
                return false;
            }
        });

    }
}
