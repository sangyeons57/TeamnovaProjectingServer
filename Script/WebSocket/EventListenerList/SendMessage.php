<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class SendMessage implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        if(!isset($from->userId)){
            $json_data[STATUS] = STATUS_ERROR; 
            $from->send(json_encode($json_data));
        } else if(!isset($from->channel) || $from->channel === NOT_SETUP){
            $json_data[STATUS] = STATUS_ERROR; 
            $from->send(json_encode($json_data));
        } else {
            $json_data[STATUS] = STATUS_SUCCESS; 
            $json_data["createTime"] = date("Y-m-d H:i:s");
            if(WebSocketMysql::Instance()->isDMChannel($from->channel)){
                $tableName = "dm_". $from->channel;
                WebSocketMysql::Instance()->addChatData($tableName, $from->userId, $json_data["message"]);
            }
            foreach ( WebSocket::Instance()->channels[$from->channel] as $client){
                if($from === $client) {
                    $json_data["isSelf"] = true;
                } else {
                    $json_data["isSelf"] = false;
                }
                $client->send(json_encode($json_data));
            }

        }
    }
}
?>