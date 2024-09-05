package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class JoinProject implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I);
        String status = jsonUtil.getString(JsonUtil.Key.STATUS, DataManager.NOT_SETUP_S);


        return false;
    }
}
