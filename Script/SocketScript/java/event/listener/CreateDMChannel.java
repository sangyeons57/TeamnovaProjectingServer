package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class CreateDMChannel implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int userId1 = jsonObject.getInt(SocketEventListener.eKey.USER_ID1.toString());
        int userId2 = jsonObject.getInt(SocketEventListener.eKey.USER_ID2.toString());

        if( SocketConnection.Instance().userMap.containsKey(channel)
                && SocketConnection.Instance().userMap.get(channel).getUserId() != SocketConnection.UserData.NOT_SETUP_I
                && SocketConnection.Instance().userMap.get(channel).getUserId() == userId1){

            int channelId = MysqlManager.Instance().getDMChannelId(userId1, userId2);
            if(channelId == 0){
                String message = "channel is created";
                channelId = MysqlManager.Instance().createDMChannel(userId1, userId2);
                MysqlManager.Instance().addChatData("dm_"+ channelId, Util.SYSTEM_ID, message, Util.getCurrentDateTime());
            }
            /**
             * dm_Channel을 사용자에게 추가해서 사용자가 dm체널을 볼떄
             * 최신 메세지 보넨순으로 정렬하게 할거다
             * 그러기 위해서 필요한것은
             * - dm의 id
             * - dm 상대의 id
             * 를 저장 해야한다.
             * {
             * channelId: "00",
             * userId: "00",
             * }
             * 이런식으로 저장하면 될ㄷ스
             */
            MysqlManager.Instance().updateUsersDMChannelMoveForward(channelId);

            Map<String, Object> user1 = MysqlManager.Instance().getUserByUserId(userId1);
            Map<String, Object> user2 = MysqlManager.Instance().getUserByUserId(userId2);


            JSONObject data;
            data = new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.CREATE_DM_CHANNEL.toString())
                    .put(SocketEventListener.eKey.USER_ID.toString(), userId2)
                    .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId)
                    .put(SocketEventListener.eKey.USERNAME.toString(), user2.get("username"))
                    .put(SocketEventListener.eKey.DATETIME.toString(), Util.getCurrentDateTime());
            SocketConnection.trySendMessage(SocketConnection.Instance().userIdMap.get(userId1), data.toString());

            data = new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.CREATE_DM_CHANNEL.toString())
                    .put(SocketEventListener.eKey.USER_ID.toString(), userId1)
                    .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId)
                    .put(SocketEventListener.eKey.USERNAME.toString(), user1.get("username"))
                    .put(SocketEventListener.eKey.DATETIME.toString(), Util.getCurrentDateTime());
            SocketConnection.trySendMessage(SocketConnection.Instance().userIdMap.get(userId2), data.toString());
        }
    }
}
