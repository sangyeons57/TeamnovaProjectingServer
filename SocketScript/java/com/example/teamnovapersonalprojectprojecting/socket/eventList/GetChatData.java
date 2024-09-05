package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetChatData implements SocketEventListener.EventListener {
    @Override
    public boolean run(JsonUtil jsonUtil) {
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, 0);
        JSONArray data = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject row = data.getJSONObject(i);
                int chatId = row.getInt(JsonUtil.Key.CHAT_ID.toString());
                int writerId = row.getInt(JsonUtil.Key.USER_ID.toString());
                String dataString = row.getString(JsonUtil.Key.DATA.toString());
                String lastTime = row.getString(JsonUtil.Key.DATETIME.toString());
                boolean isModified = row.getBoolean(JsonUtil.Key.IS_MODIFIED.toString());

                LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChat(channelId, chatId, writerId, dataString, lastTime, (isModified) ? 1 : 0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
