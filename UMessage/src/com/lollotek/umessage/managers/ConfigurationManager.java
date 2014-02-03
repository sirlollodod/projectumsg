package com.lollotek.umessage.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.os.Bundle;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class ConfigurationManager {

	private static final String TAG = ConfigurationManager.class.getName()
			+ ":\n";

	private static Configuration mConfiguration = null;
	private static boolean mConfigurationToReload = false;

	public static final String PREFIX = "PREFIX";
	public static final String NUM = "NUM";
	public static final String SESSION_ID = "SESSION_ID";
	public static final String GCM_ID = "GCM_ID";
	public static final String SIM_SERIAL = "SIM_SERIAL";
	public static final String EMAIL = "EMAIL";
	public static final String OLD_PREFIX = "OLD_PREFIX";
	public static final String OLD_NUM = "OLD_NUM";
	public static final String FIRST_EXECUTION_APP = "FIRST_EXECUTION_APP";
	public static final String SIM_IS_LOGGING = "SIM_IS_LOGGING";
	public static final String PROFILE_IMAGE_TO_UPLOAD = "PROFILE_IMAGE_TO_UPLOAD";
	public static final String LAST_DATA_DUMP_DB = "LAST_DATA_DUMP_DB";

	private static boolean checkConfigurationReady() {
		if ((mConfiguration == null) || (mConfigurationToReload)) {
			File configurationFile = new File(UMessageApplication.getContext()
					.getFilesDir() + "/" + Settings.CONFIG_FILE_NAME);
			try {
				if (!configurationFile.isFile()) {
					mConfiguration = new Configuration();
					ObjectOutputStream outputStream = new ObjectOutputStream(
							new FileOutputStream(configurationFile));
					outputStream.writeObject(mConfiguration);
					outputStream.close();
				} else {
					ObjectInputStream inputStream = new ObjectInputStream(
							new FileInputStream(configurationFile));
					mConfiguration = (Configuration) inputStream.readObject();
					inputStream.close();
				}
			} catch (Exception e) {
				Utility.reportError(UMessageApplication.getContext(), e, TAG
						+ " checkConfigurationReady()");
				return false;
			}

			mConfigurationToReload = false;
		}

		return true;

	}

	private static boolean writeConfigurationOnFile() {
		File configurationFile = new File(UMessageApplication.getContext()
				.getFilesDir() + "/" + Settings.CONFIG_FILE_NAME);

		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(
					new FileOutputStream(configurationFile));
			outputStream.writeObject(mConfiguration);
			outputStream.close();
		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ " checkConfigurationReady()");
			return false;
		}

		return true;

	}

	public static synchronized Bundle getValues(Bundle valuesWanted) {
		if (!checkConfigurationReady()) {
			return new Bundle();
		}

		Bundle values = new Bundle();

		if (valuesWanted.getBoolean(PREFIX, false)) {
			values.putString(PREFIX, mConfiguration.prefix);
		}

		if (valuesWanted.getBoolean(NUM, false)) {
			values.putString(NUM, mConfiguration.num);
		}

		if (valuesWanted.getBoolean(SESSION_ID, false)) {
			values.putString(SESSION_ID, mConfiguration.sessId);
		}

		if (valuesWanted.getBoolean(GCM_ID, false)) {
			values.putString(GCM_ID, mConfiguration.gcmId);
		}

		if (valuesWanted.getBoolean(SIM_SERIAL, false)) {
			values.putString(SIM_SERIAL, mConfiguration.simSerial);
		}

		if (valuesWanted.getBoolean(EMAIL, false)) {
			values.putString(EMAIL, mConfiguration.email);
		}

		if (valuesWanted.getBoolean(OLD_PREFIX, false)) {
			values.putString(OLD_PREFIX, mConfiguration.oldPrefix);
		}

		if (valuesWanted.getBoolean(OLD_NUM, false)) {
			values.putString(OLD_NUM, mConfiguration.oldNum);
		}

		if (valuesWanted.getBoolean(FIRST_EXECUTION_APP, false)) {
			values.putBoolean(FIRST_EXECUTION_APP,
					mConfiguration.firstExecutionApp);
		}
		if (valuesWanted.getBoolean(SIM_IS_LOGGING, false)) {
			values.putBoolean(SIM_IS_LOGGING, mConfiguration.simIsLogging);
		}
		if (valuesWanted.getBoolean(PROFILE_IMAGE_TO_UPLOAD, false)) {
			values.putBoolean(PROFILE_IMAGE_TO_UPLOAD,
					mConfiguration.profileImageToUpload);
		}
		if (valuesWanted.getBoolean(LAST_DATA_DUMP_DB, false)) {
			values.putLong(LAST_DATA_DUMP_DB, mConfiguration.lastDataDumpDB);
		}

		return values;
	}

	public static synchronized boolean saveValues(Bundle valuesToUpdate) {
		if (!checkConfigurationReady()) {
			return false;
		}

		if (valuesToUpdate.containsKey(PREFIX)) {
			mConfiguration.prefix = valuesToUpdate.getString(PREFIX, "");
		}

		if (valuesToUpdate.containsKey(NUM)) {
			mConfiguration.num = valuesToUpdate.getString(NUM, "");
		}

		if (valuesToUpdate.containsKey(SESSION_ID)) {
			mConfiguration.sessId = valuesToUpdate.getString(SESSION_ID, "");
		}

		if (valuesToUpdate.containsKey(GCM_ID)) {
			mConfiguration.gcmId = valuesToUpdate.getString(GCM_ID, "");
		}

		if (valuesToUpdate.containsKey(SIM_SERIAL)) {
			mConfiguration.simSerial = valuesToUpdate.getString(SIM_SERIAL, "");
		}

		if (valuesToUpdate.containsKey(EMAIL)) {
			mConfiguration.email = valuesToUpdate.getString(EMAIL, "");
		}

		if (valuesToUpdate.containsKey(OLD_PREFIX)) {
			mConfiguration.oldPrefix = valuesToUpdate.getString(OLD_PREFIX, "");
		}

		if (valuesToUpdate.containsKey(OLD_NUM)) {
			mConfiguration.oldNum = valuesToUpdate.getString(OLD_NUM, "");
		}

		if (valuesToUpdate.containsKey(FIRST_EXECUTION_APP)) {
			mConfiguration.firstExecutionApp = valuesToUpdate.getBoolean(
					FIRST_EXECUTION_APP, false);
		}

		if (valuesToUpdate.containsKey(SIM_IS_LOGGING)) {
			mConfiguration.simIsLogging = valuesToUpdate.getBoolean(
					SIM_IS_LOGGING, false);
		}

		if (valuesToUpdate.containsKey(PROFILE_IMAGE_TO_UPLOAD)) {
			mConfiguration.profileImageToUpload = valuesToUpdate.getBoolean(
					PROFILE_IMAGE_TO_UPLOAD, false);
		}

		if (valuesToUpdate.containsKey(LAST_DATA_DUMP_DB)) {
			mConfiguration.lastDataDumpDB = valuesToUpdate.getLong(
					LAST_DATA_DUMP_DB, 0);
		}

		// Qui valuesToReload = true ???
		if (writeConfigurationOnFile()) {
			return false;
		}

		return true;
	}

}

class Configuration implements java.io.Serializable {

	/**
	 * Autogenerato
	 */
	private static final long serialVersionUID = 1L;

	public String prefix, num, sessId, gcmId, simSerial, email, oldPrefix,
			oldNum;
	public boolean firstExecutionApp, simIsLogging, profileImageToUpload;
	public long lastDataDumpDB;

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
		this.lastDataDumpDB = 0;

	}

}
