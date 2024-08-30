package event.listener;

import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class GetProjectData implements SocketEventListener.EventListener {
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        int projectId = jsonObject.getInt(SocketEventListener.eKey.PROJECT_ID.toString());
        JSONArray requestData = jsonObject.getJSONArray(SocketEventListener.eKey.DATA.toString());

        JSONObject resultData = new JSONObject();
        ProjectEditor projectEditor = ProjectEditor.GetProjectEditor(projectId);
        if(projectEditor == null){
            SocketConnection.trySendMessage(channel, jsonObject
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR)
                    .put(SocketEventListener.eKey.MEMBERS.toString(), "porject id error"));
            return;
        }

        for (int i = 0; i < requestData.length(); ++i){
            switch ( ProjectEditor.Key.toKey( requestData.getString(i)) ){
                case ProjectName:
                    resultData.put(requestData.getString(i),
                            projectEditor.getProjectName());
                    break;
                case Structure:
                    resultData.put(
                            requestData.getString(i),
                            projectEditor.loadStructure().structureToJsonObject());
                    break;
                case Options:
                    //미구현
                    resultData.put(
                            requestData.getString(i),
                            "{}"
                    );
                    break;
                case Role:
                    resultData.put(
                            requestData.getString(i),
                            projectEditor.loadRoleData().roleDataToJsonObject().toString());
                    break;
                case NONE:
                default:
                    System.out.println("can't read type" + requestData.getString(i));
            }
        }

        SocketConnection.trySendMessage(channel, jsonObject
                .put(SocketEventListener.eKey.DATA.toString(), resultData));
    }
}
