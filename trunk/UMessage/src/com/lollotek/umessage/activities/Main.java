package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
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
		/*
		 * { Provider p = new Provider(UMessageApplication.getContext());
		 * if(p.delete(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null, null) ==
		 * -2){ Toast.makeText(UMessageApplication.getContext(),
		 * "Rimossi tutti i messaggi", Toast.LENGTH_SHORT).show();
		 * 
		 * }
		 * 
		 * ContentValues value = new ContentValues();
		 * value.put(DatabaseHelper.KEY_IDCHAT, 0);
		 * value.put(DatabaseHelper.KEY_VERSION, "asdkmoqwe");
		 * value.put(DatabaseHelper.KEY_PREFIXDEST, "+39");
		 * value.put(DatabaseHelper.KEY_NUMDEST, "3396929558");
		 * value.put(DatabaseHelper.KEY_IDLASTMESSAGE, 0); if
		 * (p.insert(DatabaseHelper.TABLE_SINGLECHAT, null, value) != -1) {
		 * Toast.makeText(UMessageApplication.getContext(), "Chat creata",
		 * Toast.LENGTH_SHORT).show(); }
		 * 
		 * value = new ContentValues();
		 * //value.put(DatabaseHelper.KEY_IDMESSAGE, 41);
		 * value.put(DatabaseHelper.KEY_IDCHAT, 0);
		 * value.put(DatabaseHelper.KEY_DIRECTION, "1");
		 * value.put(DatabaseHelper.KEY_STATUS, "0");
		 * value.put(DatabaseHelper.KEY_DATA, 23);
		 * value.put(DatabaseHelper.KEY_TYPE, "text"); value.put(
		 * DatabaseHelper.KEY_MESSAGE, "Dodicesimo messaggio: ciao scimmia...");
		 * value.put(DatabaseHelper.KEY_READ, "0");
		 * 
		 * if (p.insert(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null, value) !=
		 * -1) { Toast.makeText(UMessageApplication.getContext(),
		 * "Singolo messaggio inserito", Toast.LENGTH_SHORT) .show(); } }
		 */

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
			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);

		} else if (simIsLogging) {
			// Login
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
