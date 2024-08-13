package event.listener;

import com.sun.org.apache.bcel.internal.generic.LOOKUPSWITCH;
import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SendMessage implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        if(!SocketConnection.Instance().userMap.containsKey(channel) ||
        SocketConnection.Instance().userMap.get(channel).getUserId() == SocketConnection.UserData.NOT_SETUP){
            System.out.println("userId not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else if (SocketConnection.Instance().userMap.get(channel).getChannelId() == SocketConnection.UserData.NOT_SETUP) {
            System.out.println("channel not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else {
            String datetime = Util.getCurrentDateTime();
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
            jsonObject.put(SocketEventListener.eKey.DATETIME.toString(), datetime);

            //현제 이부분 체팅 보낼떄 마다 sql로 확인 하는데
            // 한 번만 확인하고 그이후로는 확인했다는 데이터를 저장하는 방식으로 바꾸는게 좋아보임
            if(MysqlManager.Instance().isDMChannel(
                    Integer.parseInt(SocketConnection.Instance().userMap.get(channel).getChannelId()),
                    false)){

                String channelId = SocketConnection.Instance().userMap.get(channel).getChannelId();
                int userId = Integer.parseInt(SocketConnection.Instance().userMap.get(channel).getUserId());

                String tableName = "dm_" + channelId;
                MysqlManager.Instance().addChatData(tableName, userId, jsonObject.getString(SocketEventListener.eKey.MESSAGE.toString()), datetime);
                for (SocketChannel socket: SocketConnection.Instance().channelMap.get(channelId)) {
                    //equal메소드 작동이 잘안될수도있음 관련 에러 발생시 이거체크 braodcast쪽에서는 ==를씀
                    jsonObject.put(SocketEventListener.eKey.IS_SELF.toString(), channel.equals(socket));

                    System.out.println("channel: " +channelId+ " userId: " +userId + jsonObject.toString());
                    SocketConnection.trySendMessage(socket, jsonObject.toString());
                }
            }
        }
    }
}
