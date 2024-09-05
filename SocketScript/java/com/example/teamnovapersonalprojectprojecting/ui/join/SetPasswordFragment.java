package com.example.teamnovapersonalprojectprojecting.ui.join;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.activity.JoinActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.StringCheck;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SetPasswordFragment extends Fragment {
    TextInputLayout passwordInputLayout;
    TextInputEditText passwordInput;
    TextInputLayout confirmPasswordInputLayout;
    TextInputEditText confirmPasswordInput;
    CheckBox passwordCheckBox;
    Button nextButton;

    JoinActivity joinActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ... other code ...

        View view = inflater.inflate(R.layout.fragment_setpassword, container, false);
        joinActivity = (JoinActivity) getActivity();

        this.passwordInput = view.findViewById(R.id.password_input);
        this.passwordInputLayout = view.findViewById(R.id.password_input_layout);
        this.confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        this.confirmPasswordInputLayout = view.findViewById(R.id.confirm_password_input_layout);
        this.nextButton = view.findViewById(R.id.next_button);
        this.passwordCheckBox = view.findViewById(R.id.password_checkbox);

        // CheckBox 상태 변경 리스너 설정
        passwordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 비밀번호 보이기
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // 비밀번호 숨기기
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // 커서 위치를 텍스트 끝으로 이동
            confirmPasswordInput.setSelection(confirmPasswordInput.getText().length());
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!passwordCheck(passwordInput.getText().toString())){
                    return;
                }
                if(!confirmPassword(passwordInput.getText().toString(), confirmPasswordInput.getText().toString())){
                    return;
                }

                joinActivity.setPassword(passwordInput.getText().toString());
                ((JoinActivity) getActivity()).showSetNameFragment();
            }
        });
        return view;
    }

    private void setCheckBox(CheckBox checkBox, TextInputEditText editText){

    }

    private boolean passwordCheck(String password){
        if (password.length() < 8) {
            passwordInputLayout.setError("패스워드는 최소 8글자 입니다.");
            return false;
        }
        if (password.length() > 100) {
            passwordInputLayout.setError("패스워드는 최대 100글자 입니다.");
            return false;
        }
        if (!StringCheck.containsUpperCase(password)) {
            passwordInputLayout.setError("최소 1개 이상의 영어 대문자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsLowerCase(password)) {
            passwordInputLayout.setError("최소 1개 이상의 영어 소문자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsDigit(password)) {
            passwordInputLayout.setError("최소 1개 이상의 숫자 를 포함 해야 합니다.");
            return false;
        }
        if (!StringCheck.containsSpecialChar(password)) {
            passwordInputLayout.setError("최소 1개 이상의 특수 문자 를 포함 해야 합니다.");
            return false;
        }
        passwordInputLayout.setError(null);
        return true;
    }

    private boolean confirmPassword(String password, String confirmPassword){
        if(!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("패스워드와 동일하지 않습니다.");
            return false;
        } else {
            confirmPasswordInputLayout.setError(null);
            return true;
        }
    }
}
