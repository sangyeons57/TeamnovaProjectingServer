package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class CheckChannelExist implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
        boolean isValid = jsonUtil.getBoolean(JsonUtil.Key.IS_VALID, true);

        if(isValid){
        } else {
            LocalDBMain.GetTable(DB_ProjectStructure.class).removeChannel(channelId);
            LocalDBMain.GetTable(DB_ProjectChannelList.class).removeChannel(channelId);
            LocalDBMain.GetTable(DB_ChannelList.class).removeChannel(channelId);

            SocketEventListener.callEvent(SocketEventListener.eType.DISPLAY_PROJECT_ELEMENT, new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType._RELOAD.toString()));
        }
        return false;
    }
}
