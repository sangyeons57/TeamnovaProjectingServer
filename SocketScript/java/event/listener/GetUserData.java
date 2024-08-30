package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetUserData implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
        Map<String, Object> data = MysqlManager.Instance().getUserByUserId(userId);
        if(data == null){
            SocketConnection.trySendMessage(channel, jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error"));
            return;
        }
        jsonObject.put(SocketEventListener.eKey.USERNAME.toString(), data.get("username"));
        jsonObject.put(SocketEventListener.eKey.FRIENDS.toString(), new JSONArray(data.get("friends").toString()));
        jsonObject.put(SocketEventListener.eKey.DM_CHANNELS.toString(), new JSONArray(data.get("dm_channel").toString()));

        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
