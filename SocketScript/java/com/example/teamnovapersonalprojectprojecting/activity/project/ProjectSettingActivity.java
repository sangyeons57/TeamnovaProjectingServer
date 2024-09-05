package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class ProjectSettingActivity extends AppCompatActivity {

    private EditText projectNameEditText;
    private Button deleteProjectButton;
    private Button saveProjectSettingButton;
    private Button removeImageButton;
    private ImageButton profileImageButton;

    private int projectId;
    private String projectName;

    ActivityResultLauncher<Intent> pickImageLauncher;

    private Uri profileImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_setting);
        DataManager.Instance().currentContext = this;

        profileImageButton = findViewById(R.id.profileImageButton);
        projectNameEditText = findViewById(R.id.projectNameEditText);
        deleteProjectButton = findViewById(R.id.deleteProjectButton);
        saveProjectSettingButton = findViewById(R.id.saveProjectSettingButton);
        removeImageButton = findViewById(R.id.removeImageButton);


        profileImageButton.setOnClickListener(this::onClickProfileImageButton);
        removeImageButton.setOnClickListener(this::deleteImage);
        deleteProjectButton.setOnClickListener(this::onClickDeleteProjectButton);
        saveProjectSettingButton.setOnClickListener(this::onClickSaveProjectButton);

        this.projectId = getIntent().getIntExtra(ProjectSettingDialogFragment.PROJECT_ID, DataManager.NOT_SETUP_I);
        this.projectName = getIntent().getStringExtra(ProjectSettingDialogFragment.PROJECT_NAME);

        if(this.projectId == DataManager.NOT_SETUP_I){
            finish();
        }

        projectNameEditText.setText(projectName);


        LocalDBMain.GetTable(DB_Project.class).getDefaultDataCursorById(this.projectId).execute(cursor1 -> {
            if(cursor1.moveToFirst()){
                int profileId = cursor1.getInt(2);
                LocalDBMain.GetTable(DB_FileList.class).getFileData(profileId).execute(cursor2 -> {
                    if(cursor2.moveToFirst()){
                        DB_FileList.setFileImage(profileImageButton, cursor2.getString(3));
                    }
                });
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    profileImage = data.getData();
                    Glide.with(this)
                            .load(profileImage)
                            .circleCrop()
                            .into(profileImageButton);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
        LocalDBMain.GetTable(DB_Project.class).getDefaultDataCursorById(this.projectId).execute(cursor -> {
            if(cursor.moveToFirst()){
                LocalDBMain.GetTable(DB_FileList.class).getFileData(cursor.getInt(2)).execute(cursor1 -> {
                    if(cursor1.moveToFirst()){
                        DB_FileList.setFileImage(profileImageButton, cursor1.getString(3));
                    }
                });
            }
        });
    }
    public void onClickDeleteProjectButton(View view){

    }

    private void deleteImage(View view){
        profileImageButton.setImageDrawable(null);
        profileImage = null;
    }

    public void onClickProfileImageButton(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    DataManager.PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            imagePick();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == DataManager.PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePick();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void imagePick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private boolean isClicked = false;
    public void onClickSaveProjectButton(View view){
        if (isClicked){
            return;
        }
        isClicked = true;

        String newName = projectNameEditText.getText().toString();
        DataManager.Instance().projectName = newName;
        LocalDBMain.GetTable(DB_Project.class).updateNameById(projectId, newName);

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EDIT_PROJECT_NAME.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.NAME, newName)
        );

        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );

        //이미지 설정
        if (profileImage != null) {
            FileSocketConnection.sendFile(profileImage);
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.FILE, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.FILE) {
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    int imageId = jsonUtil.getInt(JsonUtil.Key.ID, DataManager.NOT_SETUP_I);
                    setImageServerAndLocal(imageId);
                    return false;
                }
            });
        } else {
            setImageServerAndLocal(DataManager.NOT_SETUP_I);
        }
    }
    private void setImageServerAndLocal(int imageId){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_PROJECT_PROFILE_IMAGE.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.PROFILE_ID, imageId));
        LocalDBMain.GetTable(DB_FileList.class).checkFileExistAndCall(imageId);
        LocalDBMain.GetTable(DB_Project.class).updateProfileImageId(imageId, projectId);
        finish();
    }
}
