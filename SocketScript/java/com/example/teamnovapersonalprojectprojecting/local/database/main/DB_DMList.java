package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DB_DMList extends LocalDBAttribute {
    public static final int channelIdIndex = 0;
    public static final int otherIdIndex = 1;
    public static final int lastTimeIdIndex = 2;

    public DB_DMList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addDMList(int channelId, int otherId, String lastTime){
        LocalDBMain.LOG(DB_DMList.class.getSimpleName(), channelId + " " + otherId + " " + lastTime);

        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("channelId", channelId);
            values.put("otherId", otherId);
            values.put("lastTime", lastTime);

            db.insert(getTableName(), null, values);
        }
    }

    public CursorReturn getAllOrderByLastTime(){
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        String query = "SELECT mainTable.channelId AS channelId, " +
                "mainTable.otherId AS otherId, " +
                "mainTable.lastTime AS lastTime, " +
                "userListTable.username AS otherUsername, " +
                "userListTable.profileImageId AS profileImageId " +
                "FROM " + getTableName() + " AS mainTable " +
                "LEFT JOIN " + LocalDBMain.GetTable(DB_UserList.class).getTableName() + " AS userListTable " +
                "ON mainTable.otherId = userListTable.id " +
                "ORDER BY mainTable.lastTime DESC";

        return new CursorReturn(db.rawQuery(query, null), db);
    }

    public int getChannelId(int otherId){
        String query = "SELECT channelId FROM " + getTableName() + " WHERE otherId = ?";
        try( SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(otherId)})){
            if(cursor.moveToFirst()){
                return cursor.getInt(0);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return DataManager.NOT_SETUP_I;
    }

    public int getOtherId(int channelId){
        String query = "SELECT otherId FROM " + getTableName() + " WHERE channelId = ? ";
        try ( SQLiteDatabase db = this.sqlite.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(channelId)})){
            if(cursor.moveToFirst()){
                return cursor.getInt(0);
            }
        }
        return DataManager.NOT_SETUP_I;
    }

    public void changeLastTime(int channelId, String lastTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DataManager.DATE_FORMAT);
        String sql =  "UPDATE " +getTableName()+ " SET lastTime = ? WHERE channelId = ?";
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase();
              SQLiteStatement stmt = db.compileStatement(sql)){
            stmt.bindString(1, LocalDateTime.parse(lastTime, formatter).toString());
            stmt.bindLong(2, channelId);
            stmt.executeUpdateDelete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void clearDMList(){
        try( SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            db.delete(getTableName(), null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`channelId` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `otherId` INT UNSIGNED NOT NULL UNIQUE," +
                "  `lastTime` DATE NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "DMList";
    }
}
