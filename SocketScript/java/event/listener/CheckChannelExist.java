package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class CheckChannelExist implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());

        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.IS_VALID.toString(),
                        MysqlManager.Instance().isChannelExist(channelId)));
    }
}
