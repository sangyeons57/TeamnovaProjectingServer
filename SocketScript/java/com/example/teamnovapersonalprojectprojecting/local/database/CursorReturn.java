package com.example.teamnovapersonalprojectprojecting.local.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CursorReturn {
    private Cursor cursor;
    private SQLiteDatabase database;
    public CursorReturn(Cursor cursor, SQLiteDatabase database){
        this.cursor = cursor;
        this.database = database;
    }

    public void execute(Execute cursorExecute){
        try {
            if (cursor == null || cursor.isClosed()) {
                cursorExecute.whenCursorNull();
            } else {
                cursorExecute.run(cursor);
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
            if(database != null){
                database.close();
            }
        }
    }

    public interface Execute {
        void run(Cursor cursor);
        default void whenCursorNull(){ }
    }
}
