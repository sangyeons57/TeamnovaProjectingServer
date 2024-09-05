package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.List;

public class SendMessageSpecificPerson implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int otherId = jsonObject.getInt(SocketEventListener.eKey.OTHER_ID.toString());
        int userId = SocketConnection.Instance().userMap.get(channel).getUserId();
        String message = jsonObject.getString(SocketEventListener.eKey.MESSAGE.toString());
        int channelId;
        String datetime = Util.getCurrentDateTime();

        if((channelId = MysqlManager.Instance().getDMChannelId(userId, otherId)) == Util.NOT_SETUP_I){
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            jsonObject.put(SocketEventListener.eKey.MESSAGE.toString(), "dm channel id can not found");
            SocketConnection.trySendMessage(channel, jsonObject);
            return;
        }

        jsonObject.put(SocketEventListener.eKey.USER_ID.toString(), userId);
        jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
        jsonObject.put(SocketEventListener.eKey.DATETIME.toString(), datetime);
        int chatId = MysqlManager.Instance().addChatData("dm_" + channelId, userId, message, datetime);
        jsonObject.put(SocketEventListener.eKey.CHAT_ID.toString(), chatId);

        System.out.println("channelId: "+ channelId);
        SocketChannel otherChannel = SocketConnection.Instance().userIdMap.get(otherId);

        SocketConnection.trySendMessage(otherChannel, jsonObject
            .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.SEND_MESSAGE.toString()));

        SocketConnection.trySendMessage(otherChannel, new JSONObject()
            .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_DM_ELEMENT.toString())
            .put(SocketEventListener.eKey.DATETIME.toString(), datetime)
            .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId));

        SocketConnection.trySendMessage(channel, new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_DM_ELEMENT.toString())
                .put(SocketEventListener.eKey.DATETIME.toString(), datetime)
                .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId));
    }
}
