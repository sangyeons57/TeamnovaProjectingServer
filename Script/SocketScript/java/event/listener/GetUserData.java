package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetUserData implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
        Map<String, Object> data = MysqlManager.Instance().getUserByUserId(userId);
        jsonObject.put(SocketEventListener.eKey.USERNAME.toString(), data.get("username"));

        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
