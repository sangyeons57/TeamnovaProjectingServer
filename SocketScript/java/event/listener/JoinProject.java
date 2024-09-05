package event.listener;

import com.mysql.cj.protocol.x.XProtocolRow;
import org.example.MysqlManager;
import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class JoinProject implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        String token = jsonObject.getString(SocketEventListener.eKey.TOKEN.toString());

        if( MysqlManager.Instance().getProjectIdByValidToken(token, 24) == projectId ){
            ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId);
            ProjectEditor.Member member = projectEditor.addMember(SocketConnection.Instance().userMap.get(channel).getUserId());
            member.apply();
            member.registerToUser();

            SocketConnection.trySendMessage(channel, jsonObject
                    .put(SocketEventListener.eKey.STATUS.toString(), "success"));
        } else {
            SocketConnection.trySendMessage(channel, jsonObject
                    .put(SocketEventListener.eKey.STATUS.toString(), "error"));
        }
    }
}
