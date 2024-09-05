package com.example.teamnovapersonalprojectprojecting.socket.eventList;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_Project;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ProjectStructure;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.ProjectAdapter;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetProjectData implements SocketEventListener.EventListener {
    public static final String ProjectName = "ProjectName";
    public static final String Structure = "Structure";
    public static final String Options = "Options";
    public static final String Role = "Role";
    
    public static final String StructureId = "structureId";
    public static final String StructureName = "structureName";
    public static final String Elements = "elements";

    @Override
    public boolean run(JsonUtil jsonUtil) {
        int projectId = jsonUtil.getInt(JsonUtil.Key.PROJECT_ID, DataManager.NOT_SETUP_I);
        JSONObject data = jsonUtil.getJsonObject(JsonUtil.Key.DATA, new JSONObject());
        try {
            if(data.has(ProjectName)){
                String projectName = data.getString(ProjectName);
                LocalDBMain.GetTable(DB_Project.class).updateNameById(projectId, projectName);
            }
            if(data.has(Structure)){
                JSONObject structure = data.getJSONObject(Structure);
                LocalDBMain.GetTable(DB_ProjectStructure.class).replaceStructureById(projectId, structure);
            }
            if(data.has(Options)){
                JSONObject options = data.getJSONObject(Options);
                LocalDBMain.GetTable(DB_ProjectStructure.class).replaceOptionsById(projectId, options);
            }
            if(data.has(Role)){
                JSONObject role = data.getJSONObject(Role);
                LocalDBMain.GetTable(DB_ProjectStructure.class).replaceRoleById(projectId, role);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    
    public static List<ProjectAdapter.CategoryItem> getProjectItemListFromStructure(JSONObject jsonObject){
        List<ProjectAdapter.CategoryItem> result = new ArrayList<>();
        if(jsonObject == null){
            return result;
        }

        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()){
                String key = keys.next();
                JSONObject item = jsonObject.getJSONObject(key);

                List<ProjectAdapter.ChannelItem> channelItemList = new ArrayList<>();
                for (int channelId: DataManager.JsonArrayToIntegerList(item.getJSONArray(Elements))) {
                    String name = LocalDBMain.GetTable(DB_ProjectChannelList.class).getChannelName(channelId);
                    if(name.equals(DataManager.NOT_SETUP_S)){ continue; }

                    ProjectAdapter.ChannelItem channelItem = new ProjectAdapter.ChannelItem(channelId, name);
                    channelItemList.add(channelItem);
                }

                result.add( new ProjectAdapter.CategoryItem(item.getInt(StructureId), item.getString(StructureName), channelItemList));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return result;
    }
}
