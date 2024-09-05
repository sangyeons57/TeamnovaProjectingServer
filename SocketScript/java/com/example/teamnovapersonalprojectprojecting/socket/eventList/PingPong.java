package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class PingPong implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        DataManager.Instance().checkPingPong = true;
        return false;
    }
}
