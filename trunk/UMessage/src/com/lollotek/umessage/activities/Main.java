package com.lollotek.umessage.activities;

import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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

	}

	@Override
	protected void onResume() {
		super.onResume();

		// testing
		{

			Utility.prepareDirectory(context);

			Provider p = new Provider(UMessageApplication.getContext());

			ContentValues value;

			Calendar c;
			Random r = new Random();
			int totalNewMessages = 0;
			int newMessages = r.nextInt(20);
			for (int i = 0; i < newMessages; i++) {
				c = Calendar.getInstance();
				value = new ContentValues();
				value.put(DatabaseHelper.KEY_PREFIX, "+39");
				value.put(DatabaseHelper.KEY_NUM, "3488079034");
				value.put(DatabaseHelper.KEY_DIRECTION, (r.nextBoolean() ? "0"
						: "1"));
				value.put(DatabaseHelper.KEY_STATUS, "0");
				value.put(DatabaseHelper.KEY_DATA,
						Double.parseDouble("" + c.getTimeInMillis()));
				value.put(DatabaseHelper.KEY_TYPE, "text");
				value.put(
						DatabaseHelper.KEY_MESSAGE,
						""
								+ i
								+ "° messaggio: provo a inserire un messaggio più lungo, cosi probabilmente andrà a finire su più di una riga!!");
				value.put(DatabaseHelper.KEY_TOREAD, "1");

				if (p.insertNewMessage(value)) {
					totalNewMessages++;
				}

			}

			// Toast.makeText(UMessageApplication.getContext(),
			// "Singoli messaggi inseriti: " + totalNewMessages,
			// Toast.LENGTH_SHORT).show();

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
		String email = m_configuration.getEmail();

		if ((storedSerialSim.equals("")) || (storedSerialSim == null)
				|| !(actualSerialSim.equals(storedSerialSim))) {
			// Registrazione
			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			stopService(service);

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);

		} else if (simIsLogging) {
			// Login
			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			stopService(service);

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.Login.class);

			i.putExtra("prefix", m_configuration.getPrefix());
			i.putExtra("num", m_configuration.getNum());
			i.putExtra("serialSim", m_configuration.getSimserial());
			i.putExtra("email", m_configuration.getEmail());

			startActivity(i);

		} else if ((prefix != "") && (num != "") && (sessionId != "")) {
			// Utente presumibilmente loggato, bisognerebbe assicurarsi che
			// sessionId valida ed associata a numero attuale
			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);
		} else {
			// Default: Registrazione
			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			stopService(service);

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
