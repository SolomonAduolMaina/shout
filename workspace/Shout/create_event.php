<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );
require 'send_notification.php';

$db_host = 'localhost';
$db_username = 'solomon';
$db_password = 'Auremest7';
$db_name = 'shout';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$new_event = $json ['new_event'];
$event_id = "'" . $json ['event_id'] . "'";
$creator_id = "'" . $json ['creator_id'] . "'";
$title = "'" . $json ['title'] . "'";
$location = "'" . $json ['location'] . "'";
$description = "'" . $json ['description'] . "'";
$start_datetime = "'" . $json ['start_datetime'] . "'";
$end_datetime = "'" . $json ['end_datetime'] . "'";
$tag = "'" . $json ['tag'] . "'";
$shout = "'" . $json ['shout'] . "'";
$invitees = $json ['invitees'];
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$is_new_event = $new_event == "Yes";
$first = $is_new_event ? "" : "event_id,";
$second = $is_new_event ? "" : "$event_id,";

$query = "INSERT INTO Event ($first creator_id, title, location, description,
start_datetime, end_datetime, tag, shout) VALUES 
($second $creator_id, $title, $location, $description, $start_datetime,
$end_datetime, $tag, $shout) ON DUPLICATE KEY UPDATE
event_id = LAST_INSERT_ID(event_id), creator_id = $creator_id, title = $title, 
location = $location, description = $description, start_datetime = $start_datetime,
end_datetime = $end_datetime, tag = $tag, shout = $shout";
$create_event = mysqli_query ( $connection, $query );

if ($create_event) {
	$new_event_id = "'" . $connection->insert_id . "'";
	$query = "INSERT INTO Invite (invitee_id, event_id, type, going, sent) VALUES ";
	$create_invite = true;
	for($index = 0; $index < count ( $invitees ); $index ++) {
		$invitee_id = "'" . $invitees [$index] . "'";
		$my_query = $query . "($invitee_id, $new_event_id, 'Invite', 'Unset', 'No')
	ON DUPLICATE KEY UPDATE invitee_id = invitee_id, event_id = event_id";
		$create_invite = $create_invite && mysqli_query ( $connection, $my_query );
	}
	if ($create_invite) {
		$query = "SELECT DISTINCT registration_id FROM User WHERE User.user_id = ";
		$reg_ids = array ();
		for($index = 0; $index < count ( $invitees ); $index ++) {
			$invitee_id = "'" . $invitees [$index] . "'";
			$my_query = $query . $invitee_id;
			$result = mysqli_query ( $connection, $my_query );
			$row = mysqli_fetch_assoc ( $result );
			$reg_ids [] = $row ['registration_id'];
		}
		$select_query = "SELECT DISTINCT * FROM Event WHERE Event.event_id = $new_event_id";
		$select_result = mysqli_query ( $connection, $select_query );
		$messageData = array (
				'type' => ($is_new_event ? "New Invite" : "Event Update"),
				'event_id' => array (
						'event_id' => $new_event_id 
				) 
		);
		
		$response = json_decode ( sendNotification ( $reg_ids, $messageData ), true );
		$response_result = $response ['failure'] == 0;
		if ($response_result) {
			echo json_encode ( array (
					'insert' => "Success!",
					'event_invite' => mysqli_fetch_assoc ( $select_result ),
					'error_message' => NULL 
			) );
		} else {
			echo json_encode ( array (
					'insert' => "Failure!",
					'event_invite' => NULL,
					'error_message' => "There was an error sending invites." 
			) );
		}
	} else {
		echo json_encode ( array (
				'insert' => "Failure!",
				'event_invite' => NULL,
				'error_message' => mysqli_error ( $connection ) 
		) );
	}
} else {
	echo json_encode ( array (
			'insert' => "Failure!",
			'event_invite' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>