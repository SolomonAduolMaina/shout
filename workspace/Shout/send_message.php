<?php
include 'send_notification.php';

/**
 * The following function will send a GCM notification using curl.
 *
 * @param $apiKey [string]
 *        	Browser API key string for your GCM account
 * @param $registrationIdsArray [array]
 *        	An array of registration ids to send this notification to
 * @param $messageData [array]
 *        	named array of data to send as the notification payload
 */
function sendNotification($registrationIdsArray, $messageData) {
	$headers = array (
			"Content-Type:application/json",
			"Authorization:key=AIzaSyA5vV5Jq8XkUdSZeCpQAefd6VbifsI7UDI" 
	);
	$data = array (
			'data' => $messageData,
			'registration_ids' => $registrationIdsArray 
	);
	
	$ch = curl_init ();
	
	curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
	curl_setopt ( $ch, CURLOPT_CUSTOMREQUEST, "POST" );
	curl_setopt ( $ch, CURLOPT_URL, "https://fcm.googleapis.com/fcm/send" );
	curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
	curl_setopt ( $ch, CURLOPT_POSTFIELDS, json_encode ( $data ) );
	
	$response = curl_exec ( $ch );
	curl_close ( $ch );
	
	return $response;
}

$data = file_get_contents ( 'php://input' );
$json = json_decode ( $data, true );
$registrationId = $json ['registration_id'];

$response = sendNotification ( array (
		$registrationId 
), array (
		'message' => "hello world" 
) );

echo $response;
?>