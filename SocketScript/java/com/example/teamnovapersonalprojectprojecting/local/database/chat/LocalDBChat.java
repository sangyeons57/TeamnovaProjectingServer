package com.example.teamnovapersonalprojectprojecting.local.database.chat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

import java.util.HashMap;
import java.util.Map;

public class LocalDBChat extends SQLiteOpenHelper {
    public static final String DB_NAME = "Chat.db";
    public static final int DB_VERSION = 3;

    private Map<Class<? extends LocalDBAttribute>, LocalDBAttribute> databaseChatMap;

    private static LocalDBChat instance = null;
    public static LocalDBChat Instance(){
        if(instance == null){
            instance = new LocalDBChat(DataManager.Instance().currentContext);
        }
        return instance;
    }
    public static void Reset(){
        instance = null ;
    }

    public static void LOG(String title, int logText){
        Log.d(LocalDBChat.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(LocalDBChat.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(LocalDBChat.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(LocalDBChat.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(LocalDBChat.class.getSimpleName(), logText);
    }
    public LocalDBChat(@Nullable Context context) {
        super(context, DataManager.Instance().userId + "_" + DB_NAME, null, DB_VERSION);
        if(DataManager.Instance().userId == DataManager.NOT_SETUP_I) {
            throw new Error("userId not setup");
        }
        databaseChatMap = new HashMap<>();

        this.Register();
    }

    private void Register(){
        databaseChatMap.put(DB_ChatTable.class, new DB_ChatTable(this));
    }


    public static <T extends LocalDBAttribute> T GetTable(Class<T> table){
        LocalDBAttribute db = Instance().databaseChatMap.get(table);
        return table.cast(db);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (LocalDBAttribute attribute: databaseChatMap.values()) {
            if(attribute.getCreateQuery() != null){
                db.execSQL(attribute.getCreateQuery());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (LocalDBAttribute attribute: databaseChatMap.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + attribute.getTableName());
        }
        onCreate(db);
    }
}
