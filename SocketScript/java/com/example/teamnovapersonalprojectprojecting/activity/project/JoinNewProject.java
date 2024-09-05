package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

public class JoinNewProject extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_new_project);
        DataManager.Instance().currentContext = this;
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
}
