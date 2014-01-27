package com.lollotek.umessage.activities;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.listeners.SynchronizationListener;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;

public class Dropbox extends Activity {

	private static final String TAG = Dropbox.class.getName() + ":\n";

	DropboxAPI<AndroidAuthSession> mApi;

	private boolean mLoggedIn;
	private TextView connectionStatus, lastOnlineBk, lastLocalBk;
	private String lastOnlineBkData = "0", lastLocalBkData = "0";

	private SynchronizationListener syncListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_dropbox);

		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		connectionStatus = (TextView) findViewById(R.id.textView2);
		lastOnlineBk = (TextView) findViewById(R.id.textView4);
		lastLocalBk = (TextView) findViewById(R.id.textView6);

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		syncListener = new SynchronizationListener() {

			Bundle bnd;

			@Override
			public void onStart(Message msg) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgress(Message msg) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish(final Message msg) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						switch (msg.what) {
						case MessageTypes.DROPBOX_REFRESH:
							bnd = msg.getData();
							lastLocalBkData = bnd.getString("lastLocalBkData",
									lastLocalBkData);

							lastOnlineBkData = bnd.getString(
									"lastOnlineBkData", lastOnlineBkData);

							refreshView();

							break;
						}

					}

				});

			}

		};

		if (mApi.getSession().isLinked()) {
			setLoggedIn();
		} else {
			setLoggedOut();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mApi.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);

				setLoggedIn();
			} catch (IllegalStateException e) {
				// Errore login
			}
		}

		Intent service = new Intent(this, com.lollotek.umessage.services.UMessageService.class);
		service.putExtra("action", MessageTypes.GET_LAST_LOCAL_DB_BK_DATA);
		startService(service);
		
		refreshView();

		SynchronizationManager.getInstance().registerSynchronizationListener(
				syncListener);
	}

	@Override
	protected void onPause() {
		super.onPause();

		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				syncListener);
	}

	private void refreshView() {
		Calendar c = Calendar.getInstance();

		if (mLoggedIn) {
			connectionStatus.setText("connesso");
		} else {
			connectionStatus.setText("disconnesso");
		}

		if (lastLocalBkData.equals("0")) {
			lastLocalBk.setText("N/A");
		} else {
			c.setTimeInMillis(Long.parseLong(lastLocalBkData));
			String dataFormattedValue = ""
					+ (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0"
							+ c.get(Calendar.DAY_OF_MONTH) : c
							.get(Calendar.DAY_OF_MONTH))
					+ "/"
					+ ((c.get(Calendar.MONTH) + 1) < 10 ? "0"
							+ (c.get(Calendar.MONTH) + 1) : (c
							.get(Calendar.MONTH) + 1)) + "/"
					+ c.get(Calendar.YEAR);

			String timeFormattedValue = ""
					+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
							+ c.get(Calendar.HOUR_OF_DAY) : c
							.get(Calendar.HOUR_OF_DAY))
					+ ":"
					+ (c.get(Calendar.MINUTE) < 10 ? "0"
							+ c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE));

			lastLocalBk
					.setText(dataFormattedValue + "   " + timeFormattedValue);
		}

		if (lastOnlineBkData.equals("0")) {
			lastOnlineBk.setText("N/A");
		} else {
			c.setTimeInMillis(Long.parseLong(lastLocalBkData));
			String dataFormattedValue = ""
					+ (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0"
							+ c.get(Calendar.DAY_OF_MONTH) : c
							.get(Calendar.DAY_OF_MONTH))
					+ "/"
					+ ((c.get(Calendar.MONTH) + 1) < 10 ? "0"
							+ (c.get(Calendar.MONTH) + 1) : (c
							.get(Calendar.MONTH) + 1)) + "/"
					+ c.get(Calendar.YEAR);

			String timeFormattedValue = ""
					+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
							+ c.get(Calendar.HOUR_OF_DAY) : c
							.get(Calendar.HOUR_OF_DAY))
					+ ":"
					+ (c.get(Calendar.MINUTE) < 10 ? "0"
							+ c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE));

			lastOnlineBk.setText(dataFormattedValue + "   "
					+ timeFormattedValue);
		}
	}

	private AndroidAuthSession buildSession() {
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
		SharedPreferences prefs = getSharedPreferences(
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

	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = getSharedPreferences(
				Settings.SHARED_PREFS_DROPBOX, 0);
		Editor edit = prefs.edit();
		edit.putString(Settings.ACCESS_KEY_NAME, key);
		edit.putString(Settings.ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void setLoggedOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();

		mLoggedIn = false;
		refreshView();

	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(
				Settings.SHARED_PREFS_DROPBOX, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private void setLoggedIn() {
		mLoggedIn = true;
		refreshView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dropbox, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect:
			mApi.getSession().startAuthentication(
					com.lollotek.umessage.activities.Dropbox.this);
			break;

		case R.id.disconnect:
			setLoggedOut();
			break;

		case R.id.createBackup:
			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action", MessageTypes.MAKE_DB_DUMP);
			service.putExtra("forceDBDump", true);
			startService(service);

			break;

		case R.id.synchronize:
			service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action", MessageTypes.GET_LAST_LOCAL_DB_BK_DATA);
			startService(service);
			break;

		case android.R.id.home:

			try {
				NavUtils.navigateUpFromSameTask(this);
			} catch (Exception e) {
				finish();
			}

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		getMenuInflater().inflate(R.menu.dropbox, menu);
		if (mLoggedIn) {
			menu.removeItem(R.id.connect);
		} else {
			menu.removeItem(R.id.disconnect);
		}

		return super.onPrepareOptionsMenu(menu);
	}

}
