package com.example.teamnovapersonalprojectprojecting.ui.join;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public class SetNameFragment extends Fragment {
    private Handler mainHandler;
    private TextInputEditText nameInputEditText;
    private TextInputLayout nameInputLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setname, container, false);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        nameInputEditText = view.findViewById(R.id.nameInput);

        mainHandler = new Handler(Looper.getMainLooper());

        Button nextButton = view.findViewById(R.id.next_button);
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = nameInputEditText.getText().toString();
                    Log.d("JoinActivity", name);
                    new Thread(()->{
                        if(isValidName(name) && AuthCodeFragment.isAuthenticated){
                            ((JoinActivity) getActivity()).setName(name);
                            ((JoinActivity) getActivity()).generateAccount();
                        }
                    }).start();
                }
            });

            return view;
    }

    private boolean isValidName(String name){
        if(name.trim().length() != name.length()){
            mainHandler.post(()->{
                nameInputLayout.setError("처음과 끝은 공백 으로 이루어 지면 안됩니다.");
            });
            return false;
        }
        if(name.trim().length() == 0 && name.length() > 20){
            mainHandler.post(()->{
                nameInputLayout.setError("이름은 최대 길이는 20 입니다.");
            });
            return false;
        }

        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("CheckName.php"))
                .add("joinName", name);

        try(Response response = serverConnectManager.postExecute()){
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Log.d("JoinActivity", responseBody);
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean exists = jsonObject.getBoolean("exists");
                    if (exists) {
                        //이미 존재함 실패
                        mainHandler.post(()->{
                            nameInputLayout.setError("이미 존재하는 이름 입니다.");
                        });
                        return false;
                    }
                } catch (JSONException e) {
                    //json 실패
                    throw new RuntimeException(e);
                }
            } else {
                //응답 실패
                Log.d("JoinActivity", ""+response.isSuccessful());
                Log.d("JoinActivity", response.body().string());
                mainHandler.post(()->{
                    nameInputLayout.setError("확인 실패");
                });
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        mainHandler.post(()-> {
            nameInputLayout.setError(null);
        });
        return true;
    }
}