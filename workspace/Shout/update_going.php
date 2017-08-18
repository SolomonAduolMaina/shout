<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$db_host = 'localhost';
$db_username = 'solomon';
$db_password = 'Auremest7';
$db_name = 'shout';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$invitee_id = "'" . $json ['invitee_id'] . "'";
$event_id = "'" . $json ['event_id'] . "'";
$going = "'" . $json ['going'] . "'";
$type = "'" . $json ['type'] . "'";
$sent = "'" . $json ['sent'] . "'";

$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );
$query = "INSERT INTO Invite (event_id, invitee_id, going, type, sent) 
VALUES ($event_id, $invitee_id, $going, $type, $sent) ON DUPLICATE KEY UPDATE
going = $going, type = $type, sent = $sent";
$result = mysqli_query ( $connection, $query );

if ($result) {
	$select_query = "SELECT DISTINCT Event.*, invitee_id, going, type, sent
	FROM Event INNER JOIN Invite ON Event.event_id = Invite.event_id 
	WHERE Event.event_id = $event_id AND Invite.invitee_id = $invitee_id";
	$select_result = mysqli_query ( $connection, $select_query );
	echo json_encode ( array (
			'result' => "Success!",
			'event_invite' => mysqli_fetch_assoc ( $select_result ),
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'result' => "Failure!",
			'event_invite' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>