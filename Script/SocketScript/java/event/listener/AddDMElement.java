package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

/**
 * 이쪽 이벤트는 메세지를 보냈을때 발생해야할 작업을 작성할 공간이다
 * 여기서 작동할것
 *  - DM 을 보넨 맴버를 최상위로 올리기
 *  - DM 보내면 발생해야할 각종 작업들 (지금은 생각 안남)
 *  channel은 DM을 보낸사람이 들어오게 된다 따러서
 *  DM이면 상대 id를 알아내야한다.
 */
public class AddDMElement implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int channelId = SocketConnection.Instance().userMap.get(channel).getChannelId();
        int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
    }
}
