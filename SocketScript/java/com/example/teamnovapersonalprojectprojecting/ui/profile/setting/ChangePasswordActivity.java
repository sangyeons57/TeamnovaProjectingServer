package com.example.teamnovapersonalprojectprojecting.ui.profile.setting;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.StringCheck;
import com.example.teamnovapersonalprojectprojecting.util.EncryptedSharedPrefsManager;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChangePasswordActivity extends AppCompatActivity {
    TextInputLayout currentPasswordInputLayout;
    TextInputEditText currentPasswordInput;
    TextInputLayout newPasswordInputLayout;
    TextInputEditText newPasswordInput;
    TextInputLayout confirmNewPasswordInputLayout;
    TextInputEditText confirmNewPasswordInput;
    CheckBox passwordCheckBox;
    Button nextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_setting_change_password);

        this.currentPasswordInput = findViewById(R.id.current_password_input);
        this.currentPasswordInputLayout = findViewById(R.id.current_password_input_layout);
        this.newPasswordInput = findViewById(R.id.new_password_input);
        this.newPasswordInputLayout = findViewById(R.id.new_password_input_layout);
        this.confirmNewPasswordInput = findViewById(R.id.confirm_new_password_input);
        this.confirmNewPasswordInputLayout = findViewById(R.id.confirm_new_password_input_layout);
        this.passwordCheckBox = findViewById(R.id.password_checkbox);
        this.nextButton = findViewById(R.id.next_button);

        // CheckBox 상태 변경 리스너 설정
        passwordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 비밀번호 보이기
                currentPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                newPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmNewPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // 비밀번호 숨기기
                currentPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmNewPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // 커서 위치를 텍스트 끝으로 이동
            confirmNewPasswordInput.setSelection(confirmNewPasswordInput.getText().length());
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordCheck(newPasswordInputLayout, newPasswordInput.getText().toString()) &&
                        confirmPassword(newPasswordInput.getText().toString(), confirmNewPasswordInput.getText().toString())){
                    changePassword();;
                }
            }
        });
    }
    private void changePassword(){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CHANGE_PROFILE.getPath("ChangePassword.php"))
                .add("current_password", currentPasswordInput.getText().toString())
                .add("new_password", newPasswordInput.getText().toString());
        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String status = jsonObject.getString("status");
                final String message = jsonObject.getString("message");
                if(status.equals("success")) {
                    //Toast 가 나오도록 변경 필요함 나중에 callback클래스 개편 이후에 적용
                    ServerConnectManager.Log("패스워드 변경 성공");
                    EncryptedSharedPrefsManager.init(ChangePasswordActivity.this, EncryptedSharedPrefsManager.LOGIN);
                    EncryptedSharedPrefsManager.putString("password", newPasswordInput.getText().toString());

                    finish();
                } else if(status.equals("message_error")) {
                    mainHandler.post(()->{
                        currentPasswordInputLayout.setError(message);
                    });
                } else if(status.equals("session_error")) {
                    //강제 로그아웃 시켜야함
                }
                ServerConnectManager.Log(status);
                ServerConnectManager.Log(message);
            }
        });
    }

    private boolean passwordCheck(TextInputLayout layout, String password){
        if (password.length() < 8) {
            layout.setError("패스워드는 최소 8글자 입니다.");
            return false;
        }
        if (password.length() > 100) {
            layout.setError("패스워드는 최대 100글자 입니다.");
            return false;
        }
        if (!StringCheck.containsUpperCase(password)) {
            layout.setError("최소 1개 이상의 영어 대문자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsLowerCase(password)) {
            layout.setError("최소 1개 이상의 영어 소문자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsDigit(password)) {
            layout.setError("최소 1개 이상의 숫자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsSpecialChar(password)) {
            layout.setError("최소 1개 이상의 특수 문자 를 포함 해야 합니다.");
            return false;
        }
        layout.setError(null);
        return true;
    }

    private boolean confirmPassword(String password, String confirmPassword){
        if(!password.equals(confirmPassword)) {
            confirmNewPasswordInputLayout.setError("패스워드와 동일하지 않습니다.");
            return false;
        } else {
            confirmNewPasswordInputLayout.setError(null);
            return true;
        }
    }
}
