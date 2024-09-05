package event.listener;

import org.example.FileSocketConnection;
import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetFile implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int fileId = jsonObject.getInt(SocketEventListener.eKey.ID.toString());
        Map<String, Object> data = MysqlManager.Instance().getFileDataById(fileId);
        if(data != null){
            FileSocketConnection.trySendMessage(SocketConnection.Instance().userMap.get(channel).getUserId(), fileId);
        }
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.IS_VALID.toString(), data != null));
    }
}
