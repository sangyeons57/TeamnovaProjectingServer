package event.listener;

import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class CreateCategory implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        String categoryName = jsonObject.getString(SocketEventListener.eKey.NAME.toString());

        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId);
        int categoryId = projectEditor.loadStructure().addStructure(categoryName).getStructureId();

        projectEditor.applyStructure();
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.CATEGORY_ID.toString(), categoryId));
    }
}
