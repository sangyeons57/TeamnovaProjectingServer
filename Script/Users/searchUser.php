<?php
require_once __DIR__ . '/../Util.php'; 
require_once Util::CONFIG_PATH;

header('Content-Type: application/json');

Util::Instance()->checkIsPost();

// 사용자 입력 값 (예: form에서 POST 방식으로 전달된 name 값)
$name = $_POST['name'];

if (empty($name)) {
    echo "Name parameter is missing.";
    exit;
}

// MySQLi를 사용한 데이터베이스 연결
$mysqli = Util::Instance()->getMysqli();

// 연결 확인
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// SQL 쿼리 준비
$sql = "SELECT * FROM user WHERE name LIKE ?";
$stmt = $conn->prepare($sql);
if ($stmt === false) {
    die("Prepare failed: " . $conn->error);
}

// 파라미터 바인딩
$searchPattern = "%{$name}%";
$stmt->bind_param('s', $searchPattern);

// 쿼리 실행
$stmt->execute();

// 결과 가져오기
$result = $stmt->get_result();

// 결과 출력
if ($result->num_rows > 0) {
    $data = [];
    // 결과가 있는 경우
    while ($row = $result->fetch_assoc()) {
        $data[] = [
            'id' => $row['id'],
            'name' => $row['name']
        ];
    }
    echo json_encode([Util::KEY_STATUS => Util::STATUS_SUCCESS, Util::KEY_DATA => $data]);
} else {
    echo json_encode([Util::KEY_STATUS => Util::STATUS_ERROR, Util::KEY_ERROR_MESSAGE => "zero ". $mysqli->error ]);
}

// 스테이트먼트 닫기
$stmt->close();

// 데이터베이스 연결 종료
$conn->close();
?>