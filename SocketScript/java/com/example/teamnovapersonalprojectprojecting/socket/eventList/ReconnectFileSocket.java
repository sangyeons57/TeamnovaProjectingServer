package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class ReconnectFileSocket implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        FileSocketConnection.Reset();
        return false;
    }
}
