package com.lollotek.umessage.classes;

public class TempSingleChatMessage {

	private String idMessage, idChat, data, type, message;

	public TempSingleChatMessage() {
		this.idMessage = "";
		this.idChat = "";
		this.message = "";
		this.data = "";
		this.type = "";
	}

	public TempSingleChatMessage(String idMessage, String idChat, String data,
			String type, String message) {
		super();
		this.idMessage = idMessage;
		this.idChat = idChat;
		this.data = data;
		this.type = type;
		this.message = message;
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

}
