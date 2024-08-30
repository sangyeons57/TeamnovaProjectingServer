<?php
session_start();

// 로그인 폼이 제출되었는지 확인
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $username = $_POST['username'];
    $password = $_POST['password'];

    // 간단한 사용자 인증 예제 (실제로는 데이터베이스를 사용하여 인증)
    if ($username === 'user' && $password === 'pass') {
        // 사용자 인증 성공
        $_SESSION['user_id'] = 1;  // 실제로는 사용자의 고유 ID를 저장

        // 원래 요청한 페이지로 리다이렉트
        $redirect = $_GET['redirect'] ?? 'index.php';
        header("Location: " . $redirect);
        exit();
    } else {
        $error = "Invalid username or password.";
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <h1>Login</h1>
    <?php if (isset($error)) { echo "<p style='color: red;'>$error</p>"; } ?>
    <form method="POST">
        <label for="username">Username:</label>
        <input type="text" id="username" name="username"><br>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password"><br>
        <button type="submit">Login</button>
    </form>
</body>
</html>
