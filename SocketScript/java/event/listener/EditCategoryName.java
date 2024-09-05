package event.listener;

import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.sql.PreparedStatement;

public class EditCategoryName implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {

        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        int categoryId = jsonObject.getInt(SocketEventListener.eKey.CATEGORY_ID.toString());
        String newCategoryName = jsonObject.getString(SocketEventListener.eKey.NAME.toString());

        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId);
        projectEditor.loadStructure().getStructure(categoryId).setName(newCategoryName);
        projectEditor.applyStructure();

        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
