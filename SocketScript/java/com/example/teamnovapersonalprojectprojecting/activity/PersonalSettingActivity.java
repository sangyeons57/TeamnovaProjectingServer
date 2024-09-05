package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.ui.profile.setting.ChangeNameDialogFragment;
import com.example.teamnovapersonalprojectprojecting.ui.profile.setting.ChangePasswordActivity;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

public class PersonalSettingActivity extends AppCompatActivity {

    private TextView changeNameTextView;
    private TextView changePasswordTextView;
    private TextView logoutTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_setting);
        DataManager.Instance().currentContext = this;

        changeNameTextView = findViewById(R.id.change_name_textview);
        changePasswordTextView = findViewById(R.id.change_password_textview);
        logoutTextView = findViewById(R.id.logout_textview);


        changeNameTextView.setOnClickListener(v -> {
            ChangeNameDialogFragment dialogFragment = new ChangeNameDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "ChangeNameDialogFragment");
        });

        changePasswordTextView.setOnClickListener(v -> {
            startActivity( new Intent(PersonalSettingActivity.this, ChangePasswordActivity.class) );
        });

        logoutTextView.setOnClickListener(v -> {
            DataManager.Instance().Logout(this);;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
}
