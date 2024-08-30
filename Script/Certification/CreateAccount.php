<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        "status" => "error",
        "message" => "It's not post connection"
    ]);
    exit;
}

// 헤더 설정 (JSON 응답을 위한)

// POST로 전달된 데이터를 받기
$user_email = $_POST['joinEmail'] ?? '';
$user_password = $_POST['joinPassword'] ?? '';
$user_name = $_POST['joinName'] ?? '';


// 데이터베이스 연결
$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

// 연결 확인
if ($conn->connect_error) {
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit;
}

// 입력 데이터 유효성 검사
if (!empty($user_email) && !empty($user_password) && !empty($user_name)) {
    try {
        $conn->begin_transaction();

        // `user` 테이블에 사용자 이름 추가
        $stmtA = $conn->prepare("INSERT INTO users (username) VALUES (?)");
        $stmtA->bind_param("s", $user_name);
        $stmtA->execute();
        
        // 마지막으로 삽입된 사용자 ID 가져오기
        $last_user_id = $stmtA->insert_id;

        $emptyJsonArray = json_encode([]);
        //userRegister 추가하기
        $stmtRegister = $conn->prepare("INSERT INTO user_register (user_id, project) VALUES (?, ?)");
        $stmtRegister->bind_param("is", $last_user_id, $emptyJsonArray);

        // 비밀번호 해시화 (보안 강화)
        $hashed_password = password_hash($user_password, PASSWORD_DEFAULT);

        // `user_certification` 테이블에 데이터 삽입
        $stmtB = $conn->prepare("INSERT INTO users_certification (user_id, email, password) VALUES (?, ?, ?)");
        $stmtB->bind_param("iss", $last_user_id, $user_email, $hashed_password);
        $stmtB->execute();

        if ($conn->commit()) {
            echo json_encode([
                "status" => "success",
                "message" => "New record created successfully"
            ]);
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Error: " . $stmt->error
            ]);
        }
    } catch (Exception $e){
        $conn->rollback();
        echo json_encode([
            "status" => "error",
            "message" => "Error: " .  $e 
        ]);
    }

    // 준비된 문(statement) 및 연결 종료
    $stmtA->close();
    $stmtB->close();

} else {
    echo json_encode([
        "status" => "error",
        "message" => "Invalid input data. \n email: {$user_email} \n password: {$user_password} \n name: {$user_name} "
    ]);
}

$conn->close();
?>
