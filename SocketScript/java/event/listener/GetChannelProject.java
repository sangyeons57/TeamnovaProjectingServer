package event.listener;

import org.example.MysqlManager;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * channel_project테이블에
 * 데이터를 가져오는 이벤트
 *
 * 체널 id가 주어진 경우 해당 체널 데이터를 반환하고
 *
 * 프로젝트 id 가 주어진 경우 해당 프로제트 전체 체널을 반환한다.
 */
public class GetChannelProject implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        JSONArray resultArray = new JSONArray();
        if(jsonObject.has(SocketEventListener.eKey.CHANNEL_ID.toString())){
            int channelId = jsonObject.getInt(SocketEventListener.eKey.CHANNEL_ID.toString());
            if (MysqlManager.Instance().isChannelExist(channelId)){
                Map<String, Object> channelData = MysqlManager.Instance().getProjectChannelDataByChannelId(channelId);
                resultArray.put(createProjectChannelData(
                        channelId,
                        Integer.parseInt(channelData.get("category_id").toString()),
                        Integer.parseInt(channelData.get("project_id").toString()),
                        channelData.get("channel_name").toString()
                ));
            }
        } else  if (jsonObject.has(SocketEventListener.eKey.PROJECT_ID.toString())) {
            int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
            List<Map<String, Object>> channelDataList = MysqlManager.Instance().getExistProjectChannelDataByProjectId(projectId);
            for (Map<String, Object> channelData: channelDataList) {
                int channelId = Integer.parseInt(channelData.get("channel_id").toString());
                resultArray.put( createProjectChannelData(
                        channelId,
                        Integer.parseInt(channelData.get("category_id").toString()),
                        projectId,
                        channelData.get("channel_name").toString()
                ));
            }
        }
        SocketConnection.trySendMessage(channel, jsonObject.put(SocketEventListener.eKey.DATA.toString(), resultArray));
    }

    private JSONObject createProjectChannelData(int channelId, int categoryId, int projectId, String channelName){
        return new JSONObject()
                .put(SocketEventListener.eKey.CHANNEL_ID.toString(), channelId)
                .put(SocketEventListener.eKey.CATEGORY_ID.toString(), categoryId)
                .put(SocketEventListener.eKey.PROJECT_ID.toString(), projectId)
                .put(SocketEventListener.eKey.CHANNEL_NAME.toString(), channelName);
    }
}
