<?php


function sendEmail($emailAddress, $smsCode, $emailCode){
	mail($emailAddress, "Verification codes", "SMSCODE: '" . $smsCode . "'\nEMAILCODE: '" . $emailCode . "'", "From: UMessage Service");
}

function notifyNewMessage($gcmId, $prefix, $num){
	$message = array(
			'action' => '78',
			'prefix' => $prefix,
			'num' => $num
	);

	send_notification($gcmId, $message);
}

function notifyMessageDelivered($gcmId, $prefix, $num){
	$message = array(
			'action' => '78',
			'prefix' => $prefix,
			'num' => $num
	);

	send_notification($gcmId, $message);
}

function send_notification($registatoin_ids, $message) {

	define("GOOGLE_API_KEY", "AIzaSyDGjIoxS6WXXm_wvXqgPc-2bQqTDxI0gsA"); // Place your Google API Key

	// Set POST variables
	$url = 'https://android.googleapis.com/gcm/send';

	$fields = array(
			'registration_ids' => $registatoin_ids,
			'data' => $message,
	);

	$headers = array(
			'Authorization: key=' . GOOGLE_API_KEY,
			'Content-Type: application/json'
	);
	// Open connection
	$ch = curl_init();

	// Set the url, number of POST vars, POST data
	curl_setopt($ch, CURLOPT_URL, $url);

	curl_setopt($ch, CURLOPT_POST, true);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

	// Disabling SSL Certificate support temporarly
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

	curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

	// Execute post
	$result = curl_exec($ch);
	if ($result === FALSE) {
		//die('Curl failed: ' . curl_error($ch));
	}

	// Close connection
	curl_close($ch);
	//echo $result;
}




?>