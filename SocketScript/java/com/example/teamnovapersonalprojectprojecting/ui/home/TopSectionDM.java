package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.SearchActivity;

public class TopSectionDM extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_topsection_dm , container, false);

        Button showFriendAddDialogButton = view.findViewById(R.id.add_friend_dialog_button);
        showFriendAddDialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FriendAddDialogFragment dialogFragment = new FriendAddDialogFragment();
                dialogFragment.show(getParentFragmentManager(), "FriendAddDialogFragment");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
