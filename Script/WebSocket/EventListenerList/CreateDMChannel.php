<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class CreateDMChannel implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        if (!(isset($json_data["userId1"]) && isset($json_data["userId2"]))){
            $json_data[STATUS] = STATUS_ERROR;
        } else {
            $user_id1 = $json_data["userId1"];
            $user_id2 = $json_data["userId2"];
            if (WebSocketMysql::Instance()->createDMChannel($user_id1, $user_id2)) {
                $json_data[STATUS] = STATUS_SUCCESS;
            } else {
                $json_data[STATUS] = STATUS_ERROR;
            }
        }
        $from->send(json_encode($json_data));
    }
}
?>