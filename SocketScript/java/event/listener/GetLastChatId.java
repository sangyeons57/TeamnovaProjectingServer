package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.SocketEventListener.EventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetLastChatId implements EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());

        long chatId = (long)MysqlManager.Instance().getDataOfChatLast(channelId).get("id");
        jsonObject.put(SocketEventListener.eKey.CHAT_ID.toString(),chatId);

        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
