package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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

public class EditChannelDialogFragment extends DialogFragment {
    public static final String PROJECT_ID = "projectId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CHANNEL_ID = "channelId";
    public static final String CHANNEL_NAME = "channelName";

    public static EditChannelDialogFragment Instance(int projectId, int categoryId, int channelId, String categoryName){
        EditChannelDialogFragment dialogFragment = new EditChannelDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PROJECT_ID, projectId);
        bundle.putInt(CATEGORY_ID, categoryId);
        bundle.putInt(CHANNEL_ID, channelId);
        bundle.putString(CHANNEL_NAME, categoryName);
        dialogFragment.setArguments(bundle);
        return  dialogFragment;
    }
    private Button editChannelButton;
    private TextView dialogTitle;

    private int projectId;
    private int categoryId;
    private int channelId;
    private String channelName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_project_channel_edit, container, false);
        projectId = getArguments().getInt(PROJECT_ID);
        categoryId = getArguments().getInt(CATEGORY_ID);
        channelId = getArguments().getInt(CHANNEL_ID);
        channelName = getArguments().getString(CHANNEL_NAME);

        editChannelButton = view.findViewById(R.id.edit_channel_button);
        dialogTitle = view.findViewById(R.id.dialog_title);

        dialogTitle.setText(channelName);


        editChannelButton.setOnClickListener(this::onClickEditChannel);

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



    public void onClickEditChannel(View view) {
        this.dismiss();
        Intent intent = new Intent(getActivity(), EditChannelActivity.class);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(CATEGORY_ID, categoryId);
        intent.putExtra(CHANNEL_ID, channelId);
        intent.putExtra(CHANNEL_NAME, channelName);
        Log.d("EditCategoryDialogFragment", "onClickEditCategory:" + channelId + " " + channelName);
        startActivity(intent);
    }
}
