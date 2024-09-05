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
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

public class EditCategoryDialogFragment extends DialogFragment {
    public static final String PROJECT_ID = "projectId";
    public static final String CATEGORY_ID = "categoryId";
    public static final String CATEGORY_NAME = "categoryName";

    public static EditCategoryDialogFragment Instance(int projectId, int categoryId, String categoryName){
        EditCategoryDialogFragment dialogFragment = new EditCategoryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PROJECT_ID, projectId);
        bundle.putInt(CATEGORY_ID, categoryId);
        bundle.putString(CATEGORY_NAME, categoryName);
        dialogFragment.setArguments(bundle);
        return  dialogFragment;
    }
    private TextView dialogTitle;
    private Button editCategoryButton;
    private Button createChannelButton;

    private int projectId;
    private int categoryId;
    private String categoryName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_project_category_edit, container, false);
        projectId = getArguments().getInt(PROJECT_ID);
        categoryId = getArguments().getInt(CATEGORY_ID);
        categoryName = getArguments().getString(CATEGORY_NAME);

        dialogTitle = view.findViewById(R.id.dialog_title);
        editCategoryButton = view.findViewById(R.id.edit_category_button);
        createChannelButton = view.findViewById(R.id.create_channel_button);

        dialogTitle.setText(categoryName);


        editCategoryButton.setOnClickListener(this::onClickEditCategory);
        createChannelButton.setOnClickListener(this::onClickCreateChannel);

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


    public void onClickEditCategory(View view) {
        this.dismiss();
        Intent intent = new Intent(getActivity(), EditCategoryActivity.class);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(CATEGORY_ID, categoryId);
        intent.putExtra(CATEGORY_NAME, categoryName);
        Log.d("EditCategoryDialogFragment", "onClickEditCategory:" + categoryId + " " + categoryName);
        startActivity(intent);
    }

    public void onClickCreateChannel(View view) {
        this.dismiss();
        Intent intent = new Intent(getActivity(), AddChannelActivity.class);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(CATEGORY_ID, categoryId);
        Log.d("EditCategoryDialogFragment", "onClickEditCategory:" + categoryId + " " + categoryName);
        startActivity(intent);
    }
}
