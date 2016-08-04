<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

$host_name = 'localhost';
$user_name = '987829';
$password = 'Auremest7';

$data = file_get_contents('php://input');
$json = json_decode($data, true);
$invitee_id = $json['invitee_id'];
$event_id = $json['event_id'];
$going = $json['going'];

$connection = mysqli_connect($host_name, $user_name, $password, $user_name);
$query = "UPDATE Invite SET going = $going WHERE invitee_id = $invitee_id AND 
event_id = $event_id";
$result = mysqli_query($connection, $query);

if ($result) {
	echo json_encode ( array (
			'update' => "Success!",
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'insert' => "Failure!",
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>