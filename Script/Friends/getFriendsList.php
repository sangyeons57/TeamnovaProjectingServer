<?php
session_start();
require_once __DIR__ . '/../Util.php'; 
require_once Util::CONFIG_PATH;
header('Content-Type: application/json');

Util::Instance()->checkIsPost();

$userId = $_POST['userId']; 

if(!(isset($_SESSION["user_id"]) && isset($userId) && $_SESSION["user_id"] == $userId)) {
    echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => "userId not set up" . $_SESSION["user_id"]."/" .$_POST["userId"]]);
    exit;
} 


$mysqli = Util::Instance()->getMysqli();

$sql = "SELECT friends FROM users WHERE id = ?";
$stmt = $mysqli->prepare($sql);
$stmt->bind_param("i", $userId);
$stmt->execute();
$stmt->bind_result($waitingJson);
$stmt->fetch();
$stmt->close();

if ($waitingJson) {
    // waiting 컬럼의 JSON 배열을 디코딩
    $waitingArray = json_decode($waitingJson, true);

    if (is_array($waitingArray)) {
        if(count($waitingArray) <= 0) {
            $emptyArray = array();
            echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS . "_0"]);
            exit;
        }

        // waitingArray의 각 ID에 대해 username 조회
        $placeholders = implode(',', array_fill(0, count($waitingArray), '?'));
        $sql = "SELECT id, username FROM users WHERE id IN ($placeholders)";
        $stmt = $mysqli->prepare($sql);

        // 동적으로 매개변수 바인딩
        $stmt->bind_param(str_repeat('i', count($waitingArray)), ...$waitingArray);
        $stmt->execute();
        $result = $stmt->get_result();

        // 결과를 JSON으로 변환
        $usernames = array();
        while ($row = $result->fetch_assoc()) {
            $usernames[] = array("id" => $row["id"], "username" => $row["username"]);
        }

        // JSON 형식으로 출력
        echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS, Util::KEY_DATA => $usernames]);
    } else {
        // JSON 형식으로 출력
        // waiting이 배열이 아닌 경우 빈 배열 반환
        echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => "userId못찾음"]);
    }
} else {
    // waiting 컬럼이 비어있거나 존재하지 않는 경우 빈 배열 반환
    echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => "userId못찾음2"]);
}

$stmt->close()
?>