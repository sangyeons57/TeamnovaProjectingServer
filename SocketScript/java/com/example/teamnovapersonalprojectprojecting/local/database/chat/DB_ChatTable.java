package com.example.teamnovapersonalprojectprojecting.local.database.chat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

public class DB_ChatTable extends LocalDBAttribute {
    public DB_ChatTable(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public long addOrUpdateChat(int channelId, int chatId, int writerId, String data, String lastTime, int isModified){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("channelId", channelId);
            values.put("chatId", chatId);
            values.put("writerId", writerId);
            values.put("data", data);
            values.put("lastTime", lastTime);
            values.put("isModified", isModified);
            long row = db.insertWithOnConflict(getTableName(),null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if(row == -1){
                LocalDBChat.LOGe("addOrUpdateChat sql error");
            } else {
                LocalDBChat.LOG("addOrUpdate chat", "[" +row + "] " + chatId + " " + writerId + " " + data + " " + lastTime + " " + isModified);
            }
            return row;
        }
    }
    public void addOrUpdateChatByServer(int channelId, int limit, int offset){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_CHAT_DATA)
                .add(JsonUtil.Key.CHANNEL_ID, channelId)
                .add(JsonUtil.Key.LIMIT, limit)
                .add(JsonUtil.Key.OFFSET, offset));
    }

    public Cursor getChatData(int id){
        String query = "SELECT * FROM " + getTableName() + " WHERE id = ? ";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return cursor;
    }
    public Cursor getChatData(int channelId, int chatId){
        String query = "SELECT * FROM " + getTableName() + " WHERE chatId = ? AND channelId = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(chatId), String.valueOf(channelId)});
        return cursor;
    }
    public int getLastChatId(int channelId){
        String query = "SELECT chatId FROM " + getTableName() + " WHERE channelId = ? ORDER BY chatId DESC LIMIT 1";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        try ( Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(channelId)}) ){
            if(cursor.moveToFirst()){
                return cursor.getInt(0);
            }
        }
        return DataManager.NOT_SETUP_I;
    }

    public CursorReturn getChatDataRangeFromBack(int channelId, int limit, int offset) {
        LocalDBChat.LOG("getChatDataRangeFromBack", "channelId: " + channelId + ", limit: " + limit + ", offset: " + offset);
        String query = "SELECT * FROM " + getTableName() + " WHERE channelId = ? ORDER BY chatId DESC LIMIT ? OFFSET ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(channelId), String.valueOf(limit), String.valueOf(offset)});
        LocalDBChat.LOG("getChatDataRangeFromBack", cursor.getCount());
        return new CursorReturn(cursor, db);
    }


        @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                "    (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "    channelId INTEGER NOT NULL," +
                "    chatId INTEGER NOT NULL," +
                "    writerId INTEGER NOT NULL," +
                "    data VARCHAR(1000) NOT NULL," +
                "    lastTime DATE NOT NULL," +
                "    isModified INTEGER NOT NULL," +
                "    UNIQUE(chatId, channelId) ON CONFLICT REPLACE" +
                ");";
    }

    @Override
    public String getTableName() {
        return "ChatTable";
    }
}
