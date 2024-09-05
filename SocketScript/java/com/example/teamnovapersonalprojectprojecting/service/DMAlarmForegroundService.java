package com.example.teamnovapersonalprojectprojecting.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.LoginActivity;
import com.example.teamnovapersonalprojectprojecting.activity.MainActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FriendList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.FileSocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.EncryptedSharedPrefsManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.ServerConnectManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class DMAlarmForegroundService extends Service {
    public static final String NOT_RESTART_ACTION = "NotStartAction";
    private static final String TAG = "DMAlarmForegroundService";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;
    private Map<Integer, Notification> notificationMap;

    private DMAlarmForegroundService service;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ForegroundService", "Service Created");
        service = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationMap = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EncryptedSharedPrefsManager.init(this, EncryptedSharedPrefsManager.LOGIN);
        if(intent != null){
            if(NOT_RESTART_ACTION.equals(intent.getAction())) {
                //일반실행
                whenLoginFinish();
            } else {
                //서비스 종료로 인한 재실행
                Login();
            }
        }
        startForeground(NOTIFICATION_ID, createNotification());


        // 서비스가 강제로 종료되어도 다시 재시작하지 않도록 설정
        return START_STICKY;
    }

    private void whenLoginFinish(){
        SocketEventListener.addAddEventQueue(SocketEventListener.eType.ALARM_DM,(jsonUtil)->{

            return false;
        });
    }


    // Foreground Service에서 표시할 Notification 생성
    private Notification createNotification() {
        String channelId = TAG + "_channel";

        // Android 8.0 이상에서는 알림 채널을 필수로 사용해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    TAG + " Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 알림을 구성
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Foreground Service Running")
                        .setContentText("서비스가 실행 중입니다...")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        // 생성한 Notification 객체 반환
        return notificationBuilder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void Login(){
        EncryptedSharedPrefsManager.init(this, EncryptedSharedPrefsManager.LOGIN);
        if(!EncryptedSharedPrefsManager.hasKey("email", false) || !EncryptedSharedPrefsManager.hasKey("password", false)) {
            return;
        }
        String email = EncryptedSharedPrefsManager.getString("email", "");
        String password = EncryptedSharedPrefsManager.getString("password", "");
        ServerConnectManager serverConnectManager = new ServerConnectManager(ServerConnectManager.Path.CERTIFICATION.getPath("Login.php"))
                .add("email", email)
                .add("password", password);

        serverConnectManager.postEnqueue(new ServerConnectManager.EasyCallback(){
            @Override
            protected void onResponseSuccess(Response response) throws IOException {
                super.onResponseSuccess(response);
                serverConnectManager.getPHPSession(response);
            }

            @Override
            protected void onGetJson(JSONObject jsonObject) throws IOException, JSONException {
                super.onGetJson(jsonObject);
                final String status = jsonObject.getString("status");
                if(status.equals("success")) {
                    final int userId = jsonObject.getInt("user_id");
                    final String username = jsonObject.getString("user_name");
                    DataManager.Instance().userId = userId;
                    DataManager.Instance().username = username;
                    DataManager.Instance().profilePath = LocalDBMain.GetTable(DB_UserList.class).getProfileImagePath(DataManager.Instance().userId);

                    SocketConnection.sendMessage(false, new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SET_USER.toString())
                            .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));
                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.SET_USER, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.SET_USER) {
                        @Override
                        public boolean runOnce(JsonUtil jsonUtil) {
                            SocketEventListener.LOG(jsonUtil.toString());

                            //userId설정후에 FileSOckConnetion연결
                            FileSocketConnection.Instance();

                            service.whenLoginFinish();
                            return false;
                        }
                    });
                } else {
                    EncryptedSharedPrefsManager.clearFileData();
                }
            }
        });
    }
}
