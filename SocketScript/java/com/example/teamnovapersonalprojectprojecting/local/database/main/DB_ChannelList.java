package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;

public class DB_ChannelList extends LocalDBAttribute {
    public DB_ChannelList(SQLiteOpenHelper sqlite) {
        super(sqlite);

    }


    public void addChanelListByServer(int channelId){
        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_CHANNEL_DATA)
                .add(JsonUtil.Key.CHANNEL_ID, channelId));
    }

    public boolean removeChannel(int channelId){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            db.delete(getTableName(), "id = ?", new String[]{String.valueOf(channelId)});
            return true;
        }
    }

    public int addChannelList(int id, JSONArray members, int isDM){
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("members", members.toString());
            values.put("isDM", isDM);

            long row = db.insertWithOnConflict(getTableName(),null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if(row == -1){
                LocalDBChat.LOGe("addOrUpdateChat sql error");
            } else {
                LocalDBChat.LOG("addOrUpdate chat", "[" +row + "] " + id + " " + isDM);
            }

            return (int)row;
        }
    }

    public int isChannelDM(int id){
        String query = "SELECT isDM FROM " + getTableName() + " WHERE id = ?";
        try( SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})){
            if(cursor.moveToFirst()){
                return cursor.getInt(0);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return DataManager.NOT_SETUP_I;
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `members` TEXT NOT NULL ," +
                "  `isDM` INT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "ChannelList";
    }
}
