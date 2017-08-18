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
$search_query = "'" . $json ['search_query'] . "'";
$offset = $json ['offset'];
$connection = mysqli_connect ( $db_host, $db_username, $db_password, $db_name );

$events_result = $people_result = $groups_result = TRUE;
$events = $people = $groups = array ();

$events_query = "SELECT DISTINCT Event.*, Invite.invitee_id, Invite.type,
	Invite.going, Invite.sent,
	((Event.creator_id = $user_id) OR (Invite.invitee_id = $user_id)) AS is_involved
	FROM Event LEFT OUTER JOIN Invite ON Event.event_id = Invite.event_id
	WHERE INSTR(title, $search_query) > 0 ORDER BY is_involved DESC, going DESC 
	LIMIT 10 OFFSET $offset";
$events_result = mysqli_query ( $connection, $events_query );
while ( $row = mysqli_fetch_assoc ( $events_result ) ) {
	array_push ( $events, $row );
}

$people_query = "SELECT DISTINCT User.*, Connection.friend_id = $user_id 
	AS is_friend FROM User LEFT JOIN Connection ON User.user_id = Connection.user_id
	WHERE INSTR(User.user_name, $search_query) > 0 ORDER BY is_friend DESC
	LIMIT 10 OFFSET $offset";
$people_result = mysqli_query ( $connection, $people_query );
while ( $row = mysqli_fetch_assoc ( $people_result ) ) {
	array_push ( $people, $row );
}

$groups_query = "SELECT DISTINCT ShoutGroup.* FROM ShoutGroup 
	LEFT OUTER JOIN GroupMember ON ShoutGroup.group_id = GroupMember.group_id
	WHERE INSTR(ShoutGroup.group_name, $search_query) > 0 
	AND(GroupMember.user_id = $user_id OR ShoutGroup.type = 'Public')
	LIMIT 10 OFFSET $offset";
$groups_result = mysqli_query ( $connection, $groups_query );
while ( $row = mysqli_fetch_assoc ( $groups_result ) ) {
	array_push ( $groups, $row );
}

if ($events_result != FALSE && $people_result != FALSE && $groups_result != FALSE) {
	echo json_encode ( array (
			'results' => "Success!",
			'events' => $events,
			'people' => $people,
			'groups' => $groups,
			'error_message' => "" 
	) );
} else {
	echo json_encode ( array (
			'results' => "Failure!",
			'events' => "",
			'people' => "",
			'groups' => "",
			'error_message' => mysqli_error ( $connection ) 
	) );
}
?>