<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$db_host = 'localhost';
$db_username = 'solomon';
$db_password = 'Auremest7';
$db_name = 'shout';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$user_id = "'" . $json ['user_id'] . "'";
$person_id = "'" . $json ['person_id'] . "'";
$offset = $json ['offset'];
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$events_query = "SELECT DISTINCT Event.*, Invite.invitee_id, Invite.type,
Invite.going, Invite.sent FROM Event LEFT OUTER JOIN Invite ON
Event.event_id = Invite.event_id WHERE Event.creator_id = $person_id AND
(Invite.invitee_id = $user_id OR Event.shout = 'true') LIMIT 10 OFFSET $offset";
$events_result = mysqli_query ( $connection, $events_query );
$events = array ();
while ( $row = mysqli_fetch_assoc ( $events_result ) ) {
	array_push ( $events, $row );
}

if ($events_result != FALSE) {
	echo json_encode ( array (
			'result' => "Success!",
			'events' => $events,
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'result' => "Failure!",
			'events' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>