<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class AddWaiting implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {

        // 전달받은 user_id
        $waitingUserName = $json_data['waitingUserName']; // 친구로 추가할 ID
        $userId = $json_data['userId']; // 현재 사용자의 ID

        $response = array();
        $mysqli = Util::Instance()->getMysqli();

        // 현재 사용자의 waiting 컬럼 조회
        $sql = "SELECT id, waiting FROM users WHERE username = ?";
        $stmt = $mysqli ->prepare($sql);
        $stmt->bind_param('s', $waitingUserName);
        $stmt->execute();
        $result = $stmt->get_result(); 
        if ($result->num_rows > 0) {

            $row = $result->fetch_assoc();
            $waiting = json_decode($row['waiting'], true);

            if (!is_array($waiting)) {
                $waiting = [];
            }

            //데이터가 존제하면 추가하지않고 실패 반환
            if (in_array($userId, $waiting)) {
                $response = array(
                    STATUS => STATUS_ERROR,
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
                        "type" => "AddWaiting",
                        "userId" => $userId
                    );
                    WebSocket::Instance()->sendData($row["id"], json_encode($data));
                    $response = array(
                        STATUS => STATUS_SUCCESS,
                        KEY_DATA => "success"
                    );
                } else {
                    $response = array(
                        STATUS => STATUS_ERROR,
                        KEY_DATA => "fail to database update"
                    );
                }
            }
        } else {
            $response = array(
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