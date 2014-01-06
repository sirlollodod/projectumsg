<?php


function sendEmail($emailAddress, $smsCode, $emailCode){
	mail($emailAddress, "Verification codes", "SMSCODE: '" . $smsCode . "'\nEMAILCODE: '" . $emailCode . "'", "From: UMessage Service");
}

?>