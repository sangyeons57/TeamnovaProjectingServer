package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetAllProjectUserIncluded implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        try {
            JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.JSON_ARRAY, new JSONArray());
            for (int i = 0; i < jsonArray.length(); ++i){
                JSONObject defaultData = jsonArray.getJSONObject(i);
                int projectId = defaultData.getInt("ProjectId");
                String projectName = defaultData.getString("ProjectName");
                int profileImageId = defaultData.getInt("ProjectProfileImage");
                LocalDBMain.GetTable(DB_Project.class).insertOrReplaceData(projectId, projectName, profileImageId);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
