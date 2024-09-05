package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.ui.home.MemberAddDialogFragment;

public class ProjectSettingDialogFragment extends DialogFragment {
    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_NAME = "projectName";

    private int projectId;
    private String projectName;

    private Button createChannelButton;
    private Button createCategoryButton;
    private Button editProjectButton;
    private Button memberInviteButton;

    public static ProjectSettingDialogFragment Instance(int projectId, String projectName){
        ProjectSettingDialogFragment dialogFragment = new ProjectSettingDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PROJECT_ID, projectId);
        bundle.putString(PROJECT_NAME, projectName);
        dialogFragment.setArguments(bundle);
        return  dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_project_setting, container, false);
        projectId = getArguments().getInt(PROJECT_ID);
        projectName = getArguments().getString(PROJECT_NAME);


        createCategoryButton = view.findViewById(R.id.create_category_button);
        createChannelButton = view.findViewById(R.id.create_channel_button);
        editProjectButton = view.findViewById(R.id.edit_project_button);
        memberInviteButton = view.findViewById(R.id.member_invite_button);

        createCategoryButton.setOnClickListener(view1 ->{
            Intent intent = new Intent(getActivity(), AddCategoryActivity.class);
            startActivity(intent);
        });
        editProjectButton.setOnClickListener(this::onClickEditProjectButton);
        memberInviteButton.setOnClickListener(this::onClickMemberInvite);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Point size = new Point();
            Display display = getDialog().getWindow().getWindowManager().getDefaultDisplay();
            display.getSize(size);

            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(size.y * 0.7));
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    public void onClickEditProjectButton(View view){
        this.dismiss();
        Intent intent = new Intent(getActivity(), ProjectSettingActivity.class);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(PROJECT_NAME, projectName);
        startActivity(intent);;
    }

    public void onClickMemberInvite(View view){
        this.dismiss();
        MemberAddDialogFragment.Instance(projectId)
                .show(getParentFragmentManager(), "MemberAddDialogFragment");
    }

}
