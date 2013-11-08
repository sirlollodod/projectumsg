<?php

include './classes/DBMS.php';
include './classes/SingleChat.php';

/*$db->registerUser('397', '3494566596', "sirlollodod@libero.it");
 $sessione = $db->getSessionId('397', '3494566596');
echo $sessione;
$chatInfo = $db->getSingleChatInfo(4);
echo "versione della chat di id='1': " . $chatInfo->getVers() . "<br>";
echo "<br> time millis = '" . $db->getMillis() . "'";
echo "<br> new message id = '" . $db->createNewSingleChatMessage(4, 1, "proviamo a inserire un nuovo messaggio", 0, 0) . "'";
$db->createNewSingleChat('0', '397', '3494566596', '39', '3494566596');
echo "chat esistente? " . ( $db->checkSingleChatExists('309', '3494566596', '397', '3494566596') ? "SI" : "NO" ) . "<br>";
//echo "aggiorno versione della chat di id='4'... " . ($db->generateNewChatVersion(4)? "AGGIORNATA" : "NON AGGIORNATA" ) . "<br>";
$chatInfo = $db->getSingleChatInfo(4);
echo "versione della chat di id='1': " . $chatInfo->getVers() . "<br>";
*/


$db = new DBMS();

$response = array(
		'request' => $_POST['action'],
		'errorCode' => ''
);

switch ($_POST['action']){

	case 'CHECK_USER_REGISTERED':
		$result = $db->checkUserRegistered($_POST['prefix'], $_POST['num']);
		if(!$result){
			$response['errorCode'] = 'KO';
			break;
		}

		$response['errorCode'] = 'OK';

		if($result['isRegistered']){
			$response['isRegistered'] = true;
			$response['email'] = $result['email'];
		}
		else{
			$response['isRegistered'] = false;
			$response['email'] = '';
		}
		break;

	case 'REGISTER_USER':
		$result = $db->registerUser($_POST['prefix'], $_POST['num'], $_POST['email']);

		if(!$result){
			$response['errorCode'] = 'KO';
			break;
		}

		$response['errorCode'] = 'OK';
	
		$response['isRegistered'] = true;
		$response['prefix'] = $result['prefix'];
		$response['num'] = $result['num'];
		$response['email'] = $result['email'];

		break;

	case 'LOGIN_USER':

		break;


	default:
		$response['request'] = "BAD_REQUEST";
		$response['errorCode'] = "KO";
		break;
}


$encoded = json_encode($response);
header('Content-type: application/json');
exit($encoded);
?>