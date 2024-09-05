package event.listener;

import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class ExitChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = SocketConnection.Instance().userMap.get(channel).getChannelId();
        SocketConnection.Instance().setChannelId(Util.NOT_SETUP_I, channel);
        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId));
    }
}
