<?php
define("NOT_SETUP","0");
define("STATUS","status");
define("STATUS_SUCCESS","success");
define("STATUS_ERROR","error");
define("KEY_ERROR_MESSAGE", "errorMessage");

define("KEY_USER_ID", "userId");
define("KEY_WAITING_USER_NAME", "waitingUserName");
define("KEY_USERNAME", "username");
define("KEY_DATA", "data");

define("TYPE", "type");
define("TYPE_MESSAGE", "Message");
define("TYPE_SET_USER", "SetUser");
define("TYPE_CREATE_DM_CHANNEL", "CreateDMChannel");
define("TYPE_JOIN_CHANNEL", "JoinChannel");
define("TYPE_EXIT_CHANNEL", "ExitChannel");
define("TYPE_ADD_WAITING", "AddWaiting");
define("TYPE_ADD_FRIEND_ON_WAITING", "AddFriendOnWaiting");
define("TYPE_REMOVE_WAITING_DATA", "RemoveWaitingData");

require __DIR__ . '/../../vendor/autoload.php';
require_once __DIR__ . '/../Util.php';

require_once __DIR__ . '/EventListenerList/SendMessage.php';
require_once __DIR__ . '/EventListenerList/SetUser.php';
require_once __DIR__ . '/EventListenerList/CreateDMChannel.php';
require_once __DIR__ . '/EventListenerList/ExitChannel.php';
require_once __DIR__ . '/EventListenerList/JoinChannel.php';
require_once __DIR__ . '/EventListenerList/AddWaiting.php';
require_once __DIR__ . '/EventListenerList/AddFriendOnWaiting.php';

use Aws\Braket\Exception\BraketException;
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
    public $clients;
    public $channels;
    public $userIdMap;

    public $eventListenerMap;

    private function __construct() {
        $this->clients = new \SplObjectStorage;
        $this->channels = [];
        $this->userIdMap =[];
        $this->eventListenerMap = [];

        $this->Register();
        require __DIR__ . '/mysql.php';
    }

    public function addEventLister($eventName, EventListener $eventListener){
        if(!isset($this->eventListenerMap[$eventName])){
            $this->eventListenerMap[$eventName] = [];
        }
        $this->eventListenerMap[$eventName][] = $eventListener;
    }

    public function callEvent($eventName, ConnectionInterface $conn, $json_data){
        if(isset($this->eventListenerMap[$eventName])){
            foreach ($this->eventListenerMap[$eventName] as $listener){
                echo "call Event $eventName \n";
                $listener->OnCall($conn, $json_data);
            }
        }
    }

    public function Register(){
        $this->addEventLister(TYPE_MESSAGE, new SendMessage());
        $this->addEventLister(TYPE_SET_USER, new SetUser());
        $this->addEventLister(TYPE_CREATE_DM_CHANNEL, new CreateDMChannel());
        $this->addEventLister(TYPE_EXIT_CHANNEL, new ExitChannel());
        $this->addEventLister(TYPE_JOIN_CHANNEL, new JoinChannel());
        $this->addEventLister(TYPE_ADD_WAITING, new AddWaiting());
        $this->addEventLister(TYPE_ADD_FRIEND_ON_WAITING, new AddFriendOnWaiting());
    }

    private static $instance =  null;
    public static function Instance(){
        if(self::$instance === null){
            self::$instance = new WebSocket();
        }
        return self::$instance;
    }

    public function onOpen(ConnectionInterface $conn) {
        // New connection
        $this->clients->attach($conn);
        $conn->channel = NOT_SETUP;

        $queryString = $conn->httpRequest->getUri()->getQuery();
        parse_str($queryString, $queryParams);

        $this->userIdMap[$queryParams["userId"]] = $conn;
        $conn->userId= $queryParams["userId"];

        echo "New connection! ({$conn->resourceId}) / Clients Count : {$this->clients->count()}\n";
    }

    public function onMessage(ConnectionInterface $from, $msg) {
        echo $msg . "\n";

        $json_data = json_decode($msg, true);
        $type = $json_data["type"];
        $this->callEvent($type, $from, $json_data);
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

    public function getClient($userId){
        if(isset($this->userIdMap[$userId])){
            return $this->userIdMap[$userId];
        }
        return null;
    }
    public function sendData($userId, $data){
        echo "send {$data} message to";
        $conn = $this->getClient($userId);
        if (isset($conn)){
            $conn->send($data);
            echo " $userId was success \n";
        } else {
            echo " $userId was failed \n";
        }
    }
}