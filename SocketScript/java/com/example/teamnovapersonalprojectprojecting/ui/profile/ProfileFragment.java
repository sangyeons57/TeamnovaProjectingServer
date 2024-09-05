package com.example.teamnovapersonalprojectprojecting.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.adapters.ImageViewBindingAdapter;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.teamnovapersonalprojectprojecting.activity.EditProfileActivity;
import com.example.teamnovapersonalprojectprojecting.activity.PersonalSettingActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.activity.FriendsActivity;

public class ProfileFragment extends Fragment {
    private Button statusButton;
    private Button friendsButton;
    private Button editProfileButton;
    private ImageButton personalSettingButton;
    private ImageView profileImage;

    private TextView profileName;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        statusButton = view.findViewById(R.id.set_status_button);
        personalSettingButton = view.findViewById(R.id.personal_setting_button);
        friendsButton = view.findViewById(R.id.friendsButton);
        profileName = view.findViewById(R.id.profile_name);
        profileImage = view.findViewById(R.id.profile_image);
        editProfileButton = view.findViewById(R.id.editProfileButton);


        profileName.setText(DataManager.Instance().username);
        if(DataManager.Instance().profilePath != null) {
            Glide.with(DataManager.Instance().currentContext)
                    .load(DataManager.Instance().profilePath)
                    .circleCrop()
                    .into(profileImage);
        }

        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeStatusDialogFragment dialogFragment = new ChangeStatusDialogFragment();
                dialogFragment.show(getParentFragmentManager(), "ChangeStatusDialogFragment");
            }
        });

        personalSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PersonalSettingActivity.class));
            }
        });

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FriendsActivity.class));
            }
        });

        editProfileButton.setOnClickListener((View v)->{
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        profileName.setText(DataManager.Instance().username);
        Log.d("test2", "msg: " + DataManager.Instance().profilePath);
        if(DataManager.Instance().profilePath != null) {
            Glide.with(DataManager.Instance().currentContext)
                    .load(DataManager.Instance().profilePath)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageDrawable(null);
        }
    }
}