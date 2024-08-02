<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

//header('Content-Type: application/json');
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    exit;
}
// Create connection
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Receive input
$email = $_POST['email'];
$entered_code = $_POST['code'];

// Prepare the SQL query to fetch the latest code for the given email
$sql = "SELECT authentication_code, create_time FROM user_authentication_codes WHERE email = ? ORDER BY create_time DESC LIMIT 1";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();
$stmt->bind_result($stored_code, $create_time);
$stmt->fetch();
$stmt->close();

// Check if a code was found
if ($stored_code) {
    $current_time = new DateTime();
    $code_time = new DateTime($create_time);
    $interval = $current_time->diff($code_time);

    // Check if the code is valid and the time interval is within 3 minutes
    if ((int)$entered_code !== (int)$stored_code) {
        echo json_encode(array("status" => "error", "message" => "잘못된 코드 입력"));
    } else if ($interval->i > 3) {
        echo json_encode(array("status" => "error", "message" => "제한 시간 초과 "));
    } else {
        echo json_encode(array("status" => "success", "message" => "계정 인증 성공"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "해당 아이디로 인증 코드가 생성되지 않았습니다."));
}

$conn->close();
?>
