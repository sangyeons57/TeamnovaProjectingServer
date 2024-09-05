package com.example.teamnovapersonalprojectprojecting.socket;


import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketConnection {
    public static final int PORT = 5000;
    public static final String IP = "54-180-132-52";
    public static final String REGION = ".ap-northeast-2";
    public static final String SERVER_ADDRESS = "ec2-" + IP + REGION + ".compute.amazonaws.com";
    public static final String NOT_SETUP = "NOT_SETUP";

    private static SocketConnection instance = null;
    public static SocketConnection Instance(){
        if(instance == null){
            instance = new SocketConnection();
        }
        return instance;
    }
    public static void Reset(){
        instance = null;
    }

    public static void LOG(String title, int logText){
        Log.d(SocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(SocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(SocketConnection.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(SocketConnection.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(SocketConnection.class.getSimpleName(), logText);
    }
    public Thread networkThread;

    public BlockingQueue<String> taskQueue ;
    public List<SocketEventListener.eType> waitingListenList;

    private Socket clientSocket;
    private BufferedReader in;
    private DataOutputStream out;

    private SocketConnection(){
        taskQueue = new LinkedBlockingQueue<>();
        waitingListenList = new ArrayList<>();

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG("try connect to server ServerAddress: " + SERVER_ADDRESS + " PORT: " + PORT);
                    clientSocket = new Socket(SERVER_ADDRESS, PORT);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new DataOutputStream(clientSocket.getOutputStream());

                    LOG("connected to server");

                    setInputStream();
                    setOutputStream();
                    setReconnectSystem();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                    /**
                     * 오프라인 기능 대처하는 만들려면 이쪽에서 구현하면됨
                     */
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                }
            }
        });
        networkThread.start();
    }
    private void close(){
        try {
            if(clientSocket != null){
                clientSocket.close();
            }
            instance = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setInputStream(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        callEvent(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void setOutputStream(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        String message = taskQueue.take();
                        byte[] jsonBytes = message.getBytes(StandardCharsets.UTF_8);
                        out.writeInt(jsonBytes.length);
                        out.write(jsonBytes);
                        out.flush();
                        LOG("FINISH Sent to Server", message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startReconnect(){
        instance.close();
        LOG("start try reconnect message socket");
        SocketConnection.sendMessage(false, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_USER.toString())
                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));
    }

    private void setReconnectSystem(){
        new Thread(()->{
            DataManager.Instance().checkPingPong = true;
            while (DataManager.Instance().checkPingPong){
                SocketConnection.sendMessage(new JsonUtil().add(JsonUtil.Key.TYPE, SocketEventListener.eType.PING_PONG.toString()));
                DataManager.Instance().checkPingPong = false;
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            SocketConnection.Instance().startReconnect();
            FileSocketConnection.Instance().startReconnect();
        }).start();
    }

    private void callEvent(String message){
        try {
            JsonUtil jsonUtil = new JsonUtil(message);
            SocketEventListener.eType type = SocketEventListener.eType.toType(jsonUtil.getString(JsonUtil.Key.TYPE, ""));
            waitingListenList.remove(type);
            LOG("SERVER RECEIVE [" + type + "]", message);
            String waitingText = "";
            for (SocketEventListener.eType eType: waitingListenList) {
                waitingText += eType.toString() + " ";
            }
            if(!waitingText.isEmpty()){
                LOG("WAITING", waitingText);
            }

            if(jsonUtil.has(JsonUtil.Key.ERROR)) {
                LOGe("ERROR: " + jsonUtil.getString(JsonUtil.Key.ERROR, DataManager.NOT_SETUP_S));
                LOGe("ERROR MESSAGE: " + jsonUtil.getString(JsonUtil.Key.ERROR_MESSAGE, DataManager.NOT_SETUP_S));
                printStackTrace(jsonUtil.getJsonArray(JsonUtil.Key.ERROR_STACKTRACE, new JSONArray()));
                LOGe("ERROR DATA: " + jsonUtil.getString(JsonUtil.Key.DATA, DataManager.NOT_SETUP_S));
            }

            SocketEventListener.callEvent(type, new JsonUtil(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void printStackTrace(JSONArray jsonArray){
        try {
            for(int i = 0; i < jsonArray.length(); i++){
                LOGe( jsonArray.getString(i));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(JsonUtil jsonUtil) { sendMessage(true, jsonUtil); }
    public static void sendMessage(boolean waitingResponse, JsonUtil jsonUtil){
        SocketEventListener.eType type = SocketEventListener.eType.toType(jsonUtil.getString(JsonUtil.Key.TYPE, NOT_SETUP));
        if(type.equals(SocketEventListener.eType.NONE)){
            LOGe("type is not setup" + jsonUtil.toString());
            return;
        }
        sendMessage(type, jsonUtil.toString(), waitingResponse);
    }

    private static void sendMessage(SocketEventListener.eType type, String message, boolean waitingResponse) {
        if(waitingResponse && Instance().waitingListenList.contains(type)){
            LOG("ALREADY Sent to Server [" + type + "]", message);
            return;
        }
        Instance().waitingListenList.add(type);
        try {
            LOG("START Sent to Server", message);
            Instance().taskQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOG("InterruptedException", "sendMessage");
        }
    }
}
