package com.example.teamnovapersonalprojectprojecting.util;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;

import com.example.teamnovapersonalprojectprojecting.activity.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.ui.home.DMAdapter;
import com.example.teamnovapersonalprojectprojecting.ui.home.ProjectAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class DataManager {
    private static DataManager instance = null;
    public static DataManager Instance(){
        if(instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    public static final int SYSTEM_ID = 1;
    public static final int NOT_SETUP_I = 0;
    public static final String NOT_SETUP_S = "NOT_SETUP";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String URL_PATTERN = "(https?|ftp):\\/\\/([^\\s\\/?\\.#]+\\.?)+(\\/[^\\s]*)?";
    public static final String URL_INVITE_PATTERN = "https?:\\/\\/" + SocketConnection.SERVER_ADDRESS + "\\/invite\\?token=\\w+";
    public static final Integer PERMISSION_READ_EXTERNAL_STORAGE = 101;

    public boolean checkPingPong;

    public String username;
    public int userId = NOT_SETUP_I;
    public int channelId = NOT_SETUP_I;
    public int projectId = NOT_SETUP_I;
    public String profilePath = NOT_SETUP_S;
    public String projectName = NOT_SETUP_S;
    public String stringUserId(){
        return String.valueOf(userId);
    }

    public HashMap<Integer, UserData> userDataMap;

    public Handler mainHandler;
    public Context currentContext;

    public List<ProjectAdapter.CategoryItem> projectItemList;
    public List<DMAdapter.DataModel> dmItemList;

    public Pattern urlPattern;
    public Pattern urlInvitePattern;

    private DataManager(){
         userDataMap = new HashMap<>();
         projectItemList = new ArrayList<>();
         dmItemList = new ArrayList<>();
         urlPattern = Pattern.compile(URL_PATTERN);
         urlInvitePattern = Pattern.compile(URL_INVITE_PATTERN);
    }


    public void Logout(Context context){
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("Logout.php"));
        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                if(jsonObject.getString("status").equals("success")) {
                    instance = null;
                    SocketConnection.Reset();
                    SocketEventListener.Reset();
                    LocalDBMain.Reset();
                    LocalDBChat.Reset();
                    mainHandler.post(()->{
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        EncryptedSharedPrefsManager.init(context, EncryptedSharedPrefsManager.LOGIN);
                        EncryptedSharedPrefsManager.clearFileData();
                    });
                }
            }
        });
    }

    public ProjectAdapter.CategoryItem getCategoryItem(int categoryId){
        return projectItemList.stream()
                .filter(item -> item.getCategoryId() == categoryId)
                .findFirst()
                .orElse(null);
    }

    public boolean addCategoryItem(int categoryId, String categoryName, List<ProjectAdapter.ChannelItem> itemList){
        return projectItemList.add(new ProjectAdapter.CategoryItem(categoryId, categoryName,itemList));
    }
    public boolean removeCategoryItem(int categoryId){
        return projectItemList.removeIf(item -> item.getCategoryId() == categoryId);
    }

    public static String getCurrentDateTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DataManager.DATE_FORMAT);
        return now.format(formatter);
    }

    public static UserData getUserData(int userId){
        if(Instance().userDataMap.containsKey(userId)) {
            return Instance().userDataMap.get(userId);
        } else {
            UserData userData = new UserData(userId);

            new Retry(()->{
                try {
                    LocalDBMain.GetTable(DB_UserList.class).getUser(userId).execute(new CursorReturn.Execute() {
                        @Override
                        public void run(Cursor cursor) {
                            if(cursor.moveToFirst()) {
                                userData.username = cursor.getString(DB_UserList.username);
                                int imageId = cursor.getInt(DB_UserList.profileImageId);
                                if(imageId != DataManager.NOT_SETUP_I) {
                                    checkAndSetProfileImage(userData, imageId);
                                }
                            }
                        }
                    });
                    return true;
                } catch (IllegalStateException e){
                    e.printStackTrace();
                    return false;
                }
            }).setMaxRetries(5).execute();
            Instance().userDataMap.put(userId, userData);
            return userData;
        }
    }
    public static UserData reloadUserData(int userId){
        Instance().userDataMap.remove(userId);
        return getUserData(userId);
    }

    private static void checkAndSetProfileImage(UserData userData, int imageId){
        if(LocalDBMain.GetTable(DB_FileList.class).checkFileExistAndCall(imageId)){
            LocalDBMain.GetTable(DB_FileList.class).getFileData(imageId).execute((cursor1)->{
                if(cursor1.moveToFirst()){
                    userData.profileImagePath = cursor1.getString(3);
                }
            });
        } else {
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.FILE_INPUT_STREAM,new SocketEventListener.EventListenerOnce(SocketEventListener.eType.FILE_INPUT_STREAM){
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    userData.profileImagePath = jsonUtil.getString(JsonUtil.Key.DATA, "");
                    return false;
                }
            });
        }
    }

    public static List<String> JsonArrayToStringList(JSONArray jsonArray) throws JSONException {
        List<String> result = new ArrayList<>();
        for(int i  = 0; i < jsonArray.length(); ++i ){
            result.add(jsonArray.getString(i));
        }
        return result;
    }
    public static List<Integer> JsonArrayToIntegerList(JSONArray jsonArray) throws JSONException {
        List<Integer> result = new ArrayList<>();
        for(int i  = 0; i < jsonArray.length(); ++i ){
            result.add(jsonArray.getInt(i));
        }
        return result;
    }
}
