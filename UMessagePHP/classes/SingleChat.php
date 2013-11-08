<?php

class SingleChat{

	private $id, $vers, $prefix1, $num1, $prefix2, $num2;

	function __construct($id, $vers, $prefix1, $num1, $prefix2, $num2){
		$this->id = id;
		$this->vers = $vers;
		$this->prefix1 = $prefix1;
		$this->num1 = $num1;
		$this->prefix2 = $prefix2;
		$this->num2 = $num2;
	}




	public function getId()
	{
		return $this->id;
	}

	public function setId($id)
	{
		$this->id = $id;
	}

	public function getVers()
	{
		return $this->vers;
	}

	public function setVers($vers)
	{
		$this->vers = $vers;
	}

	public function getPrefix1()
	{
		return $this->prefix1;
	}

	public function setPrefix1($prefix1)
	{
		$this->prefix1 = $prefix1;
	}

	public function getNum1()
	{
		return $this->num1;
	}

	public function setNum1($num1)
	{
		$this->num1 = $num1;
	}

	public function getPrefix2()
	{
		return $this->prefix2;
	}

	public function setPrefix2($prefix2)
	{
		$this->prefix2 = $prefix2;
	}

	public function getNum2()
	{
		return $this->num2;
	}

	public function setNum2($num2)
	{
		$this->num2 = $num2;
	}


}



?>