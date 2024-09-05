package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class SetProfileImage implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = SocketConnection.Instance().userMap.get(channel).getUserId();
        int imageId = jsonObject.getInt(SocketEventListener.eKey.ID.toString());

        MysqlManager.Instance().updateUserProfileByUserId(userId, imageId);
        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
