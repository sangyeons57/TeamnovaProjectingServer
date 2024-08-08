<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class ExitChannel implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        if(isset(WebSocket::Instance()->channels[$from->channel])){
            WebSocket::Instance()->channels[$from->channel]->detach($from);
        }
        $from->channel = NOT_SETUP;
    }
}
?>