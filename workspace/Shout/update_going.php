<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$host_name = 'localhost';
$user_name = '987829';
$password = 'Auremest7';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$invitee_id = "'$json ['invitee_id']'";
$event_id = "'$json ['event_id']'";
$going = "'$json ['going']'";
$type = "'$json ['type']'";
$sent = "'$json ['sent']'";

$connection = mysqli_connect ( $host_name, $user_name, $password, $user_name );
$query = "INSERT INTO Invite (event_id, invitee_id, going, type, sent) 
VALUES ($event_id, $invitee_id, $going, $type, $sent) ON DUPLICATE KEY UPDATE
going = $going, type = $type, sent = $sent";
$result = mysqli_query ( $connection, $query );

if ($result) {
	$select_query = "SELECT DISTINCT Event.*, invitee_id, going, type, sent
	FROM Event INNER JOIN Invite ON Event.event_id = Invite.invite_id 
	WHERE Event.event_id = $event_id AND Invite.invite_id = $invitee_id";
	$select_result = mysqli_query ( $connection, $select_query );
	echo json_encode ( array (
			'update' => "Success!",
			'event_invite' => mysqli_fetch_assoc ( $select_result ),
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'insert' => "Failure!",
			'event_invite' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>