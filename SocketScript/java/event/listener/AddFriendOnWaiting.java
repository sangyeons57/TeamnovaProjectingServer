package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class AddFriendOnWaiting implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
        int friendId = jsonObject.getInt(SocketEventListener.eKey.USER_ID1.toString());

        JSONObject userJsonObject = new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_FRIEND_ON_WAITING.toString())
                .put(SocketEventListener.eKey.USER_ID.toString(), friendId);
        JSONObject friendJsonObject = new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_FRIEND_ON_WAITING.toString())
                .put(SocketEventListener.eKey.USER_ID.toString(), userId);

        Map<String, Object> userData = MysqlManager.Instance().getUserByUserId(userId);
        Map<String, Object> friendData = MysqlManager.Instance().getUserByUserId(friendId);
        if(userData != null && friendData != null){
            JSONArray userWaiting = new JSONArray(userData.get("waiting").toString());
            JSONArray userFriends = new JSONArray(userData.get("friends").toString());
            JSONArray friendWaiting = new JSONArray(friendData.get("waiting").toString());
            JSONArray friendFriends = new JSONArray(friendData.get("friends").toString());

            Util.removeElement(userWaiting, friendId);
            userFriends.put(friendId);
            friendJsonObject.put(SocketEventListener.eKey.USERNAME.toString(), userData.get("username"));

            Util.removeElement(friendWaiting, userId);
            friendFriends.put(userId);
            userJsonObject.put(SocketEventListener.eKey.USERNAME.toString(), friendData.get("username"));

            //mysql 적용
            MysqlManager.Instance().addFriendOnWaiting(userWaiting, userFriends, userId, friendWaiting, friendFriends, friendId);

            JSONObject createDMChannelData = new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.CREATE_DM_CHANNEL.toString())
                    .put(SocketEventListener.eKey.USER_ID1.toString(),userId)
                    .put(SocketEventListener.eKey.USER_ID2.toString(), friendId);

            SocketEventListener.callEvent(SocketEventListener.eType.CREATE_DM_CHANNEL, channel, createDMChannelData);
            SocketConnection.trySendMessage(userId, userJsonObject.toString());
            SocketConnection.trySendMessage(friendId, friendJsonObject.toString());

        }
    }
}
