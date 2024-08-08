<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class AddFriendOnWaiting implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        $userId = $json_data["userId"];
        $friendId = $json_data["userId1"];

        $mysqli = Util::Instance()->getMysqli();

        $userData = array(
            TYPE => TYPE_ADD_FRIEND_ON_WAITING,
            KEY_USER_ID => $friendId,
        );
        $friendData = array(
            TYPE => TYPE_ADD_FRIEND_ON_WAITING,
            KEY_USER_ID => $userId ,
        );

        // 트랜잭션 시작
        $mysqli->begin_transaction();

        try {
            // 현재 사용자의 waiting, friends 컬럼 조회
            $result = WebSocketMysql::Instance()->getUserByUserId($userId);

            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                $waiting = json_decode($row['waiting'], true);
                $friends = json_decode($row['friends'], true);

                if (!is_array($waiting)) {
                    $waiting = [];
                }

                if (!is_array($friends)) {
                    $friends = [];
                }

                // waiting에서 user_id 제거
                if (($key = array_search($friendId, $waiting)) !== false) {
                    unset($waiting[$key]);
                }

                // friends에 user_id 추가
                if (!in_array($friendId, $friends)) {
                    $friends[] = $friendId;
                }

                // 업데이트된 waiting과 friends 배열을 JSON으로 인코딩
                $newWaitingJson = json_encode(array_values($waiting));
                $newFriendsJson = json_encode($friends);

                $friendData["username"] = $row["username"];

                // 데이터베이스에 업데이트
                $updateSql = "UPDATE users SET waiting = ?, friends = ? WHERE id = ?";
                $stmt = $mysqli->prepare($updateSql);
                $stmt->bind_param('ssi', $newWaitingJson, $newFriendsJson, $userId);
                if (!$stmt->execute()) {
                    throw new Exception("Error updating current user's record: " . $mysqli->error);
                }

                // 상대방의 friends 컬럼에도 추가
                $result = WebSocketMysql::Instance()->getUserByUserId($friendId);

                // 데이터베이스에 업데이트
                if ($result->num_rows > 0) {
                    $row = $result->fetch_assoc();
                    $friendWaitings = json_decode($row['waiting'], true);
                    $friendFriends = json_decode($row['friends'], true);

                    if (!is_array($friendFriends)) {
                        $friendFriends = [];
                    }
                    if (!is_array($friendWaitings)) {
                        $friendWaitings = [];
                    }

                    if (($key = array_search($userId, $friendWaitings)) !== false) {
                        unset($friendWaitings[$key]);
                    }

                    if (!in_array($userId, $friendFriends)) {
                        $friendFriends[] = $userId;
                    }

                    $newFriendWaitingJson = json_encode(array_values($friendWaitings));
                    $newFriendFriendsJson = json_encode($friendFriends);

                    $userData["username"] = $row["username"];

                    // 데이터베이스에 상대방 업데이트
                    $updateFriendSql = "UPDATE users SET waiting = ?, friends = ? WHERE id = ?";
                    $stmt = $mysqli->prepare($updateFriendSql);
                    $stmt->bind_param('ssi', $newFriendWaitingJson, $newFriendFriendsJson, $friendId);
                    if (!$stmt->execute()) {
                        throw new Exception("Error updating friend's record: " . $mysqli->error);
                    }

                } else {
                    throw new Exception("Friend user not found");
                }
                $mysqli->commit();
                $createDMChannelData = array(
                    TYPE => TYPE_CREATE_DM_CHANNEL,
                    KEY_USER_ID1 => $userId,
                    KEY_USER_ID2 => $friendId
                );
                WebSocket::Instance()->callEvent(TYPE_CREATE_DM_CHANNEL, $from, $createDMChannelData);
                WebSocket::Instance()->sendData($friendId, json_encode($friendData));
                WebSocket::Instance()->sendData($userId, json_encode($userData));

                echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS, Util::KEY_MESSAGE => "success"]) . "\n";
            } else {
                throw new Exception("User not found");
            }
        } catch (Exception $e) {
            // 에러 발생 시 롤백
            $mysqli->rollback();
            echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => $e->getMessage()]) + "\n";
        }


        // 연결 종료
        $mysqli->close();

    }
}
?>