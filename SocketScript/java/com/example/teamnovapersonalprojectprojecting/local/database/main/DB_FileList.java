package com.example.teamnovapersonalprojectprojecting.local.database.main;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.io.File;

public class DB_FileList extends LocalDBAttribute {

    public DB_FileList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public boolean checkFileExistAndCall(int fileId){
        return checkFileExistAndCall(fileId, null);
    }
    public boolean checkFileExistAndCall(int fileId, LocalDBMain.AfterCall afterCall){
        String query = "SELECT 1 FROM " + getTableName() + " WHERE id = ?";
        try (SQLiteDatabase db = this.sqlite.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(fileId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                LocalDBMain.AfterCall.Execute(new JsonUtil().add(JsonUtil.Key.IS_EXIST, true), afterCall);
                return true;
            } else {
                getFileFromServer(fileId);

                //이벤트를 안보네는경우에는 바로 실행
                if (fileId <= DataManager.NOT_SETUP_I){
                    LocalDBMain.AfterCall.Execute(new JsonUtil().add(JsonUtil.Key.IS_EXIST, false), afterCall);
                } else {
                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.FILE_INPUT_STREAM, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.FILE_INPUT_STREAM) {
                        @Override
                        public boolean runOnce(JsonUtil jsonUtil) {
                            LocalDBMain.AfterCall.Execute(new JsonUtil().add(JsonUtil.Key.IS_EXIST, false), afterCall);
                            return false;
                        }
                    });
                }
                return false;
            }
        }
    }
    public static void getFileFromServer(int fileId){
        LocalDBMain.LOG("[" +fileId+ "] and file request");
        if(fileId <= DataManager.NOT_SETUP_I){
            return;
        }

        SocketConnection.sendMessage(false, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_FILE.toString())
                .add(JsonUtil.Key.ID, fileId));
    }

    public CursorReturn getFileData(int id) {
        String query = "SELECT id, fileName, fileSize, path FROM " + getTableName() + " WHERE id = ?";
        SQLiteDatabase db = this.sqlite.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        return new CursorReturn(cursor, db);
    }

    public void addFileList(int id, String fileName, long fileSize, String path) {
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("fileName", fileName);
            values.put("fileSize", fileSize);
            values.put("path", path);
            db.replace(getTableName(), null, values);
        }
    }
    public static void setFileImage(ImageView imageView, String filePath){
        if(filePath == null){
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            DataManager.Instance().mainHandler.post(() -> {
                Glide.with(DataManager.Instance().currentContext)
                        .load(file.getAbsolutePath())
                        .circleCrop()
                        .into(imageView);
            });
        }
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`id` INT PRIMARY KEY NOT NULL UNIQUE," +
                "  `fileName` TEXT NOT NULL UNIQUE," +
                "  `fileSize` INTEGER NOT NULL," +
                "  `path` TEXT NOT NULL);";
    }

    @Override
    public String getTableName() {
        return "FileList";
    }
}
