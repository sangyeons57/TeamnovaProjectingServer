<?php
// CORS 헤더 설정 (클라이언트에서 요청을 보낼 수 있도록 허용)
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

// 요청된 경로를 확인
$requestUri = $_SERVER['REQUEST_URI'];

// 요청된 경로가 "/path/to/resource"인지 확인
if ($requestUri === 'Script/test/') {
    // 요청된 경로에 대한 응답을 생성
    $response = array('message' => 'Hello, Netty Client!');
    echo json_encode($response);
} else {
    // 요청된 경로가 맞지 않을 경우 404 Not Found 응답
    http_response_code(404);
    echo json_encode(array('error' => 'Not Found' . $requestUri));
}
?>
