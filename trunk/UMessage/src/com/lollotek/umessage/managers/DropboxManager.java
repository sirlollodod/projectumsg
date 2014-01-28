package com.lollotek.umessage.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.lollotek.umessage.utils.Settings;

public class DropboxManager {

	private Context mContext;
	private static DropboxManager instance = null;

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

}
