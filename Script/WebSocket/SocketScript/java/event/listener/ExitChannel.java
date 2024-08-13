package event.listener;

import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

public class ExitChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        SocketConnection.Instance().setChannelId(SocketConnection.UserData.NOT_SETUP, channel);
    }
}
