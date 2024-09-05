package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

import java.nio.file.Files;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        DataManager.Instance().currentContext = this;
        DataManager.Instance().mainHandler = new Handler(Looper.getMainLooper());
        boolean isFirstRun = preferences.getBoolean(KEY_IS_FIRST_RUN, true);


        if(isFirstRun){
            setContentView(R.layout.activity_splash);
            new Handler().postDelayed(()-> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }, SPLASH_DISPLAY_LENGTH);


            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_IS_FIRST_RUN, false);
            editor.apply();
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));

            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
}
