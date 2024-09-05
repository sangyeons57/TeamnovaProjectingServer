package event.listener;

import org.json.JSONObject;
import org.example.*;
import org.example.SocketEventListener;

import java.nio.channels.SocketChannel;


public class SetUser implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {

        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());

        SocketConnection.Instance().setUserId(userId, channel);

        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.STATUS.toString(), "success")
                .toString());
    }
}
