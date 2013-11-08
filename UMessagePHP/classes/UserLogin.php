<?php

class UserLogin{

	private $prefix, $num, $emailver, $smsver;

	function __construct($prefix, $num, $emailver, $smsver){
		$this->prefix = $prefix;
		$this->num = $num;
		$this->emailver = $emailver;
		$this->smsver = $smsver;
	}






	public function getPrefix()
	{
		return $this->prefix;
	}

	public function setPrefix($prefix)
	{
		$this->prefix = $prefix;
	}

	public function getNum()
	{
		return $this->num;
	}

	public function setNum($num)
	{
		$this->num = $num;
	}

	public function getEmailver()
	{
		return $this->emailver;
	}

	public function setEmailver($emailver)
	{
		$this->emailver = $emailver;
	}

	public function getSmsver()
	{
		return $this->smsver;
	}

	public function setSmsver($smsver)
	{
		$this->smsver = $smsver;
	}
}

?>