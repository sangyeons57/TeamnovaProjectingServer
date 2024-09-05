package event.listener;

import org.example.ProjectEditor;
import org.example.SocketConnection;
import org.example.SocketEventListener;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;

public class CreateProject implements SocketEventListener.EventListener {
    public static final int PROJECT_MANAGER_ID = 0;
    public static final String PROJECT_MANAGER = "관리자";
    @Override
    public void run(SocketChannel channel, JSONObject jsonObject) {
        String projectName = jsonObject.getString(SocketEventListener.eKey.PROJECT_NAME.toString());
        boolean isOpen = jsonObject.getBoolean(SocketEventListener.eKey.IS_PRIVATE.toString());


        ProjectEditor projectEditor = ProjectEditor.GenerateNewProject(projectName);

        projectEditor.setIsOpen(isOpen);
        ProjectEditor.Member creator = projectEditor.addMember(SocketConnection.Instance().userMap.get(channel).getUserId());
        creator.registerToUser();

        projectEditor.addRole(PROJECT_MANAGER_ID, PROJECT_MANAGER).addAttribute(ProjectEditor.Role.Attribute.ProjectMaster);
        projectEditor.addRoleToMember(creator, PROJECT_MANAGER_ID);
        projectEditor.applyAllMember();
        projectEditor.applyRoleUpdate();
        SocketConnection.trySendMessage(channel, jsonObject);
    }
}
