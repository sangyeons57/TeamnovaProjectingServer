package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

public class CreateDMChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        String userId1 = jsonObject.getString(SocketEventListener.eKey.USER_ID1.toString());
        String userId2 = jsonObject.getString(SocketEventListener.eKey.USER_ID2.toString());

        if( SocketConnection.Instance().userMap.containsKey(channel)
                &&!SocketConnection.Instance().userMap.get(channel).getUserId().equals(SocketConnection.UserData.NOT_SETUP)
                && SocketConnection.Instance().userMap.get(channel).getUserId().equals(userId1)){
            MysqlManager.Instance().createDMChannel(Integer.parseInt(userId1), Integer.parseInt(userId2));
        }
    }
}
