package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;

public class ReloadDMList implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        JSONArray jsonArray = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
        LocalDBMain.GetTable(DB_DMList.class).clearDMList();
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JsonUtil data = new JsonUtil(jsonArray.getJSONObject(i));
                int channelId= data.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
                int otherId= data.getInt(JsonUtil.Key.OTHER_ID, DataManager.NOT_SETUP_I);
                String otherUsername = data.getString(JsonUtil.Key.OTHER_USERNAME, DataManager.NOT_SETUP_S);
                String lastTime = data.getString(JsonUtil.Key.DATETIME, DataManager.NOT_SETUP_S);

                LocalDBMain.GetTable(DB_UserList.class).addUserByServer(otherId, null);
                LocalDBMain.GetTable(DB_DMList.class).addDMList(channelId, otherId, lastTime);

                SocketEventListener.LOG("ReloadDMList" ,channelId + " " + otherId + " " + otherUsername + " " + lastTime);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
