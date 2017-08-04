<?php
// API Url
$url = 'localhost/create_event.php';

// Initiate cURL.
$ch = curl_init ( $url );
$jsonData = array (
		'creator_id' => "21",
		'shout' => "true",
		'description' => "Just Noshing",
		'title' => "Late Afternoon Nosh",
		'tag' => "Nosh",
		'location' => "My Back Garden",
		'start_datetime' => "2017:7:1:16:0",
		'end_datetime' => "2017:7:1:16:30",
		'new_event' => "Yes",
		'event_id' => null,
		'invitees' => array ("22", "26") 
);

$jsonDataEncoded = json_encode ( $jsonData );

curl_setopt ( $ch, CURLOPT_POST, 1 );
curl_setopt ( $ch, CURLOPT_POSTFIELDS, $jsonDataEncoded );
curl_setopt ( $ch, CURLOPT_HTTPHEADER, array (
		'Content-Type: application/json' 
) );
curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );

$result = curl_exec ( $ch );
curl_close ( $ch );

echo $result;

?>