package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import android.content.Intent;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.sun.mail.imap.protocol.INTERNALDATE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DeleteChannel implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, 0);
        int categoryId = jsonUtil.getInt(JsonUtil.Key.CATEGORY_ID, 0);
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);

        try {
            JSONObject category = LocalDBMain.GetTable(DB_ProjectStructure.class).getCategoryByID(projectId, categoryId);

            List<Integer> elements = DataManager.JsonArrayToIntegerList(category.getJSONArray("elements"));
            elements.remove(Integer.valueOf(channelId));
            category.put("elements", new JSONArray(elements));

            LocalDBMain.GetTable(DB_ProjectStructure.class).replaceCategoryById(projectId, categoryId, category);

            if(DataManager.Instance().projectId == projectId){
                DataManager.Instance().getCategoryItem(categoryId).removeChannelItem(channelId);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
