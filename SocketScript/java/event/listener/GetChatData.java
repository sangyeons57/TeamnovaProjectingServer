package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class GetChatData implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());
        int limit = jsonObject.getInt(SocketEventListener.eKey.LIMIT.toString());
        int offset = jsonObject.getInt(SocketEventListener.eKey.OFFSET.toString());

        JSONArray resultArray = new JSONArray();
        List<Map<String, Object>> chatData;
        try {
            chatData = MysqlManager.Instance().getDataOfChatFormBack(channelId,limit,offset);
        } catch (RuntimeException e) {
            SocketConnection.trySendMessage(channel, jsonObject
                    .put(SocketEventListener.eKey.DATA.toString(), resultArray)
                    .put(SocketEventListener.eKey.ERROR.toString(), "not valid channelId"));
            return;
        }

        for (Map<String, Object> row: chatData) {
            long chatId = (long)row.get("id");
            long writerId = (long)row.get("writer_id");
            String data = row.get("data").toString();
            String lastTime = row.get("update_time").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Util.DATE_FORMAT);
            lastTime = LocalDateTime.parse(lastTime).format(formatter);
            boolean isModified = false;


            resultArray.put(new JSONObject()
                    .put(SocketEventListener.eKey.CHAT_ID.toString(), chatId)
                    .put(SocketEventListener.eKey.USER_ID.toString(), writerId)
                    .put(SocketEventListener.eKey.DATA.toString(), data)
                    .put(SocketEventListener.eKey.DATETIME.toString(), lastTime)
                    .put(SocketEventListener.eKey.IS_MODIFIED.toString(), isModified));
        }
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.DATA.toString(), resultArray));
    }
}
