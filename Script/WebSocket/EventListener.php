<?php
use Ratchet\ConnectionInterface;

interface EventListener{
    public function OnCall(ConnectionInterface $from, $json_data);
}


/* 이벤트리스터 추가 형식 : 12y
<?php
require_once __DIR__ . "/../EventListener.php";
require_once __DIR__ . '/../../Util.php'; 
require_once __DIR__ . '/../WebSocket.php'; 
require_once __DIR__ . '/../WebSocketMysql.php'; 
require_once Util::CONFIG_PATH;

use Ratchet\ConnectionInterface;

class ClassName implements EventListener{
    public function OnCall(ConnectionInterface $from, $json_data) {
    }
}
?>
*/


?>