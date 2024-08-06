<?php
define("NOT_SETUP","0");
define("STATUS","status");
define("STATUS_SUCCESS","success");
define("STATUS_ERROR","error");

define("TYPE_MESSAGE", "Message");
define("TYPE_SET_USER", "SetUser");
define("TYPE_CREATE_DM_CHANNEL", "CreateDMChannel");
define("TYPE_JOIN_CHANNEL", "JoinChannel");
define("TYPE_EXIT_CHANNEL", "ExitChannel");

require __DIR__ . '/../../vendor/autoload.php';
//require '/var/www/html/vendor/autoload.php';
//require $_SERVER['DOCUMENT_ROOT']. '/vendor/autoload.php';
require_once __DIR__ . '/../Util.php'; 


use GuzzleHttp\Client;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;
use React\Socket\ConnectorInterface;

error_reporting(E_ALL);
ini_set('log_errors', 1);
ini_set('error_log', '/var/www/log/error.log');
 ini_set('display_errors', 1);
 ini_set('display_startup_errors', 1);


class WebSocket implements MessageComponentInterface {
    protected $clients;
    protected $channels;


    public function __construct() {
        $this->clients = new \SplObjectStorage;
        $this->channels = [];
        require __DIR__ . '/mysql.php';

        echo "construct";
    }

    public function onOpen(ConnectionInterface $conn) {
        // New connection
        $this->clients->attach($conn);
        $conn->channel = NOT_SETUP;
        //$queryParams = $conn->httpRequest->getUri()->getQuery();

        echo "New connection! ({$conn->resourceId}) / Clients Count : {$this->clients->count()}\n";
    }

    public function onMessage(ConnectionInterface $from, $msg) {
        echo $msg . "\n";

        $json_data = json_decode($msg, true);
        $type = $json_data["type"];
        switch ($type){
            case TYPE_MESSAGE:
                $this->sendMessage($from, $json_data);
                break;
            case TYPE_SET_USER:
                $this->setUser($from, $json_data);
                break;
            case TYPE_CREATE_DM_CHANNEL:
                $this->createDMChannel($from, $json_data);
                break;
            case TYPE_JOIN_CHANNEL:
                $this->joinChannel($from, $json_data);
                break;
            case TYPE_EXIT_CHANNEL:
                $this->exitChannel($from, $json_data);
                break;

        }
    }

    public function onClose(ConnectionInterface $conn) {
        // Handle connection close
        $this->clients->detach($conn);
        echo "Connection {$conn->resourceId} has disconnected\n";
    }

    public function onError(ConnectionInterface $conn, \Exception $e) {
        // Handle errors
        echo "An error has occurred: {$e->getMessage()}\n";
        $conn->close();
    }

    private function sendMessage (ConnectionInterface $from, $json_data){
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
            foreach ($this->channels[$from->channel] as $client){
                if($from === $client) {
                    $json_data["isSelf"] = true;
                } else {
                    $json_data["isSelf"] = false;
                }
                $client->send(json_encode($json_data));
            }

        }
    }

    private function exitChannel (ConnectionInterface $from, $json_data){
        if(isset($this->channels[$from->channel])){
            $this->channels[$from->channel]->detach($from);
        }
        $from->channel = NOT_SETUP;
    }
    private function joinChannel (ConnectionInterface $from, $json_data){
        if (!isset($json_data["channelId"])){
            $json_data[STATUS] = STATUS_ERROR;
        } else {
            $this->exitChannel($from, $json_data);
            $channel = $json_data["channelId"]; 
            $userId = $json_data["userId"]; 
            if(isset($from->userId) && $userId == $from->userId &&
            WebSocketMysql::Instance()->checkChannelExist($channel) &&
            WebSocketMysql::Instance()->checkIsMember($channel, $userId)){
                if(!isset($this->channels[$channel])){
                    $this->channels[$channel] = new \SplObjectStorage;
                }
                $this->channels[$channel]->attach($from);
                $from->channel = $channel;
                $json_data[STATUS] = STATUS_SUCCESS;
            } else {
                $json_data[STATUS] = STATUS_ERROR;
            }
        }

        $from->send(json_encode($json_data));
    }


    private function createDMChannel(ConnectionInterface $from, $json_data){
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

    private function setUser(ConnectionInterface $from, $json_data){
        $json_data[STATUS] = STATUS_SUCCESS;
        foreach ($this->clients as $client){
            if( $client != $from and $client->userId == $json_data["userId"]){
                $json_data[STATUS] = STATUS_ERROR;
            }
        }

        if($json_data[STATUS] === STATUS_SUCCESS){
            $from->userId = $json_data["userId"];
        }

        echo "user [{$from->resourceId}] is [{$json_data[STATUS]}] to assigned [{$json_data["userId"]}] \n";
        $from->send(json_encode($json_data));
    }
}