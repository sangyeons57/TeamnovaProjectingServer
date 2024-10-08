package org.example;

import com.mysql.cj.xdevapi.JsonArray;
import jdk.jshell.Snippet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * 서버 부분에서 소켓 으로 오는 데이터와 가는 데이터의 중간부분을 처리하는 역할
 */
public class SocketConnection {
    public static class UserData{
        private int userId = Util.NOT_SETUP_I;
        private int channelId = Util.NOT_SETUP_I;

        public int getUserId(){ return userId;}
        public int getChannelId(){ return channelId;}
    }
    public static class ChannelBuffer{
        public static final int LENGTH_FIELD_SIZE = 4;
        public ByteBuffer lengthBuffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE);
        public ByteBuffer messageBuffer = null;
        public int expectedLength = -1;
    }
    public static final int PORT = 5000;

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

    public Map<SocketChannel, ChannelBuffer> channelBufferMap;
    public Map<SocketChannel, UserData> userMap;
    public Map<Integer, SocketChannel> userIdMap;
    public Map<Integer, List<SocketChannel>> channelMap;

    private SocketConnection(){
        channelBufferMap = new HashMap<>();
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
        if(channelId == Util.NOT_SETUP_I || channel == null) {
            return false;
        }
        if(!userMap.containsKey(channel) || userMap.get(channel).userId == Util.NOT_SETUP_I) {
            return false;
        }

        //이전 체널의 체널 아이디로 체널 접근해서 제거
        int beforeChannelId = userMap.get(channel).channelId;
        if (beforeChannelId != Util.NOT_SETUP_I) {
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
        ChannelBuffer buffer = channelBufferMap.computeIfAbsent(clientChannel, k -> new ChannelBuffer());

        try {
            if (buffer.messageBuffer == null) {
                readMessageLength(clientChannel, buffer);
            }

            if(buffer.messageBuffer != null){
                readMessageContent(clientChannel, buffer);
            }
        } catch (IOException e) {
            cleanChannel(clientChannel);
            clientChannel.close();
            System.out.println("Client disconnected");
        } catch (JSONException e) {
            trySendMessage(clientChannel, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR.toString())
                    .put(SocketEventListener.eKey.MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.PRINT.toString(), e.getStackTrace())
            );
            e.printStackTrace();
            cleanChannel(clientChannel);
            //여기부분에서 JsoneEcetpion을 처리할떄 문제가 발생하는것 같음
        } catch (OutOfMemoryError e){
            //버퍼 문제 발생 버퍼 제거해서 초기화시키기
            channelBufferMap.remove(clientChannel);
            trySendMessage(clientChannel, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR.toString())
                    .put(SocketEventListener.eKey.MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.PRINT.toString(), e.getStackTrace())
            );
            e.printStackTrace();
            cleanChannel(clientChannel);
        }
    }

    private void cleanChannel(SocketChannel clientChannel){
        try {
            long size = 0;
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                int bytesRead = clientChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                size += bytesRead;

                buffer.clear();
            }

            System.out.println("clean channel: " + size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readMessageLength(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {
        clientChannel.read(buffer.lengthBuffer);
        if(!buffer.lengthBuffer.hasRemaining()) {
            buffer.lengthBuffer.flip();
            buffer.expectedLength = buffer.lengthBuffer.getInt();
            buffer.messageBuffer = ByteBuffer.allocate(buffer.expectedLength);
            buffer.lengthBuffer.clear();
        }
    }

    public void readMessageContent(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {
        clientChannel.read(buffer.messageBuffer);
        if(!buffer.messageBuffer.hasRemaining()){
            buffer.messageBuffer.flip();
            String jsonString = StandardCharsets.UTF_8.decode(buffer.messageBuffer).toString();
            System.out.println("RECEIVED [" + Optional.ofNullable(userMap.get(clientChannel)).orElse(new UserData()).getUserId() + "]: " + jsonString);
            callEventAndCatchError(clientChannel, jsonString);
            buffer.messageBuffer = null;
            buffer.expectedLength = -1;
        }
    }

    public void callEventAndCatchError(SocketChannel clientChannel, String jsonString){
        try{
            SocketEventListener.callEvent(clientChannel, new JSONObject(jsonString));
        } catch (JSONException jsonException){
            SocketConnection.trySendMessageOnlyUseSocketChannel(clientChannel, new JSONObject()
                    .put(SocketEventListener.eKey.ERROR.toString(), "error-json")
                    .put(SocketEventListener.eKey.ERROR_MESSAGE.toString(), jsonException.getMessage())
                    .put(SocketEventListener.eKey.ERROR_STACKTRACE.toString(), stackTraceToJsonArray(jsonException.getStackTrace()))
                    .put(SocketEventListener.eKey.DATA.toString(), jsonString)
                    .toString());
        } catch (Exception e) {
            SocketConnection.trySendMessageOnlyUseSocketChannel(clientChannel, new JSONObject()
                    .put(SocketEventListener.eKey.ERROR.toString(), "error")
                    .put(SocketEventListener.eKey.ERROR_MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.ERROR_STACKTRACE.toString(), stackTraceToJsonArray(e.getStackTrace()))
                    .put(SocketEventListener.eKey.DATA.toString(), jsonString)
                    .toString());
        }
    }

    public JSONArray stackTraceToJsonArray(StackTraceElement[] stackTraceElements){
        JSONArray result = new JSONArray();
        for (StackTraceElement element: stackTraceElements) {
            result.put(element.toString());
        }
        return result;
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
        if(clientChannel  == null){
            return false;
        }
        try {
            String user = "user";
            if(Instance().userMap.containsKey(clientChannel)) {
                user = "[" + Instance().userMap.get(clientChannel).userId + "]";
            }
            System.out.println(user + " Send Message: " + message);

            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            clientChannel.write(buffer);

            return true;
        } catch (IOException e){
            e.printStackTrace();
            Instance().cleanChannel(clientChannel);
            try {
                clientChannel.close();
            } catch (IOException ex) {
                e.printStackTrace();
            }
            System.out.println("Client disconnected");
        }
        return false;
    }

    public static boolean trySendMessage(SelectionKey receiver, String message){
        return trySendMessage((SocketChannel) receiver.channel(), message);
    }

    public static boolean trySendMessage(int userId, String message){
        if(Instance().userIdMap.containsKey(userId)){
            return trySendMessage(Instance().userIdMap.get(userId), message);
        }
        return false;
    }
    public static boolean trySendMessage(int userId, JSONObject message){
        return trySendMessage(userId, message.toString());
    }
    public static void trySendMessageOnlyUseSocketChannel(SocketChannel clientChannel, String message){
        try {
            ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            clientChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
