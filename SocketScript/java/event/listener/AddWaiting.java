package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class AddWaiting implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {

        String waitingUserName = jsonObject.getString(SocketEventListener.eKey.WAITING_USER_NAME.toString());
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
        String username = jsonObject.getString(SocketEventListener.eKey.USERNAME.toString());

        Map<String, Object> waitingUser = MysqlManager.Instance().getUserByUsername(waitingUserName);
        if (waitingUser != null) {
            JSONArray waiting = new JSONArray(waitingUser.get("waiting").toString());
            JSONArray friends = new JSONArray(waitingUser.get("friends").toString());

            if (Util.isInArray(waiting, userId) || Util.isInArray(friends, userId)) {

                SocketConnection.trySendMessage(channel, new JSONObject()
                        .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_WAITING.toString())
                        .put(SocketEventListener.eKey.STATUS.toString(), "success_0")
                        .put(SocketEventListener.eKey.DATA.toString(), "user id already added")
                        .toString());
                return;
            }

            waiting.put(userId);

            if (MysqlManager.Instance().updateUsersWaitingByUsername(waiting, waitingUserName)) {
                SocketConnection.LOG(waitingUser.get("id").toString());

                SocketChannel socketChannel;
                if((socketChannel = SocketConnection.Instance().userIdMap.get(((Long)waitingUser.get("id")).intValue())) != null){
                    SocketConnection.trySendMessage(socketChannel, new JSONObject()
                            .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_WAITING.toString())
                            .put(SocketEventListener.eKey.USER_ID.toString(), userId)
                            .put(SocketEventListener.eKey.USERNAME.toString(), username)
                            .toString());
                }

                SocketConnection.trySendMessage(channel, new JSONObject()
                        .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_WAITING.toString())
                        .put(SocketEventListener.eKey.STATUS.toString(), "success")
                        .put(SocketEventListener.eKey.DATA.toString(), "success")
                        .toString());

            } else {
                SocketConnection.trySendMessage(channel, new JSONObject()
                        .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_WAITING.toString())
                        .put(SocketEventListener.eKey.STATUS.toString(), "error")
                        .put(SocketEventListener.eKey.DATA.toString(), "fail to db update")
                        .toString());

            }
        } else {
            SocketConnection.trySendMessage(channel, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_WAITING.toString())
                    .put(SocketEventListener.eKey.STATUS.toString(), "error")
                    .put(SocketEventListener.eKey.DATA.toString(), "fail to find user")
                    .toString());

        }
    }
}
