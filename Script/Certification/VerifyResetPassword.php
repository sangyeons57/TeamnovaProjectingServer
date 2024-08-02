<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

echo "인증시작 // ";
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// 토큰 확인
$token = $_GET['token'] ?? '';
$stmt = $conn->prepare("SELECT user_id FROM reset_password WHERE token = ? AND expires_at > NOW() AND is_verified = 0");
$stmt->bind_param("s", $token);
$stmt->execute();
$stmt->store_result();
echo "인증확인 // ";

if ($stmt->num_rows > 0) {
    $stmt->bind_result($user_id);
    $stmt->fetch();

    $stmt = $conn->prepare("UPDATE reset_password SET is_verified = 1 WHERE token = ? AND expires_at > NOW() AND is_verified = 0");
    $stmt->bind_param("s",$token);
    $stmt->execute();


    echo "인증 완료";
} else {
    echo "인증 실패" . $token;
}
$stmt->close();
$conn->close();

?>