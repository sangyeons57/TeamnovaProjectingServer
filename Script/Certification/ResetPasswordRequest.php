<?php
require $_SERVER['DOCUMENT_ROOT']. '/vendor/autoload.php';
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

$conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode( array("status" => "error","message"=> "REQUEST_METHOD is not POST ") );
    exit;
}

// 이메일 주소 가져오기
$email = $_POST['email'];

// 이메일 주소로 사용자 확인
$stmt = $conn->prepare("SELECT user_id FROM users_certification WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    $stmt->bind_result($user_id);
    $stmt->fetch();

    // 토큰 생성 및 저장
    $token = bin2hex(random_bytes(32));
    //현제로부터 1시간
    $expires_at = date('Y-m-d H:i:s', strtotime('+1 hour'));

    $stmt = $conn->prepare("INSERT INTO reset_password (user_id, token, expires_at) VALUES (?, ?, ?)");
    $stmt->bind_param("iss", $user_id, $token, $expires_at);
    $stmt->execute();

    $stmt->close();
    $conn->close();

    $reset_link  = $BASE_URL . "Script/Certification/VerifyResetPassword.php?token=" . $token;


    $mail = new PHPMailer(true);
    try {
        // SMTP 서버 설정
        $mail->isSMTP();
        $mail->Host       = 'email-smtp.ap-northeast-2.amazonaws.com'; // AWS SES SMTP 엔드포인트
        $mail->SMTPAuth   = true;
        $mail->Username   = $SMTPUserName; // SMTP 사용자 이름
        $mail->Password   = $SMTPPassword; // SMTP 비밀번호
        $mail->SMTPSecure = 'tls'; // 보안 프로토콜
        $mail->Port       = 587; // SMTP 포트

        // $mail->SMTPDebug = 2;

        // 이메일 설정
        $mail->setFrom($SMTPSender, 'Projecting'); // 발신자 이메일 및 이름
        $mail->addAddress($email); // 수신자 이메일

        $mail->isHTML(true);
        $mail->Subject = "Password Reset";

        $mail->Body = $reset_link;

        // 이메일 전송
        $mail->send();

        //데이터 베이스에 이메일과 인증코드 추가
        // 인증코드 입력시 데이터 베이스에서 인증코드 비교후
        // 같으면 응답
    } catch (Exception $e) {
        echo json_encode( array("status" => "fail","message"=> "Message could not be sent. Mailer Error: {$mail->ErrorInfo}") );
        exit;
    }

    echo json_encode(["status" => "success", "message" => "Password reset link sent", "userId" => $user_id]);
} else {
    echo json_encode(["status" => "error", "message" => "Email not found"]);
}
?>
