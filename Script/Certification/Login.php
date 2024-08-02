<?php
session_start();
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');
use \Firebase\JWT\JWT;
use Firebase\JWT\Key;

$ONE_DAY = 60 * 60 * 24;
$mysqli = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}

// 연결 확인
if ($mysqli->connect_error) {
    die(json_encode(["status" => "error", "message" => "데이터 연결 실패"]));
}

// 요청 데이터 가져오기
$email = isset($_POST['email']) ? $mysqli->real_escape_string($_POST['email']) : '';
$plain_password = isset($_POST['password']) ? $_POST['password'] : '';

// 이메일과 비밀번호 확인
if (empty($email) || empty($plain_password)) {
    echo json_encode(["status" => "error", "message" => "이메일과 페스워드 입력이 안됨"]);
    exit();
}

// SQL 쿼리 준비 및 실행
$query = "SELECT u.id, u.username, uc.password 
    FROM users_certification as uc
    INNER JOIN users as u ON uc.user_id = u.id
    WHERE uc.email = ?";
$stmt = $mysqli->prepare($query);
$stmt->bind_param("s", $email);
$stmt->execute(); //쿼리실행 
$stmt->store_result(); // 쿼리결과를 메모리에 저장 -> 여러번 사용할수 있도록 함 -> 결과집합을 fetch()를 사용하여 순차적으로 읽을수잇음

if ($stmt->num_rows === 0) {
    echo json_encode(["status" => "email_error", "message" => "유효하지 않은 이메일"]);
    $stmt->close();
    $mysqli->close();
    exit();
}

// 결과 가져오기
$stmt->bind_result($user_id, $user_name, $hashed_password);
$stmt->fetch();
$stmt->close();
$mysqli->close();

// 비밀번호 검증
if (password_verify($plain_password, $hashed_password)) {
    $_SESSION["user_id"] = $user_id;
    $_SESSION["username"] = $user_name;

    echo json_encode(["status" => "success", "message" => "Login successful.", "user_id" => $_SESSION["user_id"], "user_name" => $_SESSION["username"]]);
} else {
    echo json_encode(["status" => "error", "message" => "유효하지 않은 패스워드"]);
}
?>
