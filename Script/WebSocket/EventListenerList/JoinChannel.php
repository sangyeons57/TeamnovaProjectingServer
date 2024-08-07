<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class JoinChannel implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        if (!isset($json_data["channelId"])){
            $json_data[STATUS] = STATUS_ERROR;
        } else {
            WebSocket::Instance()->callEvent(TYPE_EXIT_CHANNEL, $from, $json_data);
            $channel = $json_data["channelId"]; 
            $userId = $json_data["userId"]; 
            if(isset($from->userId) && $userId == $from->userId &&
            WebSocketMysql::Instance()->checkChannelExist($channel) &&
            WebSocketMysql::Instance()->checkIsMember($channel, $userId)){
                if(!isset(WebSocket::Instance()->channels[$channel])){
                    WebSocket::Instance()->channels[$channel] = new \SplObjectStorage;
                }
                WebSocket::Instance()->channels[$channel]->attach($from);
                $from->channel = $channel;
                $json_data[STATUS] = STATUS_SUCCESS;
            } else {
                $json_data[STATUS] = STATUS_ERROR;
            }
        } 
        $from->send(json_encode($json_data));
    }
}
?>
