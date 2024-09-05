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
    public static void LOG(int logText){
        System.out.println("MysqlManger: " + logText);
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
    //아직 테스트 안됨
    private static List<Map<String, Object>> resultToMapAll(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()){
            Map<String, Object> row = new HashMap<>();
            for(int i = 1; i <= metaData.getColumnCount(); i++){
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(row);
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
            String jdbcUrl = "jdbc:mysql://localhost:3306/"+mysql.getString("DBname") + "?autoReconnect=true";
            String jdbcUrlChat = "jdbc:mysql://localhost:3306/"+mysql.getString("ChatDBname") + "?autoReconnect=true";
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
        String query = "SELECT COUNT(*) AS count FROM channel WHERE id = ? AND status = 1";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery(); ) {
                if(resultSet.next()){
                    return resultSet.getInt("count") > 0;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isChannelExist sql error");
        }
        return false;
    }

    public boolean isMemberOfChannel(int channelId, int userId){
        String query = "SELECT members FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery(); ) {
                if(resultSet.next()){
                    JSONArray jsonArray = new JSONArray(resultSet.getString("members"));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if(userId == jsonArray.getInt(i)) {
                            return true;
                        }
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
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultSet.getBoolean("is_dm");
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isDMChannel sql error");
        }
        return defaultValue;
    }

    public boolean checkMemberOfProjectByChannelId(int channelId, int userId){
        String query = "SELECT 1" +
                " FROM channel_project AS cp" +
                " JOIN project_member AS pm ON cp.project_id = pm.project_id" +
                " WHERE cp.channel_id = ? AND pm.user_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            statement.setInt(2, userId);
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return true;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("isDMChannel sql error");
        }
        return false;

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

    public int createProjectChannel(int projectId, int categoryId, String channelName) {
        String query1 = "INSERT INTO channel (is_dm, members) VALUES (0, ?)";
        String query2 = "INSERT INTO channel_project (channel_id, category_id, project_id, channel_name) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            connectionChat.setAutoCommit(false);
            try (PreparedStatement statement1 = connection.prepareStatement(query1, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement statement2 = connection.prepareStatement(query2)){

                statement1.setString(1, new JSONArray().toString());
                statement1.executeUpdate();

                int lastInsertedId;
                try ( ResultSet generatedKeys = statement1.getGeneratedKeys(); ){
                    generatedKeys.next();
                    lastInsertedId = generatedKeys.getInt(1);
                }

                statement2.setInt(1, lastInsertedId);
                statement2.setInt(2, categoryId);
                statement2.setInt(3, projectId);
                statement2.setString(4, channelName);
                statement2.executeUpdate();


                //테이블 생성에 문제가 생긴경우
                if(!createChatTable("p_" + lastInsertedId)){
                    throw new SQLException();
                }

                connection.commit();
                connectionChat.commit();
                return lastInsertedId;
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
        return 0;
    }
    public int createDMChannel(int userId1, int userId2){
        String query1 = "INSERT INTO channel (is_dm, members) VALUES (1, ?)";
        String query2 = "INSERT INTO channel_dm (channel_id, user_id1, user_id2) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            connectionChat.setAutoCommit(false);
            try (PreparedStatement statement1 = connection.prepareStatement(query1, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement statement2 = connection.prepareStatement(query2)){

                int[] members = new int[]{userId1, userId2};
                statement1.setString(1, new JSONArray(members).toString());
                statement1.executeUpdate();

                int lastInsertedId;
                try ( ResultSet generatedKeys = statement1.getGeneratedKeys(); ){
                    generatedKeys.next();
                    lastInsertedId = generatedKeys.getInt(1);
                }

                statement2.setInt(1, lastInsertedId);
                statement2.setInt(2, userId1);
                statement2.setInt(3, userId2);
                statement2.executeUpdate();


                //테이블 생성에 문제가 생긴경우
                if(!createChatTable("dm_" + lastInsertedId)){
                    throw new SQLException();
                }

                connection.commit();
                connectionChat.commit();
                return lastInsertedId;
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
        return 0;
    }

    public int createProject(String projectName, int profileImageId, boolean isOpen){
        String query1 = "INSERT INTO project (name, profile_image_id, is_open) VALUES (?, ?, ?)";
        String query2 = "INSERT INTO project_structure (project_id, structure, options, role) VALUES (?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement statement1 = connection.prepareStatement(query1, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement statement2 = connection.prepareStatement(query2); ){

                statement1.setString(1, projectName);
                statement1.setInt(2, profileImageId);
                statement1.setBoolean(3, isOpen);
                statement1.executeUpdate();

                int projectId;
                try ( ResultSet generatedKeys = statement1.getGeneratedKeys(); ){
                    generatedKeys.next();
                    projectId = generatedKeys.getInt(1);
                }

                statement2.setInt(1, projectId);
                statement2.setString(2, new JSONObject().toString());
                statement2.setString(3, new JSONObject().toString());
                statement2.setString(4, new JSONObject().toString());
                statement2.executeUpdate();

                return projectId;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                LOG("createDMChannel SQL exception");
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e){
            e.printStackTrace();
            LOG("createDMChannel sql transaction error occur");
        }
        return ProjectEditor.PROJECT_ID_NOT_SETUP;
    }

    public int addChatData(String tableName, Integer writerId, String data, String datetime){
        String query = "INSERT INTO " + tableName + " (writer_id, data, create_time, update_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connectionChat.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
            statement.setInt(1,writerId);
            statement.setString(2,data);
            statement.setString(3, datetime);
            statement.setString(4, datetime);
            statement.executeUpdate();
            try ( ResultSet generatedKeys = statement.getGeneratedKeys(); ){
                if(generatedKeys.next()){
                    LOG("chat " + tableName + " is added " + data + " at " + datetime);
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("addChatData sql error");
        }
        return -1;
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

    public void addProjectToUserRegister(int userId, int projectId){
        String selectSql = "SELECT project FROM user_register WHERE user_id = ?";
        String updateSql = "UPDATE user_register SET project = ? WHERE user_id = ?";
        JSONArray projectJson;

        try (PreparedStatement stmt1 = connection.prepareStatement(selectSql);
            PreparedStatement stmt2 = connection.prepareStatement(updateSql);) {
            stmt1.setInt(1, userId);
            try (ResultSet rs = stmt1.executeQuery()) {
                if (rs.next()) {
                    String projectJsonString = rs.getString("project");
                    projectJson = new JSONArray(projectJsonString);
                } else {
                    // 사용자가 존재하지 않으면 새 JSONArray를 생성합니다.
                    projectJson = new JSONArray();

                }
            }

            if(!projectJson.toString().contains(String.valueOf(projectId))){
                projectJson.put(projectId);
            }

            stmt2.setString(1, projectJson.toString());
            stmt2.setInt(2, userId);
            int affectedRows = stmt2.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Project list updated successfully.");
            } else {
                System.out.println("Update failed. User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("addProjectToUserRegister sql error");
        }
    }

    public int addFileSave(String fileName, String fileType, long fileSize, String filePath){
        String query = "INSERT INTO file_save " +
                " (file_name, file_type, file_size, file_path, created_time)" +
                " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS );) {
            statement.setString(1, fileName);
            statement.setString(2, fileType);
            statement.setLong(3, fileSize);
            statement.setString(4, filePath);
            statement.setString(5, Util.getCurrentDateTime());
            int affectedRows = statement.executeUpdate();
            if(affectedRows > 0){
                try(ResultSet generatedKeys = statement.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public JSONArray getAllProjectUserIncluded(int userId){
        String query = "SELECT project FROM user_register WHERE user_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()) {
                    return new JSONArray(resultSet.getString("project"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getStructureByProjectId(int projectId) {
        String query = "SELECT structure FROM project_structure WHERE project_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()) {
                    return new JSONObject(resultSet.getString("structure"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> getDefaultProjectData(int projectId){
        String query = "SELECT * FROM project WHERE id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, projectId);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public JSONObject getOptionsByProjectId(int projectId){
        String query = "SELECT options FROM project_structure WHERE project_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()) {
                    return new JSONObject(resultSet.getString("options"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getDMChatData(int channelId){
        String query = "SELECT * FROM dm_" + channelId;
        JSONArray data = new JSONArray();
        try (PreparedStatement statement = connectionChat.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery();){
            while (resultSet.next()){
                try(PreparedStatement statement1 = connection.prepareStatement("SELECT username FROM users where id = ?")){
                    statement1.setInt(1, resultSet.getInt("writer_id"));
                    try ( ResultSet userResult = statement1.executeQuery(); ){
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
            }
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return null;
    }

    private JSONObject getJsonObject(String query) {
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery(); ) {
            new JSONObject( resultSet.getString(1) );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject();
    }

    public JSONObject getRoleJsonObject(int projectId){
        String query = "SELECT role FROM project_structure WHERE project_id = " + projectId;
        return getJsonObject(query);
    }

    public List<Map<String, Object>> getMembersJsonObjectInProject(int projectId){
        String query = "SELECT members FROM project_member WHERE project_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, projectId);
            statement.executeQuery();
            try ( ResultSet resultSet = statement.executeQuery();){
                return resultToMapAll(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDMChannelId(int userId1, int userId2){
        String query = "SELECT channel_id FROM channel_dm WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, userId1);
            statement.setInt(2, userId2);
            statement.setInt(3, userId2);
            statement.setInt(4, userId1);
            try( ResultSet resultSet = statement.executeQuery();){
                if(resultSet.next()){
                    return resultSet.getInt("channel_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return Util.NOT_SETUP_I;
    }

    public int getOtherIdByDMChannelId(int channelId, int userId){
        String query = "SELECT * FROM channel_dm WHERE channel_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            try( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    int userId1 = resultSet.getInt("user_id1");
                    int userId2 = resultSet.getInt("user_id2");
                    return (userId1 == userId) ? userId2 : userId1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return 0;
    }
    public Map<String, Object> getChannelDataByChannelId(int channelId){

        String query = "SELECT * FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return null;
    }

    public Map<String, Object> getFileDataById(int fileId){
        String query = "SELECT * FROM file_save WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, fileId);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getDMChannel sql error");
        }
        return null;
    }

    public Map<String, Object> getUserByUsername(String username) {
         String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, username);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()) {
                    return resultToMap(resultSet);
                }
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
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getUserByUsername sql error");
        }
        return null;
    }

    public Map<String, Object> getProjectData(int projectId){
         String query = "SELECT id, name, profile_image_id, is_open, status FROM project WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, projectId);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("getUserByUsername sql error");
        }
        return null;
    }

    public String getProjectJoinToken(int projectId, int validTime){
        String query = "SELECT token FROM project_join_token WHERE project_id = ? AND create_time > DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1,  projectId);
            statement.setInt(2, validTime);
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return resultSet.getString(1);
                } else {
                    return updateProjectJoinToken(projectId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Util.NOT_SETUP_S;
    }

    public int getProjectIdByValidToken(String token, int validTime){
        String query = "SELECT project_id FROM project_join_token WHERE token = ? AND create_time > DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1,  token);
            statement.setInt(2, validTime);
            try ( ResultSet resultSet = statement.executeQuery();){
                if (resultSet.next()){
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Util.NOT_SETUP_I;
    }

    public Map<String, Object> getProjectChannelDataByChannelId(int channelId){
        String query = "SELECT * FROM channel_project WHERE channel_id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery();){
                if(resultSet.next()){
                    return resultToMap(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Map<String, Object>> getExistProjectChannelDataByProjectId(int projectId){
        String query = "SELECT * FROM channel_project AS cp " +
                " JOIN channel AS c ON cp.channel_id = c.id " +
                " WHERE project_id = ? AND c.status = 1 ";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectId);
            try ( ResultSet resultSet = statement.executeQuery();){
                return resultToMapAll(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Map<String, Object> getDataOfChatLast(int channelId) throws NoSuchElementException{
        return getDataOfChat(channelId, 1).getFirst();
    }
    public List<Map<String, Object>> getDataOfChat(int channelId, int count){
        List<Map<String, Object>> resultData = new ArrayList<>();
        String channelName = (isDMChannel(channelId, false) ? "dm_" : "p_") + channelId;
        String query = "SELECT * FROM "+channelName+" ORDER BY id DESC LIMIT ?";

        try (PreparedStatement statement = connectionChat.prepareStatement(query)){
            statement.setInt(1, count);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                while (resultSet.next()) {
                    resultData.add(resultToMap(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultData;
    }

    public List<Map<String, Object>> getDataOfChatFormBack (int channelId, int limit, int offset) {
        String channelName = (isDMChannel(channelId, false) ? "dm_" : "p_") + channelId;
        String query = "SELECT * FROM "+channelName+" ORDER BY id DESC LIMIT ? OFFSET ? ";
        try (PreparedStatement statement = connectionChat.prepareStatement(query)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                return resultToMapAll(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> getMembersOfChannel(int channelId) {
        String query = "SELECT members FROM channel WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery(); ){
                if(resultSet.next()){
                    JSONArray jsonArray = new JSONArray(resultSet.getString("members"));
                    List<Integer> membersId = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        membersId.add(jsonArray.getInt(i));
                    }
                    return membersId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  new ArrayList<>();
    }

    public void updateUserProfileByUserId(int userId, int imageId){
        String query = "UPDATE users SET profile_image_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query );) {
            statement.setInt(1, imageId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateProjectProfileById(int projectId, int imageId){
        String query = "UPDATE project SET profile_image_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query );) {
            statement.setInt(1, imageId);
            statement.setInt(2, projectId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateProjectMember(int projectId, int userId, JSONArray roleList){
        String query = "INSERT INTO project_member (project_id, user_id, role_list)" +
                " VALUES (?, ?, ?) " +
                " ON DUPLICATE KEY UPDATE " +
                " role_list = VALUES(role_list)";
        try (PreparedStatement statement = connection.prepareStatement(query );) {
            statement.setInt(1, projectId);
            statement.setInt(2, userId);
            statement.setString(3, roleList.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateProjectName(int projectId, String projectName) {
        String query = "UPDATE project SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query );) {
            statement.setString(1, projectName);
            statement.setInt(2, projectId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStructureById(int projectId, JSONObject structureObject){
        String query = "UPDATE project_structure SET structure = ? WHERE project_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query );) {
            statement.setString(1, structureObject.toString());
            statement.setInt(2, projectId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRoleInProject(int projectId, JSONObject roleObject){
        String query = "UPDATE project_structure SET role = ? WHERE project_id = ? ";
        try (PreparedStatement statement = connection.prepareStatement(query);){
            statement.setString(1, roleObject.toString());
            statement.setInt(2, projectId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    public boolean updateUsersWaitingByUserId(JSONArray waitingData, int userId){
        String query = "UPDATE users SET waiting = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, waitingData.toString());
            statement.setInt(2, userId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("updateUsersWaitingByUserId sql error");
        }
        return false;
    }

    public boolean updateUsersDMChannel(int userId, JSONArray data){
        String query = "UPDATE users SET dm_channel = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, data.toString());
            statement.setInt(2, userId);
            statement.executeUpdate();
            LOG(4);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            LOG("updateUsersWaitingByUserId sql error");
        }

        return false;
    }

    public boolean updateChannelName(int channelId, String channelName){
        String query = "UPDATE channel_project SET channel_name = ? WHERE channel_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, channelName);
            statement.setInt(2, channelId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 원본데이터를 받아서 위치를 바꿀 요소 데이터를 특정 위치로 이동시킴
     * 요소데이터 확이느은 [data]  에 channelId 값으로 한다.
     *
     * @param jsonArray 원본데이터
     * @param newPosition 위치
     * @return 위치가 바뀐 DMChannel데이터 가 반호나된다.
     */
    private JSONArray changePositionOfDMChannel(JSONArray jsonArray, int newPosition, int channelId){
        JSONArray resultArray = new JSONArray();
        for(int i = 0; i < jsonArray.length(); i++){
            //정해진 위치에 데이터 추가
            if(i == newPosition){
                resultArray.put(channelId);
            }
            //추가 한 데이터는 빼고 나머지 데이터 다시 저장
            if(jsonArray.getInt(i) != channelId) {
                resultArray.put(jsonArray.getInt(i));
            }
        }
        if(jsonArray.length() == 0){
            resultArray.put(channelId);
        }
        return resultArray;
    }

    public boolean updateUsersDMChannelMoveForward(int channelId){
        LOG(1);
        int userId1, userId2;
        String query = "SELECT user_id1, user_id2 FROM channel_dm WHERE channel_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, channelId);
            try ( ResultSet resultSet = statement.executeQuery();){
                if(resultSet.next()) {
                    userId1 = resultSet.getInt("user_id1");
                    userId2 = resultSet.getInt("user_id2");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOG(2);

        //[channelId] 형태
        JSONArray user1JsonArray = new JSONArray(getUserByUserId(userId1).get("dm_channel").toString());
        JSONArray user2JsonArray = new JSONArray(getUserByUserId(userId2).get("dm_channel").toString());


        JSONArray resultArray1 = changePositionOfDMChannel(user1JsonArray, 0, channelId);
        JSONArray resultArray2 = changePositionOfDMChannel(user2JsonArray, 0, channelId);

        LOG(3);
        return updateUsersDMChannel(userId1, resultArray1)
                && updateUsersDMChannel(userId2, resultArray2);
    }

    public boolean updateOptionsByProjectId(int projectId, JSONObject options){
        String query = "UPDATE project_structure SET options = ? WHERE project_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, options.toString());
            statement.setInt(2, projectId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String updateProjectJoinToken(int projectId){
        String query = "INSERT INTO project_join_token (project_id, token, create_time) " +
                " VALUES (?, ?, ?) " +
                " ON DUPLICATE KEY UPDATE " +
                "   token = VALUES(token), " +
                "   create_time = VALUES(create_time)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String currentTime = Util.getCurrentDateTime();
            String token = UUID.randomUUID().toString();
            statement.setInt(1, projectId);
            statement.setString(2, token);
            statement.setString(3, currentTime);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return token;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Util.NOT_SETUP_S;
    }
    public boolean deleteChannel(int channelId){
        String query1 = "UPDATE channel SET status = 0 WHERE id = ?";
        try (PreparedStatement statement1 = connection.prepareStatement(query1);){

            statement1.setInt(1, channelId);
            statement1.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

