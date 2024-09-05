package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class SendMessage implements SocketEventListener.EventListener {
    public static long lastChatId = DataManager.NOT_SETUP_I;
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
        int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0);
        int writerId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
        String dataString = jsonUtil.getString(JsonUtil.Key.DATA, "");
        String lastTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "");
        boolean isModified = jsonUtil.getBoolean(JsonUtil.Key.IS_MODIFIED, false);

        lastChatId = LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChat(channelId, chatId, writerId, dataString, lastTime, (isModified) ? 1 : 0);

        return false;
    }
}
