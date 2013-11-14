package com.lollotek.umessage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;

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

	public static void setConfiguration(Context context,
			Configuration configuration) {
		File configurationFile = new File(context.getFilesDir() + "/"
				+ Settings.CONFIG_FILE_NAME);
		saveConfiguration(configuration, configurationFile);
	}

	public static String getSerialSim(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String simId = tm.getSimSerialNumber();
		return simId;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static JSONObject doPostRequest(String urlToOpen,
			JSONObject parameters) throws HttpException{

		JSONObject result;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		String postParams = "";
		boolean isFirst = true;
		
		
		if(!isNetworkAvailable(UMessageApplication.getContext())){
			throw new HttpException("Connessione non disponibile");
		}
		
		try {
			Iterator<String> keys = parameters.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				String value = parameters.getString(key);
				nameValuePairs.add(new BasicNameValuePair(key, value));

				postParams += (isFirst ? "" : "&") + key + "="
						+ URLEncoder.encode(value, "UTF-8");
				if (isFirst)
					isFirst = false;
			}

			URL url = new URL(urlToOpen);
			HttpURLConnection conn;

			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");

			conn.setFixedLengthStreamingMode(postParams.getBytes().length);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// send the POST out
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(postParams);
			out.close();

			String response = "";

			Scanner inStream = new Scanner(conn.getInputStream());

			while (inStream.hasNextLine())
				response += (inStream.nextLine());

			result = new JSONObject(response);

		} catch (Exception e) {
			return null;
		}

		return result;

	}
}
