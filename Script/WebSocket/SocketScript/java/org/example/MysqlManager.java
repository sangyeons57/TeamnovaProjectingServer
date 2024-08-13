package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class MysqlManager {
    private static MysqlManager instance = null;
    public static MysqlManager Instance(){
        if(instance == null){
            instance = new MysqlManager();
        }
        return instance;
    }
    public static void LOG(String logText){
        System.out.println("MysqlManger: " + logText);
    }

    private static Map<String, Object> resultToMap(ResultSet resultSet) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for(int i = 1; i <= metaData.getColumnCount(); i++){
            result.put(metaData.getColumnName(i), resultSet.getObject(i));
        }
        return result;
    }

    public static String filePath = "/var/www/config.json";
    //public static String filePath = "D:/repository/aws/TeamnovaProjecting/TeamnovaProjecting/config.json";


    private Connection connection;
    private Connection connectionChat;
    private MysqlManager(){
        System.out.println("Test");
        try {
            String context = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println(context);

            JSONObject jsonObject = new JSONObject(context);

            JSONObject mysql = jsonObject.getJSONObject("Mysql");
            String jdbcUrl = "jdbc:mysql://localhost:3306/"+mysql.getString("DBname");
            String jdbcUrlChat = "jdbc:mysql://localhost:3306/"+mysql.getString("ChatDBname");
            String dbUser = mysql.getString("Username");
            String dbPassword = mysql.getString("Password");

            System.out.println(jdbcUrl);
            this.connection = DriverManager.getConnection(jdbcUrl, dbUser,dbPassword);
            this.connectionChat = DriverManager.getConnection(jdbcUrlChat, dbUser,dbPassword);

            System.out.println("Connection succesful!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isChannelExist(int channelId){
        String query = "SELECT COUNT(*) AS count FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("count") > 0;
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isChannelExist sql error");
        }
        return false;
    }

    public boolean isMemberOfChannel(int channelId, String userId){
        String query = "SELECT members FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                JSONArray jsonArray = new JSONArray(resultSet.getString("members"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    if(userId.equals(jsonArray.getString(i))) {
                        return true;
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isMemberOfChannel sql error");
        }
        return false;
    }

    public boolean isDMChannel(int channelId, boolean defaultValue) {
        String query = "SELECT is_dm FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getBoolean("is_dm");
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isDMChannel sql error");
        }
        return defaultValue;
    }

    public boolean createChatTable(String tableName){
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "            id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                "            writer_id INT UNSIGNED NOT NULL," +
                "            data VARCHAR(1000) NOT NULL," +
                "            create_time DATETIME NOT NULL," +
                "            update_time DATETIME NOT NULL," +
                "            PRIMARY KEY (id))";
        try(PreparedStatement statement = connectionChat.prepareStatement(query)){
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("createChatTable sql error");
        }
        return false;
    }

    public boolean createDMChannel(int userId1, int userId2){
        boolean status = false;
        String query1 = "INSERT INTO channel (is_dm, members) VALUES (1, ?)";
        String query2 = "INSERT INTO channel_dm (channel_id, user_id1, user_id2) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            connectionChat.setAutoCommit(false);
            try (PreparedStatement statement1 = connection.prepareStatement(query1, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement statement2 = connection.prepareStatement(query2)){

                String[] members = new String[]{""+userId1,""+userId2};
                statement1.setString(1, new JSONArray(members).toString());
                statement1.executeUpdate();

                ResultSet generatedKeys = statement1.getGeneratedKeys();
                generatedKeys.next();
                int lastInsertedId = generatedKeys.getInt(1);

                statement2.setInt(1, lastInsertedId);
                statement2.setInt(2, userId1);
                statement2.setInt(3, userId2);
                statement2.executeUpdate();

                //테이블 생성에 문제가 생긴경우
                if(createChatTable("dm_" + lastInsertedId)){
                    status = true;
                } else {
                    throw new SQLException();
                }

                connection.commit();
                connectionChat.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                connectionChat.rollback();
                e.printStackTrace();
                LOG("createDMChannel SQL exception");
            } finally {
                connection.setAutoCommit(true);
                connectionChat.setAutoCommit(true);
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("createDMChannel sql transaction error occur");
        }
        return status;
    }

    public boolean addChatData(String tableName, Integer writerId, String data, String datetime){
        String query = "INSERT INTO " + tableName + " (writer_id, data, create_time, update_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connectionChat.prepareStatement(query)){
            statement.setInt(1,writerId);
            statement.setString(2,data);
            statement.setString(3, datetime);
            statement.setString(4, datetime);
            statement.executeUpdate();
            LOG("chat " + tableName + " is added " + data + " at " + datetime);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("addChatData sql error");
        }
        return false;
    }

    public void addFriendOnWaiting(JSONArray newUserWaitingArray, JSONArray newUserFriendArray, int userId,
                                   JSONArray newFriendWaitingArray, JSONArray newFriendFriendArray, int friendId){
        String query = "UPDATE users SET waiting = ?, friends = ? WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(query);
                PreparedStatement friendStatement = connection.prepareStatement(query)){

                userStatement.setString(1, newUserWaitingArray.toString());
                userStatement.setString(2, newUserFriendArray.toString());
                userStatement.setInt(3, userId);
                userStatement.executeUpdate();

                friendStatement.setString(1, newFriendWaitingArray.toString());
                friendStatement.setString(2, newFriendFriendArray.toString());
                friendStatement.setInt(3, friendId);
                friendStatement.executeUpdate();

                connection.commit();
            } catch (SQLException e){
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("addFriendOnWaiting sql error");
        }
    }

    public JSONArray getDMChatData(int channelId){
        String query = "SELECT * FROM dm_" + channelId;
        JSONArray data = new JSONArray();
        try (PreparedStatement statement = connectionChat.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                try(PreparedStatement statement1 = connection.prepareStatement("SELECT username FROM users where id = ?")){
                    statement1.setInt(1, resultSet.getInt("writer_id"));
                    ResultSet userResult = statement1.executeQuery();
                    if(userResult.next()){
                        data.put(new JSONObject()
                                .put(SocketEventListener.eKey.ID.toString(), resultSet.getInt("id"))
                                .put(SocketEventListener.eKey.USERNAME.toString(), userResult.getString("username"))
                                .put(SocketEventListener.eKey.DATETIME.toString(), resultSet.getString("create_time"))
                                .put(SocketEventListener.eKey.MESSAGE.toString(), resultSet.getString("data"))
                        );
                    }
                }
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return null;
    }

    public int getDMChannel(int userId1, int userId2){
        String query = "SELECT channel_id FROM channel_dm WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, userId1);
            statement.setInt(2, userId2);
            statement.setInt(3, userId2);
            statement.setInt(4, userId1);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("channel_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return 0;
    }

    public Map<String, Object> getUserByUsername(String username) {
         String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultToMap(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getUserByUsername sql error");
        }
        return null;
    }

    public Map<String, Object> getUserByUserId(int userId) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,userId);
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            if(resultSet.next()){
                return resultToMap(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getUserByUsername sql error");
        }
        return null;
    }

    public boolean updateUsersWaitingByUsername(JSONArray waitingData, String username){
        String query = "UPDATE users SET waiting = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, waitingData.toString());
            statement.setString(2, username);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("updateUsersWaitingByUsername sql error");
        }
        return false;
    }
}

