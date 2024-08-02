<?php
// POST 요청 데이터 가져오기
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // POST 데이터 수신

    // 데이터 처리 (예: 데이터베이스에 저장, 기타 작업 등)
    // 이 예제에서는 단순히 데이터를 확인하고 응답을 반환합니다.
    $response = array(
        'status' => 'success',
        'message' => 'Data received',
        'data' => $_POST
    );

    // 응답을 JSON 형식으로 반환
    header('Content-Type: application/json');
    echo json_encode($response);
} else {
    // POST 요청이 아닌 경우
    http_response_code(405); // Method Not Allowed
    echo json_encode(array('status' => 'error', 'message' => 'Only POST method is allowed.'));
}
?>
