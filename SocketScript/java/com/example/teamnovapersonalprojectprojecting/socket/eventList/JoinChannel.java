package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import android.content.Intent;

import com.example.teamnovapersonalprojectprojecting.chat.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class JoinChannel implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        String status = jsonUtil.getString(JsonUtil.Key.STATUS, "fail");

        if (status.contains("success")) {
            DataManager.Instance().channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);

            Intent intent = new Intent(DataManager.Instance().currentContext, ChatActivity.class);
            intent.putExtra(ChatActivity.LAST_CHAT_ID, jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0));
            intent.putExtra(ChatActivity.IS_DM, jsonUtil.getBoolean(JsonUtil.Key.IS_DM, true));

            DataManager.Instance().currentContext.startActivity(intent);
        } else {
            SocketEventListener.LOGe(jsonUtil.toString());
            SocketConnection.sendMessage(new JsonUtil()
                    .add(JsonUtil.Key.TYPE, SocketEventListener.eType.CHECK_CHANNEL_EXIST)
                    .add(JsonUtil.Key.CHANNEL_ID, DataManager.Instance().channelId));
        }
        return false;
    }
}
