<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';
//header('Content-Type: application/json');

// 모든 에러 보고
error_reporting(E_ALL);

// 에러를 화면에 표시
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}
// MySQL 데이터베이스에 연결
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

// 연결 상태 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// POST 요청에서 아이디 가져오기
$id = $_POST['joinEmail'];

// 아이디 존재 여부 확인
$sql = $conn->prepare("SELECT * FROM users_certification WHERE email = ?");
$sql->bind_param("s", $id);
$sql->execute();
$sql->store_result();

$response = array();
if ($sql->num_rows > 0) {
    $response["exists"] = true;
} else {
    $response["exists"] = false;
}

// JSON 형식으로 응답
echo json_encode($response);

$sql->close();
$conn->close();
?>
