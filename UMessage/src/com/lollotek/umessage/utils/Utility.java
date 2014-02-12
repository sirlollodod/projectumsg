package com.lollotek.umessage.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.HttpResponseUmsg;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.ConfigurationManager;

public class Utility {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static final String TAG = Utility.class.getName() + ":\n";

	public static boolean saveDumpDB(Context context, JSONObject dumpDB,
			File file) {

		try {
			// ObjectOutputStream outputStream = new ObjectOutputStream(
			// new FileOutputStream(dumpDBFile));
			// outputStream.writeObject(dumpDB);
			// outputStream.close();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(dumpDB.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": saveDumpDB()");

			return false;
		}

		return true;
	}

	public static boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				// GooglePlayServicesUtil.getErrorDialog(resultCode, context,
				// PLAY_SERVICES_RESOLUTION_REQUEST).show();

				// Toast.makeText(UMessageApplication.getContext(),
				// "Device compatibile... aggiornare GPSs",
				// Toast.LENGTH_SHORT).show();
			} else {
				// Toast.makeText(UMessageApplication.getContext(),
				// "This device is not supported.", Toast.LENGTH_SHORT)
				// .show();

			}
			return false;
		}
		return true;
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
		if (Settings.debugMode) {
			Toast.makeText(context, TAG + "Connessione non presente...",
					Toast.LENGTH_LONG).show();
		}
		return false;
	}

	public static JSONObject doPostRequest(String urlToOpen,
			JSONObject parameters) throws HttpException {

		JSONObject result;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		String postParams = "";
		boolean isFirst = true;

		if (!isNetworkAvailable(UMessageApplication.getContext())) {
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
			// Utility.reportError(UMessageApplication.getContext(), e, TAG
			// + ": doPostRequest()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
			return null;
		}

		return result;

	}

	public static void prepareDirectory(Context context) {
		try {
			// PackageManager m = context.getPackageManager();
			// String s = context.getPackageName();
			// PackageInfo pi = m.getPackageInfo(s, 0);
			// s = pi.applicationInfo.dataDir;

			// File mainFolder = new File(s + Settings.MAIN_FOLDER);
			// mainFolder.mkdir();
			File mainFolder = context.getExternalFilesDir(null);

			File imageFolder = new File(mainFolder.toString()
					+ Settings.CONTACT_PROFILE_IMAGES_FOLDER);
			imageFolder.mkdir();

		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": prepareDirectory()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
		}
	}

	public static File getMainFolder(Context context) {
		File mainFolder = null;
		try {
			// PackageManager m = context.getPackageManager();
			// String s = context.getPackageName();
			// PackageInfo pi = m.getPackageInfo(s, 0);
			// s = pi.applicationInfo.dataDir;

			// mainFolder = new File(s + Settings.MAIN_FOLDER);

			mainFolder = context.getExternalFilesDir(null);
		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": getMainFolder()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
		}

		return mainFolder;
	}

	// testing
	public static void copyFile(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": copyFile()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
		}

	}

	public static JSONObject uploadImageProfile(Context context,
			String urlToOpen, File imageToUpload, String sessionId)
			throws HttpException {

		if (!isNetworkAvailable(UMessageApplication.getContext())) {
			throw new HttpException("Connessione non disponibile");
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(urlToOpen);
		JSONObject result = null;
		try {
			MultipartEntity entity = new MultipartEntity();

			entity.addPart("action", new StringBody("SEND_NEW_PROFILE_IMAGE"));
			entity.addPart("sessionId", new StringBody(sessionId));
			entity.addPart("userProfileImage", new FileBody(imageToUpload));
			httppost.setEntity(entity);
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity resEntity = response.getEntity();

			String responseString = EntityUtils.toString(resEntity);
			result = new JSONObject(responseString);

		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": uploadImageProfile()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
			throw new HttpException("Errore upload immagine.");
		}

		httpclient.getConnectionManager().shutdown();

		return result;

	}

	public static boolean downloadFileFromUrl(Context context, File file,
			String fileUrlToDownload) throws HttpException {

		if (!isNetworkAvailable(UMessageApplication.getContext())) {
			throw new HttpException("Connessione non disponibile");
		}

		try {

			URL url = new URL(fileUrlToDownload);

			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();

		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": downloadFileFromUrl()");

			return false;
		}

		return true;
	}

	public static String md5(String in) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(in.getBytes());
			byte[] a = digest.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": md5()");
			/*
			 * Toast.makeText(UMessageApplication.getContext(), TAG +
			 * e.toString(), Toast.LENGTH_LONG).show();
			 */
		}
		return null;
	}

	public static void reportError(Context context, Exception e, String classTag) {

		if (Settings.debugMode) {
			Toast.makeText(context, classTag + e.toString(), Toast.LENGTH_LONG)
					.show();
		}
		Provider p = new Provider(context);
		Calendar c = Calendar.getInstance();

		String dataFormattedValue = ""
				+ (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0"
						+ c.get(Calendar.DAY_OF_MONTH) : c
						.get(Calendar.DAY_OF_MONTH))
				+ "/"
				+ ((c.get(Calendar.MONTH) + 1) < 10 ? "0"
						+ (c.get(Calendar.MONTH) + 1)
						: (c.get(Calendar.MONTH) + 1)) + "/"
				+ c.get(Calendar.YEAR);

		String timeFormattedValue = ""
				+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
						+ c.get(Calendar.HOUR_OF_DAY) : c
						.get(Calendar.HOUR_OF_DAY))
				+ ":"
				+ (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE)
						: c.get(Calendar.MINUTE));

		long newErrorId = p.insertError(classTag, "@" + dataFormattedValue
				+ "  " + timeFormattedValue + "\n" + e.toString() + "\n"
				+ e.getStackTrace().toString());
		HttpResponseUmsg result = new HttpResponseUmsg();

		if (newErrorId != -1) {
			try {

				PackageInfo packageInfo = context.getPackageManager()
						.getPackageInfo(context.getPackageName(), 0);

				Bundle request, response;
				request = new Bundle();
				request.putBoolean(ConfigurationManager.SESSION_ID, true);
				response = ConfigurationManager.getValues(request);

				JSONObject parameters = new JSONObject();
				parameters.accumulate("action", "REPORT_ERROR");
				parameters
						.accumulate("sessionId", response.getString(
								ConfigurationManager.SESSION_ID, ""));
				parameters.accumulate("tag", classTag);
				parameters.accumulate(
						"info",
						"@" + dataFormattedValue + "  " + timeFormattedValue
								+ "\n" + e.toString() + "\n"
								+ e.getStackTrace());
				parameters.accumulate("appVersion", packageInfo.versionCode);

				result.result = Utility.doPostRequest(Settings.SERVER_URL,
						parameters);

				if ((result.result == null)
						|| (result.result.getString("errorCode").equals("KO"))) {
					result.error = true;
				}

			} catch (Exception exc) {
				result.error = true;
			}

			if (!result.error) {
				p.removeError(newErrorId);
			}
		}

	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	/*
	public static DropboxAPI.Entry getDropboxIndexFile(
			DropboxAPI<AndroidAuthSession> connection, File localIndexFile) {

		if (!connection.getSession().isLinked()) {
			return null;
		}

		if (localIndexFile.isFile()) {
			localIndexFile.delete();
		}

		String indexFileToDownload = "UMessageIndex";

		FileOutputStream outputStream = new FileOutputStream(localIndexFile);
		DropboxFileInfo info = connection.getFile("/" + indexFileToDownload,
				null, outputStream, null);

		return new DropboxAPI.Entry();
	}
	*/

}
