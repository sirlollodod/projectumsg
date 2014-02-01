package com.lollotek.umessage.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.os.Bundle;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class ConfigurationManager {

	private static final String TAG = ConfigurationManager.class.getName()
			+ ":\n";

	private static Configuration mConfiguration = null;
	private static boolean mConfigurationToReload = false;

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

	public static synchronized Bundle getValues() {
		checkConfigurationReady();

		return new Bundle();
	}

}
