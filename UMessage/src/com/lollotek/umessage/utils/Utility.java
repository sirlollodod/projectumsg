package com.lollotek.umessage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.lollotek.umessage.Configuration;

public class Utility {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Carica la configurazione dal file specificato.
	 * 
	 * @param configFile
	 *            puntatore al file da cui caricare la configurazione.
	 * @return la configurazione caricata da file se disponibile, null
	 *         altrimenti.
	 */
	public static Configuration loadConfiguration(File configFile) {
		Configuration configuration;
		try {
			ObjectInputStream inputStream = new ObjectInputStream(
					new FileInputStream(configFile));
			configuration = (Configuration) inputStream.readObject();
			inputStream.close();
			return configuration;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Salva su file la configurazione attuale.
	 * 
	 * @param configuration
	 *            puntatore dell'oggetto da salvare su file.
	 * @param configFile
	 *            puntatore al file in cui salvare la configurazione.
	 * @return true se configurazione salvata con successo, false altrimenti.
	 */
	public static boolean saveConfiguration(Configuration configuration,
			File configFile) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(
					new FileOutputStream(configFile));
			outputStream.writeObject(configuration);
			outputStream.close();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public static boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			/*
			 * if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
			 * GooglePlayServicesUtil.getErrorDialog(resultCode, this,
			 * PLAY_SERVICES_RESOLUTION_REQUEST).show(); } else { Log.i(TAG,
			 * "This device is not supported."); finish(); }
			 */
			return false;
		}
		return true;
	}

	public static Configuration getConfiguration(Context context) {
		File configurationFile = new File(context.getFilesDir() + "/"
				+ Settings.CONFIG_FILE_NAME);
		Configuration configuration = loadConfiguration(configurationFile);
		if (configuration == null) {
			configuration = new Configuration();
			Utility.saveConfiguration(configuration, configurationFile);
		}

		return configuration;
	}

	public static void setConfiguration(Context context, Configuration configuration){
		File configurationFile = new File(context.getFilesDir() + "/"
				+ Settings.CONFIG_FILE_NAME);
		saveConfiguration(configuration, configurationFile);
	}
}
