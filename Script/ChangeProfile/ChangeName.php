<?php
session_start();
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    exit;
}
// MySQL 데이터베이스에 연결
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

// 연결 상태 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 요청 데이터 가져오기
$new_username = isset($_POST['username']) ? $conn->real_escape_string($_POST['username']) : '';

// 사용자 ID를 세션에서 가져오기
if (!isset($_SESSION["user_id"])) {
    echo json_encode(["status" => "session_error", "message" => "". count($_SESSION)]);
    exit();
}

$user_id = $_SESSION["user_id"];


// 트랜잭션 시작
$conn->begin_transaction();

try {
    // `username`이 이미 존재하는지 확인
    $stmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
    $stmt->bind_param("s", $new_username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // `username`이 이미 존재하는 경우
        echo json_encode(["status" => "message_error", "message" => "이름이 이미 존재합니다."]);
        $stmt->close();
        $conn->rollback();
        exit();
    }

    // 사용자 `username` 업데이트
    $stmt = $conn->prepare("UPDATE users SET username = ? WHERE id = ?");
    $stmt->bind_param("si", $new_username, $user_id);
    $success = $stmt->execute();
    
    if ($success) {
        // 트랜잭션 커밋
        $conn->commit();
        echo json_encode(["status" => "success"]);
    } else {
        // 트랜잭션 롤백
        $conn->rollback();
        echo json_encode(["status" => "message_error", "message" => "이름 변경 실패"]);
    }
    
    $stmt->close();
} catch (Exception $e) {
    // 예외 발생 시 트랜잭션 롤백
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}

// 데이터베이스 연결 종료
$conn->close();
?>
