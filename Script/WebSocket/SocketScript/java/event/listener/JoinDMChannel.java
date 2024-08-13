package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import javax.swing.*;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.ArrayList;

public class JoinDMChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {

        String userId = jsonObject.getString(SocketEventListener.eKey.USER_ID1.toString());
        String friendId = jsonObject.getString(SocketEventListener.eKey.USER_ID2.toString());

        int channelId = MysqlManager.Instance().getDMChannel(Integer.parseInt(userId), Integer.parseInt(friendId));
        jsonObject.put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId);

        if(MysqlManager.Instance().isChannelExist(channelId) && MysqlManager.Instance().isMemberOfChannel(channelId, userId)
        && SocketConnection.Instance().setChannelId(""+channelId, channel)){
            jsonObject.put(SocketEventListener.eKey.FRIEND_NAME.toString(), MysqlManager.Instance().getUserByUserId(Integer.parseInt(userId)).get("username"));
            jsonObject.put(SocketEventListener.eKey.DATA.toString(), MysqlManager.Instance().getDMChatData(channelId));
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
            SocketConnection.Instance().setChannelId( channelId +"", channel);
        } else {
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
        }
        SocketConnection.trySendMessage(channel, jsonObject.toString());
    }
}
