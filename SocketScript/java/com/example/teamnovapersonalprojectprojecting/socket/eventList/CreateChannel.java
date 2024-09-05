package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.ProjectAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CreateChannel implements SocketEventListener.EventListener {
    public static final String ELEMENTS = "elements";

    @Override
    public boolean run(JsonUtil jsonUtil) {
        int projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I);
        int categoryId = jsonUtil.getInt(JsonUtil.Key.CATEGORY_ID, DataManager.NOT_SETUP_I);
        int channelId = jsonUtil.getInt(JsonUtil.Key.CHANNEL_ID, DataManager.NOT_SETUP_I);
        String channelName = jsonUtil.getString(JsonUtil.Key.NAME, DataManager.NOT_SETUP_S);

        try {
            JSONObject categoryJson = LocalDBMain.GetTable(DB_ProjectStructure.class).getCategoryByID(projectId, categoryId);
            List<Integer> elements = DataManager.JsonArrayToIntegerList(categoryJson.getJSONArray(ELEMENTS));
            if(!elements.contains(channelId)){
                elements.add(channelId);
            }

            LocalDBMain.GetTable(DB_ProjectStructure.class).replaceCategoryById(projectId, categoryId,
                    categoryJson.put(ELEMENTS, new JSONArray(elements)));
            LocalDBMain.GetTable(DB_ProjectChannelList.class).addProjectList(channelId, categoryId, projectId, channelName);

            if(DataManager.Instance().projectId == projectId){
                DataManager.Instance()
                        .getCategoryItem(categoryId)
                        .addChannel(new ProjectAdapter.ChannelItem(channelId, channelName));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return false;
    }
}
