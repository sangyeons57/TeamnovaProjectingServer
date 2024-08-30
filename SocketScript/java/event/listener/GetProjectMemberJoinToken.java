package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class GetProjectMemberJoinToken implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());

        //유효시간이 4시간 이하면 그냥 새로운 토큰 만들도록 작동
        String token = MysqlManager.Instance().getProjectJoinToken(projectId, 20);
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.TOKEN.toString(), token));
    }
}
