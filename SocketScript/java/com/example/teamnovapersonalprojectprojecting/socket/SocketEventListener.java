package com.example.teamnovapersonalprojectprojecting.socket;

import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.socket.eventList.CheckChannelExist;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.CreateChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.CreateCategory;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.DeleteChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetAllProjectUserIncluded;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetChatData;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.CreateDMChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.FriendListAdded;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetChannelData;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetChannelProject;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.GetProjectData;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.JoinChannel;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.AddDMElement;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.PingPong;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.ReconnectFileSocket;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.ReloadDMList;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.SendMessage;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SocketEventListener {
    public enum eType {
        NONE("NONE"),
        PING_PONG("PING_PONG"),
        FILE("FILE"),
        FILE_INPUT_STREAM("FILE_INPUT_STREAM"),
        ADD_DM_ELEMENT("AddDMElement"),
        ADD_FRIEND_ON_WAITING("AddFriendOnWaiting"),
        ADD_WAITING("AddWaiting"),
        ALARM_DM("AlarmDM"),
        CHECK_CHANNEL_EXIST("CheckChannelExist"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        CREATE_PROJECT("CreateProject"),
        CREATE_CATEGORY("CreateCategory"),
        CREATE_CHANNEL("CreateChannel"),
        DELETE_CATEGORY("DeleteCategory"),
        DELETE_CHANNEL("DeleteChanel"),
        DELETE_PROJECT("DeleteProject"),
        DISPLAY_PROJECT_ELEMENT("DisplayProjectElement"),
        EDIT_CATEGORY_NAME("EditCategoryName"),
        EDIT_CHANNEL_NAME("EditChannelName"),
        EDIT_PROJECT_NAME("EditProjectName"),
        EXIT_CHANNEL("ExitChannel"),
        GET_PROJECT_DATA("GetProjectData"),
        GET_CHANNEL_PROJECT("GetChannelProject"),
        GET_ALL_PROJECT_USER_INCLUDED("GetAllProjectUserIncluded"),
        GET_CHANNEL_DATA("GetChannelData"),
        GET_CHAT_DATA("GetChatData"),
        GET_USER_DATA("GetUserData"),
        GET_LAST_CHAT_ID("GetLastChatId"),
        GET_FILE("GetFile"),
        GET_PROJECT_MEMBER_JOIN_TOKEN("GetProjectMemberJoinToken"),
        JOIN_CHANNEL("JoinChannel"),
        JOIN_PROJECT("JoinProject"),
        REMOVE_WAITING("RemoveWaiting"),
        RELOAD_DM_LIST("ReloadDMList"),
        RECONNECT_FILE_SOCKET("ReconnectFileSocket"),
        SEND_DM_END("SendDMEnd"),
        SEND_MESSAGE("SendMessage"),
        SEND_MESSAGE_SPECIFIC_PERSON("SendMessageSpecificPerson"),
        SET_USER("SetUser"),
        SET_PROFILE_IMAGE("SetProfileImage"),
        SET_PROJECT_PROFILE_IMAGE("SetProjectProfileImage"),
        UPDATE_FRIEND_LIST("UpdateFriendList"),
        IDENTIFY_PROJECT_INVITATIONS("IdentifyProjectInvitations"),

        _RELOAD("_Reload"),
        ;

        private eType(String name){
            this.name = name;
        }

        private String name;

        @Override
        public String toString() {
            return name;
        }

        public static eType toType(String str){
            for (eType event: eType.values()) {
                if(event.name.equalsIgnoreCase(str))
                    return event;
            }
            return eType.NONE;
        }
    }
    private static SocketEventListener instance = null;
    public static SocketEventListener Instance(){
        if(instance == null){
            instance = new SocketEventListener();
            instance.Register();
        }
        return instance;
    }
    public static void Reset(){
        instance = null;
    }

    public void Register(){
        addEvent(eType.JOIN_CHANNEL, new JoinChannel());
        addEvent(eType.ADD_FRIEND_ON_WAITING, new FriendListAdded());
        addEvent(eType.CHECK_CHANNEL_EXIST, new CheckChannelExist());
        addEvent(eType.CREATE_CATEGORY, new CreateCategory());
        addEvent(eType.CREATE_DM_CHANNEL, new CreateDMChannel());
        addEvent(eType.CREATE_CHANNEL, new CreateChannel());
        addEvent(eType.ADD_DM_ELEMENT, new AddDMElement());
        addEvent(eType.GET_ALL_PROJECT_USER_INCLUDED, new GetAllProjectUserIncluded());
        addEvent(eType.GET_CHANNEL_DATA, new GetChannelData());
        addEvent(eType.GET_PROJECT_DATA, new GetProjectData());
        addEvent(eType.RELOAD_DM_LIST, new ReloadDMList());
        addEvent(eType.RECONNECT_FILE_SOCKET, new ReconnectFileSocket());
        addEvent(eType.GET_CHAT_DATA, new GetChatData());
        addEvent(eType.SEND_MESSAGE, new SendMessage());
        addEvent(eType.GET_CHANNEL_PROJECT, new GetChannelProject());
        addEvent(eType.DELETE_CHANNEL, new DeleteChannel());
        addEvent(eType.PING_PONG, new PingPong());
    }

    public static void LOG(String title, String logText){
        Log.d(SocketEventListener.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(SocketEventListener.class.getSimpleName(), logText);
    }
    public static void LOGe(String logText){
        Log.e(SocketEventListener.class.getSimpleName(), logText);
    }

    private final Map<eType, List<EventListener>> eventListMap;
    private SocketEventListener(){
        eventListMap = new HashMap<>();

    }

    private static void addEvent (eType event, EventListener eventListener){
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }
        Instance().eventListMap.get(event).add(eventListener);
    }

    private static void removeEvent(eType event, EventListener eventListener){
        if(Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.get(event).remove(eventListener);
        }
    }

    private static final Queue<Map.Entry<eType, EventListener>> removeListenerQueue = new LinkedList<>();
    private static final Queue<Map.Entry<eType, EventListener>> addListenerQueue = new LinkedList<>();
    private static final Queue<Runnable> callEventQueue = new LinkedList<>();
    private static boolean isExecuting = false;
    public static EventListener addRemoveEventQueue(eType event, EventListener listener){
        removeListenerQueue.add(new AbstractMap.SimpleEntry<>(event, listener));
        return listener;
    }
    public static EventListener addAddEventQueue(eType event, EventListener listener){
        addListenerQueue.add(new AbstractMap.SimpleEntry<>(event, listener));
        return listener;
    }

    public static synchronized void callEvent(eType event, JsonUtil jsonutil){
        callEventQueue.add(()->{
            if(!Instance().eventListMap.containsKey(event)) {
                Instance().eventListMap.put(event,new ArrayList<>());
            }

            queueRotation();

            SocketConnection.LOG("CALL EVENT", event.toString() + " " + Instance().eventListMap.get(event).size());
            new Retry(()->{
                try {
                    Instance().eventListMap.get(event).stream()
                            .filter(listener -> listener.run(jsonutil))
                            .findFirst();
                    return true;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return false;
                }
            }).setMaxRetries(5).execute();
        });

        if(!isExecuting){
            executeCallEvent();
        }
    }

    public static synchronized void executeCallEvent(){
        isExecuting = true;
        SocketConnection.LOG("EXECUTE CALL EVENT");
        Runnable task;
        while ((task = callEventQueue.poll()) != null) {
            SocketConnection.LOG("REMAINING CALL EVENT SIZE", callEventQueue.size());
            task.run();
        }
        isExecuting = false;
    }

    private static void queueRotation(){
        Map.Entry<eType, EventListener> entry;

        while ((entry = addListenerQueue.poll()) != null){
            SocketConnection.LOG("ADD EVENT", entry.getKey().toString());
            addEvent( entry.getKey(), entry.getValue());
        }

        while ((entry = removeListenerQueue.poll()) != null){
            removeEvent(entry.getKey(), entry.getValue());
        }
    }

    public interface EventListener{
        public boolean run(JsonUtil jsonUtil);
    }

    public abstract static class EventListenerOnce implements EventListener{
        public abstract boolean runOnce(JsonUtil jsonUtil);
        public eType type;
        public EventListenerOnce(eType type){
            this.type = type;
        }
        @Override
        public boolean run(JsonUtil jsonUtil){
            addRemoveEventQueue(type, this);
            return runOnce(jsonUtil);
        }
    }
}
