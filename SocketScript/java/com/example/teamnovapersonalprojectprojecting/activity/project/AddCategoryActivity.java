package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class AddCategoryActivity extends AppCompatActivity {
    public static final String PROJECT_ID = "projectId";
    public static final String CATEGORY_NAME = "categoryName";

    private String categoryName;

    Button createCategoryButton;
    EditText categoryNameEditText;
    TextView warningTextView;

    Intent intent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_create_category);
        DataManager.Instance().currentContext = this;
        intent = getIntent();

        if(DataManager.Instance().projectId == DataManager.NOT_SETUP_I){
            finish();
        }

        createCategoryButton = findViewById(R.id.createCategoryButton);
        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        warningTextView = findViewById(R.id.warningTextView);


        createCategoryButton.setOnClickListener(this::createCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }


    public void createCategory(View view) {
        categoryName = categoryNameEditText.getText().toString().trim();
        if(categoryName.isEmpty()){
            warningTextView.setText("카테고리 이름을 입력해주세요.");
            return;
        } else {
            warningTextView.setText("");
        }

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.CREATE_CATEGORY.toString())
                .add(JsonUtil.Key.PROJECT_ID, DataManager.Instance().projectId)
                .add(JsonUtil.Key.NAME, categoryName)
        );
        finish();
    }
}
