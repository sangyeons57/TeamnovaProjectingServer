package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class SendMessage implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        if(!SocketConnection.Instance().userMap.containsKey(channel) ||
        SocketConnection.Instance().userMap.get(channel).getUserId() == SocketConnection.UserData.NOT_SETUP_I){
            System.out.println("userId not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else if (SocketConnection.Instance().userMap.get(channel).getChannelId() == SocketConnection.UserData.NOT_SETUP_I) {
            System.out.println("channel not setup");
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "error");
            SocketConnection.trySendMessage(channel, jsonObject.toString());
        } else {
            String datetime = Util.getCurrentDateTime();
            jsonObject.put(SocketEventListener.eKey.STATUS.toString(), "success");
            jsonObject.put(SocketEventListener.eKey.DATETIME.toString(), datetime);

            int channelId = SocketConnection.Instance().userMap.get(channel).getChannelId();
            int userId = jsonObject.getInt(SocketEventListener.eKey.USER_ID.toString());
            String message = jsonObject.getString(SocketEventListener.eKey.MESSAGE.toString());
            boolean isDM = jsonObject.getBoolean(SocketEventListener.eKey.IS_DM.toString());
            String tableName = ((isDM) ? "dm_" : "p_") + channelId;

            //현제 이부분 체팅 보낼떄 마다 sql로 확인 하는데
            // 한 번만 확인하고 그이후로는 확인했다는 데이터를 저장하는 방식으로 바꾸는게 좋아보임
            if(MysqlManager.Instance().isMemberOfChannel(channelId, ""+userId)){

                int chatId = MysqlManager.Instance().addChatData(tableName, userId, message, datetime);
                for (int memberId: MysqlManager.Instance().getMembersOfChannel(channelId)) {
                    if (SocketConnection.Instance().userIdMap.containsKey(memberId)) {
                        SocketChannel socketChannel = SocketConnection.Instance().userIdMap.get(memberId);

                        if(SocketConnection.Instance().channelMap.containsKey(channelId)
                        && SocketConnection.Instance().channelMap.get(channelId).contains(socketChannel)){
                            //equal메소드 작동이 잘안될수도있음 관련 에러 발생시 이거체크 braodcast쪽에서는 ==를씀
                            jsonObject.put(SocketEventListener.eKey.IS_SELF.toString(), channel.equals(socketChannel));
                            jsonObject.put(SocketEventListener.eKey.CHAT_ID.toString(), chatId);

                            System.out.println("channel: " +channelId+ " userId: " +userId + jsonObject.toString());
                            SocketConnection.trySendMessage(socketChannel, jsonObject.toString());
                        }

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
    }
}
