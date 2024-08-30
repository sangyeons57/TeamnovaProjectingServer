package org.example;

import event.listener.*;
import org.json.JSONObject;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketEventListener {
    public enum eKey{
        NONE("NONE"),
        ERROR("ERROR"),
        TYPE("type"),
        STATUS("status"),
        ID("id"),
        PROJECT_ID("projectId"),
        USER_ID("userId"),
        USER_ID1("userId1"),
        USER_ID2("userId2"),
        OTHER_ID("otherId"),
        CHAT_ID("chatId"),
        NAME("name"),
        USERNAME("username"),
        OTHER_USERNAME("otherUsername"),
        FRIEND_NAME("friendName"),
        WAITING_USER_NAME("waitingUserName"),
        PROFILE_IMAGE("profileImage"),
        DATETIME("datetime"),
        CATEGORY_ID("categoryId"),
        CHANNEL_ID("channelId"),
        LAST_CHAT_ID("lastChatId"),
        CHANNEL_NAME("channelName"),
        PROJECT_NAME("projectName"),
        MESSAGE("message"),
        MEMBERS("members"),
        DATA("data"),
        LIMIT("limit"),
        OFFSET("offset"),
        IS_SELF("isSelf"),
        IS_VALID("isValid"),
        IS_DM("isDM"),
        IS_MODIFIED("isModified"),
        IS_PRIVATE("isPrivate"),
        DM_CHANNELS("dmChannels"),
        FRIENDS("friends"),
        JSON_ARRAY("jsonArray"),
        JSON_OBJECT("jsonObject"),
        PRINT("print"),
        TOKEN("token"),
        ;

        private eKey(String name){
            this.name = name;
        }

        private String name;

        @Override
        public String toString() {
            return name;
        }

        public static eKey toKey(String str){
            for (eKey key: eKey.values()) {
                if(key.name.equalsIgnoreCase(str))
                    return key;
            }
            return eKey.NONE;
        }
    }
    public enum eType {
        NONE("NONE"),
        ERROR("ERROR"),
        GET_CHAT_DATA("GetChatData"),
        ADD_DM_ELEMENT("AddDMElement"),
        ADD_FRIEND_ON_WAITING("AddFriendOnWaiting"),
        ADD_WAITING("AddWaiting"),
        CHECK_CHANNEL_EXIST("CheckChannelExist"),
        CREATE_CATEGORY("CreateCategory"),
        CREATE_CHANNEL("CreateChannel"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        CREATE_PROJECT("CreateProject"),
        DELETE_CATEGORY("DeleteCategory"),
        DELETE_CHANNEL("DeleteChanel"),
        DELETE_PROJECT("DeleteProject"),
        EDIT_CATEGORY_NAME("EditCategoryName"),
        EDIT_CHANNEL_NAME("EditChannelName"),
        EDIT_PROJECT_NAME("EditProjectName"),
        EXIT_CHANNEL("ExitChannel"),
        GET_ALL_PROJECT_USER_INCLUDED("GetAllProjectUserIncluded"),
        GET_CHANNEL_DATA("GetChannelData"),
        GET_USER_DATA("GetUserData"),
        GET_LAST_CHAT_ID("GetLastChatId"),
        GET_CHANNEL_PROJECT("GetChannelProject"),
        GET_PROJECT_DATA("GetProjectData"),
        GET_PROJECT_MEMBER_JOIN_TOKEN("GetProjectMemberJoinToken"),
        JOIN_CHANNEL("JoinChannel"),
        JOIN_PROJECT("JoinProject"),
        RELOAD_DM_LIST("ReloadDMList"),
        REMOVE_WAITING("RemoveWaiting"),
        SEND_MESSAGE("SendMessage"),
        SET_USER("SetUser"),
        IDENTIFY_PROJECT_INVITATIONS("IdentifyProjectInvitations"),

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

    public Map<eType, List<EventListener>> eventListMap;
    private SocketEventListener(){
        eventListMap = new HashMap<>();

    }

    public void Register(){
        addEvent(eType.ADD_FRIEND_ON_WAITING, new AddFriendOnWaiting());
        addEvent(eType.ADD_WAITING, new AddWaiting());
        addEvent(eType.CHECK_CHANNEL_EXIST, new CheckChannelExist());
        addEvent(eType.CREATE_CATEGORY, new CreateCategory());
        addEvent(eType.CREATE_DM_CHANNEL, new CreateDMChannel());
        addEvent(eType.CREATE_PROJECT, new CreateProject());
        addEvent(eType.CREATE_CHANNEL, new CreateChannel());
        addEvent(eType.DELETE_CATEGORY, new DeleteCategory());
        addEvent(eType.DELETE_CHANNEL, new DeleteChannel());
        addEvent(eType.EDIT_CATEGORY_NAME, new EditCategoryName());
        addEvent(eType.EDIT_CHANNEL_NAME, new EditChannelName());
        addEvent(eType.EDIT_PROJECT_NAME, new EditProjectName());
        addEvent(eType.EXIT_CHANNEL, new ExitChannel());
        addEvent(eType.RELOAD_DM_LIST, new ReloadDMList());
        addEvent(eType.GET_ALL_PROJECT_USER_INCLUDED, new GetAllProjectUserIncluded());
        addEvent(eType.GET_CHANNEL_DATA, new GetChannelData());
        addEvent(eType.GET_CHAT_DATA, new GetChatData());
        addEvent(eType.GET_USER_DATA, new GetUserData());
        addEvent(eType.GET_LAST_CHAT_ID, new GetLastChatId());
        addEvent(eType.GET_CHANNEL_PROJECT, new GetChannelProject());
        addEvent(eType.GET_PROJECT_DATA, new GetProjectData());
        addEvent(eType.GET_PROJECT_MEMBER_JOIN_TOKEN, new GetProjectMemberJoinToken());
        addEvent(eType.JOIN_CHANNEL, new JoinChannel());
        addEvent(eType.JOIN_PROJECT, new JoinProject());
        addEvent(eType.REMOVE_WAITING, new RemoveWaiting());
        addEvent(eType.SEND_MESSAGE, new SendMessage());
        addEvent(eType.SET_USER, new SetUser());
        addEvent(eType.IDENTIFY_PROJECT_INVITATIONS, new IdentifyProjectInvitations());
    }

    public static void addEvent(eType event, EventListener eventListener){
        if(!instance.eventListMap.containsKey(event)) {
            instance.eventListMap.put(event,new ArrayList<>());
        }
        instance.eventListMap.get(event).add(eventListener);
    }

    public static void callEvent(SocketChannel channel, JSONObject jsonObject){
        callEvent(eType.toType(jsonObject.getString(eKey.TYPE.toString())), channel, jsonObject);
    }
    public static void callEvent(eType event, SocketChannel channel, JSONObject jsonObject){
        System.out.println("CallEvent: " + event);
        if(!Instance().eventListMap.containsKey(event)) {
            Instance().eventListMap.put(event,new ArrayList<>());
        }
        Instance().eventListMap.get(event).stream()
                .forEach(eventListener -> eventListener.run(channel, jsonObject));
    }

    public interface EventListener{
        public void run(SocketChannel channel, JSONObject jsonObject);
    }
}
