<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');

// MySQLi 연결
$mysqli = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

// 연결 오류 처리
if ($mysqli->connect_error) {
    die("Connection failed: " . $mysqli->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}

// 세션 시작
session_start();

// POST 데이터 가져오기
$current_password = isset($_POST['current_password']) ? $_POST['current_password'] : '';
$new_password = isset($_POST['new_password']) ? $_POST['new_password'] : '';

// 세션에서 user_id 가져오기
if (!isset($_SESSION['user_id'])) {
    echo json_encode(["status" => "session_error", "message" => "User not logged in."]);
    exit();
}

$user_id = $_SESSION['user_id'];

// 비밀번호가 제공되었는지 확인
if (empty($current_password) || empty($new_password)) {
    echo json_encode(["status" => "error", "message" => "Current password and new password are required."]);
    exit();
}

// 현재 비밀번호 확인
$stmt = $mysqli->prepare("SELECT password FROM users_certification WHERE user_id = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "User not found."]);
    exit();
}

$row = $result->fetch_assoc();
$hashed_password = $row['password'];

// 현재 비밀번호 검증
if (!password_verify($current_password, $hashed_password)) {
    echo json_encode(["status" => "message_error", "message" => "현재 패스워드가 잘못됬습니다."]);
    exit();
}

// 새 비밀번호 해시화
$new_hashed_password = password_hash($new_password, PASSWORD_BCRYPT);

// 비밀번호 업데이트
$stmt = $mysqli->prepare("UPDATE users_certification SET password = ? WHERE user_id = ?");
$stmt->bind_param("si", $new_hashed_password, $user_id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Password updated successfully."]);
} else {
    echo json_encode(["status" => "error", "message" => "Failed to update password."]);
}

// 리소스 해제
$stmt->close();
$mysqli->close();
?>
