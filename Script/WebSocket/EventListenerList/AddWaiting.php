<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use AWS\CRT\Internal\Encoding;
use Ratchet\ConnectionInterface;

class AddWaiting implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {

        // 전달받은 user_id
        $waitingUserName = $json_data[KEY_WAITING_USER_NAME]; // 친구로 추가할 ID
        $userId = $json_data[KEY_USER_ID]; // 현재 사용자의 ID
        $username = $json_data[KEY_USERNAME]; // 현재 사용자의 ID

        $response = array();
        $mysqli = Util::Instance()->getMysqli();


        $row = WebSocketMysql::Instance()->getUserByUsername($waitingUserName);
        if ($row) {
            $waiting = json_decode($row['waiting'], true);
            $friends = json_decode($row['friends'], true);

            if (!is_array($waiting)) {
                $waiting = [];
            }

            //데이터가 존제하면 추가하지않고 실패 반환
            if (in_array($userId, $waiting) || in_array($userId, $friends)) {
                $response = array(
                    TYPE => TYPE_ADD_WAITING,
                    STATUS => STATUS_SUCCESS . "_0",
                    KEY_DATA => "user id already added"
                );
            } else {
                $waiting[] = $userId;

                // 업데이트된 waiting 배열을 다시 JSON으로 인코딩
                $newWaitingJson = json_encode($waiting);

                // 데이터베이스에 업데이트
                $stmt = $mysqli->prepare("UPDATE users SET waiting = ? WHERE username = ?");
                $stmt->bind_param('ss', $newWaitingJson, $waitingUserName);

                if ($stmt->execute() == true) {
                    $data = array(
                        TYPE => TYPE_ADD_WAITING,
                        KEY_USER_ID  => $userId,
                        KEY_USERNAME => $username
                    );
                    WebSocket::Instance()->sendData($row["id"], json_encode($data));

                    $response = array(
                        TYPE => TYPE_ADD_WAITING,
                        STATUS => STATUS_SUCCESS,
                        KEY_DATA => "success"
                    );
                } else {
                    $response = array(
                        TYPE => TYPE_ADD_WAITING,
                        STATUS => STATUS_ERROR,
                        KEY_DATA => "fail to database update"
                    );
                }
            }
        } else {
            $response = array(
                TYPE => TYPE_ADD_WAITING,
                STATUS => STATUS_ERROR,
                KEY_DATA => "fail to find user"
            );
        }
        echo $userId . json_encode($response) . "\n";
        $from->send(json_encode($response));
        $mysqli->close();
    }
} 
?>