package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.ProjectAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateCategory implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I);
        int categoryId = jsonUtil.getInt(JsonUtil.Key.CATEGORY_ID, DataManager.NOT_SETUP_I);
        String categoryName = jsonUtil.getString(JsonUtil.Key.NAME, DataManager.NOT_SETUP_S);;
        LocalDBMain.GetTable(DB_ProjectStructure.class).addCategory(projectId, categoryId,categoryName);

        if(DataManager.Instance().projectId == projectId){
            DataManager.Instance().addCategoryItem(categoryId, categoryName, new ArrayList<>());
            SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString()));
        }
        return false;
    }
}
