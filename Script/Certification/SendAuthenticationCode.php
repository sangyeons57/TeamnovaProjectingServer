<?php
require $_SERVER['DOCUMENT_ROOT']. '/vendor/autoload.php';
require_once $_SERVER['DOCUMENT_ROOT'] . '/../config.php';

header('Content-Type: application/json');

/*
// 모든 에러 보고
error_reporting(E_ALL);

// 에러를 화면에 표시
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
*/


use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = $_POST['email'];
    $verificationCode = rand(100000, 999999);

    $conn = new mysqli($MysqlServername, $MysqlUsername, $MysqlPassword, $MysqlDbname);

    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }

    $sql = "INSERT INTO user_authentication_codes (email, authentication_code) VALUES (?, ?)";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param('si', $email, $verificationCode);

    if($stmt->execute() == false){
        echo json_encode( array("status" => "fail","message"=> "add authentication code to db is failed") );
        exit;
    }
    $stmt->close();
    $conn->close();
    

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
        $mail->Subject = "Projecting app Authentication Code";

        $mail->Body    = "인증코드 : {$verificationCode}";

        // 이메일 전송
        $mail->send();

        //데이터 베이스에 이메일과 인증코드 추가
        // 인증코드 입력시 데이터 베이스에서 인증코드 비교후
        // 같으면 응답
        echo json_encode( array("status" => "success","message"=>"Email has been sent successfully") );
        exit;
    } catch (Exception $e) {
        echo json_encode( array("status" => "fail","message"=> "Message could not be sent. Mailer Error: {$mail->ErrorInfo}") );
        exit;
    }

}
?>