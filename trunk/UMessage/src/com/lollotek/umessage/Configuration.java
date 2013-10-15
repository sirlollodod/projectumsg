package com.lollotek.umessage;

public class Configuration implements java.io.Serializable {

	/**
	 * Autogenerato
	 */
	private static final long serialVersionUID = 1L;

	private String prefix, num, sessId, gcmId, simSerial;
	private boolean firstExecutionApp, simIsLogging;

	public Configuration() {
		super();
		this.prefix = "";
		this.num = "";
		this.sessId = "";
		this.gcmId = "";
		this.simSerial = "";
		this.firstExecutionApp = true;
		this.simIsLogging = false;
	}

	public Configuration(String prefix, String num, String sessId, String gcmId,
			String simSerial, boolean firstExecutionApp, boolean simIsLogging) {
		super();
		this.prefix = prefix;
		this.num = num;
		this.sessId = sessId;
		this.gcmId = gcmId;
		this.simSerial = simSerial;
		this.firstExecutionApp = firstExecutionApp;
		this.simIsLogging = simIsLogging;
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

	public String getSessid() {
		return sessId;
	}

	public void setSessid(String sessid) {
		this.sessId = sessid;
	}

	public String getGcmid() {
		return gcmId;
	}

	public void setGcmid(String gcmid) {
		this.gcmId = gcmid;
	}

	public String getSimserial() {
		return simSerial;
	}

	public void setSimserial(String simserial) {
		this.simSerial = simserial;
	}

	public boolean isFirstExecutionApp() {
		return firstExecutionApp;
	}

	public void setFirstExecutionApp(boolean firstExecutionApp) {
		this.firstExecutionApp = firstExecutionApp;
	}

	public boolean isSimIsLogging() {
		return simIsLogging;
	}

	public void setSimIsLogging(boolean simIsLogging) {
		this.simIsLogging = simIsLogging;
	}

}
