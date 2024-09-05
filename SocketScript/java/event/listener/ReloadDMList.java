package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.example.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class ReloadDMList implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        JSONArray result = new JSONArray();
        JSONArray dmChannelList = new JSONArray(MysqlManager.Instance().getUserByUserId(SocketConnection.Instance().userMap.get(channel).getUserId()).get("dm_channel").toString());
        for(int i = 0; i < dmChannelList.length(); i++) {
            int otherId = MysqlManager.Instance().getOtherIdByDMChannelId(dmChannelList.getInt(i), SocketConnection.Instance().userMap.get(channel).getUserId());
            String otherUsername = MysqlManager.Instance().getUserByUserId(otherId).get("username").toString();
            String lastChatTime = MysqlManager.Instance().getDataOfChatLast(dmChannelList.getInt(i)).get("update_time").toString();
            lastChatTime = Util.changeDatetimeToFormat(lastChatTime);


            JSONObject row = new JSONObject()
                    .put(SocketEventListener.eKey.CHANNEL_ID.toString(), dmChannelList.getInt(i))
                    .put(SocketEventListener.eKey.OTHER_ID.toString(), otherId)
                    .put(SocketEventListener.eKey.OTHER_USERNAME.toString(), otherUsername)
                    .put(SocketEventListener.eKey.DATETIME.toString(), lastChatTime);

            result.put(row);
        }

        SocketConnection.trySendMessage(channel, new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.RELOAD_DM_LIST.toString())
                .put(SocketEventListener.eKey.DATA.toString(), result)
                .toString());
    }
}
