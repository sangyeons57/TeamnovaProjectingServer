package event.listener;

import org.example.MysqlManager;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class EditProjectName implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        String newProjectName = jsonObject.getString(SocketEventListener.eKey.NAME.toString());

        MysqlManager.Instance().updateProjectName(projectId, newProjectName);

    }
}
