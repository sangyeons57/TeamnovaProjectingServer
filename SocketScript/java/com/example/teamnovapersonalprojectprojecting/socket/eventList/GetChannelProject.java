package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;

public class GetChannelProject implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        JSONArray data = jsonUtil.getJsonArray(JsonUtil.Key.DATA, null);
        if(data == null){
            return false;
        }

        try {
            for (int i = 0; i < data.length(); ++i) {
                JsonUtil channelData = new JsonUtil(data.getJSONObject(i));
                int channelId = channelData.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
                int categoryId = channelData.getInt(JsonUtil.Key.CATEGORY_ID, DataManager.NOT_SETUP_I);
                int projectId = channelData.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I);
                String channelName = channelData.getString(JsonUtil.Key.CHANNEL_NAME, DataManager.NOT_SETUP_S);
                LocalDBMain.GetTable(DB_ProjectChannelList.class).addProjectList(channelId, categoryId, projectId, channelName);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
