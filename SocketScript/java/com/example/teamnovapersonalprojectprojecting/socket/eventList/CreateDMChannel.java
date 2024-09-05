package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class CreateDMChannel implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int userId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
        String datetime = jsonUtil.getString(JsonUtil.Key.DATETIME, DataManager.getCurrentDateTime());

        LocalDBMain.GetTable(DB_UserList.class).addUserByServer(userId, null);
        LocalDBMain.GetTable(DB_DMList.class).addDMList(channelId, userId, datetime);
        SocketConnection.sendMessage(new JsonUtil().add(JsonUtil.Key.TYPE, SocketEventListener.eType.RELOAD_DM_LIST));


        return false;
    }
}
