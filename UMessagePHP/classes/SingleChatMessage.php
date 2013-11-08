<?php

class SingleChatMessage{

	private $id, $idchat, $direction, $msg, $status, $data, $type;
	 
	function __construct($id, $idchat, $direction, $msg, $status, $data, $type){
		$this->id = $id;
		$this->idchat = $idchat;
		$this->direction = $direction;
		$this->msg = $msg;
		$this->status = $status;
		$this->data = $data;
		$this->type = $type;
		
	}

	public function getId()
	{
	    return $this->id;
	}

	public function setId($id)
	{
	    $this->id = $id;
	}

	public function getIdchat()
	{
	    return $this->idchat;
	}

	public function setIdchat($idchat)
	{
	    $this->idchat = $idchat;
	}

	public function getDirection()
	{
	    return $this->direction;
	}

	public function setDirection($direction)
	{
	    $this->direction = $direction;
	}

	public function getMsg()
	{
	    return $this->msg;
	}

	public function setMsg($msg)
	{
	    $this->msg = $msg;
	}

	public function getStatus()
	{
	    return $this->status;
	}

	public function setStatus($status)
	{
	    $this->status = $status;
	}

	public function getData()
	{
	    return $this->data;
	}

	public function setData($data)
	{
	    $this->data = $data;
	}

	public function getType()
	{
	    return $this->type;
	}

	public function setType($type)
	{
	    $this->type = $type;
	}
}

?>