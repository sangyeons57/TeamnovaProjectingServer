package com.example.teamnovapersonalprojectprojecting.socket.eventList;


import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class LoadProjectData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        /**
         * 먼저 프로젝트 데이터 를 가지고 와서
         * 클라이언트 데이터 베이스에 저장하는것 까지 여기서 구현
         *
          */
        jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, 0);

        return false;
    }
}
