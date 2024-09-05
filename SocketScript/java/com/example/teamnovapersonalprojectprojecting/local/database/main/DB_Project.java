package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;

/**
 * 여기 코드 겹치는 부분히 상당히 많은데 어떤 식으로 정리해야할지 생각해봐야할것 같음
 */
public class DB_Project extends LocalDBAttribute {
    public DB_Project(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }


    public void updateNameById(int id, String name) {
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            ContentValues values = new ContentValues();
            values.put("name", name);
            db.update(getTableName(), values, "id = ?", new String[]{String.valueOf(id)});
        }
    }

    public void updateProfileImageById(int id, int profileImageId) {
        try( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            ContentValues values = new ContentValues();
            values.put("profileImageId", profileImageId);
            db.update(getTableName(), values, "id = ?", new String[]{String.valueOf(id)});
        }
    }


    public void insertData(int id, String name, int profileImageId) {
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("name", name);
            values.put("profileImageId", profileImageId);

            db.insert(getTableName(), null, values);
        }
    }

    public void insertOrReplaceData(int id, String name, int profileImageId) {
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("name", name);
            values.put("profileImageId", profileImageId);

            db.replace(getTableName(), null, values);
        }
    }

    public boolean updateProfileImageId(int profileImageId, int projectId) {
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("profileImageId", profileImageId);

            return db.update(getTableName(), values, "id = ?", new String[]{ String.valueOf(projectId) }) > 0;
        }
    }

    public boolean isProjectExists(int id) {
        String query = "SELECT 1 FROM " + getTableName() + " WHERE id = ? LIMIT 1";
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)})) {

            return cursor != null && cursor.moveToFirst();
        }
    }

    public CursorReturn getDefaultDataCursor() {
        String query = "SELECT id, name, profileImageId FROM " + getTableName();
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        return new CursorReturn(cursor, db);
    }
    public CursorReturn getDefaultDataCursorById(int id){
        String query = "SELECT id, name, profileImageId FROM " + getTableName() + " WHERE id = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return new CursorReturn(cursor, db);
    }


    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `name` TEXT NOT NULL," +
                "  `profileImageId` INT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "Project";
    }
}
