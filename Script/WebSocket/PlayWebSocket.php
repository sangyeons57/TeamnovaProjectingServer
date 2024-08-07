<?php
error_reporting(E_ALL);
ini_set('log_errors', 1);
ini_set('error_log', '/var/www/log/error.log');

// 에러를 화면에 표시
 ini_set('display_errors', 1);
 ini_set('display_startup_errors', 1);



// require '/var/www/html/vendor/autoload.php';
require __DIR__ . '/../../vendor/autoload.php';
require __DIR__ . '/WebSocket.php';

use Ratchet\Http\HttpServer;
use Ratchet\Server\IoServer;
use Ratchet\WebSocket\WsServer;
use React\Socket\Server;


$server = IoServer::factory(
    new HttpServer(
        new WsServer(
            WebSocket::Instance()
        )
    ),
    8080
);

$server->run();
?>