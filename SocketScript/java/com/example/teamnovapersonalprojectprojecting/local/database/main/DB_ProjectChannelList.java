package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class DB_ProjectChannelList extends LocalDBAttribute {
    public DB_ProjectChannelList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addProjectByServer(int channelId){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_CHANNEL_PROJECT)
                .add(JsonUtil.Key.CHANNEL_ID, channelId));

    }
    public Map<String, Object> getChannelByChannelId(int channelId){
        Map<String, Object> result = new HashMap<>();
        String query = "SELECT * FROM " + getTableName() + " WHERE channelId = ?";
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(channelId)})){
            if (cursor.moveToFirst()) {
                result.put("channelId", cursor.getInt(cursor.getColumnIndexOrThrow("channelId")));
                result.put("categoryId", cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")));
                result.put("projectId", cursor.getInt(cursor.getColumnIndexOrThrow("projectId")));
                result.put("channelName", cursor.getString(cursor.getColumnIndexOrThrow("channelName")));
                return result;
            }
        } catch (Exception e){
            LocalDBMain.LOGe(e.getMessage());
        }
        return null;
    }

    public boolean removeChannel(int channelId){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            db.delete(getTableName(), "channelId = ?", new String[]{String.valueOf(channelId)});
            return true;
        }
    }

    public void addProjectList(int channelId, int categoryId, int projectId, String channelName){
        LocalDBMain.LOG(DB_ProjectChannelList.class.getSimpleName(), channelId + " " + categoryId + " " + projectId + " " + channelName);

        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("channelId", channelId);
            values.put("categoryId", categoryId);
            values.put("projectId", projectId);
            values.put("channelName", channelName);

            db.replace(getTableName(), null, values);
        }
    }

    public String getChannelName(int channelId) {
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT channelName FROM " + getTableName() + " WHERE channelId = ?", new String[]{String.valueOf(channelId)})){
            if( cursor.moveToFirst() ){
                return cursor.getString(0);
            }
        }
        return DataManager.NOT_SETUP_S;
    }
    public void setChannelName(int channelId, String channelName){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("channelName", channelName);

            db.update(getTableName(), values, "channelId = ?", new String[]{String.valueOf(channelId)});
        }
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`channelId` INT PRIMARY KEY NOT NULL ," +
                "  `categoryId` INT NOT NULL ," +
                "  `projectId` INT NOT NULL ," +
                "  `channelName` TEXT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "ProjectChannelList";
    }
}
