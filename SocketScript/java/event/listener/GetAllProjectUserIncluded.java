package event.listener;

import org.example.MysqlManager;
import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.text.html.Option;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GetAllProjectUserIncluded implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());

        JSONArray projectIdArray = MysqlManager.Instance().getAllProjectUserIncluded(userId);
        JSONArray projectData = new JSONArray();
        if(projectIdArray != null){
            for (int i = 0; i < projectIdArray.length(); i++){
                Optional.ofNullable( ProjectEditor.GetProjectEditor(projectIdArray.getInt(i)).getDefaultData() )
                                .ifPresent(projectData::put);
            }
        }
        jsonObject.put(SocketEventListener.eKey.JSON_ARRAY.toString(), projectData);
        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
