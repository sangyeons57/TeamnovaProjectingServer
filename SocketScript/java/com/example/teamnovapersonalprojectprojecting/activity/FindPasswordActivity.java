package com.example.teamnovapersonalprojectprojecting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.StringCheck;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public class FindPasswordActivity extends AppCompatActivity {
    private Button nextButton;
    private TextInputEditText emailInput;
    private TextInputLayout emailInputLayout;
    private TextView lookEmailText;
    private TextView resendTokenText;
    private ConstraintLayout constraintLayout;
    private Button changePasswordButton;

    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmNewPasswordInput;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmNewPasswordLayout;

    private CheckBox passwordCheckBox;

    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        DataManager.Instance().currentContext = this;

        nextButton = findViewById(R.id.next_button);
        emailInput = findViewById(R.id.email_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        lookEmailText = findViewById(R.id.look_email_text);
        resendTokenText = findViewById(R.id.btn_resend_token);
        constraintLayout = findViewById(R.id.new_password_layout);
        changePasswordButton = findViewById(R.id.change_password_button);

        newPasswordInput = findViewById(R.id.new_password_input);
        confirmNewPasswordInput = findViewById(R.id.confirm_new_password_input);
        newPasswordLayout = findViewById(R.id.new_password_input_layout);
        confirmNewPasswordLayout = findViewById(R.id.confirm_new_password_input_layout);

        passwordCheckBox = findViewById(R.id.password_checkbox);

        // CheckBox 상태 변경 리스너 설정
        passwordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 비밀번호 보이기
                newPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmNewPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // 비밀번호 숨기기
                newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmNewPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // 커서 위치를 텍스트 끝으로 이동
            confirmNewPasswordInput.setSelection(confirmNewPasswordInput.getText().length());
        });

        nextButton.setOnClickListener(v -> {
            sendMail(emailInput.getText().toString());
        });

        resendTokenText.setOnClickListener(v -> {
            sendMail(emailInput.getText().toString());
        });


        changePasswordButton.setOnClickListener( v -> {
            if(passwordCheck(newPasswordLayout, newPasswordInput.getText().toString())&&
                    confirmPassword(newPasswordInput.getText().toString(), confirmNewPasswordInput.getText().toString())){
                changePassword(userId, newPasswordInput.getText().toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
    private void sendMail(String email) {
        if( email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("ResetPasswordRequest.php"))
                    .add("email", email);

            serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
                @Override
                protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                    super.onGetJson(jsonObject);
                    String status = jsonObject.getString("status");
                    if(status.equals("success")){
                        userId = jsonObject.getString("userId");
                        mainHandler.post(()->{
                            emailInputLayout.setError(null);
                            lookEmailText.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.INVISIBLE);
                            constraintLayout.setVisibility(View.VISIBLE);

                            //편집 비활성화
                            emailInput.setEnabled(false);
                            emailInput.setFocusable(false);
                        });
                    } else {
                        mainHandler.post(()->{
                            emailInputLayout.setError("존제하지 않는 이메일 입니다.");
                            lookEmailText.setVisibility(View.INVISIBLE);
                        });
                    }
                }
            });

        } else {
            emailInputLayout.setError("이메일 형식 틀림");
        }
    }

    private void changePassword(String userId, String newPassword) {
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("ResetPassword.php"))
                .add("user_id", userId)
                .add("new_password", newPassword);
        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback() {
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                String status = jsonObject.getString("status");
                if(status.equals("success")) {
                    mainHandler.post(()->{
                        startActivity(new Intent(FindPasswordActivity.this, LoginActivity.class));
                        Toast.makeText(FindPasswordActivity.this, "비밀번호 변경 완료", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    mainHandler.post(()->{
                        lookEmailText.setTextColor(Color.RED);
                        lookEmailText.setText("먼저 이메일에서 인증을 확인해 주세요.");
                    });
                }
            }

            @Override
            protected void onResponseFailure(Response response) throws IOException {
                super.onResponseFailure(response);
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
            confirmNewPasswordLayout.setError("패스워드와 동일하지 않습니다.");
            return false;
        } else {
            confirmNewPasswordLayout.setError(null);
            return true;
        }
    }
}
