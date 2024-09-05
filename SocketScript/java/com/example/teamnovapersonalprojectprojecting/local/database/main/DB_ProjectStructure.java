package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 여기 코드 겹치는 부분히 상당히 많은데 어떤 식으로 정리해야할지 생각해봐야할것 같음
 */
public class DB_ProjectStructure extends LocalDBAttribute {
    public DB_ProjectStructure(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    private JSONArray removeChannel(JSONArray elements, int channelId) throws JSONException {
        for (int i = 0; i < elements.length(); i++) {
            if(elements.getInt(i) == channelId) {
                elements.remove(i);
                break;
            }
        }
        return elements;
    }

    public boolean removeChannel(int channelId){
        try {
            Map<String, Object> projectChannelData = LocalDBMain.GetTable(DB_ProjectChannelList.class).getChannelByChannelId(channelId);
            if(projectChannelData == null){
                return false;
            }

            int projectId = Integer.parseInt(projectChannelData.get("projectId").toString());
            int categoryId = Integer.parseInt(projectChannelData.get("categoryId").toString());

            JSONObject categoryData = getCategoryByID(projectId, categoryId);
            JSONArray elements = removeChannel(categoryData.getJSONArray("elements"), channelId);

            categoryData.put("elements", elements);
            replaceCategoryById(projectId, categoryId, categoryData);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean removeCategory(int projectId, int categoryId){
        JSONObject structureData = getStructureById(projectId);
        structureData.remove(String.valueOf(categoryId));
        replaceStructureById(projectId, structureData);
        return true;
    }
    public boolean addCategory(int projectId, int categoryId, String categoryName){
        JSONObject structureData = getStructureById(projectId);
        try {
            structureData.put(String.valueOf(categoryId), new JSONObject()
                    .put("structureId", categoryId)
                    .put("structureName", categoryName)
                    .put("elements", new JSONArray()));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        replaceStructureById(projectId, structureData);
        return true;
    }
    public boolean replaceCategoryById(int projectId, int categoryId, JSONObject category){
        JSONObject structureData = getStructureById(projectId);
        if(structureData == null){
            return false;
        }

        try {
            structureData.put(String.valueOf(categoryId), category);
            replaceStructureById(projectId, structureData);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getCategoryByID(int projectId, int categoryId){
        String query = "SELECT structure FROM " + getTableName() + " WHERE projectId = ?";
        try( SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});){
            if( cursor.moveToFirst() ){
                return new JSONObject(cursor.getString(0)).getJSONObject(String.valueOf(categoryId));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void replaceStructureById(int projectId, JSONObject structure) {
        try( SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("projectId", projectId);
            values.put("structure", structure.toString());
            db.replace(getTableName(), null, values);
        }
    }

    public void replaceOptionsById(int projectId, JSONObject options) {
        try( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){
            ContentValues values = new ContentValues();
            values.put("projectId", projectId);
            values.put("options", options.toString());
            db.replace(getTableName(), null, values);
        }
    }

    public void replaceRoleById(int projectId, JSONObject role) {
        try( SQLiteDatabase db = this.sqlite.getWritableDatabase();) {
            ContentValues values = new ContentValues();
            values.put("projectId", projectId);
            values.put("role", role.toString());
            db.replace(getTableName(), null, values);
        }
    }

    public void insertData(int projectId, JSONObject structure, JSONObject options, JSONObject role) {
        try ( SQLiteDatabase db = this.sqlite.getWritableDatabase(); ){

            ContentValues values = new ContentValues();
            values.put("projectId", projectId);
            values.put("structure", structure.toString());
            values.put("options", options.toString());
            values.put("role", role.toString());

            db.insert(getTableName(), null, values);
        }
    }

    public void insertOrReplaceData(int projectId, JSONObject structure, JSONObject options, JSONObject role) {
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("projectId", projectId);
            values.put("structure", structure.toString());
            values.put("options", options.toString());
            values.put("role", role.toString());

            db.replace(getTableName(), null, values);
        }
    }

    public JSONObject getStructureById(int projectId) {
        String query = "SELECT structure FROM " + getTableName() + " WHERE projectId = ?";
        try ( SQLiteDatabase db = this.sqlite.getReadableDatabase();
              Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});) {

            if (cursor != null && cursor.moveToFirst()) {
                return new JSONObject( cursor.getString(cursor.getColumnIndexOrThrow("structure")));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null; // 존재하지 않는 경우
    }

    public boolean isProjectExists(int projectId) {
        String query = "SELECT 1 FROM " + getTableName() + " WHERE projectId = ? LIMIT 1";
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)})) {

            return cursor != null && cursor.moveToFirst();
        }
    }

    public CursorReturn getDefaultDataCursor() {
        String query = "SELECT projectId, name, profileImage FROM " + getTableName();
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{});
        return new CursorReturn(cursor, db);
    }


    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`projectId` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `structure` TEXT NOT NULL DEFAULT '{}'," +
                "  `options` TEXT NOT NULL DEFAULT '{}' ," +
                "  `role` TEXT NOT NULL DEFAULT '{}');";
    }

    @Override
    public String getTableName() {
        return "ProjectStructure";
    }
}
