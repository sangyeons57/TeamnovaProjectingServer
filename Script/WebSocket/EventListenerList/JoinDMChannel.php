<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class JoinDMChannel implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
        WebSocket::Instance()->callEvent(TYPE_EXIT_CHANNEL, $from, $json_data);
        $userId = $json_data[KEY_USER_ID1]; 
        $friendId = $json_data[KEY_USER_ID2]; 

        $channel = WebSocketMysql::Instance()->getDMChannel($userId, $friendId);
        $json_data[KEY_CHANNEL_ID] = $channel; 

        if(isset($channel) && isset($from->userId) && $userId == $from->userId &&
        WebSocketMysql::Instance()->checkChannelExist($channel) &&
        WebSocketMysql::Instance()->checkIsMember($channel, $userId)){
            if(!isset(WebSocket::Instance()->channels[$channel])){
                WebSocket::Instance()->channels[$channel] = new \SplObjectStorage;
            }
            WebSocket::Instance()->channels[$channel]->attach($from, $userId);
            $json_data[KEY_FRIEND_NAME] = WebSocketMysql::Instance()->getUserByUserId($friendId)[KEY_USERNAME];
            $json_data[KEY_DATA] = WebSocketMysql::Instance()->getDMChatData($channel);
            $from->channel = $channel;
            $json_data[STATUS] = STATUS_SUCCESS;
            echo "{$userId} join channel {$channel} \n";
        } else {
            $json_data[STATUS] = STATUS_ERROR;
        }
        $from->send(json_encode($json_data));
    }
}
?>
