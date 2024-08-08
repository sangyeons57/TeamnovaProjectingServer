<?php
class Util{
    const CONFIG_PATH = "/var/www/config.php";

    const KEY_STATUS = "status";
    const STATUS_ERROR = "error"; 
    const STATUS_SUCCESS = "success"; 
    const KEY_ERROR_MESSAGE = "errorMessage"; 
    const KEY_MESSAGE = "message"; 
    const KEY_DATA = "data"; 

    private static $instance = null;
    private function __construct() {
    }

    // 복사 메서드 방지
    public function __clone() { }
    // 직렬화 방지
    public function __wakeup() { }

    // 싱글톤 인스턴스 반환 메서드
    public static function Instance() {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    public function checkIsPost($errorMessage = "REQUEST_METHOD is not POST "){
        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            echo json_encode( array(self::KEY_STATUS => self::STATUS_ERROR, self::KEY_ERROR_MESSAGE => $errorMessage));
            exit;
        }
    }

    public function getMysqli(){
        require self::CONFIG_PATH;
        $mysqli = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);
        if ($mysqli->connect_error) {
            die("Connection failed: " . $mysqli->connect_error);
        }
        return $mysqli;
    }
    public function getChatMysqli(){
        require self::CONFIG_PATH;
        return new mysqli($MysqlChatDbname, $MysqlChatUsername, $MysqlChatPassword, $MysqlChatDbname);
    }
}
?>