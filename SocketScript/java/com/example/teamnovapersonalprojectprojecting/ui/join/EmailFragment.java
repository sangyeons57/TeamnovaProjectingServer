package com.example.teamnovapersonalprojectprojecting.ui.join;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;


public class EmailFragment extends Fragment {
    JoinActivity joinActivity;
    private Handler mainHandler;
    TextInputLayout emailInputLayout;
    TextInputEditText emailInput;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email, container, false);
        emailInput = view.findViewById(R.id.emailInput);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);

        joinActivity = (JoinActivity) getActivity();
        mainHandler = new Handler(Looper.getMainLooper());

        Button nextButton = view.findViewById(R.id.next_button);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailInput.getText().toString();

                    new Thread(()->{
                        if(isValidEmail(email)){
                            Log.d("JoinActivity", "valid");
                            joinActivity.setEmail(email);
                            joinActivity.showAuthCodeFragment();
                        }
                    }).start();
                }
            });

            return view;
    }

    private boolean isValidEmail(String email){
        if(email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("CheckId.php"))
                    .add("joinEmail", email);

            try(Response response = serverConnectManager.postExecute()){
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("JoinActivity", "responseBody: " +  responseBody);
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);

                        boolean exists = jsonObject.getBoolean("exists");
                        Log.d("JoinActivity", ""+exists);

                        if (!exists) { //존재하지 않음 성공
                            return true;
                        }

                        //이미 존재함 실패
                        mainHandler.post(()->{
                            emailInputLayout.setError("이미 존재하는 이름 입니다.");
                        });
                    } catch (JSONException e) {
                        //json 실패
                        throw new RuntimeException(e);
                    }
                } else {
                    //응답 실패
                    Log.d("JoinActivity", ""+response.isSuccessful());
                    Log.d("JoinActivity", ""+response.request().toString());
                    Log.d("JoinActivity", response.body().string());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            mainHandler.post(()->{
                emailInputLayout.setError("잘못된 형식의 이메일 입니다.");
            });
        }
        return false;
    }

}
