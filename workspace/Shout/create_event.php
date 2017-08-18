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
$description = "'" . $json ['description'] . "'";
$tag = "'" . $json ['tag'] . "'";
$shout = "'" . $json ['shout'] . "'";
$location_name = "'" . $json ['location_name'] . "'";
$latitude = "'" . $json ['latitude'] . "'";
$longitude = "'" . $json ['longitude'] . "'";
$start_year = "'" . $json ['start_year'] . "'";
$start_month = "'" . $json ['start_month'] . "'";
$start_day = "'" . $json ['start_day'] . "'";
$start_hour = "'" . $json ['start_hour'] . "'";
$start_minute = "'" . $json ['start_minute'] . "'";
$end_year = "'" . $json ['end_year'] . "'";
$end_month = "'" . $json ['end_month'] . "'";
$end_day = "'" . $json ['end_day'] . "'";
$end_hour = "'" . $json ['end_hour'] . "'";
$end_minute = "'" . $json ['end_minute'] . "'";

$invitees = $json ['invitees'];
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$is_new_event = $new_event == "Yes";
$first = $is_new_event ? "" : "event_id,";
$second = $is_new_event ? "" : "$event_id,";

$query = "INSERT INTO Event ($first creator_id, title, description,
tag, shout, location_name, latitude, longitude, start_year, start_month,
start_day, start_hour, start_minute, end_year, end_month, end_day, 
end_hour, end_minute) VALUES 
($second $creator_id, $title, $description,
$tag, $shout, $location_name, $latitude, $longitude, $start_year, $start_month,
$start_day, $start_hour, $start_minute, $end_year, $end_month, $end_day, 
$end_hour, $end_minute) ON DUPLICATE KEY UPDATE
event_id = LAST_INSERT_ID(event_id), creator_id = $creator_id, title = $title, 
description = $description, tag = $tag, shout = $shout, 
location_name = $location_name, latitude = $latitude, longitude = $longitude,
start_year = $start_year, start_month = $start_month, start_day = $start_day,
start_hour = $start_hour, start_minute = $start_minute, end_year = $end_year,
end_month = $end_month, end_day = $end_day, end_hour = $end_hour,
end_minute = $end_minute";
$create_event = mysqli_query ( $connection, $query );

if ($create_event) {
	$new_event_id = "'" . $connection->insert_id . "'";
	$query = "INSERT INTO Invite (invitee_id, event_id, type, going, sent) VALUES (";
	$query_end = ", 'Invite', 'Unset', 'No') ON DUPLICATE KEY 
			UPDATE invitee_id = invitee_id, event_id = event_id";
	$create_invite = true;
	for($index = 0; $index < count ( $invitees ); $index ++) {
		$invitee_id = "'" . $invitees [$index] . "'";
		$my_query = $query . "$invitee_id, $new_event_id" . $query_end;
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
				'type' => ($is_new_event ? "New Event" : "Event Update"),
				'event_id' => $new_event_id 
		);
		
		$response = json_decode ( sendNotification ( $reg_ids, $messageData ), true );
		$response_result = $response ['failure'] == 0;
		if ($response_result) {
			echo json_encode ( array (
					'result' => "Success!",
					'event_invite' => mysqli_fetch_assoc ( $select_result ),
					'error_message' => NULL 
			) );
		} else {
			echo json_encode ( array (
					'result' => "Failure!",
					'event_invite' => NULL,
					'error_message' => "There was an error sending invites." 
			) );
		}
	} else {
		echo json_encode ( array (
				'result' => "Failure!",
				'event_invite' => NULL,
				'error_message' => mysqli_error ( $connection ) 
		) );
	}
} else {
	echo json_encode ( array (
			'result' => "Failure!",
			'event_invite' => NULL,
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>