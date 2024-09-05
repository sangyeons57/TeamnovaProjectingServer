package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class SetProjectProfileImage implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        int imageId = jsonObject.getInt(SocketEventListener.eKey.PROFILE_ID.toString());

        MysqlManager.Instance().updateProjectProfileById(projectId, imageId);
        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
