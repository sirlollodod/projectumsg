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

/**
 * FOR EVERY ACTION, oltre ai valori specifici per tipo di richiesta:
 * return:
 * + request: request type
 * + errorCode: 'OK' or 'KO', a seconda se operazione eseguita correttamente o meno
 * * errorInfo: se errorCode = 'KO', breve descrizione dell'errore
 *
*/
$response = array(
		'request' => $_GET['action'],
		'errorCode' => '',
		'errorInfo' => ''
);

//debug
$_GET['destPrefix'] = "+39";

switch ($_GET['action']){

	/**
	 * + action: CHECK_USER_REGISTERED
	 * + prefix: prefisso internazionale, comprensivo del '+'
	 * + num: numero di telefono
	 * * anonymous: 'yes' se omettere la mail nel risultato
	 * return:
	 * + isRegistered: true o false, a seconda che utente richiesto sia già registrato al servizio online
	 * + email: email dell'utente se registrato e richiesta non anonima
	 *
	 */
	case 'CHECK_USER_REGISTERED':
		$result = $db->checkUserRegistered($_GET['prefix'], $_GET['num']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		$response['errorCode'] = 'OK';

		if($result['isRegistered']){
			$response['isRegistered'] = true;
			if(!isset($_GET['anonymous']) || $_GET['anonymous'] != 'yes'){
				$response['email'] = $result['email'];
			}

		}
		else{
			$response['isRegistered'] = false;
			$response['email'] = '';
		}
		break;

		/**
		 * + action: REGISTER_USER
		 * + prefix: prefisso internazionale, comprensivo del '+'
		 * + num: numero di telefono
		 * * email: email da associare all'utente, se non ancora registrato
		 * return:
		 * + prefix: prefisso internazionale, comprensivo del '+'
		 * + num: numero di telefono
		 * + email: email associata all'utente selezionato
		 * + isRegistered: true se operazione completata con successo
		 * + verificationCodes: 'OK' o 'KO', a seconda che i codici necessari al login dell'utente siano stati generati correttamente.
		 *
		 */
	case 'REGISTER_USER':
		$result = $db->registerUser($_GET['prefix'], $_GET['num'], $_GET['email']);

		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		$response['errorCode'] = 'OK';

		$response['isRegistered'] = true;
		$response['prefix'] = $result['prefix'];
		$response['num'] = $result['num'];
		$response['email'] = $result['email'];

		if($db->requestLoginUser($_GET['prefix'], $_GET['num'])){
			$response['verificationCodes'] = 'OK';
		}
		else{
			$response['verificationCodes'] = 'KO';
		}

		break;

		/**
		 * + action: LOGIN_USER
		 * + prefix: prefisso internazionale, comprensivo del '+'
		 * + num: numero di telefono
		 * + emailCode: codice di conferma inviato per email
		 * + smsCode: codice di conferma inviato per sms
		 * return:
		 * + sessionId: valore della sessione per l'utente richiesto se utente loggato
		 *
		 */
	case 'LOGIN_USER':
		$result = $db->loginUser($_GET['prefix'], $_GET['num'], $_GET['emailCode'], $_GET['smsCode']);

		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			$response['sessionId'] = '';//debug
			break;
		}

		$response['errorCode'] = 'OK';
		$response['sessionId'] = $result['sessionId'];

		break;

		/**
		 * + action: SEND_NEW_MESSAGE
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + destPrefix: prefisso internazionale comprensivo del '+' del destinatario
		 * + destNum: numero di telefono del destinatario
		 * + localChatVersion: versione locale (Android) della chat posseduta dal mittente
		 * + message: messaggio da inviare
		 * + type: tipo di messaggio (text, ...)
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isDestValid: true o false, a seconda che il numero di telefono del destinatario sia già registrato al sistema online
		 * + dataNewMessage: timestamp (ms) associata al nuovo messaggio
		 * + statusNewMessage: stato del nuovo messaggio
		 * + syncChatRequired: true o false, a seconda che sia richiesta una sincronizzazione con la chat, prima di poter inserire il messaggio richiesto. Se true, la richiesta di creazione del nuovo messaggio viene scartata.
		 *
		 */
	case 'SEND_NEW_MESSAGE':
		$result = $db->checkSessionId($_GET['sessionId']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$response['isSessionValid'] = true;
			$myPrefix = $result['prefix'];
			$myNum = $result['num'];
		}
		else{
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'Session invalid';
			$response['isSessionValid'] = false;
			break;
		}

		$result = $db->checkUserRegistered($_GET['destPrefix'], $_GET['destNum']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if(!$result['isRegistered']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'Destination not registered';
			$response['isDestValid'] = false;
			break;
		}
		else{
			$response['isDestValid'] = true;
		}

		$result = $db->checkSingleChatExists($myPrefix, $myNum, $_GET['destPrefix'], $_GET['destNum'], $_GET['localChatVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['chatExists']){
			if($result['syncChatRequired']){
				$response['errorCode'] = 'OK';
				$response['syncChatRequired'] = true;
				break;
			}

			$response['syncChatRequired'] = false;
			$idChat = $result['idChat'];
		}
		else{
			$result = $db->createNewSingleChat($myPrefix, $myNum, $_GET['destPrefix'], $_GET['destNum']);
			if(!$result){
				$response['errorCode'] = 'KO';
				$response['errorInfo'] = 'PHP error';
				break;
			}
				
			$response['syncChatRequired'] = false;
			$idChat = $result['idChat'];
				
				
		}

		$result = $db->getDirectionMessage($idChat, $myPrefix, $myNum);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$myDirection = $result['direction'];

		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error: checking direction';
			break;
		}

		$result = $db->createNewSingleChatMessage($idChat, $myDirection, $_GET['msg'], $_GET['type']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$response['errorCode'] = 'OK';
			$response['statusNewMessage'] = $result['statusNewMessage'];
			$response['dataNewMessage'] = $result['dataNewMessage'];
			$response['chatVersionChanged'] = $result['chatVersionChanged'];
			if($result['chatVersionChanged']){
				$response['newChatVersion'] = $result['newChatVersion'];
			}
		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}


		break;


	default:
		$response['request'] = "BAD_REQUEST";
		$response['errorCode'] = "KO";
		break;
}


$encoded = json_encode($response);
//header('Content-type: application/json');
exit($encoded);
?>