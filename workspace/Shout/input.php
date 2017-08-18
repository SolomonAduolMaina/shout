<?php
// API Url
$url = 'localhost/person_events.php';

// Initiate cURL.
$ch = curl_init ( $url );
$jsonData = array (
		'user_id' => "21",
		'person_id' => "23",
		'offset' => "0"
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