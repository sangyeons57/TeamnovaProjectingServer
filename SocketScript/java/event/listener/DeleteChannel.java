package event.listener;

import org.example.MysqlManager;
import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class DeleteChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());

        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId).loadStructure();
        projectEditor.getStructureByElementId(channelId).removeElement(channelId);
        projectEditor.applyStructure();

        MysqlManager.Instance().deleteChannel(channelId);

        for (SocketChannel socketChannel : SocketConnection.Instance().channelMap.get(channelId)) {
            if(socketChannel.equals(channel)){
                continue;
            }

            SocketEventListener.callEvent(socketChannel, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.EXIT_CHANNEL.toString()));
            SocketConnection.trySendMessage(socketChannel, jsonObject);
        }
    }
}
