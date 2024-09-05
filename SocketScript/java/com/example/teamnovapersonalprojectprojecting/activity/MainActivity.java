package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataManager.Instance().currentContext = this;

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
        navController.setGraph(navGraph);
        NavigationUI.setupWithNavController(navView, navController);

        // testHttpGetConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    private void testHttpGetConnection() {
        Log.d("Response", "test");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://ec2-3-34-42-15.ap-northeast-2.compute.amazonaws.com/phpinfo.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Log.d("Response", response.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
