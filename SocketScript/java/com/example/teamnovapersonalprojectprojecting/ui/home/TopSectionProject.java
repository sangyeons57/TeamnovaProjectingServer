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
import com.example.teamnovapersonalprojectprojecting.activity.project.ProjectSettingDialogFragment;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class TopSectionProject extends Fragment {
    public static final String PROJECT_NAME = "PROJECT_NAME";
    private TextView projectNameTextView;
    private Button searchButton;
    private Button addMemberButton;

    private SocketEventListener.EventListener displayProjectElement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_topsection_project , container, false);

        this.projectNameTextView = view.findViewById(R.id.project_name);
        this.searchButton = view.findViewById(R.id.search_button);
        this.addMemberButton = view.findViewById(R.id.add_project_member_button);

        projectNameTextView.setText(DataManager.Instance().projectName);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberAddDialogFragment.Instance(DataManager.Instance().projectId)
                        .show(getParentFragmentManager(), "MemberAddDialogFragment");
            }
        });

        projectNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectSettingDialogFragment.Instance(DataManager.Instance().projectId, DataManager.Instance().projectName)
                        .show(getParentFragmentManager(), "ProjectSettingDialogFragment");
            }
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, displayProjectElement = (j)->{
            DataManager.Instance().mainHandler.post(()->{ projectNameTextView.setText(DataManager.Instance().projectName);});
            return false;
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, displayProjectElement);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
