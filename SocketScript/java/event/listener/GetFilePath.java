package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetFilePath implements SocketEventListener.EventListener {
    String basePath = "/var/www/html";
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int imageId = jsonObject.getInt(SocketEventListener.eKey.ID.toString());
        Map<String, Object> fileData = MysqlManager.Instance().getFileDataById(imageId);
        String filePath = fileData.get("file_path").toString();
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.DATA.toString(), removeBasePath(filePath)));
    }

    public String removeBasePath(String fullPath){
        if(fullPath.startsWith(basePath)){
            return fullPath.substring(basePath.length());
        } else {
            return fullPath;
        }
    }
}
