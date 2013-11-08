<?php

class User{

	private $prefix, $num, $email, $sessid, $gcmid;

	function __construct($prefix, $num, $email, $sessid, $gcmid){
		$this->prefix = $prefix;
		$this->num = $num;
		$this->email = $email;
		$this->sessid = $sessid;
		$this->gcmid = $gcmid;
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

	public function getEmail()
	{
		return $this->email;
	}

	public function setEmail($email)
	{
		$this->email = $email;
	}

	public function getSessid()
	{
		return $this->sessid;
	}

	public function setSessid($sessid)
	{
		$this->sessid = $sessid;
	}

	public function getGcmid()
	{
		return $this->gcmid;
	}

	public function setGcmid($gcmid)
	{
		$this->gcmid = $gcmid;
	}
}


?>