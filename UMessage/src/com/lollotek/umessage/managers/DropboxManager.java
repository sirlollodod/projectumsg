package com.lollotek.umessage.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class DropboxManager {

	private Context mContext;
	private static DropboxManager instance = null;
	public static final String PREFIX = "PREFIX";
	public static final String NUM = "NUM";
	public static final String LAST_BK_FILE = "LAST_BK_FILE";
	public static final String LAST_BK_DATA = "LAST_BK_DATA";
	public static final String EXISTS_BK = "EXISTS_BK";

	private static final String USERS_BK = "USERS_BK";
	private static final String TAG = DropboxManager.class.getName() + ":\n";

	private DropboxManager(Context context) {
		mContext = context;
	}

	public static DropboxManager getInstance(Context context) {
		if (instance == null) {
			instance = new DropboxManager(context.getApplicationContext());
		}

		return instance;
	}

	public AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(Settings.APP_KEY,
				Settings.APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
					stored[1]);
			session = new AndroidAuthSession(appKeyPair, Settings.ACCESS_TYPE,
					accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, Settings.ACCESS_TYPE);
		}

		return session;
	}

	private String[] getKeys() {
		SharedPreferences prefs = mContext.getSharedPreferences(
				Settings.SHARED_PREFS_DROPBOX, 0);
		String key = prefs.getString(Settings.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(Settings.ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	public void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = mContext.getSharedPreferences(
				Settings.SHARED_PREFS_DROPBOX, 0);
		Editor edit = prefs.edit();
		edit.putString(Settings.ACCESS_KEY_NAME, key);
		edit.putString(Settings.ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	public void clearKeys() {
		SharedPreferences prefs = mContext.getSharedPreferences(
				Settings.SHARED_PREFS_DROPBOX, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	// Crea JSONObject vuoto o lo scrive su file locale temporaneo
	private static boolean createDropboxIndexFile() {
		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localIndexFile = new File(mainFolder.toString()
				+ Settings.DROPBOX_INDEX_LOCAL_FILE_TEMP);
		JSONObject index = new JSONObject();
		JSONArray users = new JSONArray();

		try {
			index.accumulate(USERS_BK, users);

			FileOutputStream fos = new FileOutputStream(localIndexFile);
			fos.write(index.toString().getBytes());
			fos.close();

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Upload file locale index su dropbox
	private static boolean uploadIndexFile(
			DropboxAPI<AndroidAuthSession> connection) {
		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localIndexFile = new File(mainFolder.toString()
				+ Settings.DROPBOX_INDEX_LOCAL_FILE_TEMP);

		if (!localIndexFile.isFile()) {
			return false;
		}

		try {
			FileInputStream inputStream = new FileInputStream(localIndexFile);
			Entry response = connection.putFile("/"
					+ Settings.DROPBOX_INDEX_FILE, inputStream,
					localIndexFile.length(), null, null);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Download index file da Dropbox se presente e lo salva localmente,
	// altrimenti lo crea localmente
	private static boolean downloadIndexFile(
			DropboxAPI<AndroidAuthSession> connection) {

		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localIndexFile = new File(mainFolder.toString()
				+ Settings.DROPBOX_INDEX_LOCAL_FILE_TEMP);

		if (localIndexFile.isFile()) {
			localIndexFile.delete();
		}

		String indexFileToDownload = "";
		boolean indexFileFound = false;

		try {
			DropboxAPI.Entry entries = connection.metadata("/", 100, null,
					true, null);
			for (DropboxAPI.Entry e : entries.contents) {
				String fileName = e.fileName();
				if (fileName.startsWith(Settings.DROPBOX_INDEX_FILE
						.substring(1))) {
					indexFileFound = true;
					indexFileToDownload = fileName;
					break;
				}

			}

			if (!indexFileFound) {
				createDropboxIndexFile();
				return true;
			}

			FileOutputStream outputStream = new FileOutputStream(localIndexFile);
			DropboxFileInfo indexInfo = connection.getFile("/"
					+ indexFileToDownload, null, outputStream, null);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Cerca all'interno del file index locale i dati relativi al backup
	// dell'utente voluto
	private static Bundle getDropboxUserBKInfo(
			DropboxAPI<AndroidAuthSession> connection, String prefix, String num) {

		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localIndexFile = new File(mainFolder.toString()
				+ Settings.DROPBOX_INDEX_LOCAL_FILE_TEMP);

		String bkFileToDownload = "";
		boolean bkFileFound = false;
		Bundle response = new Bundle();

		try {

			JSONObject index = new JSONObject(
					Utility.getStringFromFile(localIndexFile.toString()));
			JSONArray users = index.getJSONArray(USERS_BK);
			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (!user.getString(PREFIX).equals(prefix)
						|| !user.getString(NUM).equals(num)) {
					continue;
				}

				response.putBoolean(EXISTS_BK, true);
				response.putString(PREFIX, user.getString(PREFIX));
				response.putString(NUM, user.getString(NUM));
				response.putString(LAST_BK_FILE, user.getString(LAST_BK_FILE));
				response.putString(LAST_BK_DATA, user.getString(LAST_BK_DATA));

				if (localIndexFile.isFile()) {
					localIndexFile.delete();
				}

				break;

			}

		} catch (Exception e) {
			if (localIndexFile.isFile()) {
				localIndexFile.delete();
			}

			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ " getDropboxUserBKInfo()");
		}

		if (response.getBoolean(EXISTS_BK, false)) {
			return response;
		} else {
			response.putBoolean(EXISTS_BK, false);
			return response;
		}
	}

	// Upload file backup dell'utente
	private static boolean uploadBk(DropboxAPI<AndroidAuthSession> connection,
			File localBk, String dataBk) {

		try {
			FileInputStream inputStream = new FileInputStream(localBk);
			Entry response = connection.putFile("/" + Settings.DROPBOX_USER_BK
					+ "_" + dataBk, inputStream, localBk.length(), null, null);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Crea nuovo file index locale con dati backup utente aggiornati
	private static boolean updateLocalIndex(String prefix, String num,
			String dataBk) {

		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localIndexFile = new File(mainFolder.toString()
				+ Settings.DROPBOX_INDEX_LOCAL_FILE_TEMP);

		boolean userEntryFound = false;

		try {
			JSONObject index = new JSONObject(
					Utility.getStringFromFile(localIndexFile.toString()));
			JSONArray users = index.getJSONArray(USERS_BK);

			JSONObject newIndex = new JSONObject();
			JSONArray newUsers = new JSONArray();

			for (int i = 0; i < users.length(); i++) {
				JSONObject user = users.getJSONObject(i);
				if (!user.getString(PREFIX).equals(prefix)
						|| !user.getString(NUM).equals(num)) {
					newUsers.put(user);
					continue;
				} else {
					JSONObject newUser = new JSONObject();
					newUser.accumulate(PREFIX, prefix);
					newUser.accumulate(NUM, num);
					newUser.accumulate(LAST_BK_DATA, dataBk);
					newUser.accumulate(LAST_BK_FILE, Settings.DROPBOX_USER_BK
							+ "_" + dataBk);
					newUsers.put(newUser);
					userEntryFound = true;
				}
			}

			if (!userEntryFound) {
				JSONObject newUser = new JSONObject();
				newUser.accumulate(PREFIX, prefix);
				newUser.accumulate(NUM, num);
				newUser.accumulate(LAST_BK_DATA, dataBk);
				newUser.accumulate(LAST_BK_FILE, Settings.DROPBOX_USER_BK + "_"
						+ dataBk);
				newUsers.put(newUser);
			}

			newIndex.accumulate(USERS_BK, newUsers);

			if (localIndexFile.isFile()) {
				localIndexFile.delete();
			}

			FileOutputStream fos = new FileOutputStream(localIndexFile);
			fos.write(newIndex.toString().getBytes());
			fos.close();

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Download file di backup voluto
	private static boolean downloadUserBk(
			DropboxAPI<AndroidAuthSession> connection, String bkFile) {

		File mainFolder = Utility.getMainFolder(UMessageApplication
				.getContext());
		File localBk = new File(mainFolder.toString()
				+ Settings.DROPBOX_USER_BK);

		if (localBk.isFile()) {
			localBk.delete();
		}

		try {
			FileOutputStream outputStream = new FileOutputStream(localBk);
			DropboxFileInfo bkInfo = connection.getFile("/" + bkFile, null,
					outputStream, null);
		} catch (Exception e) {
			if (localBk.isFile()) {
				localBk.delete();
			}

			return false;
		}

		return true;
	}

	public static boolean uploadUserBk(
			DropboxAPI<AndroidAuthSession> connection, File fileToUpload,
			String dataBk, String prefix, String num) {

		if (!connection.getSession().isLinked()) {
			return false;
		}

		if (!downloadIndexFile(connection)) {
			return false;
		}

		Bundle userBkInfo = getDropboxUserBKInfo(connection, prefix, num);

		try {
			if (userBkInfo.getBoolean(EXISTS_BK, false)
					&& (Long.parseLong(userBkInfo.getString(LAST_BK_DATA, "0")) >= Long
							.parseLong(dataBk))) {
				return true;
			}

			if (!uploadBk(connection, fileToUpload, dataBk)) {
				return false;
			}

			if (updateLocalIndex(prefix, num, dataBk)) {
				return uploadIndexFile(connection);
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public static boolean getUserBk(DropboxAPI<AndroidAuthSession> connection,
			String prefix, String num) {

		if (!connection.getSession().isLinked()) {
			return false;
		}

		if (!downloadIndexFile(connection)) {
			return false;
		}

		Bundle userBkInfo = getDropboxUserBKInfo(connection, prefix, num);

		try {
			if (userBkInfo.getBoolean(EXISTS_BK, false)) {
				return downloadUserBk(connection,
						userBkInfo.getString(LAST_BK_FILE));
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

	}
}
