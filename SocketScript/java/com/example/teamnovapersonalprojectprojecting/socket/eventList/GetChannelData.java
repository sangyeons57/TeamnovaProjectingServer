package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;

public class GetChannelData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
        String channelName = jsonUtil.getString(JsonUtil.Key.CHANNEL_NAME, "");
        boolean isDM = jsonUtil.getBoolean(JsonUtil.Key.IS_DM, false);
        JSONArray members = jsonUtil.getJsonArray(JsonUtil.Key.MEMBERS, new JSONArray());

        LocalDBMain.GetTable(DB_ChannelList.class).addChannelList(channelId, members, isDM ? 1 : 0);
        return false;
    }
}
