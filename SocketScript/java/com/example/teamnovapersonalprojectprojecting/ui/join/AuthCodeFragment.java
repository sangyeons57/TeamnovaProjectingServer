package com.example.teamnovapersonalprojectprojecting.ui.join;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.activity.JoinActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class AuthCodeFragment extends Fragment {
    private TextInputLayout authCodeInputLayout;
    private TextInputEditText authCodeInput;
    private TextView textViewTimer;
    private Button btnResendCode;
    private Button btnNext;

    private CountDownTimer countDownTimer;
    private boolean isTimerFinish;

    private static final long ONE_SECOND = 1000; // 1 second
    private static final long ONE_MINUTE = 60 * 1000; // 1 minute

    private static final long TIMER_DURATION = 3 * ONE_MINUTE; // 30 seconds
    private static final long TIMER_INTERVAL = 1 * ONE_SECOND; // 1 second

    public static boolean isAuthenticated = false;

    private JoinActivity joinActivity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authcode, container, false);
        authCodeInputLayout = view.findViewById(R.id.authCodeInputLayout);
        authCodeInput = view.findViewById(R.id.authCodeInput);
        textViewTimer = view.findViewById(R.id.textview_timer);
        btnResendCode = view.findViewById(R.id.btn_resend_code);
        btnNext = view.findViewById(R.id.next_button);

        joinActivity = (JoinActivity) getActivity();

        isAuthenticated = false;

        sendAuthentication(joinActivity.getEmail());

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText authCodeInput = view.findViewById(R.id.authCodeInput);
                String authCode = authCodeInput.getText().toString();
                Log.d("AuthCodeFragment", "authCode: " + authCode);

                checkValidAuthCode(joinActivity.getEmail(), authCode);
            }
        });

        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAuthentication(joinActivity.getEmail());
            }
        });
        return view;
    }

    private void checkValidAuthCode(String email, String code){
        if(isTimerFinish){
            authCodeInputLayout.setError("제한 시간이 초과");
            return;
        }
        if(code.trim().length() != 6) {
            authCodeInputLayout.setError("잘못된 입력");
            return;
        }

        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e){
            authCodeInputLayout.setError("잘못된 입력");
            return ;
        }

        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("VerifyAuthenticationCode.php"))
                .add("email", email)
                .add("code", code);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String status = jsonObject.getString("status");
                final String message = jsonObject.getString("message");
                Log.d("EmailFragment", status);
                if (status.equals("success")) {
                    setAuthCodeError(null);
                    isAuthenticated = true;
                    joinActivity.setAuthenticated();
                    joinActivity.showPasswordFragment();
                } else {
                    setAuthCodeError(message);
                }
            }
            @Override
            protected void onResponseFailure(Response response) throws IOException {
                super.onResponseFailure(response);
                setAuthCodeError("이메일 인증에 실패했습니다.");
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                setAuthCodeError("이메일 인증에 실패했습니다.");
            }
        });
    }
    private void setAuthCodeError(String message){
        joinActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                authCodeInputLayout.setError(message);
            }
        });
    }

    private void startTimer(){
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerFinish = false;

        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / ONE_MINUTE;
                long seconds = (millisUntilFinished % ONE_MINUTE) / ONE_SECOND;
                textViewTimer.setText(String.format("남은 시간: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                textViewTimer.setText("남은 시간: 00:00");
                // btnResendCode.setEnabled(true); // 시간 종료 후 다시 받기 버튼 활성화
                isTimerFinish = true;
            }
        }.start();
        // btnResendCode.setEnabled(false); // 타이머가 시작되면 다시 받기 버튼 비활성화
    }


    private void sendAuthentication(String email){
        startTimer();

        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("SendAuthenticationCode.php"))
                .add("email",email);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("EmailFragment", "Failed to send authentication code", e);
            }

            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String message = jsonObject.getString("message");
                final String status = jsonObject.getString("status");
                Log.d("EmailFragment", status);
                Log.d("EmailFragment", message);
            }

            @Override
            protected void onResponseFailure(Response response) throws IOException {
                super.onResponseFailure(response);
                joinActivity.showEmailFragment();
            }
        });

    }
}