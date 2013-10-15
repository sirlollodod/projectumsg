package com.lollotek.umessage.classes;

public class User {

	private String prefix, num, name;

	public User() {
		this.prefix = "";
		this.num = "";
		this.name = "";
	}

	public User(String prefix, String num, String name) {
		super();
		this.prefix = prefix;
		this.num = num;
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
