package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.ui.join.AuthCodeFragment;
import com.example.teamnovapersonalprojectprojecting.ui.join.EmailFragment;
import com.example.teamnovapersonalprojectprojecting.ui.join.SetNameFragment;
import com.example.teamnovapersonalprojectprojecting.ui.join.SetPasswordFragment;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.EncryptedSharedPrefsManager;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JoinActivity extends AppCompatActivity {
    private String joinEmail;
    private boolean isAuthenticated;
    private String joinPassword;
    private String joinName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join); // Your activity layout
        DataManager.Instance().currentContext = this;

        // Start with the EmailFragment
        showEmailFragment();
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }

    //필요한 정보를 다 체운후 계정 생성 하는 코드
    public void generateAccount(){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("CreateAccount.php"))
                .add("joinEmail",joinEmail)
                .add("joinEmail", joinEmail)
                .add("joinPassword", joinPassword)
                .add("joinName", joinName);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String status = jsonObject.getString("status");
                final String message = jsonObject.getString("message");
                mainHandler.post(()->{
                    if(status.equals("success")){
                        Toast.makeText(JoinActivity.this, "계정 생성 완료", Toast.LENGTH_SHORT).show();
                        EncryptedSharedPrefsManager.init(JoinActivity.this, EncryptedSharedPrefsManager.LOGIN);
                        EncryptedSharedPrefsManager.putString("email", joinEmail);
                        EncryptedSharedPrefsManager.putString("password", joinPassword);

                        //계정 생성 성공시 로그인 하는 부분
                        startActivity(new Intent(JoinActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(JoinActivity.this, "잘못된 접근", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d("JoinActivity", status);
                Log.d("JoinActivity", message);
            }
        });
    }

    // Method to replace fragments
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    // Add methods to handle button clicks and switch fragments
    public void showEmailFragment() {
        replaceFragment(new EmailFragment());
    }

    public void showAuthCodeFragment() {
        replaceFragment(new AuthCodeFragment());
    }

    public void showPasswordFragment() {
        replaceFragment(new SetPasswordFragment());
    }
    public void showSetNameFragment() {
        replaceFragment(new SetNameFragment());
    }

    public void setEmail(String email){
        this.joinEmail = email;
    }
    public void setPassword(String password){
        this.joinPassword = password;
    }
    public void setName(String name){
        this.joinName = name;
    }
    public String getEmail() {
        return joinEmail;
    }
    public void setAuthenticated(){isAuthenticated = true;}

}
