<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class SetUser implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        //이전 userId설정 제거
        if(isset($from->userId) && isset(WebSocket::Instance()->userIdMap[$from->userId])){
            unset(WebSocket::Instance()->userIdMap[$from->userId]);
        }

        //새로운 userId설정
        $from->userId = $json_data["userId"];
        WebSocket::Instance()->userIdMap[$json_data["userId"]] = $from;

        echo "user [{$from->resourceId}] is assigned [{$json_data["userId"]}] \n";
        $from->send(json_encode($json_data));
    }
}
?>