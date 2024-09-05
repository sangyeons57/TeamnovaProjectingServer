package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

public class ProjectJoinDialogFragment extends DialogFragment {
    public static final String Token = "token";

    public static ProjectJoinDialogFragment Instance(String token) {
        ProjectJoinDialogFragment dialogFragment = new ProjectJoinDialogFragment();
        Bundle args = new Bundle();
        args.putString(Token, token);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private Button joinButton;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_join_project, container, false);

        this.textView = view.findViewById(R.id.dialog_title_textview);
        this.joinButton = view.findViewById(R.id.join_button);

        String token = getArguments().getString(Token);

        if (token == null) {
            setupInvitationFail();
        } else {
            SocketConnection.sendMessage(false, new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.IDENTIFY_PROJECT_INVITATIONS)
                    .add(JsonUtil.Key.TOKEN, token));
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.IDENTIFY_PROJECT_INVITATIONS, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.IDENTIFY_PROJECT_INVITATIONS){
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    if(jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, false)) {
                        setUpInvitationSuccess(
                                jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I),
                                jsonUtil.getString(JsonUtil.Key.PROJECT_NAME, DataManager.NOT_SETUP_S),
                                token
                        );
                    } else {
                        setupInvitationFail();
                    }
                    return false;
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }

    public void setUpInvitationSuccess(int projectId, String projectName, String token) {
        this.textView.setText(projectName);
        this.joinButton.setVisibility(View.VISIBLE);

        joinButton.setOnClickListener((view) -> {
            this.dismiss();
            SocketConnection.sendMessage(false, new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.JOIN_PROJECT)
                    .add(JsonUtil.Key.TOKEN, token)
                    .add(JsonUtil.Key.PROJECT_ID, projectId));
        });
    }

    public void setupInvitationFail() {
        this.textView.setText("유효하지 않은 초대 입니다.");
        this.joinButton.setVisibility(View.GONE);
    }
}
