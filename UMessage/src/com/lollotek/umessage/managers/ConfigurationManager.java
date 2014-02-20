package com.lollotek.umessage.managers;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.lollotek.umessage.UMessageApplication;

public class ConfigurationManager {

	private static final String TAG = ConfigurationManager.class.getName()
			+ ":\n";

	private static final String SHARED_PREFS_CONFIGURATION_MANAGER = "CONFIGURATION_MANAGER_PREFS";

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

	private static boolean checkSharedPrefReady() {
		File sharedPrefsConfigurationManager = new File(
				"/data/data/com.lollotek.umessage/shared_prefs/"
						+ SHARED_PREFS_CONFIGURATION_MANAGER + ".xml");

		if (!sharedPrefsConfigurationManager.isFile()) {
			SharedPreferences prefs = UMessageApplication.getContext()
					.getSharedPreferences(SHARED_PREFS_CONFIGURATION_MANAGER,
							UMessageApplication.getContext().MODE_PRIVATE);
			Editor edit = prefs.edit();

			edit.putString(PREFIX, "");
			edit.putString(NUM, "");
			edit.putString(SESSION_ID, "");
			edit.putString(GCM_ID, "");
			edit.putString(SIM_SERIAL, "");
			edit.putString(EMAIL, "");
			edit.putString(OLD_PREFIX, "");
			edit.putString(OLD_NUM, "");
			edit.putBoolean(FIRST_EXECUTION_APP, true);
			edit.putBoolean(SIM_IS_LOGGING, false);
			edit.putBoolean(PROFILE_IMAGE_TO_UPLOAD, false);
			edit.putLong(LAST_DATA_DUMP_DB, 0);

			edit.commit();
		}

		return true;
	}

	public static synchronized Bundle getValues(Bundle valuesWanted) {
		checkSharedPrefReady();

		SharedPreferences prefs = UMessageApplication.getContext()
				.getSharedPreferences(SHARED_PREFS_CONFIGURATION_MANAGER,
						UMessageApplication.getContext().MODE_PRIVATE);

		Bundle values = new Bundle();

		if (valuesWanted.getBoolean(PREFIX, false)) {
			values.putString(PREFIX, prefs.getString(PREFIX, ""));
		}

		if (valuesWanted.getBoolean(NUM, false)) {
			values.putString(NUM, prefs.getString(NUM, ""));
		}

		if (valuesWanted.getBoolean(SESSION_ID, false)) {
			values.putString(SESSION_ID, prefs.getString(SESSION_ID, ""));
		}

		if (valuesWanted.getBoolean(GCM_ID, false)) {
			values.putString(GCM_ID, prefs.getString(GCM_ID, ""));
		}

		if (valuesWanted.getBoolean(SIM_SERIAL, false)) {
			values.putString(SIM_SERIAL, prefs.getString(SIM_SERIAL, ""));
		}

		if (valuesWanted.getBoolean(EMAIL, false)) {
			values.putString(EMAIL, prefs.getString(EMAIL, ""));
		}

		if (valuesWanted.getBoolean(OLD_PREFIX, false)) {
			values.putString(OLD_PREFIX, prefs.getString(OLD_PREFIX, ""));
		}

		if (valuesWanted.getBoolean(OLD_NUM, false)) {
			values.putString(OLD_NUM, prefs.getString(OLD_NUM, ""));
		}

		if (valuesWanted.getBoolean(FIRST_EXECUTION_APP, false)) {
			values.putBoolean(FIRST_EXECUTION_APP,
					prefs.getBoolean(FIRST_EXECUTION_APP, true));
		}
		if (valuesWanted.getBoolean(SIM_IS_LOGGING, false)) {
			values.putBoolean(SIM_IS_LOGGING,
					prefs.getBoolean(SIM_IS_LOGGING, false));
		}
		if (valuesWanted.getBoolean(PROFILE_IMAGE_TO_UPLOAD, false)) {
			values.putBoolean(PROFILE_IMAGE_TO_UPLOAD,
					prefs.getBoolean(PROFILE_IMAGE_TO_UPLOAD, false));
		}
		if (valuesWanted.getBoolean(LAST_DATA_DUMP_DB, false)) {
			values.putLong(LAST_DATA_DUMP_DB,
					prefs.getLong(LAST_DATA_DUMP_DB, 0));
		}

		return values;
	}

	public static synchronized boolean saveValues(Bundle valuesToUpdate) {
		checkSharedPrefReady();

		SharedPreferences prefs = UMessageApplication.getContext()
				.getSharedPreferences(SHARED_PREFS_CONFIGURATION_MANAGER,
						UMessageApplication.getContext().MODE_PRIVATE);
		Editor edit = prefs.edit();

		if (valuesToUpdate.containsKey(PREFIX)) {
			edit.putString(PREFIX, valuesToUpdate.getString(PREFIX, ""));
		}

		if (valuesToUpdate.containsKey(NUM)) {
			edit.putString(NUM, valuesToUpdate.getString(NUM, ""));
		}

		if (valuesToUpdate.containsKey(SESSION_ID)) {
			edit.putString(SESSION_ID, valuesToUpdate.getString(SESSION_ID, ""));
		}

		if (valuesToUpdate.containsKey(GCM_ID)) {
			edit.putString(GCM_ID, valuesToUpdate.getString(GCM_ID, ""));
		}

		if (valuesToUpdate.containsKey(SIM_SERIAL)) {
			edit.putString(SIM_SERIAL, valuesToUpdate.getString(SIM_SERIAL, ""));
		}

		if (valuesToUpdate.containsKey(EMAIL)) {
			edit.putString(EMAIL, valuesToUpdate.getString(EMAIL, ""));
		}

		if (valuesToUpdate.containsKey(OLD_PREFIX)) {
			edit.putString(OLD_PREFIX, valuesToUpdate.getString(OLD_PREFIX, ""));
		}

		if (valuesToUpdate.containsKey(OLD_NUM)) {
			edit.putString(OLD_NUM, valuesToUpdate.getString(OLD_NUM, ""));
		}

		if (valuesToUpdate.containsKey(FIRST_EXECUTION_APP)) {
			edit.putBoolean(FIRST_EXECUTION_APP,
					valuesToUpdate.getBoolean(FIRST_EXECUTION_APP, true));
		}

		if (valuesToUpdate.containsKey(SIM_IS_LOGGING)) {
			edit.putBoolean(SIM_IS_LOGGING,
					valuesToUpdate.getBoolean(SIM_IS_LOGGING, false));
		}

		if (valuesToUpdate.containsKey(PROFILE_IMAGE_TO_UPLOAD)) {
			edit.putBoolean(PROFILE_IMAGE_TO_UPLOAD,
					valuesToUpdate.getBoolean(PROFILE_IMAGE_TO_UPLOAD, false));
		}

		if (valuesToUpdate.containsKey(LAST_DATA_DUMP_DB)) {
			edit.putLong(LAST_DATA_DUMP_DB,
					valuesToUpdate.getLong(LAST_DATA_DUMP_DB, 0));
		}

		if (!edit.commit()) {
			return false;
		}

		return true;
	}

}
