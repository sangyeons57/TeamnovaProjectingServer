package com.example.teamnovapersonalprojectprojecting.ui.profile.setting;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangeNameDialogFragment extends DialogFragment {

    private Handler mainHandler;
    TextInputEditText changeNameInput;
    TextInputLayout changeNameInputLayout;
    Button changeNameButton;

    private String newUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_change_name, container, false);
        this.changeNameInput = view.findViewById(R.id.change_name_input);
        this.changeNameInputLayout = view.findViewById(R.id.change_name_input_layout);
        this.changeNameButton = view.findViewById(R.id.change_name_button);

        mainHandler = new Handler(Looper.getMainLooper());

        changeNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUsername = changeNameInput.getText().toString();
                ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CHANGE_PROFILE.getPath("ChangeName.php"))
                        .add("username", newUsername);

                serverConnectManager.postEnqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                String status = jsonObject.getString("status");
                                if (status.equals("success")){
                                    DataManager.Instance().username = newUsername;
                                    mainHandler.post(() -> {
                                        dismiss();
                                        Toast.makeText(getContext(), "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                    });
                                } else if (status.equals("message_error")) {
                                    String message = jsonObject.getString("message");
                                    mainHandler.post(() -> {
                                        changeNameInputLayout.setError(message);
                                    });
                                } else if (status.equals("session_error")) {
                                    //사용자를 강제 로그아웃 시킨다.
                                    String message = jsonObject.getString("message");
                                    Log.d("ChangeStatusDialogFragment", "강제 로그아웃");
                                    Log.d("ChangeStatusDialogFragment", message);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Log.e("ChangeStatusDialogFragment", "Response not successful");
                            String responseBody = response.body().string();
                            Log.e("ChangeStatusDialogFragment", "Response body:" + responseBody);
                        }
                    }
                });
            }
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
}
