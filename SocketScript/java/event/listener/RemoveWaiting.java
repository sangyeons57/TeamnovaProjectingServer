package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class RemoveWaiting implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
        int waitingUserId = jsonObject.getInt(SocketEventListener.eKey.USER_ID1.toString());

        if(SocketConnection.Instance().userMap.get(channel).getUserId() == userId){
            Map<String, Object> userData = MysqlManager.Instance().getUserByUserId(userId);
            JSONArray userWaitingArray = new JSONArray(userData.get("waiting").toString());
            Util.removeElement(userWaitingArray, waitingUserId);
            MysqlManager.Instance().updateUsersWaitingByUserId(userWaitingArray, userId);
        }
    }
}
