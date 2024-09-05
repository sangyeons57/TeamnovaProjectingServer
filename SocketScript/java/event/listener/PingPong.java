package event.listener;

import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class PingPong implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        SocketConnection.trySendMessageOnlyUseSocketChannel(channel, jsonObject.toString());
    }
}
