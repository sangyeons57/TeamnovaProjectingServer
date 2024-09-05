package com.example.teamnovapersonalprojectprojecting.activity.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetProjectData;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class EditCategoryActivity extends AppCompatActivity {

    EditText categoryNameEditText;
    TextView warningTextView;
    Button deleteCategoryButton;
    Button saveCategoryButton;

    private int projectId;
    private int categoryId;
    private String categoryName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_edit_category);
        DataManager.Instance().currentContext = this;

        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        warningTextView = findViewById(R.id.warningTextView);
        deleteCategoryButton = findViewById(R.id.deleteCategoryButton);
        saveCategoryButton = findViewById(R.id.saveCategoryButton);


        projectId = getIntent().getIntExtra(EditCategoryDialogFragment.PROJECT_ID,0);
        categoryId = getIntent().getIntExtra(EditCategoryDialogFragment.CATEGORY_ID, 0);
        categoryName = getIntent().getStringExtra(EditCategoryDialogFragment.CATEGORY_NAME);

        categoryNameEditText.setText(categoryName);

        deleteCategoryButton.setOnClickListener(this::onClickDeleteCategoryButton);
        saveCategoryButton.setOnClickListener(this::onClickSaveCategoryButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.Instance().currentContext = this;
    }
    public void onClickDeleteCategoryButton(View view){
        finish();
        LocalDBMain.GetTable(DB_ProjectStructure.class).removeCategory(projectId, categoryId);
        DataManager.Instance().removeCategoryItem(categoryId);

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.DELETE_CATEGORY)
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.CATEGORY_ID, categoryId));

        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );
    }

    public void onClickSaveCategoryButton(View view){
        String newName = categoryNameEditText.getText().toString();
        DataManager.Instance().getCategoryItem(categoryId).setName(newName);
        JSONObject categoryData = LocalDBMain.GetTable(DB_ProjectStructure.class).getCategoryByID(projectId, categoryId);
        try {
            LocalDBMain.GetTable(DB_ProjectStructure.class).replaceCategoryById(projectId, categoryId,
                    categoryData.put(GetProjectData.StructureName, newName));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EDIT_CATEGORY_NAME.toString())
                .add(JsonUtil.Key.PROJECT_ID, projectId)
                .add(JsonUtil.Key.CATEGORY_ID, categoryId)
                .add(JsonUtil.Key.NAME, newName));

        SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString())
        );
        finish();
    }
}
