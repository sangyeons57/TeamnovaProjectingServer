<?php
require_once __DIR__ . '/../Util.php'; 
require_once Util::CONFIG_PATH;

// 전달받은 user_id
$waitingUserName = $_POST['waitingUserName']; // 친구로 추가할 ID
$userId = $_POST['userId']; // 현재 사용자의 ID

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

    // 새로운 waitingUser를 waiting 배열에 추가
    if (!in_array($userId, $waiting)) {
        $waiting[] = $userId;
    }

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
        echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS, Util::KEY_DATA => "success"]);
    } else {
        echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => "Error updating record: " . $mysqli->error ]);
    }
} else {
    echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE =>  "User not found"]);
}

$mysqli->close();
?>
