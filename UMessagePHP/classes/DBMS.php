
<?php

class DBMS{
	private $host = "mysql.netsons.com";
	private $db = "wqicankh_umsg";
	private $user = "wqicankh_admin";
	private $pass = "kj1209ASKJiwje0laq";
	private $connection;

	//Costruttore. Si connette col DB.
	function __construct(){
		/*if(!($this->connection = mysql_connect($this->host,$this->user,$this->pass)))
			return false;

		if(!mysql_select_db($this->db))
			return false;

		return true;
		*/

		// connessione a MySQL con l'estensione MySQLi
		$this->connection = new mysqli($this->host,$this->user,$this->pass,$this->db);

		// verifica dell'avvenuta connessione
		if (mysqli_connect_errno()) {
			// notifica in caso di errore
			//echo "Errore in connessione al DBMS: ".mysqli_connect_error();
			// interruzione delle esecuzioni i caso di errore
			return false;

		}
		else {
			// notifica in caso di connessione attiva
			//echo "Connessione avvenuta con successo";
		}

		// chiusura della connessione
		//$mysqli->close();

		return true;

	}

	//Ritorna la data attuale in millisecondi
	function getMillis(){
		list($usec, $sec) = explode(' ', microtime());
		return (int) ((int) $sec * 1000 + ((float) $usec * 1000));
	}

	function installDB(){
		$query = "CREATE TABLE IF NOT EXISTS `singlechat` (
				`id` int(12) NOT NULL AUTO_INCREMENT,
				`vers` varchar(32) NOT NULL,
				`prefix1` int(4) NOT NULL,
				`num1` int(4) NOT NULL,
				`prefix2` int(15) NOT NULL,
				`num2` int(15) NOT NULL,
				PRIMARY KEY (`id`)
				) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
				";

		if(!($result = $connection->query($query))){
			return false;
		}

		$query = "CREATE TABLE IF NOT EXISTS `singlechatmessages` (
				`id` int(15) NOT NULL AUTO_INCREMENT,
				`idchat` int(12) NOT NULL,
				`direction` tinyint(1) NOT NULL,
				`msg` varchar(1000) NOT NULL,
				`status` int(2) NOT NULL,
				`data` int(16) NOT NULL,
				`type` int(1) NOT NULL,
				PRIMARY KEY (`id`,`idchat`),
				KEY `type` (`type`)
				) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
				";

		if(!($result = $connection->query($query))){
			return false;
		}

		$query = "CREATE TABLE IF NOT EXISTS `user` (
				`prefix` int(4) NOT NULL,
				`num` int(15) NOT NULL,
				`email` varchar(40) NOT NULL,
				`sessid` varchar(32) DEFAULT NULL,
				`gcmid` varchar(32) DEFAULT NULL,
				PRIMARY KEY (`prefix`,`num`),
				UNIQUE KEY `sessid` (`sessid`)
				) ENGINE=InnoDB DEFAULT CHARSET=utf8;
				";

		if(!($result = $connection->query($query))){
			return false;
		}

		$query = "CREATE TABLE IF NOT EXISTS `userlogin` (
				`prefix` int(4) NOT NULL,
				`num` int(15) NOT NULL,
				`emailver` varchar(6) NOT NULL,
				`smsver` varchar(6) NOT NULL,
				PRIMARY KEY (`prefix`,`num`)
				) ENGINE=InnoDB DEFAULT CHARSET=utf8;
				";

		if(!($result = $connection->query($query))){
			return false;
		}


		return true;
	}

	//Inserisce un utente nel database, associandone anche l'indirizzo email, e ne genera una sessionId
	function registerUser($prefix, $num, $email){

		$result = $this->checkUserRegistered($prefix, $num);
		if(!$result){
			return false;
		}

		$response = array(
				'prefix' => $prefix,
				'num' => $num,
				'email' =>	''
		);

		if($result['isRegistered']){
			$response['email'] = $result['email'];
			return $response;
		}

		$query = "SELECT * FROM user WHERE sessid=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		do{
			$newSessId = md5("" . $prefix . $num . $email . time());

			$stmt->bind_param('s', $newSessId);

			if(!$stmt->execute()){
				$stmt->close();
				return false;
			}

			$stmt->store_result();
			if(!$stmt->num_rows){
				break;
			}

		} while (true);

		$query = "INSERT INTO user(prefix, num, email, sessid, gcmid, imgSrc, dataImg) VALUES(?, ?, ?, ?, '', '', '') ;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ssss', $prefix, $num, $email, $newSessId);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		$response['email'] = $email;
		$stmt->close();

		return $response;
	}

	//Controlla che l'utente sia già registrato.
	function checkUserRegistered($prefix, $num){
		$query = "SELECT * FROM user WHERE prefix=? AND num=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		$response = array(
				'isRegistered' => false,
				'email' => '',
				'imageProfileSrc' => ''
		);

		if($stmt->num_rows == 1){

			$stmt->bind_result($sPrefix, $sNum, $sEmail, $sSessId, $sGcmId, $sImgSrc, $sDataImg);
			$stmt->fetch();
			$stmt->close();
			$response['isRegistered'] = true;
			$response['email'] = $sEmail;
			if($sImgSrc == "" || $sImgSrc == null){
				$response['imageProfileSrc'] = '';
			}
			else{
				$response['imageProfileSrc'] = $sImgSrc . ".jpg";
			}
			return $response;
		}
		else{
			$stmt->close();
			return $response;
		}

	}

	//Controlla che i codici inviati per email e per sms corrispondano, e quindi logga l'utente, generando l'id di sessione ed associandolo all'utente in questione.
	function loginUser($prefix, $num, $emailver, $smsver){

		$query = "SELECT * FROM userlogin WHERE prefix=? AND num=? AND emailver=? AND smsver=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ssss', $prefix, $num, $emailver, $smsver);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$query = "SELECT * FROM user WHERE sessid=?;";
			if(!$stmt = $this->connection->prepare($query)){
				$stmt->close();
				return false;
			}

			do{
				$newSessId = md5("" . $prefix . $num . $emailver . $smsver . time());


					
				$stmt->bind_param('s', $newSessId);
					
				if(!$stmt->execute()){
					$stmt->close();
					return false;
				}
					
				$stmt->store_result();
				if(!$stmt->num_rows){
					break;
				}

			} while (true);

			$query = "UPDATE user SET sessid=? WHERE prefix=? AND num=?;";
			if(!$stmt = $this->connection->prepare($query)){
				$stmt->close();
				return false;
			}

			$stmt->bind_param('sss', $newSessId, $prefix, $num);

			if(!$stmt->execute()){
				$stmt->close();
				return false;
			}

			$stmt->store_result();
			if($stmt->affected_rows == 1){
				$response = array(
						'errorCode' => 'OK',
						'sessionId' => '',
						'imageProfileSrc' => ''
				);
					
				$response['sessionId'] = $newSessId;

				$query = "SELECT * FROM user WHERE sessid=?;";
				if(!$stmt = $this->connection->prepare($query)){
					$stmt->close();
					return false;
				}

				$stmt->bind_param('s', $newSessId);

				if(!$stmt->execute()){
					$stmt->close();
					return false;
				}

				$stmt->store_result();
				if(!$stmt->num_rows){
					return false;
				}

				$stmt->bind_result($sPrefix, $sNum, $sEmail, $sSessId, $sGcmId, $sImgSrc, $sDataImg);
				$stmt->fetch();
				$stmt->close();
					
				if($sImgSrc == "" || $sImgSrc == null){
					$response['imageProfileSrc'] = '';
				}
				else{
					$response['imageProfileSrc'] = $sImgSrc . ".jpg";
				}


				$query = "DELETE FROM userlogin WHERE prefix=? AND num=?;";
				if(!$stmt = $this->connection->prepare($query)){
					$stmt->close();
					//return false;
				}

				$stmt->bind_param('ss', $prefix, $num);

				if(!$stmt->execute()){
					$stmt->close();
					//return true;
				}

				$stmt->close();
				return $response;
			}
			else{
				$stmt->close();
				return false;
			}
		}
		else{
			$stmt->close();
			return false;
		}

	}

	//L'utente richiede il login: vengono generati i codici di controllo da inviare successivamente per mail e per sms.
	function requestLoginUser($prefix, $num){
		/*$query = "UPDATE user SET sessid='' WHERE prefix=? AND num=?;";
		 if(!$stmt = $this->connection->prepare($query)){
		$stmt->close();
		return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
		$stmt->close();
		return false;
		}

		*/

		$response = array(
				'verificationCodesGenerated' => false,
				'smsCode' => '',
				'emailCode' => ''
		);

		$query = "DELETE FROM userlogin WHERE prefix=? AND num=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$newEmailver  = substr(md5("" . $prefix . time()),0,6);
		$newSmsver = substr(md5("" . $newEmailver . time()),0,6);

		$query = "INSERT INTO userlogin VALUES (?, ?, ?, ?)";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ssss', $prefix, $num, $newEmailver, $newSmsver);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();
		if($stmt->affected_rows == 1){
			$stmt->close();
			$response['verificationCodesGenerated'] = true;
			$response['smsCode'] = $newSmsver;
			$response['emailCode'] = $newEmailver;
			return $response;
		}
		else{
			$stmt->close();
			$response['verificationCodesGenerated'] = false;
			return $response;
		}
	}

	//Controlla se id di sessione richiesta corrisponde a qualche utente
	function checkSessionId($sessid){
		$query = "SELECT * FROM user WHERE sessid=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$response = array(
				'errorCode' => '',
				'prefix' => '',
				'num' => '',
				'sessionId' => $sessid,
				'isRegistered' => false
		);


		$stmt->bind_param('s', $sessid);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){

			$stmt->bind_result($sPrefix, $sNum, $sEmail, $sSessionId, $sGcmId, $sImsSrc, $sDataImg);
			$stmt->fetch();
			$stmt->close();
			$response['isRegistered'] = true;
			$response['errorCode'] = 'OK';
			$response['prefix'] = $sPrefix;
			$response['num'] = $sNum;

			return $response;
		}
		else{
			$stmt->close();
			return $response;
		}
	}

	//Controlla che la chat tra i due numeri richiesti sia già stata creata e ne ritorna l'id in caso la chat esista, false altrimenti
	function checkSingleChatExists($prefix1, $num1, $prefix2, $num2, $localChatVersion){
		$query = "SELECT id, vers FROM singlechat WHERE prefix1=? AND num1=? AND prefix2=? AND num2=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$response = array(
				'errorCode' => '',
				'chatExists' => false,
				'idChat' => '',
				'syncChatRequired' => '',
				'chatVersion' => ''
		);

		$stmt->bind_param('ssss', $prefix1, $num1, $prefix2, $num2);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($sChatId, $sVersChat);
			$stmt->fetch();
			$stmt->close();
			$response['errorCode'] = 'OK';
			$response['chatExists'] = true;
			$response['idChat'] = $sChatId;
			$response['chatVersion'] = $sVersChat;
			if($sVersChat == $localChatVersion){
				$response['syncChatRequired'] = false;
			}
			else{
				$response['syncChatRequired'] = true;
			}

			return $response;
		}
		else{
			$stmt->bind_param('ssss', $prefix2, $num2, $prefix1, $num1);

			if(!$stmt->execute()){
				$stmt->close();
				return false;
			}

			$stmt->store_result();

			if($stmt->num_rows == 1){
				$stmt->bind_result($sChatId, $sVersChat);
				$stmt->fetch();
				$stmt->close();
				$response['errorCode'] = 'OK';
				$response['chatExists'] = true;
				$response['idChat'] = $sChatId;
				$response['chatVersion'] = $sVersChat;
				if($sVersChat == $localChatVersion){
					$response['syncChatRequired'] = false;
				}
				else{
					$response['syncChatRequired'] = true;
				}
				return $response;
			}

			$stmt->close();
			$response['errorCode'] = 'OK';
			$response['chatExists'] = false;
			return $response;
		}
	}

	//Crea una chat tra i due numeri richiesti e ritorna l'id della chat appena creata
	function createNewSingleChat($prefix1, $num1, $prefix2, $num2){
		$query = "INSERT INTO singlechat(vers, prefix1, num1, prefix2, num2) VALUES (?, ?, ?, ?, ?)";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$response = array(
				'errorCode' => '',
				'idChat' => ''
		);

		$vers = md5("" . $prefix1 . $num1 . $prefix2 . $num2 . time());

		$stmt->bind_param('sssss', $vers, $prefix1, $num1, $prefix2, $num2);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->affected_rows == 1){
			$newChatId = $stmt->insert_id;
			$stmt->close();
			$response['errorCode'] = 'OK';
			$response['idChat'] = $newChatId;
			return $response;
		}
		else{
			$stmt->close();
			return false;
		}
	}

	//Ritorna 0 se sender è inserito come user1, 1 se sender è inserito come user 2
	function getDirectionMessage($idChat, $senderPrefix, $senderNum){
		$query = "SELECT * FROM singlechat WHERE id=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$response = array(
				'errorCode' => '',
				'direction' => ''
		);

		$stmt->bind_param('i', $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($sId, $sVers, $sPrefix1, $sNum1, $sPrefix2, $sNum2);
			$stmt->fetch();
			$stmt->close();
			if(($senderPrefix == $sPrefix1) && ($senderNum == $sNum1)){
				$response['errorCode'] = 'OK';
				$response['direction'] = '0';
			}
			elseif (($senderPrefix == $sPrefix2) && ($senderNum == $sNum2)){
				$response['errorCode'] = 'OK';
				$response['direction'] = '1';
			}

			return $response;
		}
		else{
			$stmt->close();
			$response['errorCode'] = 'KO';
			return $response;
		}
	}

	//Crea un nuovo messaggio tra i due utenti richiesti e ne ritorna l'id, in caso positivo aggiorna anche la versione della chat
	function createNewSingleChatMessage($idChat, $direction, $msg, $type, $messageTag){

		$response = array(
				'errorCode' => '',
				'idNewMessage' => '',
				'dataNewMessage' => '',
				'statusNewMessage' => '',
				'chatVersionChanged' => false,
				'newChatVersion' => '',
				'messageTag' => ''
		);

		$query = "SELECT * FROM singlechatmessages WHERE idchat=? AND direction=? AND messageTag=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('iss', $idChat, $direction, $messageTag);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){

			$stmt->bind_result($sId, $sIdChat, $sDirection, $sMsg, $sStatus, $sData, $sType, $sMessageTag);
			$stmt->fetch();
			$stmt->close();
			$response['errorCode'] = 'OK';
			$response['idNewMessage'] = $sId;
			$response['dataNewMessage'] = $sData;
			$response['statusNewMessage'] = $sStatus;
			$response['messageTag'] = $sMessageTag;

			return $response;
		}

		$query = "INSERT INTO singlechatmessages (idchat, direction, msg, status, data, type, messageTag) VALUES (?, ?, ?, ?, ?, ?, ?);";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}




		$time =  $this->getMillis();
		$status = '1';
		$stmt->bind_param('isssiss', $idChat, $direction, $msg, $status, $time , $type, $messageTag);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->affected_rows == 1){
			$newMessageId = $stmt->insert_id;
			$stmt->close();
			$response['errorCode'] = 'OK';
			$response['idNewMessage'] = $newMessageId;
			$response['dataNewMessage'] = $time;
			$response['statusNewMessage'] = $status;
			$response['messageTag'] = $messageTag;

			{
				$query = "SELECT vers FROM singlechat WHERE id=?;";
				if(!$stmt = $this->connection->prepare($query)){
					$stmt->close();
					return $response;
				}

				$stmt->bind_param('i', $idChat);

				if(!$stmt->execute()){
					return $response;
				}

				$stmt->store_result();

				if($stmt->num_rows == 1){
					$stmt->bind_result($sVersion);
					$stmt->fetch();
					$stmt->close();
				}
				else{
					$stmt->close();
					return $response;
				}

				$query = "UPDATE singlechat SET vers=? WHERE id=?;";
				if(!$stmt = $this->connection->prepare($query)){
					$stmt->close();
					return $response;
				}

				$newChatVersion = md5("" . $sVersion . time());

				$stmt->bind_param('si', $newChatVersion, $idChat);

				if(!$stmt->execute()){
					$stmt->close();
					return $response;
				}

				if($stmt->affected_rows == 1){
					$stmt->close();
					$response['chatVersionChanged'] = true;
					$response['newChatVersion'] = $newChatVersion;
					return $response;
				}
				else{
					$stmt->close();
					return $response;
				}
			}

			return $response;
		}
		else{
			return false;
		}

	}

	//Aggiorna tabella utente, modificando l'immagine del profilo e la data di modifica.
	function changeUserImage($sessid, $imgSrc, $data){
		$query = "SELECT * FROM user WHERE sessid=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$response = array(
				'errorCode' => '',
				'oldImageSrc' => ''
		);


		$stmt->bind_param('s', $sessid);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){

			$stmt->bind_result($sPrefix, $sNum, $sEmail, $sSessionId, $sGcmId, $sImgSrc, $sDataImg);
			$stmt->fetch();
			$stmt->close();
			$response['oldImageSrc'] = $sImgSrc;

			$query = "UPDATE user SET imgSrc=?, dataImg=? WHERE sessid=?;";
			if(!$stmt = $this->connection->prepare($query)){
				$stmt->close();
				return false;
			}


			$stmt->bind_param('sis', $imgSrc, $data, $sessid);

			if(!$stmt->execute()){
				$stmt->close();
				return false;
			}

			if($stmt->affected_rows == 1){
				$stmt->close();
				$response['errorCode'] = 'OK';
					
				return $response;
			}
			else{
				$stmt->close();
				return false;
			}

		}
		else{
			$stmt->close();
			return false;
		}


	}

	//Ritorna tutti i messaggi relativi a una conversazione
	function getConversationMessages($idChat, $myPrefix, $myNum, $myDirection){
		$response = array(
				'errorCode' => '',
				'numMessages' => 0,
				'messages' => array()
		);

		$query = "SELECT direction, msg, status, data, type, messageTag FROM singlechatmessages WHERE idChat=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('i', $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows > 0){

			$response['errorCode'] = 'OK';
			$response['numMessages'] = $stmt->num_rows;
			$stmt->bind_result($sDirection, $sMsg, $sStatus, $sData, $sType, $sMessageTag);


			while($stmt->fetch()) {
				if($myDirection == '1'){
					if($sDirection == '0'){
						$sDirection = '1';
					}
					else{
						$sDirection = '0';
					}
				}
				$message = new SingleChatMessage('', $idChat, $sDirection, $sMsg, $sStatus, $sData, $sType, $sMessageTag);
				$response['messages'][] = $message;
			}

			return $response;
		}
		else{
			$response['errorCode'] = 'OK';

			return $response;
		}
	}

	//Modifica lo stato del messaggio richiesto
	function updateMessageStatus($idChat, $messageDirection, $messageData, $messageTag, $newStatus){
		$response = array(
				'errorCode' => '',
				'debugInfo' => '',
				'messageStatusUpdated' => false
		);

		$query = "SELECT id, status FROM singlechatmessages WHERE idChat=? AND direction=? AND data=? AND messageTag=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('isis', $idChat, $messageDirection, $messageData, $messageTag);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows > 0){


			$response['errorCode'] = 'OK';
			$stmt->bind_result($sId, $sStatus);
			$stmt->fetch();

			switch($sStatus){
				case '1':
					if($newStatus == "3"){
						$query = "UPDATE singlechatmessages SET status=? WHERE idChat=? AND direction=? AND data=? AND messageTag=?;";
						if(!$stmt = $this->connection->prepare($query)){
							$stmt->close();
							return false;
						}

						$stmt->bind_param('sisis', $newStatus, $idChat, $messageDirection, $messageData, $messageTag);

						if(!$stmt->execute()){
							$stmt->close();
							return false;
						}

						$stmt->store_result();

						if($stmt->affected_rows > 0){
							$response['messageStatusUpdated'] = true;

							// DA IMPLEMENTARE: aggiornare versione chat
							{
								$query = "SELECT vers FROM singlechat WHERE id=?;";
								if(!$stmt = $this->connection->prepare($query)){
									$stmt->close();
									break;
								}
									
								$stmt->bind_param('i', $idChat);
									
								if(!$stmt->execute()){
									break;
								}
									
								$stmt->store_result();
									
								if($stmt->num_rows == 1){
									$stmt->bind_result($sVersion);
									$stmt->fetch();
									$stmt->close();
								}
								else{
									$stmt->close();
									break;
								}
									
								$query = "UPDATE singlechat SET vers=? WHERE id=?;";
								if(!$stmt = $this->connection->prepare($query)){
									$stmt->close();
									break;
								}
									
								$newChatVersion = md5("" . $sVersion . time());
									
								$stmt->bind_param('si', $newChatVersion, $idChat);
									
								if(!$stmt->execute()){
									$stmt->close();
									break;
								}
									
								if($stmt->affected_rows == 1){
									$stmt->close();
									break;
								}
								else{
									$stmt->close();
									break;
								}
							}

						}
					}

					break;
				case '3':
					if($newStatus == "5"){
						$query = "UPDATE singlechatmessages SET status=? WHERE idChat=? AND direction=? AND data=? AND messageTag=?;";
						if(!$stmt = $this->connection->prepare($query)){
							$stmt->close();
							return false;
						}

						$stmt->bind_param('sisis', $newStatus, $idChat, $messageDirection, $messageData, $messageTag);

						if(!$stmt->execute()){
							$stmt->close();
							return false;
						}

						$stmt->store_result();

						if($stmt->affected_rows > 0){
							$response['messageStatusUpdated'] = true;

							$query = "DELETE FROM singlechatmessages WHERE  idChat=? AND direction=? AND data=? AND messageTag=?;";
							if(!$stmt = $this->connection->prepare($query)){
								$stmt->close();
								break;
							}

							$stmt->bind_param('isis', $idChat, $messageDirection, $messageData, $messageTag);

							if(!$stmt->execute()){
								$stmt->close();
								break;
							}
						}
					}

				case '5':
					$query = "DELETE FROM singlechatmessages WHERE  idChat=? AND direction=? AND data=? AND messageTag=?;";
					if(!$stmt = $this->connection->prepare($query)){
						$stmt->close();
						break;
					}

					$stmt->bind_param('isis', $idChat, $messageDirection, $messageData, $messageTag);

					if(!$stmt->execute()){
						$stmt->close();
						break;
					}

					break;

				default:
					$response['errorCode'] = 'KO';
					break;
			}

		}
		else{
			$response['errorCode'] = 'KO';
			return $response;
		}

		return $response;

	}

	//-----------------------------      OK    ---------------------------------------------

	// Inserisce l'errore ricevuto
	function insertError($prefix, $num, $tag, $info){
		$query = "INSERT INTO errors(prefix, num, tag, info) VALUES (?, ?, ?, ?)";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}
		
		$response = array(
				'errorCode' => ''
		);
		
		$stmt->bind_param('ssss', $prefix, $num, $tag, $info);
		
		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}
		
		$stmt->store_result();
		
		if($stmt->affected_rows == 1){
			$stmt->close();
			$response['errorCode'] = 'OK';
			return $response;
		}
		else{
			$stmt->close();
			return false;
		}
	}


	// Ritorna tutte le chat del numero richiesto
	function getAllChats($prefix, $num){
		$response = array(
				'errorCode' => '',
				'numChats' => 0,
				'chatsInfo' => array()
		);

		$query = "SELECT vers, prefix2, num2 FROM singlechat WHERE prefix1=? AND num1=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows > 0){

			$response['numChats'] = $stmt->num_rows;
			$stmt->bind_result($sVers, $sPrefixDest, $sNumDest);


			while($stmt->fetch()) {
				$chat = array(
						'version' => $sVers,
						'prefixDest' => $sPrefixDest,
						'numDest' => $sNumDest
				);
					
				$response['chatsInfo'][] = $chat;
			}
		}

		$query = "SELECT vers, prefix1, num1 FROM singlechat WHERE prefix2=? AND num2=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows > 0){

			$response['numChats'] += $stmt->num_rows;
			$stmt->bind_result($sVers, $sPrefixDest, $sNumDest);


			while($stmt->fetch()) {
				$chat = array(
						'version' => $sVers,
						'prefixDest' => $sPrefixDest,
						'numDest' => $sNumDest
				);
					
				$response['chatsInfo'][] = $chat;
			}
		}

		return $response;

	}

	//Controlla se un utente è in attesa di loggarsi sul terminale Android
	function checkUserIsLogging($prefix, $num){
		$query = "SELECT * FROM userlogin WHERE prefix=? AND num=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->close();
			return true;
		}
		else{
			$stmt->close();
			return false;
		}
	}




	//Aggiorna il valore della sessione di GCM associato alla id di sessione richiesto.
	function updateGcmSessId($sessid, $newGcmid){
		$query = "UPDATE user SET gcmid=? WHERE sessid=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $newGcmid, $sessid);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->close();
			return true;
		}
		else{
			$stmt->close();
			return false;
		}
	}

	//Ritorna il valore di session id associato al numero di telefono richiesto
	function getSessionId($prefix, $num){
		$query = "SELECT sessid FROM user WHERE prefix=? AND num=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ss', $prefix, $num);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($searchedSessionId);
			$stmt->fetch();
			$stmt->close();
			return $searchedSessionId;
		}
		else{
			$stmt->close();
			return false;
		}
	}




	//Ritorna l'oggetto SingleChat relativo all'id richiesto, false altrimenti.
	function getSingleChatInfo($id){
		$query = "SELECT * FROM singlechat WHERE id=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('i', $id);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($sId, $sVers, $sPrefix1, $sNum1, $sPrefix2, $sNum2);
			$stmt->fetch();
			$stmt->close();
			$searchedChat = new SingleChat($sId, $sVers, $sPrefix1, $sNum1, $sPrefix2, $sNum2);
			return $searchedChat;
		}
		else{
			$stmt->close();
			return false;
		}
	}



	//Ritorna l'oggetto SingleChatMessage relativo all'id e all'idchat richiesti, false se il messaggio non esiste.
	function getSingleChatMessageInfo($id,$idChat){
		$query = "SELECT * FROM singlechatmessages WHERE id=? AND idchat=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ii', $id, $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($sId, $sIdChat, $sDirection, $smsg, $sStatus, $sData, $sType);
			$stmt->fetch();
			$stmt->close();
			$searchedSingleChatMessage = new SingleChatMessage($sId, $sIdChat, $sDirection, $sMsg, $sStatus, $sData, $sType);
			return $searchedSingleChatMessage;
		}
		else{
			$stmt->close();
			return false;
		}
	}

	//Aggiorna la versione di una chat in seguito ad un evento che ne modifica lo stato.
	function generateNewChatVersion($idChat){
		$query = "SELECT vers FROM singlechat WHERE id=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('i', $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->num_rows == 1){
			$stmt->bind_result($sVersion);
			$stmt->fetch();
			$stmt->close();
		}
		else{
			$stmt->close();
			return false;
		}

		$query = "UPDATE singlechat SET vers=? WHERE id=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$newChatVersion = md5("" . $sVersion . time());

		$stmt->bind_param('si', $newChatVersion, $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		if($stmt->affected_rows == 1){
			$stmt->close();
			return true;
		}
		else{
			$stmt->close();
			return false;
		}

	}

	//Rimuove il messaggio indicato dall'id e idChat
	function removeSingleChatMessage($id, $idChat){
		$query = "DELETE FROM singlechatmessages WHERE id=? AND idChat=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}

		$stmt->bind_param('ii', $id, $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->affected_rows == 1){
			$stmt->close();
			return true;
		}
		else{
			$stmt->close();
			return false;
		}
	}

	//Cambia lo stato corrente di un messaggio, aggiornando anche la versione della chat
	function changeSingleChatMessageStatus($id, $idChat, $newStatus){
		$query = "UPDATE singlechatmessages SET status=? WHERE id=? AND idchat=?;";
		if(!$stmt = $this->connection->prepare($query)){
			$stmt->close();
			return false;
		}
		$stmt->bind_param('iii', $newStatus, $id, $idChat);

		if(!$stmt->execute()){
			$stmt->close();
			return false;
		}

		$stmt->store_result();

		if($stmt->affected_rows == 1){
			$stmt->close();
			$this->generateNewChatVersion($idChat);
			return true;
		}
		else{
			$stmt->close();
			return false;
		}
	}





}

?>
























