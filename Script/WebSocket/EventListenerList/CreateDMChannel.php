<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class CreateDMChannel implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        if (isset($json_data[KEY_USER_ID1]) && isset($json_data[KEY_USER_ID2])){
            $user_id1 = $json_data[KEY_USER_ID1];
            $user_id2 = $json_data[KEY_USER_ID2];
            if (WebSocketMysql::Instance()->createDMChannel($user_id1, $user_id2)) {
                $data[STATUS] = STATUS_SUCCESS;
            } else {
                $data[STATUS] = STATUS_ERROR;
            }
        } else {
            $data[STATUS] = STATUS_ERROR;
        }
        //$from->send(json_encode($json_data));
    }
}
?>