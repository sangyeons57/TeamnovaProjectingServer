package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FriendList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class FriendListAdded implements SocketEventListener.EventListener{
    @Override
    public boolean run(JsonUtil jsonUtil) {
        LocalDBMain.GetTable(DB_FriendList.class).addOrUpdateFriend(jsonUtil.getInt(JsonUtil.Key.USER_ID, 0));
        SocketEventListener.callEvent(SocketEventListener.eType.UPDATE_FRIEND_LIST, new JsonUtil());
        return false;
    }
}
