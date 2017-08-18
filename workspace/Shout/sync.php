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
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$events_query = "SELECT DISTINCT Event.* FROM Event LEFT OUTER JOIN Invite ON 
Event.event_id  = Invite.event_id WHERE Event.creator_id = $user_id OR 
(Invite.invitee_id =  $user_id AND Invite.sent =  'No')";
$events_result = mysqli_query ( $connection, $events_query );
$events = array ();
while ( $row = mysqli_fetch_assoc ( $events_result ) ) {
	$row['sent'] = "Yes";
	array_push ( $events, $row );
}

$invites_query = "SELECT DISTINCT * FROM Invite WHERE invitee_id = $user_id AND sent = 
'No'";
$invites_result = mysqli_query ( $connection, $invites_query );
$invites = array ();
while ( $row = mysqli_fetch_assoc ( $invites_result ) ) {
	$row['sent'] = "Yes";
	array_push ( $invites, $row );
}

$update_query = "UPDATE Invite SET sent = 'Yes' WHERE invitee_id = $user_id AND 
sent = 'No'";
$update_result = mysqli_query ( $connection, $update_query );

if ($events_result != FALSE && $invites_result != FALSE && $update_result) {
	echo json_encode ( array (
			'result' => "Success!",
			'events' => $events,
			'invites' => $invites,
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'result' => "Failure!",
			'events' => "",
			'invites' => "",
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>