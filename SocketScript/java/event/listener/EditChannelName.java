package event.listener;

import org.example.MysqlManager;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class EditChannelName implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());
        String newChannelName = jsonObject.getString(SocketEventListener.eKey.NAME.toString());

        MysqlManager.Instance().updateChannelName(channelId, newChannelName);

    }
}
