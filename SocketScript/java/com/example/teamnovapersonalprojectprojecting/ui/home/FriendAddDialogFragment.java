package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;
import com.google.android.material.textfield.TextInputEditText;

public class FriendAddDialogFragment extends DialogFragment {
    TextInputEditText searchNameInput;
    Button searchNameButton;
    TextView infoTextView;
    Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_friend_add, container, false);
        this.searchNameInput = view.findViewById(R.id.searchNameInput);
        this.searchNameButton = view.findViewById(R.id.searchNameButton);
        this.infoTextView = view.findViewById(R.id.infoTextView);

        mainHandler = new Handler(Looper.getMainLooper());

        this.searchNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waitingUserName = searchNameInput.getText().toString().trim();
                if(waitingUserName.equals(DataManager.Instance().username)){
                    infoTextView.setText("자기자신 에게는 친구요청을 보낼수 없습니다.");
                    infoTextView.setVisibility(View.VISIBLE);
                    infoTextView.setTextColor(Color.RED);
                    return;
                }

                SocketConnection.sendMessage(new JsonUtil()
                        .add(JsonUtil.Key.TYPE, SocketEventListener.eType.ADD_WAITING)
                        .add(JsonUtil.Key.WAITING_USER_NAME, waitingUserName)
                        .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                        .add(JsonUtil.Key.USERNAME, DataManager.Instance().username));

                SocketEventListener.addAddEventQueue(SocketEventListener.eType.ADD_WAITING, new SocketEventListener.EventListener() {
                    @Override
                    public boolean run(JsonUtil jsonUtil) {
                        SocketConnection.LOG(jsonUtil.toString());
                        final String status = jsonUtil.getString(JsonUtil.Key.STATUS, "error");
                        if (status.equals("success")){
                            mainHandler.post(() -> {
                                infoTextView.setText("[" + waitingUserName + "] 에게 친구요청을 보냈습니다.");
                                infoTextView.setVisibility(View.VISIBLE);
                                infoTextView.setTextColor(Color.BLACK);
                            });

                            final String message = jsonUtil.getString(JsonUtil.Key.DATA,"data 읽기 실패");
                            ServerConnectManager.Log(status);
                            ServerConnectManager.Log(message);
                        } else if (status.equals("success_0")) {
                            mainHandler.post(() -> {
                                infoTextView.setText("[" + waitingUserName + "] 는 이미 친구 요청을 보냈습니다.");
                                infoTextView.setVisibility(View.VISIBLE);
                                infoTextView.setTextColor(Color.RED);
                            });
                        } else if (status.equals("error")) {
                            mainHandler.post(() -> {
                                infoTextView.setText("[" + waitingUserName + "] 에게 친구요청에 실패 했습니다.");
                                infoTextView.setVisibility(View.VISIBLE);
                                infoTextView.setTextColor(Color.RED);
                            });
                        } else {
                            ServerConnectManager.Log("FriendAddDialogFragment couldn't handle it: " + jsonUtil.toString());
                        }
                        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.ADD_WAITING, this);

                        return true;
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
