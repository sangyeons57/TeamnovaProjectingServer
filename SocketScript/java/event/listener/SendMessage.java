package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class SendMessage implements SocketEventListener.EventListener {
    private int userId;
    private int channelId;
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        if(!SocketConnection.Instance().userMap.containsKey(channel) ||
                (userId = SocketConnection.Instance().userMap.get(channel).getUserId()) == Util.NOT_SETUP_I){
            System.out.println("userId not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else if ((channelId =SocketConnection.Instance().userMap.get(channel).getChannelId()) == Util.NOT_SETUP_I
                && SocketConnection.Instance().channelMap.get(channelId).contains(channel)) {
            System.out.println("channel not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else {
            String datetime = Util.getCurrentDateTime();
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
            jsonObject.put(SocketEventListener.eKey.DATETIME.toString(), datetime);

            String message = jsonObject.getString(SocketEventListener.eKey.MESSAGE.toString());
            boolean isDM = MysqlManager.Instance().isDMChannel(channelId, true);
            String tableName = ((isDM) ? "dm_" : "p_") + channelId;

            //현제 이부분 체팅 보낼떄 마다 sql로 확인 하는데
            // 한 번만 확인하고 그이후로는 확인했다는 데이터를 저장하는 방식으로 바꾸는게 좋아보임
            int chatId = MysqlManager.Instance().addChatData(tableName, userId, message, datetime);
            for (SocketChannel socketChannel : SocketConnection.Instance().channelMap.get(channelId)) {

                jsonObject.put(SocketEventListener.eKey.IS_SELF.toString(), channel.equals(socketChannel));
                jsonObject.put(SocketEventListener.eKey.CHAT_ID.toString(), chatId);

                System.out.println("channel: " +channelId+ " userId: " +userId + jsonObject.toString());
                SocketConnection.trySendMessage(socketChannel, jsonObject.toString());

                if (isDM){
                    SocketConnection.trySendMessage(socketChannel, new JSONObject()
                            .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ADD_DM_ELEMENT.toString())
                            .put(SocketEventListener.eKey.DATETIME.toString(), datetime)
                            .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId)
                            .toString());
                }
            }
        }
    }
}
