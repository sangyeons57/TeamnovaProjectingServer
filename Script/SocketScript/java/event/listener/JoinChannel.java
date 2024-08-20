package event.listener;

import jdk.internal.icu.impl.Trie;
import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class JoinChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {

        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());
        jsonObject.put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId);

        if(MysqlManager.Instance().isChannelExist(channelId)
                && MysqlManager.Instance().isMemberOfChannel(channelId, ""+ SocketConnection.Instance().userMap.get(channel).getUserId())
                && SocketConnection.Instance().setChannelId(channelId, channel)){

            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
        } else {
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
        }

        SocketConnection.trySendMessage(channel, jsonObject.toString());
    }
}
