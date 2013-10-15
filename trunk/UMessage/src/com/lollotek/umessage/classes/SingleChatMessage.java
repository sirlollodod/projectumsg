package com.lollotek.umessage.classes;

public class SingleChatMessage {

	private String idMessage, idChat, direction, status, data, type, message, read;

	public SingleChatMessage() {
		this.idMessage = "";
		this.idChat = "";
		this.direction = "";
		this.message = "";
		this.status = "";
		this.data = "";
		this.type = "";
		this.read = "";
	}

	public SingleChatMessage(String idMessage, String idChat, String direction,
			String status, String data, String type, String message, String read) {
		super();
		this.idMessage = idMessage;
		this.idChat = idChat;
		this.direction = direction;
		this.status = status;
		this.data = data;
		this.type = type;
		this.message = message;
		this.read = read;;
	}

	public String getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(String idMessage) {
		this.idMessage = idMessage;
	}

	public String getIdChat() {
		return idChat;
	}

	public void setIdChat(String idChat) {
		this.idChat = idChat;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRead() {
		return read;
	}

	public void setRead(String read) {
		this.read = read;
	}

}
