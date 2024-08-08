<?php
class WebSocketMysql {
    private $mysqli;
    private $chatMysqli;
    private static $instance =  null;

    private function __construct() {
        $config = require __DIR__ . '/../../../config.php';
        if ($config === false) {
            echo "\n Failed to include the file. \n";
        } else {
            echo "\n File included successfully. \n ";
        }
        /*
        global $MysqlServername;
        global $MysqlUsername;
        global $MysqlPassword;
        global $MysqlChatDbname;
        global $MysqlDbname;
        */

        $this->mysqli = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);
        $this->chatMysqli = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlChatDbname);

        if ($this->mysqli->connect_error){
            die('Connection failed: ' . $this->mysqli->connect_error);
        }
        if ($this->chatMysqli->connect_error){
            die('Connection failed: ' . $this->chatMysqli->connect_error);
        }
    }

    public static function Instance(){
        if(self::$instance === null){
            self::$instance = new WebSocketMysql();
        }
        return self::$instance;
    }


    public function checkChannelExist($channel){
        $stmt = $this->mysqli->prepare('SELECT COUNT(*) FROM channel WHERE id = ?');
        $stmt->bind_param('i', $channel);
        $stmt->execute();
        $stmt->bind_result($count);
        $stmt->fetch();
        $stmt->close();
        return $count > 0;
    }

    public function getDMChatData($channelId){
        $query = "SELECT * FROM dm_{$channelId}";
        $stmt = $this->chatMysqli->prepare($query);
        $stmt->execute();
        $result = $stmt->get_result();
        $data = array();
        while($row = $result->fetch_assoc()){
            $stmt1 = $this->mysqli->prepare("SELECT username FROM users where id = ?");
            $stmt1->bind_param("i", $row["writer_id"]);
            $stmt1->execute();
            $userResult = $stmt1->get_result();
            $usersData = $userResult->fetch_assoc();
            $data[] = array(
                KEY_ID => $row["id"],
                KEY_USERNAME => $usersData["username"],
                KEY_DATETIME => $row["create_time"],
                KEY_MESSAGE => $row["data"],
            );
            $stmt1->close();
        }
        echo json_encode($data);
        $stmt->close();
        return $data; 
    }

    public function checkIsMember($channel, $userId){
        $stmt = $this->mysqli->prepare("SELECT members FROM channel WHERE id = ?");
        $stmt->bind_param('i', $channel);
        $stmt->execute();
        $stmt->bind_result($members);
        $stmt->fetch();
        $stmt->close();
        return in_array($userId, json_decode($members, true));
    }
    public function isDMChannel($channel){
        $stmt = $this->mysqli->prepare("SELECT is_dm FROM channel WHERE id = ?");
        $stmt->bind_param('i', $channel);
        $stmt->execute();
        $stmt->bind_result($isDM);
        $stmt->fetch();
        $stmt->close();
        return $isDM == "1";
    }

    public function getUserByUsername($username){
        // 현재 사용자의 waiting 컬럼 조회
        $stmt = $this->mysqli->prepare("SELECT * FROM users WHERE username = ?");
        $stmt->bind_param('s', $waitingUserName);
        $stmt->execute();
        $result = $stmt->get_result(); 
        $stmt->close();
        return $result->fetch_assoc();
    }

    public function getUserByUserId($userId){
        // 현재 사용자의 waiting 컬럼 조회
        $stmt = $this->mysqli->prepare("SELECT * FROM users WHERE id = ?");
        $stmt->bind_param('i', $userId);
        $stmt->execute();
        $result = $stmt->get_result(); 
        $stmt->close();
        return $result->fetch_assoc();
    }


    public function createChatTable($tableName){
        $tableName = $this->chatMysqli->real_escape_string($tableName);

        $query = "CREATE TABLE IF NOT EXISTS $tableName (
            id INT UNSIGNED NOT NULL AUTO_INCREMENT,
            writer_id INT UNSIGNED NOT NULL,
            data VARCHAR(1000) NOT NULL,
            create_time DATETIME NOT NULL,
            update_time DATETIME NOT NULL,
            PRIMARY KEY (id))";

        if($this->chatMysqli->query($query) === TRUE){
            echo "Table {$tableName} created successfully.\n";
            return true;
        } else {
            echo "Error: " . $this->chatMysqli->error;
            return false;
        }
    }

    public function createDMChannel($user_id1, $user_id2){
        $status = false;
        $stmt1 = null;
        $stmt2 = null;

        $this->mysqli->begin_transaction();
        try {
            $stmt1 = $this->mysqli->prepare("INSERT INTO channel (is_dm, members) VALUES (1, ?)");
            $members=[$user_id1,$user_id2];
            $stmt1->bind_param('s', json_encode($members));
            $stmt1->execute();

            $table1_id = $this->mysqli->insert_id;
            $stmt2 = $this->mysqli->prepare("INSERT INTO channel_dm (channel_id, user_id1, user_id2) VALUES (?, ?, ?)");
            $stmt2->bind_param('iii', $table1_id, $user_id1, $user_id2);
            $stmt2->execute();


            $status = $this->createChatTable("dm_{$table1_id}");

            $this->mysqli->commit();
        } catch(Exception $e){
            $this->mysqli->rollback();
            $status = false;
            echo $e;
        }
        $stmt1->close();
        $stmt2->close();
        return $status;
    }

    public function addChatData($tableName, $writerId, $data){
        $currentTime = date("Y-m-d H:i:s");
        $stmt = $this->chatMysqli->prepare("INSERT INTO {$tableName} (writer_id, data, create_time, update_time) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("isss", $writerId, $data, $currentTime, $currentTime);
        if($stmt->execute()){
            echo "chat {$tableName} is added {$data} at {$currentTime} \n";
            return true;
        } else {
            echo "add chat {$data} to {$tableName} is failed \n";
            return false;
        }
    }

    public function getDMChannel($userId1, $userId2){
        $stmt = $this->mysqli->prepare("SELECT channel_id FROM channel_dm WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)");
        $stmt->bind_param("iiii", $userId1, $userId2, $userId2, $userId1);
        $stmt->execute();
        $result = $stmt->get_result();
        if($result->num_rows > 0){
            $row = $result->fetch_assoc();
            return $row["channel_id"];
        }
        return NOT_SETUP;
    }
}

?>