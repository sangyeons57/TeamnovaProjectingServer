package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class GetChannelData implements SocketEventListener.EventListener {
    private String channelName;
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());

        Map<String, Object> data = MysqlManager.Instance().getChannelDataByChannelId(channelId);
        boolean isDM = (int) data.get("is_dm") == 1;
        JSONArray members = new JSONArray(data.get("members").toString());

        if(isDM){
            for(int i = 0; i < members.length(); i++){
                int userId = members.getInt(i);
                if(SocketConnection.Instance().userMap.get(channel).getUserId() != userId){
                    channelName = MysqlManager.Instance().getUserByUserId(userId).get("username").toString();
                    break;
                }
            }
        } else {
            channelName = MysqlManager.Instance().getProjectChannelDataByChannelId(channelId).get("channel_name").toString();
        }
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.CHANNEL_NAME.toString(), channelName)
                .put(SocketEventListener.eKey.IS_DM.toString(), isDM)
                .put(SocketEventListener.eKey.MEMBERS.toString(), members));
    }
}
