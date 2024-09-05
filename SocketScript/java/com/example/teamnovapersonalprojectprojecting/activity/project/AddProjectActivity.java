package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

public class AddProjectActivity extends AppCompatActivity {
    private Button joinNewProjectButton;
    private Button startNewOpenProjectButton;
    private Button startNewPrivateProjectButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        DataManager.Instance().currentContext = this;

        joinNewProjectButton = findViewById(R.id.project_join_button);
        startNewOpenProjectButton = findViewById(R.id.start_new_open_project_button);
        startNewPrivateProjectButton = findViewById(R.id.start_new_private_project_button);

        startNewOpenProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProjectActivity.this, AddProjectSetNameActivity.class);
            intent.putExtra(AddProjectSetNameActivity.IS_PRIVATE, false);
            startActivity(intent);
        });
        startNewPrivateProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProjectActivity.this, AddProjectSetNameActivity.class);
            intent.putExtra(AddProjectSetNameActivity.IS_PRIVATE, true);
            startActivity(intent);
        });
        joinNewProjectButton.setOnClickListener( v -> {
            Intent intent = new Intent(AddProjectActivity.this, JoinNewProject.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

}
