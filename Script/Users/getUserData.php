<?php
require_once __DIR__ . '/../WebSocket/WebSocket.php';
require_once __DIR__ . '/../Util.php'; 
require_once Util::CONFIG_PATH;
header('Content-Type: application/json');

Util::Instance()->checkIsPost();

$userId = $_POST["userId"];

$mysqli = Util::Instance()->getMysqli();

// SQL 쿼리문 작성 및 실행
$stmt = $mysqli->prepare("SELECT * FROM users WHERE id = ?");
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

$response = array();
// 사용자 정보 조회
if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    $response[STATUS] = STATUS_SUCCESS;
    $response[KEY_DATA] = $user; 
} else {
    $response[STATUS] = STATUS_ERROR;
    $response[KEY_ERROR_MESSAGE] = "user not found"; 
}

echo json_encode($response);
// 연결 종료
$stmt->close();

?>