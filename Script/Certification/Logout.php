<?php
session_start();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}
// 세션 데이터 삭제
session_unset();

// 세션 쿠키 삭제
/*
if (ini_get("session.use_cookies")) {
    $params = session_get_cookie_params();
    setcookie(session_name(), '', time() - 42000, 
        $params["path"], 
        $params["domain"], 
        $params["secure"], 
        $params["httponly"]
    );
}
*/

// 세션 종료
session_destroy();

echo json_encode(["status" => "success", "message" => "Logged out successfully."]);
?>
