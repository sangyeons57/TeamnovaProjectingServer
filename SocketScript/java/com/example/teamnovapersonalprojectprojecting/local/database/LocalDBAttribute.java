package com.example.teamnovapersonalprojectprojecting.local.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class LocalDBAttribute {
    public SQLiteOpenHelper sqlite;
    public LocalDBAttribute(SQLiteOpenHelper sqlite){
        this.sqlite = sqlite;
    }
    public abstract String getCreateQuery();
    public abstract String getTableName();
}

