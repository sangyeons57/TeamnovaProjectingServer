package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import static com.example.teamnovapersonalprojectprojecting.socket.SocketConnection.LOG;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class AddDMElement implements SocketEventListener.EventListener {
    private static int lastChannelId = DataManager.NOT_SETUP_I;
    @Override
    public boolean run(JsonUtil jsonUtil) {
        LocalDBMain.GetTable(DB_DMList.class).changeLastTime(jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0), jsonUtil.getString(JsonUtil.Key.DATETIME, DataManager.getCurrentDateTime()));

        if(lastChannelId != jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I)) {
            lastChannelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
            LOG("ADD_DM_ELEMENT");
            SocketConnection.sendMessage(new JsonUtil().add(JsonUtil.Key.TYPE, SocketEventListener.eType.RELOAD_DM_LIST));
        }
        return false;
    }
}
