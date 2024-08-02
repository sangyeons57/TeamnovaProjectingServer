<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}

$user_id = (int) $_POST['user_id'];
$hashed_password = password_hash($_POST['new_password'], PASSWORD_DEFAULT);

$sql = "SELECT * FROM reset_password WHERE user_id = ? AND expires_at > NOW() AND is_verified = 1";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$stmt->store_result();
$stmt->fetch();

if ($stmt->num_rows > 0) {

    // 비밀번호 업데이트
    $stmt = $conn->prepare("UPDATE users_certification SET password = ? WHERE user_id = ?");
    $stmt->bind_param("si", $hashed_password, $user_id);
    $stmt->execute();

    // 토큰 제거
    $stmt = $conn->prepare("DELETE FROM reset_password WHERE user_id = ?");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();

    echo json_encode(["status" => "success", "message" => "Password updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Not Verified user_id"]);
}

$stmt->close();
$conn->close();
?>
