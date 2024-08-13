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
        TYPE("type"),
        STATUS("status"),
        ID("id"),
        USER_ID("userId"),
        USER_ID1("userId1"),
        USER_ID2("userId2"),
        USERNAME("username"),
        FRIEND_NAME("friendName"),
        WAITING_USER_NAME("waitingUserName"),
        DATETIME("datetime"),
        CHANNEL_ID("channelId"),
        MESSAGE("message"),
        DATA("data"),
        IS_SELF("isSelf"),
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
        ADD_FRIEND_ON_WAITING("AddFriendOnWaiting"),
        ADD_WAITING("AddWaiting"),
        CREATE_DM_CHANNEL("CreateDMChannel"),
        EXIT_CHANNEL("ExitChannel"),
        JOIN_DM_CHANNEL("JoinDMChannel"),
        SEND_MESSAGE("SendMessage"),
        SET_USER("SetUser"),
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
        addEvent(eType.CREATE_DM_CHANNEL, new CreateDMChannel());
        addEvent(eType.EXIT_CHANNEL, new ExitChannel());
        addEvent(eType.JOIN_DM_CHANNEL, new JoinDMChannel());
        addEvent(eType.SEND_MESSAGE, new SendMessage());
        addEvent(eType.SET_USER, new SetUser());
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
