package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class IdentifyProjectInvitations implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        String token = jsonObject.getString(SocketEventListener.eKey.TOKEN.toString());

        int projectId = MysqlManager.Instance().getProjectIdByValidToken(token, 24);
        String projectName = MysqlManager.Instance().getProjectData(projectId).get("name").toString();

        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.IS_VALID.toString(), (projectId != Util.NOT_SETUP_I))
                .put(SocketEventListener.eKey.PROJECT_NAME.toString(), projectName)
                .put(SocketEventListener.eKey.PROJECT_ID.toString(), projectId));
    }
}
