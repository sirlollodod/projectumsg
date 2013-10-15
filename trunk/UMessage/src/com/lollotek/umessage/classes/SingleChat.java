package com.lollotek.umessage.classes;

public class SingleChat {

	private String idChat, version, prefixDest, numDest, idLastMessage;

	public SingleChat() {
		this.idChat = "";
		this.version = "";
		this.prefixDest = "";
		this.numDest = "";
		this.idLastMessage = "";
	}

	public SingleChat(String idChat, String version, String prefixDest,
			String numDest, String idLastMessage) {
		super();
		this.idChat = idChat;
		this.version = version;
		this.prefixDest = prefixDest;
		this.numDest = numDest;
		this.idLastMessage = idLastMessage;
	}

	public String getIdChat() {
		return idChat;
	}

	public void setIdChat(String idChat) {
		this.idChat = idChat;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPrefixDest() {
		return prefixDest;
	}

	public void setPrefixDest(String prefixDest) {
		this.prefixDest = prefixDest;
	}

	public String getNumDest() {
		return numDest;
	}

	public void setNumDest(String numDest) {
		this.numDest = numDest;
	}

	public String getIdLastMessage() {
		return idLastMessage;
	}

	public void setIdLastMessage(String idLastMessage) {
		this.idLastMessage = idLastMessage;
	}

}