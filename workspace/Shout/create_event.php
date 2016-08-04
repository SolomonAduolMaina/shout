<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$host_name = 'localhost';
$user_name = '987829';
$password = 'Auremest7';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );

$creator_id = $json ['creator_id'];
$title = $json ['title'];
$location = $json ['location'];
$description = $json ['description'];
$start_datetime = $json ['start_datetime'];
$end_datetime = $json ['end_datetime'];
$tag = $json ['tag'];
$shout = $json ['shout'];

$connection = mysqli_connect ( $host_name, $user_name, $password, $user_name );
$query = "INSERT INTO Event (creator_id, title, location, description,
start_datetime, end_datetime, tag, shout) VALUES 
($creator_id, $title, $location, $description, $start_datetime, $end_datetime,
$tag, $shout)";
$create_event = mysqli_query ( $connection, $query );

$event_id = $connection->insert_id;
$new_event_id = "'" . $event_id . "'";
$invitees = $json ['invitees'];
$type = "'Invite'";
$going = "'Unset'";
$sent = "'No'";
$query = "INSERT INTO Invite (invitee_id, event_id, type, going, sent) VALUES";
$create_invite = true;

for($index = 0; $index < count ( $invitees ); $index ++) {
	$invitee_id = $invitees [$index];
	$my_query = $query . "($invitee_id, $new_event_id, $type, $going, $sent)";
	$create_invite = $create_invite && mysqli_query ( $connection, $my_query );
}

if ($create_event && $create_invite) {
	echo json_encode ( array (
			'insert' => "Success!",
			'event_id' => $event_id,
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'insert' => "Failure!",
			'event_id' => "0",
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>