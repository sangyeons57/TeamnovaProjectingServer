package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;

import java.util.ArrayList;
import java.util.List;

public class DB_ProjectMember extends LocalDBAttribute implements LocalDBMain.TriggerQuery {
    public DB_ProjectMember(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() + " (" +
                "   projectId INTEGER NOT NULL," +
                "   userId INTEGER NOT NULL," +
                "   memberId TEXT," +
                "   roleList TEXT NOT NULL DEFAULT '[]'," +
                "   PRIMARY KEY (projectId, userId)" +
                ");";
    }

    @Override
    public String getTableName() {
        return "ProjectMember";
    }

    @Override
    public List<String> getTriggerQuery() {
        List<String> result = new ArrayList<>();
        result.add(
                "CREATE TRIGGER update_memberId_after_insert\n" +
                        "AFTER INSERT ON "+ getTableName() +" \n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "    UPDATE ProjectMember\n" +
                        "    SET memberId = NEW.projectId || '-' || NEW.userId\n" +
                        "    WHERE projectId = NEW.projectId AND userId = NEW.userId;\n" +
                        "END;\n"
        );
        result.add(
                "CREATE TRIGGER update_memberId_after_update\n" +
                        "AFTER UPDATE ON "+ getTableName() +" \n" +
                        "FOR EACH ROW\n" +
                        "BEGIN\n" +
                        "    UPDATE ProjectMember\n" +
                        "    SET memberId = NEW.projectId || '-' || NEW.userId\n" +
                        "    WHERE projectId = NEW.projectId AND userId = NEW.userId;\n" +
                        "END;\n"
        );
        return result;
    }
}
