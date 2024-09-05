package com.example.teamnovapersonalprojectprojecting.local.database.main;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.teamnovapersonalprojectprojecting.activity.FriendsActivity;
import com.example.teamnovapersonalprojectprojecting.local.database.LocalDBAttribute;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DB_FriendList extends LocalDBAttribute {
    public DB_FriendList(SQLiteOpenHelper sqlite) {
        super(sqlite);
    }

    public void addFriendByServer(){
        SocketConnection.sendMessage(false, new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.GET_USER_DATA.toString())
                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId));

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_USER_DATA, new SocketEventListener.EventListener() {
            @Override
            public boolean run(JsonUtil jsonUtil) {
                JSONArray friends = jsonUtil.getJsonArray(JsonUtil.Key.FRIENDS, new JSONArray());

                for(int i = 0; i < friends.length(); i++){
                    try {
                        addOrUpdateFriend(friends.getInt(i));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                //이미지 다운 받고 결로 설정하는 코드가 필요함

                SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.GET_USER_DATA, this);
                return true;
            }
        });
    }

    public void addOrUpdateFriend(int userId){
        LocalDBMain.LOG("addOrUpdateFriend: " + userId);
        try (SQLiteDatabase db = this.sqlite.getWritableDatabase();){
            ContentValues values = new ContentValues();
            values.put("userId", userId);

            db.insertWithOnConflict(getTableName(), null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public List<FriendsActivity.DataModel> getFriendListUseDataModel(){
        String query = "SELECT * FROM " + getTableName();
        List<FriendsActivity.DataModel> result = new ArrayList<>();
        try ( SQLiteDatabase db = this.sqlite.getReadableDatabase();
              Cursor cursor = db.rawQuery(query, null)){

            while (cursor.moveToNext()){
                result.add(new FriendsActivity.DataModel(cursor.getInt(0)));
            }
            LocalDBMain.LOG(cursor.getCount());
        }
        return result;
    }

    @Override
    public String getCreateQuery() {
        return "CREATE TABLE " + getTableName() +
                " (`userId` INT PRIMARY KEY NOT NULL UNIQUE );";
    }

    @Override
    public String getTableName() {
        return "FriendList";
    }
}
