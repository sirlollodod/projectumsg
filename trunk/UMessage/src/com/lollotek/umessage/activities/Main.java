package com.lollotek.umessage.activities;

import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Utility;

public class Main extends Activity {

	private Configuration m_configuration;

	GoogleCloudMessaging gcm;

	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		context = this;

		Intent service = new Intent(this,
				com.lollotek.umessage.services.UMessageService.class);
		startService(service);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// testing
		{
			Utility.prepareDirectory(context);
			Configuration configuration = Utility.getConfiguration(context);
/*
			Provider p = new Provider(UMessageApplication.getContext());
			Cursor users = p.getTotalUser();
			String[] messages = {
					"12-0",
					"Esci questa sera?",
					"Penso di no, non sto molto bene, casomai facciamo un'altra volta.. OK??",
					"Meglio di no",
					"scrivo qualcosa di breve...",
					"Ciao!!!",
					"Qualcosa di leggermente più lungo, forse va su 2 righe...",
					"Questo invece è decisamente più lungo, spero che oltre a prendere la seconda riga vada anche sulla terza e/o 4°!!" };

			ContentValues value;

			Calendar c;
			Random r = new Random();
			int totalNewMessages = 0;
			int newMessages = r.nextInt(0);
			boolean unknownPhoneNumber, direction;
			while (totalNewMessages < newMessages) {

				unknownPhoneNumber = false;
				if (!users.moveToNext()) {
					if (!users.moveToFirst()) {
						break;
					}

				}

				if (!r.nextBoolean()) {
					continue;
				}

				if (r.nextInt(40) == 0) {
					unknownPhoneNumber = true;
				}
				c = Calendar.getInstance();
				value = new ContentValues();
				value.put(DatabaseHelper.KEY_PREFIX, users.getString(users
						.getColumnIndex(DatabaseHelper.KEY_PREFIX)));
				value.put(
						DatabaseHelper.KEY_NUM,
						(unknownPhoneNumber ? (users.getString(users
								.getColumnIndex(DatabaseHelper.KEY_NUM)).substring(
								0,
								users.getString(
										users.getColumnIndex(DatabaseHelper.KEY_NUM))
										.length() - 1))
								+ r.nextInt(10)
								: users.getString(users
										.getColumnIndex(DatabaseHelper.KEY_NUM))));

				direction = r.nextBoolean();
				value.put(DatabaseHelper.KEY_DIRECTION, (direction ? "0" : "1"));
				value.put(DatabaseHelper.KEY_STATUS, (direction ? "0" : "1"));
				value.put(DatabaseHelper.KEY_DATA,
						Double.parseDouble("" + c.getTimeInMillis()));
				value.put(DatabaseHelper.KEY_TYPE, "text");
				value.put(DatabaseHelper.KEY_MESSAGE, messages[r.nextInt(8)]);
				value.put(DatabaseHelper.KEY_TOREAD, (direction ? "0" : "1"));
				value.put(DatabaseHelper.KEY_TAG, Utility.md5(users
						.getString(users
								.getColumnIndex(DatabaseHelper.KEY_PREFIX))
						+ users.getString(users
								.getColumnIndex(DatabaseHelper.KEY_NUM))
						+ c.getTimeInMillis()));

				if (p.insertNewMessage(value) != -1) {
					totalNewMessages++;
				}

			}
*/
		}

		m_configuration = Utility.getConfiguration(UMessageApplication
				.getContext());
		String storedSerialSim = m_configuration.getSimserial();
		String sessionId = m_configuration.getSessid();
		boolean simIsLogging = m_configuration.isSimIsLogging();
		String actualSerialSim = Utility.getSerialSim(UMessageApplication
				.getContext());
		String prefix = m_configuration.getPrefix();
		String num = m_configuration.getNum();
		boolean isFirstExecutionApp = m_configuration.isFirstExecutionApp();

		if (isFirstExecutionApp) {
			m_configuration.setFirstExecutionApp(false);
			Utility.setConfiguration(context, m_configuration);
			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action",
					MessageTypes.STARTED_FROM_FIRST_EXECUTION_APP);
			startService(service);
		}

		if ((storedSerialSim.equals("")) || (storedSerialSim == null)
				|| !(actualSerialSim.equals(storedSerialSim))) {
			// Registrazione

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);

		} else if (simIsLogging) {
			// Login

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.Login.class);

			startActivity(i);

		} else if ((!prefix.equals("")) && (!num.equals(""))
				&& (!sessionId.equals(""))) {
			// Utente presumibilmente loggato, bisognerebbe assicurarsi che
			// sessionId valida ed associata a numero attuale
			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);
		} else {
			// Default: Registrazione

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.UMessageSettings.class);
			startActivity(i);

			break;
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
