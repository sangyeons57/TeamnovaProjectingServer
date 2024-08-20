package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;


/**
 * 서버 부분에서 소켓 으로 오는 데이터와 가는 데이터의 중간부분을 처리하는 역할
 */
public class SocketConnection {
    public static class UserData{
        public static final String NOT_SETUP_S = "NOT_SETUP";
        public static final int NOT_SETUP_I = 0;
        private int userId = 0;
        private int channelId = 0;

        public int getUserId(){ return userId;}
        public int getChannelId(){ return channelId;}
    }
    public static int PORT = 5000;

    private static SocketConnection instance = null;
    public static SocketConnection Instance(){
        if(instance == null){
            instance = new SocketConnection();
        }
        return instance;
    }
    public static void LOG(String logText){
        System.out.println("SocketConnection: " + logText);
    }

    public Map<SocketChannel, UserData> userMap;
    public Map<Integer, SocketChannel> userIdMap;
    public Map<Integer, List<SocketChannel>> channelMap;

    private SocketConnection(){
        userMap = new HashMap<>();
        userIdMap = new HashMap<>();
        channelMap = new HashMap<>();
    }
    public void Start(){
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("version: 0.1");
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(serverSocketChannel, selector);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException ex){
                            ex.printStackTrace();
                        }
                        System.out.println("Client disconnected unexpectedly");
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean setUserId(int userId, SocketChannel channel){
        if(userId == 0 || channel == null){
            return false;
        }

        UserData userData;
        //데이터가 없는 경우 생성
        if(!userMap.containsKey(channel)){
            userData = new UserData();
            userData.userId = userId;
            userMap.put(channel, userData);
            userIdMap.put(userId, channel);
        } else{
            userData = userMap.get(channel);
        }

        //이전 데이터 제거
        userIdMap.remove(userData.userId);

        userMap.get(channel).userId = userId;
        userIdMap.put(userId, channel);

        return true;
    }
    public boolean setChannelId(int channelId, SocketChannel channel){
        if(channelId == UserData.NOT_SETUP_I || channel == null) {
            return false;
        }
        if(!userMap.containsKey(channel) || userMap.get(channel).userId == UserData.NOT_SETUP_I) {
            return false;
        }

        //이전 체널의 체널 아이디로 체널 접근해서 제거
        int beforeChannelId = userMap.get(channel).channelId;
        if (beforeChannelId != UserData.NOT_SETUP_I) {
            channelMap.get(beforeChannelId).remove(channel);

            //체널에 사용자가 없으면 제거
            if(channelMap.get(beforeChannelId).size() == 0){
                channelMap.remove(beforeChannelId);
            }
        }


        //체널 설정
        userMap.get(channel).channelId = channelId;
        if(!channelMap.containsKey(channelId)){
            channelMap.put(channelId, new ArrayList<>());
        }
        channelMap.get(channelId).add(channel);
        return true;
    }
    private void handleAccept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            clientChannel.close();
            System.out.println("Client disconnected");
            return;
        }

        buffer.flip();
        String message = new String(buffer.array(), 0, buffer.limit());
        System.out.println("Received: " + message);
        try {
            SocketEventListener.callEvent(clientChannel, new JSONObject(message));
        } catch (JSONException e){
            e.printStackTrace();
            System.out.println("It's not json object: " + message);
        }
    }

    public static void broadcastMessage(SelectionKey senderKey, String message) throws IOException {
        Selector selector = senderKey.selector();
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel && key != senderKey) {
                trySendMessage(senderKey, message);
            }
        }
    }
    public static boolean trySendMessage(SocketChannel clientChannel, JSONObject message) {
        return trySendMessage(clientChannel, message.toString());
    }
    public static boolean trySendMessage(SocketChannel clientChannel, String message) {
        try {
            String user = "[" + Instance().userMap.get(clientChannel).userId + "]";
            System.out.print(user + " Send Message: " + message);

            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            clientChannel.write(buffer);

            System.out.println("---success");
            return true;
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("---error");
            return false;
        }
    }

    public static boolean trySendMessage(SelectionKey receiver, String message){
        return trySendMessage((SocketChannel) receiver.channel(), message);
    }

    public static boolean trySendMessage(int userId, String message){
        return trySendMessage(Instance().userIdMap.get(userId), message);
    }
}
