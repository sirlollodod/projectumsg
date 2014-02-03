package com.lollotek.umessage.activities;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.listeners.SynchronizationListener;
import com.lollotek.umessage.managers.DropboxManager;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;

public class Dropbox extends Activity {

	private static final String TAG = Dropbox.class.getName() + ":\n";

	DropboxAPI<AndroidAuthSession> mApi;

	private boolean mLoggedIn;
	private TextView connectionStatus, lastOnlineBk, lastLocalBk;
	private String lastOnlineBkData = "0", lastLocalBkData = "0",
			userLoggedIn = "sconosciuto";

	private SynchronizationListener syncListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_dropbox);

		AndroidAuthSession session = DropboxManager.getInstance(
				UMessageApplication.getContext()).buildSession();
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

							userLoggedIn = bnd.getString("userLoggedIn",
									userLoggedIn);

							if (bnd.getBoolean("forceReloadAllData", false)) {
								reloadData();
							}

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

		reloadData();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				DropboxManager.getInstance(UMessageApplication.getContext())
						.storeKeys(tokens.key, tokens.secret);

				setLoggedIn();
			} catch (IllegalStateException e) {
				// Errore login
			}
		}

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
			connectionStatus.setText("connesso" + "  ( " + userLoggedIn + " )");
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
			c.setTimeInMillis(Long.parseLong(lastOnlineBkData));
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

	private void setLoggedOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		DropboxManager.getInstance(UMessageApplication.getContext())
				.clearKeys();

		mLoggedIn = false;
		refreshView();

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

	private void reloadData() {
		Intent service = new Intent(this,
				com.lollotek.umessage.services.UMessageService.class);
		service.putExtra("action", MessageTypes.GET_DROPBOX_ACCOUNT_INFO);
		startService(service);

		service = new Intent(this,
				com.lollotek.umessage.services.UMessageService.class);
		service.putExtra("action", MessageTypes.GET_LAST_LOCAL_DB_BK_DATA);
		startService(service);

		service = new Intent(this,
				com.lollotek.umessage.services.UMessageService.class);
		service.putExtra("action", MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA);
		startService(service);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent service;

		switch (item.getItemId()) {
		case R.id.connect:
			mApi.getSession().startAuthentication(
					com.lollotek.umessage.activities.Dropbox.this);
			break;

		case R.id.disconnect:
			setLoggedOut();
			break;

		case R.id.createBackup:
			service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action", MessageTypes.MAKE_DB_DUMP);
			service.putExtra("forceDBDump", true);
			startService(service);

			break;

		case R.id.synchronize:
			service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action",
					MessageTypes.START_DROPBOX_SYNCHRONIZATION);
			startService(service);

			break;

		case R.id.refresh:
			reloadData();

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
			menu.removeItem(R.id.synchronize);
			menu.removeItem(R.id.refresh);
		}

		return super.onPrepareOptionsMenu(menu);
	}

}
