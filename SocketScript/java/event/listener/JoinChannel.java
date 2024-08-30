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
        jsonObject.put(SocketEventListener.eKey.CHAT_ID.toString(),
                MysqlManager.Instance().getDataOfChatLast(channelId).get("id"));

        if(MysqlManager.Instance().isChannelExist(channelId)){
            if(MysqlManager.Instance().isDMChannel(channelId, false)){
                jsonObject.put(SocketEventListener.eKey.IS_DM.toString(), true);
                if(MysqlManager.Instance().isMemberOfChannel(channelId, SocketConnection.Instance().userMap.get(channel).getUserId())
                        && SocketConnection.Instance().setChannelId(channelId, channel)){

                    jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success-dm");
                } else {
                    jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "fail-dm");
                }
            } else {
                jsonObject.put(SocketEventListener.eKey.IS_DM.toString(), false);
                if(MysqlManager.Instance().checkMemberOfProjectByChannelId(channelId, SocketConnection.Instance().userMap.get(channel).getUserId())
                    && SocketConnection.Instance().setChannelId(channelId, channel)){
                    jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success-p");
                } else {
                    jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "fail-p");
                }
            }
        }

        SocketConnection.trySendMessage(channel, jsonObject.toString());
    }
}
