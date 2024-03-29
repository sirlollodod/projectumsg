<?php

include './classes/DBMS.php';
include './classes/SingleChat.php';
include './classes/SingleChatMessage.php';
include './functions/functions.php';

$imageContactFolder = "./UMessage/contactImages/";
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
		'request' => $_POST['action'],
		'errorCode' => '',
		'errorInfo' => ''
);

switch ($_POST['action']){

	/**
	 * + action: CHECK_USER_REGISTERED
	 * + prefix: prefisso internazionale, comprensivo del '+'
	 * + num: numero di telefono
	 * * anonymous: 'yes' se omettere la mail nel risultato
	 * return:
	 * + isRegistered: true o false, a seconda che utente richiesto sia gi� registrato al servizio online
	 * + email: email dell'utente se registrato e richiesta non anonima
	 * + imageProfileSrc: percorso preceduto da ./ relativo all'immagine profilo dell'utente, se immagine presente, vuoto altrimenti
	 *
	 */

	case 'CHECK_USER_REGISTERED':
		$result = $db->checkUserRegistered($_POST['prefix'], $_POST['num']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		$response['errorCode'] = 'OK';

		if($result['isRegistered']){
			$response['isRegistered'] = true;
			$response['imageProfileSrc'] = $result['imageProfileSrc'];
			if(!isset($_POST['anonymous']) || $_POST['anonymous'] != 'yes'){
				$response['email'] = $result['email'];
			}
			else{
				$response['email'] = '';
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
		$result = $db->registerUser($_POST['prefix'], $_POST['num'], $_POST['email']);

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

		$result = $db->requestLoginUser($_POST['prefix'], $_POST['num']);

		if($result['verificationCodesGenerated']){
			$response['verificationCodes'] = 'OK';
			sendEmail($response['email'], $result['smsCode'], $result['emailCode']);
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
		 * + imageProfileSrc: percorso preceduto da ./ relativo all'immagine profilo dell'utente, se immagine presente, vuoto altrimenti
		 * + prefix: prefisso dell'utente loggato
		 * + num: numero dell'utente loggato
		 */
	case 'LOGIN_USER':
		$result = $db->loginUser($_POST['prefix'], $_POST['num'], $_POST['emailCode'], $_POST['smsCode']);

		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			$response['sessionId'] = '';//debug
			break;
		}

		$response['errorCode'] = 'OK';
		$response['prefix'] = $_POST['prefix'];
		$response['num'] = $_POST['num'];
		$response['sessionId'] = $result['sessionId'];
		$response['imageProfileSrc'] = $result['imageProfileSrc'];

		break;

		/**
		 * + action: SEND_NEW_MESSAGE
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + destPrefix: prefisso internazionale comprensivo del '+' del destinatario
		 * + destNum: numero di telefono del destinatario
		 * + localChatVersion: versione locale (Android) della chat posseduta dal mittente
		 * + message: messaggio da inviare
		 * + type: tipo di messaggio (text, ...)
		 * + messageTag: pseudo-identificativo del messaggio lato user Android
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isDestValid: true o false, a seconda che il numero di telefono del destinatario sia gi� registrato al sistema online
		 * + dataNewMessage: timestamp (ms) associata al nuovo messaggio
		 * + statusNewMessage: stato del nuovo messaggio
		 * + syncChatRequired: true o false, a seconda che sia richiesta una sincronizzazione con la chat, prima di poter inserire il messaggio richiesto. Se true, la richiesta di creazione del nuovo messaggio viene scartata.
		 * + chatVersionChanged: true o false, a seconda che la versione della chat sia stata modificata
		 * + newChatVersion: la nuova versione della chat, se � stata aggiornata
		 *
		 */
	case 'SEND_NEW_MESSAGE':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->checkUserRegistered($_POST['destPrefix'], $_POST['destNum']);
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

		$result = $db->checkSingleChatExists($myPrefix, $myNum, $_POST['destPrefix'], $_POST['destNum'], $_POST['localChatVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['chatExists']){
			if($result['syncChatRequired']){
				$response['errorCode'] = 'OK';
				$response['errorInfo'] = 'Chat synchronization required';
				$response['syncChatRequired'] = true;
				break;
			}

			$response['syncChatRequired'] = false;
			$idChat = $result['idChat'];
		}
		else{
			$result = $db->createNewSingleChat($myPrefix, $myNum, $_POST['destPrefix'], $_POST['destNum']);
			if(!$result){
				$response['errorCode'] = 'KO';
				$response['errorInfo'] = 'PHP error creating new single chat';
				break;
			}

			$response['syncChatRequired'] = false;
			$idChat = $result['idChat'];


		}

		$result = $db->getDirectionMessage($idChat, $myPrefix, $myNum);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error getting direction message';
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

		$result = $db->createNewSingleChatMessage($idChat, $myDirection, $_POST['message'], $_POST['type'], $_POST['messageTag']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error creating new single chat message';
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

			$result = $db->getGcmId($_POST['destPrefix'], $_POST['destNum']);
			if($result['errorCode'] == 'OK' && $result['isDestinationValid']){
				notifyNewMessage(array($result['gcmId']), $myPrefix, $myNum);
			}

		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error: other';
			break;
		}


		break;

		/**
		 * + action: SEND_NEW_PROFILE_IMAGE
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + userProfileImage: nuovo file immagine del profilo dell'utente
		 * return:
		 * + isSessionValid: true o false, a seconda che la sessionId della richiesta sia valida o meno
		 *
		 */
	case 'SEND_NEW_PROFILE_IMAGE':
		$result = $db->checkSessionId($_POST['sessionId']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if(!$result['isRegistered']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'SessionId not valid';
			$response['isSessionValid'] = false;
			break;
		}
		else{
			$response['isSessionValid'] = true;
		}

		$time = $db->getMillis();
		$imageBaseName = $imageContactFolder . $result['prefix'] . $result['num'] . "_" . $time;
		$imageName = $imageBaseName . ".jpg";

		if(move_uploaded_file($_FILES['userProfileImage']['tmp_name'], $imageName)){
			$result = $db->changeUserImage($_POST['sessionId'], $imageBaseName, $time);
			if(!$result){
				$response['errorCode'] = 'KO';
				$response['errorInfo'] = 'PHP error';
				if(is_file($imageName)){
					unlink($imageName);
				}
				break;
			}

			if($result['errorCode'] == 'OK'){
				$response['errorCode'] = 'OK';
				$oldImageBaseSrc = $result['oldImageSrc'];
				$oldImageSrc = $oldImageBaseSrc . ".jpg";
				if(is_file($oldImageSrc)){
					unlink($oldImageSrc);
				}
				break;
			}
			else{
				$response['errorCode'] = 'KO';
				$response['errorInfo'] = 'PHP error';
				if(is_file($imageName)){
					unlink($imageName);
				}
				break;
			}
		}
		else{
			$response['errorCode'] = 'KO';
			$response["errorInfo"] = 'Error uploading file.';
			break;
		}


		break;

		/**
		 * + action: GET_CONVERSATION_MESSAGES
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + destPrefix: prefisso internazionale comprensivo del '+' del destinatario
		 * + destNum: numero di telefono del destinatario
		 * + localChatVersion: versione locale (Android) della chat posseduta dal mittente
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isDestValid: true o false, a seconda che il numero di telefono del destinatario sia gi� registrato al sistema online
		 * + numMessages: numero di messaggi scaricati
		 * + onlineChatVersion: versione online attuale della chat
		 * + messages: [] contenente tutti i messaggi della chat selezionata presenti online
		 *
		 */
	case 'GET_CONVERSATION_MESSAGES':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->checkUserRegistered($_POST['destPrefix'], $_POST['destNum']);
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

		$result = $db->checkSingleChatExists($myPrefix, $myNum, $_POST['destPrefix'], $_POST['destNum'], $_POST['localChatVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if(!$result['chatExists']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'No messages to download.';
			$response['numMessages'] = 0;
			$response['onlineChatVersion'] = $_POST['localChatVersion'];
			$response['messages'] = array();
			break;
		}

		if(!$result['syncChatRequired']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'Chat synchronization not required';
			$response['numMessages'] = 0;
			$response['onlineChatVersion'] = $_POST['localChatVersion'];
			$response['messages'] = array();
			break;
		}

		$idChat = $result['idChat'];
		$response['onlineChatVersion'] = $result['chatVersion'];

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

		$result = $db->getConversationMessages($idChat, $myPrefix, $myNum, $myDirection);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		$response['errorCode'] = 'OK';
		$response['errorInfo'] = 'Messages download';
		$response['numMessages'] = $result['numMessages'];
		$response['messages'] = $result['messages'];

		break;

		/**
		 * + action: UPDATE_MESSAGE_DOWNLOADED
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + destPrefix: prefisso internazionale comprensivo del '+' del destinatario
		 * + destNum: numero di telefono del destinatario
		 * + messageData: data del messaggio di cui si vuole modificare lo stato
		 * + messageTag: tag del messaggio di cui si vuole modificare lo stato
		 * + messageDirection: direzione del messaggio rispetto all'utente che invoca la richiesta
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isDestValid: true o false, a seconda che il numero di telefono del destinatario sia gi� registrato al sistema online
		 * + messageStatusUpdated: true o false, a seconda che lo stato del messaggio sia stato modificato come richiesto
		 *
		 */
	case 'UPDATE_MESSAGE_DOWNLOADED':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->checkUserRegistered($_POST['destPrefix'], $_POST['destNum']);
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

		$result = $db->checkSingleChatExists($myPrefix, $myNum, $_POST['destPrefix'], $_POST['destNum'], $_POST['localChatVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if(!$result['chatExists']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'Chat do not exists.';
			$response['messageStatusUpdated'] = false;
			break;
		}

		$idChat = $result['idChat'];

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

		if($myDirection == "1"){
			if($_POST['messageDirection'] == "0"){
				$messageDirection = "1";
			}
			else{
				$messageDirection = "0";
			}
		}
		else{
			$messageDirection = $_POST['messageDirection'];
		}

		$result = $db->updateMessageStatus($idChat, $messageDirection, $_POST['messageData'], $_POST['messageTag'], "3");
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$response['errorCode'] = 'OK';
			$response['messageStatusUpdated'] = $result['messageStatusUpdated'];



			$result = $db->getGcmId($_POST['destPrefix'], $_POST['destNum']);
			if($result && $result['errorCode'] == 'OK' && $result['isDestinationValid']){
				notifyMessageDelivered(array($result['gcmId']), $myPrefix , $myNum);
			}



			break;
		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error: changing message status';
			$response['debugInfo'] = $result['debugInfo'];
			$response['messageStatusUpdated'] = false;
			break;
		}

		break;

		/**
		 * + action: UPDATE_MESSAGE_NOTIFICATION_DELIVERED
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + destPrefix: prefisso internazionale comprensivo del '+' del destinatario
		 * + destNum: numero di telefono del destinatario
		 * + messageData: data del messaggio di cui si vuole modificare lo stato
		 * + messageTag: tag del messaggio di cui si vuole modificare lo stato
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isDestValid: true o false, a seconda che il numero di telefono del destinatario sia gi� registrato al sistema online
		 * + messageStatusUpdated: true o false, a seconda che lo stato del messaggio sia stato modificato come richiesto
		 *
		 */
	case 'UPDATE_MESSAGE_NOTIFICATION_DELIVERED':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->checkUserRegistered($_POST['destPrefix'], $_POST['destNum']);
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

		$result = $db->checkSingleChatExists($myPrefix, $myNum, $_POST['destPrefix'], $_POST['destNum'], $_POST['localChatVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if(!$result['chatExists']){
			$response['errorCode'] = 'OK';
			$response['errorInfo'] = 'Chat do not exists.';
			$response['messageStatusUpdated'] = false;
			break;
		}

		$idChat = $result['idChat'];

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

		if($myDirection == "1"){
			if($_POST['messageDirection'] == "0"){
				$messageDirection = "1";
			}
			else{
				$messageDirection = "0";
			}
		}
		else{
			$messageDirection = $_POST['messageDirection'];
		}

		$result = $db->updateMessageStatus($idChat, $messageDirection, $_POST['messageData'], $_POST['messageTag'], "5");
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$response['errorCode'] = 'OK';
			$response['messageStatusUpdated'] = $result['messageStatusUpdated'];
			break;
		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error: changing message status';
			$response['messageStatusUpdated'] = false;
			break;
		}

		break;

		/**
		 * + action: GET_CHATS_VERSION
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + numChats: numero delle chat relative all'utente
		 * + chatsInfo: [] contenente tutte le chat registrate relative all'utente che invoca la richiesta
		 *
		 */
	case 'GET_CHATS_VERSION':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->getAllChats($myPrefix, $myNum);
		if($result['errorCode'] == 'KO'){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'Error getting all chats info';
			$response['chatsInfo'] = array();
			break;
		}
		else{
			$response['errorCode'] = 'OK';
			$response['numChats'] = $result['numChats'];
			$response['chatsInfo'] = $result['chatsInfo'];
		}

		break;

		/**
		 * + action: REPORT_ERROR
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + tag: classe che ha generato l'errore
		 * + info: messaggio di errore
		 * + appVersion: versione dell'applicazione installata
		 * return:
		 *
		 */
	case 'REPORT_ERROR':
		$result = $db->checkSessionId($_POST['sessionId']);
		if(!$result){
			//debug, user anonymous	
			//$response['errorCode'] = 'KO';
			//$response['errorInfo'] = 'PHP error';
			//break;
		}

		if($result['errorCode'] == 'OK'){
			$myPrefix = $result['prefix'];
			$myNum = $result['num'];
		}
		else{
			$myPrefix = "N/A";
			$myNum = "N/A";
			break;
		}

		if(!isset($_POST['appVersion'])){
			$_POST['appVersion'] = '< 4';
		}

		$result = $db->insertError($myPrefix, $myNum, $_POST['tag'], $_POST['info'], $_POST['appVersion']);
		if(!$result){
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}

		if($result['errorCode'] == 'OK'){
			$response['errorCode'] = 'OK';
			break;
		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'PHP error';
			break;
		}


		break;

		/**
		 * + action: UPDATE_GCM_ID
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * + gcmId: nuovo valore di GCM relativo all'utente che invoca la richiesta
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 * + isGcmIdUpdated: true o false, a seonca che il nuovo gcmId sia stato aggiornato
		 */
	case 'UPDATE_GCM_ID':
		$result = $db->updateGcmSessId($_POST['gcmId'], $_POST['sessionId']);
		if($result['errorCode'] == 'OK'){
			if($result['isSessionValid']){
				$response['errorCode'] = 'OK';
				$response['isSessionValid'] = true;
				$response['isGcmIdUpdated'] = $result['isGcmIdUpdated'];

				break;
			}
			else{
				$response['errorCode'] = 'OK';
				$response['errorInfo'] = 'Session invalid';
				$response['isSessionValid'] = false;
				break;
			}
		}
		else{
			$response['errorCode'] = 'KO';
			$response['errorInfo'] = 'Error updating gcmId';
			break;
		}

		break;

		/**
		 * + action: PING_ME_GCM
		 * + sessionId: id di sessione utente che invoca la richiesta
		 * return:
		 * + isSessionValid: true o false, a seconda che l'id di sessione sia valido o meno
		 */
	case  'PING_ME_GCM':
		$result = $db->checkSessionId($_POST['sessionId']);
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

		$result = $db->getGcmId($myPrefix, $myNum);
		if($result && $result['errorCode'] == 'OK' && $result['isDestinationValid'] && $result['gcmId'] != ""){
			pingMe(array($result['gcmId']));
		}

		break;

		/**
		 * Caso di default.
		 *
		 */
	default:
		$response['request'] = "BAD_REQUEST";
		$response['errorCode'] = "KO";
		break;
}


$encoded = json_encode($response);
//header('Content-type: application/json');
exit($encoded);
?>