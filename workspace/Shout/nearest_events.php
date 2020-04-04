<?php
error_reporting ( E_ALL );
ini_set ( 'display_errors', 1 );

$db_host = 'localhost';
$db_username = 'solomon';
$db_password = 'Auremest7';
$db_name = 'shout';

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$latitude = $json ['latitude'];
$longitude = $json ['longitude'];
$offset = $json ['offset'];
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$events_result = $people_result = $groups_result = TRUE;
$events = $people = $groups = array ();

$events_query = "SELECT DISTINCT Event.*, 69.0 *
    DEGREES(ACOS(COS(RADIANS(Event.latitude))
         * COS(RADIANS($latitude))
         * COS(RADIANS(Event.longitude - $longitude))
         + SIN(RADIANS(Event.latitude))
         * SIN(RADIANS($latitude)))) AS distance FROM Event HAVING distance <= 20
LIMIT 10 OFFSET $offset";
$events_result = mysqli_query ( $connection, $events_query );
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
			'events' => "",
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>