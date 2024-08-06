<?php
require_once __DIR__ . '/../Util.php'; 
require_once Util::CONFIG_PATH;
header('Content-Type: application/json');

Util::Instance()->checkIsPost();

// 전달받은 user_id
$waitingUserId = $_POST['waitingUserId']; // 친구로 추가된 ID
$userId = $_POST['userId']; // 현재 사용자의 ID

// MySQLi를 사용한 데이터베이스 연결
$conn = Util::Instance()->getMysqli(); 

// 연결 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 트랜잭션 시작
$conn->begin_transaction();

try {
    // 현재 사용자의 waiting, friends 컬럼 조회
    $sql = "SELECT waiting, friends FROM users WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param('i', $userId);
    $stmt->execute();
    $result = $stmt->get_result();

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
        if (($key = array_search($waitingUserId, $waiting)) !== false) {
            unset($waiting[$key]);
        }

        // friends에 user_id 추가
        if (!in_array($waitingUserId, $friends)) {
            $friends[] = $waitingUserId;
        }

        // 업데이트된 waiting과 friends 배열을 JSON으로 인코딩
        $newWaitingJson = json_encode(array_values($waiting));
        $newFriendsJson = json_encode($friends);

        // 데이터베이스에 업데이트
        $updateSql = "UPDATE user SET waiting = ?, friends = ? WHERE id = ?";
        $stmt = $conn->prepare($updateSql);
        $stmt->bind_param('ssi', $newWaitingJson, $newFriendsJson, $userId);
        if (!$stmt->execute()) {
            throw new Exception("Error updating current user's record: " . $conn->error);
        }

        // 상대방의 friends 컬럼에도 추가
        $sql = "SELECT friends, waiting FROM user WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param('i', $waitingUserId);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $friendFriends = json_decode($row['friends'], true);
            $friendWaitings = json_decode($row['waiting'], true);

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

            $newFriendWaitingJson = json_encode(array_values($waiting));
            $newFriendFriendsJson = json_encode($friendFriends);

            // 데이터베이스에 상대방 업데이트
            $updateFriendSql = "UPDATE user SET waiting = ?, friends = ? WHERE id = ?";
            $stmt = $conn->prepare($updateFriendSql);
            $stmt->bind_param('ssi', $newFriendWaitingJson, $newFriendFriendsJson, $waitingUserId);
            if (!$stmt->execute()) {
                throw new Exception("Error updating friend's record: " . $conn->error);
            }

        } else {
            throw new Exception("Friend user not found");
        }

        $conn->commit();
        echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS, Util::KEY_MESSAGE => "success"]);
    } else {
        throw new Exception("User not found");
    }
} catch (Exception $e) {
    // 에러 발생 시 롤백
    $conn->rollback();
    echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => $e->getMessage()]);
}

// 연결 종료
$conn->close();
?>