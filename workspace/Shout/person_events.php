<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$host_name = 'localhost';
$user_name = '987829';
$password = 'Auremest7';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$user_id = "'$json ['user_id']'";
$person_id = "'$json ['person_id']'";
$offset = $json ['offset'];
$connection = mysqli_connect ( $host_name, $user_name, $password, $user_name );

$events_query = "SELECT DISTINCT Event.*, Invite.invitee_id, Invite.type,
Invite.going, Invite.sent FROM Event LEFT OUTER JOIN Invite ON
Event.event_id = Invite.event_id WHERE Event.creator_id = $person_id AND
(Invite.invitee_id = $user_id OR Event.shout = 'Yes') LIMIT 10 OFFSET $offset";
$events_result = mysqli_query ( $connection, $events_query );
$events = array ();
while ( $row = mysqli_fetch_assoc ( $events_result ) ) {
	array_push ( $events, $row );
}

if ($events_result != FALSE) {
	echo json_encode ( array (
			'event_results' => "Success!",
			'events' => $events,
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'event_results' => "Failure!",
			'events' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>