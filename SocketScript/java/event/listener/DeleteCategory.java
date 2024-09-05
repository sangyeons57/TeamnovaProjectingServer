package event.listener;

import org.example.MysqlManager;
import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class DeleteCategory implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        int categoryId = jsonObject.getInt(SocketEventListener.eKey.CATEGORY_ID.toString());

        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId);
        projectEditor.loadStructure().deleteStructure(categoryId);
        projectEditor.applyStructure();

    }
}
