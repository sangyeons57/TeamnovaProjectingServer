package com.example.teamnovapersonalprojectprojecting.activity;

import static java.io.File.createTempFile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class EditProfileActivity extends AppCompatActivity {
    private ImageButton profileImageButton;
    private Button removeImageButton;
    private Button saveSettingButton;
    private EditText usernameEditText;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Uri profileImage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        DataManager.Instance().currentContext = this;

        profileImageButton = findViewById(R.id.profileImageButton);
        removeImageButton = findViewById(R.id.removeImageButton);
        saveSettingButton = findViewById(R.id.saveSettingButton);

        profileImageButton.setOnClickListener(this::onClickProfileImageButton);
        removeImageButton.setOnClickListener(this::deleteImage);
        saveSettingButton.setOnClickListener(this::sendData);

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
        DB_FileList.setFileImage(profileImageButton, DataManager.Instance().profilePath);
    }

    private void deleteImage(View view){
        profileImageButton.setImageResource(R.drawable.ic_account_black_24dp);
        profileImage = null;
    }

    private boolean isClicked = false;
    private void sendData(View view){
        if (isClicked){
            return;
        }
        isClicked = true;

        if (profileImage != null) {
            FileSocketConnection.sendFile(profileImage);
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.FILE, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.FILE){
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
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_PROFILE_IMAGE.toString())
                .add(JsonUtil.Key.ID, imageId));
        LocalDBMain.GetTable(DB_UserList.class).addUserByServer(DataManager.Instance().userId, (jsonUtil)->{
            LocalDBMain.GetTable(DB_FileList.class).checkFileExistAndCall(imageId, (n)->{
                DataManager.Instance().profilePath = LocalDBMain.GetTable(DB_UserList.class).getProfileImagePath(DataManager.Instance().userId);
                DataManager.reloadUserData(DataManager.Instance().userId);
                finish();
            });
        });
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

    private String getRealPathFromUri(Uri uri){
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }
}
