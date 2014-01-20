package com.lollotek.umessage;

public class Configuration implements java.io.Serializable {

	/**
	 * Autogenerato
	 */
	private static final long serialVersionUID = 1L;

	private String prefix, num, sessId, gcmId, simSerial, email, oldPrefix,
			oldNum;
	private boolean firstExecutionApp, simIsLogging, profileImageToUpload;
	private long lastDataDumpDB;

	public Configuration() {
		super();
		this.prefix = "";
		this.num = "";
		this.sessId = "";
		this.gcmId = "";
		this.simSerial = "";
		this.firstExecutionApp = true;
		this.simIsLogging = false;
		this.profileImageToUpload = false;
		this.oldPrefix = "";
		this.oldNum = "";
		this.setLastDataDumpDB(0);

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isProfileImageToUpload() {
		return profileImageToUpload;
	}

	public void setProfileImageToUpload(boolean profileImateToUpload) {
		this.profileImageToUpload = profileImateToUpload;
	}

	public String getOldPrefix() {
		return oldPrefix;
	}

	public void setOldPrefix(String oldPrefix) {
		this.oldPrefix = oldPrefix;
	}

	public String getOldNum() {
		return oldNum;
	}

	public void setOldNum(String oldNum) {
		this.oldNum = oldNum;
	}

	public long getLastDataDumpDB() {
		return lastDataDumpDB;
	}

	public void setLastDataDumpDB(long lastDataDumpDB) {
		this.lastDataDumpDB = lastDataDumpDB;
	}

}
