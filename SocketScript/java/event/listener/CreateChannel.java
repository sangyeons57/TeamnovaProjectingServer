package event.listener;

import jdk.internal.icu.impl.Trie;
import org.example.*;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class CreateChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        int categoryId = jsonObject.getInt(SocketEventListener.eKey.CATEGORY_ID.toString());
        String channelName = jsonObject.getString(SocketEventListener.eKey.NAME.toString());

        int channelId = MysqlManager.Instance().createProjectChannel(projectId, categoryId, channelName);
        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId).loadStructure();
        projectEditor.getStructure(categoryId).addElement(channelId);
        projectEditor.applyStructure();

        String message = "channel is created : " + channelName;

        MysqlManager.Instance().addChatData(
                "p_" + channelId,
                Util.SYSTEM_ID,
                message,
                Util.getCurrentDateTime());

        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId));
    }
}
