package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;
import com.example.teamnovapersonalprojectprojecting.util.Retry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDBMain extends SQLiteOpenHelper {
    public static final String DB_NAME = "Main.db";
    public static final int DB_VERSION = 15;

    private Map<Class<? extends LocalDBAttribute>, LocalDBAttribute> databaseMainMap;

    private static LocalDBMain instance = null;
    public static LocalDBMain Instance(){
        if(instance == null){
            instance = new LocalDBMain(DataManager.Instance().currentContext);
        }
        return instance;
    }
    public static void Reset(){
        instance = null ;
    }

    public static void LOG(String title, int logText){
        Log.d(LocalDBMain.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(LocalDBMain.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(LocalDBMain.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(LocalDBMain.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(LocalDBMain.class.getSimpleName(), logText);
    }
    public LocalDBMain(@Nullable Context context) {
        super(context, DataManager.Instance().userId + "_" + DB_NAME, null, DB_VERSION);
        if(DataManager.Instance().userId == DataManager.NOT_SETUP_I) {
            throw new Error("userId not setup");
        }
        databaseMainMap = new HashMap<>();

        this.Register();
    }

    private void Register(){
        databaseMainMap.put(DB_DMList.class, new DB_DMList(this));
        databaseMainMap.put(DB_UserList.class, new DB_UserList(this));
        databaseMainMap.put(DB_ChannelList.class, new DB_ChannelList(this));
        databaseMainMap.put(DB_FriendList.class, new DB_FriendList(this));
        databaseMainMap.put(DB_Project.class, new DB_Project(this));
        databaseMainMap.put(DB_ProjectMember.class, new DB_ProjectMember(this));
        databaseMainMap.put(DB_ProjectStructure.class, new DB_ProjectStructure(this));
        databaseMainMap.put(DB_ProjectChannelList.class, new DB_ProjectChannelList(this));
        databaseMainMap.put(DB_FileList.class, new DB_FileList(this));
    }

    public interface AfterCall {
        public void execute(JsonUtil jsonUtil);
        public static void Execute(JsonUtil jsonUtil, AfterCall afterCall) {
            if (afterCall != null) {
                afterCall.execute(jsonUtil);
            }
        }
    }

    public static <T extends LocalDBAttribute> T GetTable(Class<T> table){
        LocalDBAttribute db = Instance().databaseMainMap.get(table);
        return table.cast(db);
    }

    public static <T extends LocalDBAttribute> void printAllRows(Class<T> table){
        new Retry(()->{
            LocalDBAttribute attribute = Instance().databaseMainMap.get(table);
            String query = "SELECT * FROM " + attribute.getTableName();

            LOG("[" + attribute.getTableName() + "] 테이블 전체 출력");
            // SQLiteDatabase 인스턴스를 가져와서 읽기 전용으로 엽니다.
            try (SQLiteDatabase db = attribute.sqlite.getReadableDatabase();
                 Cursor cursor = db.rawQuery(query, null)) {

                // 커서가 가리키는 첫 번째 행으로 이동합니다.
                if (cursor != null && cursor.moveToFirst()) {
                    // 테이블의 컬럼 수를 가져옵니다.
                    int columnCount = cursor.getColumnCount();

                    // 컬럼 이름들을 출력합니다.
                    StringBuilder columnNames = new StringBuilder("Columns: ");
                    for (int i = 0; i < columnCount; i++) {
                        columnNames.append(cursor.getColumnName(i)).append(" | ");
                    }
                    LOG(columnNames.toString());

                    LOG("-----------------------");

                    // 각 행을 반복하며 데이터를 출력합니다.
                    do {
                        for (int i = 0; i < columnCount; i++) {
                            String columnName = cursor.getColumnName(i);
                            String columnValue = cursor.getString(i);
                            LOG(columnName, columnValue);
                        }
                        LOG("-----------------------");
                    } while (cursor.moveToNext()); // 다음 행으로 이동
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }).setMaxRetries(5).setRetryInterval(1000).execute();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (LocalDBAttribute attribute: databaseMainMap.values()) {
            db.execSQL(attribute.getCreateQuery());

            if(attribute instanceof TriggerQuery){
                for (String query: ((TriggerQuery)attribute).getTriggerQuery()) {
                    LOG("TriggerQuery", query);
                    db.execSQL(query);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (LocalDBAttribute attribute: databaseMainMap.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + attribute.getTableName());
        }
        onCreate(db);
    }

    public static interface TriggerQuery {
        public List<String> getTriggerQuery();
    }
}
