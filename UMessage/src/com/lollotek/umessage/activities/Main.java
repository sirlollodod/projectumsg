package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
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

		m_configuration = Utility.getConfiguration(UMessageApplication
				.getContext());
		String storedSerialSim = m_configuration.getSimserial();
		String sessionId = m_configuration.getSessid();
		boolean simIsLogging = m_configuration.isSimIsLogging();
		String actualSerialSim = Utility.getSerialSim(UMessageApplication
				.getContext());
		String prefix = m_configuration.getPrefix();
		String num = m_configuration.getNum();

		if ((storedSerialSim == "") || (storedSerialSim == null)
				|| (actualSerialSim != storedSerialSim)) {
			// Registrazione
			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);

		} else if (simIsLogging) {
			//Login
			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.Login.class);

			i.putExtra("prefix", m_configuration.getPrefix());
			i.putExtra("num", m_configuration.getNum());
			i.putExtra("serialSim", m_configuration.getSimserial());

			startActivity(i);

		} else if ((prefix != "") && (num != "") && (sessionId != "")) {
			// Utente presumibilmente loggato, bisognerebbe assicurarsi che
			// sessionId valida ed associata a numero attuale
		} else {
			// Default: Registrazione
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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	{ // Metodi registrazione GCM

		/*
		 * private void registerGCM(){ Toast msg; if
		 * (Utility.checkPlayServices(UMessageApplication.getContext())) { msg =
		 * Toast.makeText(this, "PlayServices available", Toast.LENGTH_SHORT);
		 * msg.show();
		 * 
		 * String gcmid = m_configuration.getGcmid(); if ((gcmid == "") ||
		 * (gcmid == null)) { msg = Toast.makeText(this,
		 * "gcmid non presente, mi registro", Toast.LENGTH_SHORT); msg.show();
		 * new RegisterInBackground().execute("");
		 * 
		 * } else { msg = Toast.makeText(this, "gcmid=" +
		 * m_configuration.getGcmid(), Toast.LENGTH_SHORT); msg.show(); } } else
		 * { msg = Toast.makeText(this, "PlayServices NOT available",
		 * Toast.LENGTH_SHORT); msg.show(); } }
		 */

		/*
		 * private class RegisterInBackground extends AsyncTask<String, Void,
		 * String> {
		 * 
		 * protected String doInBackground(String... params) { String regid =
		 * "provaaa"; if (gcm == null) { gcm =
		 * GoogleCloudMessaging.getInstance(UMessageApplication .getContext());
		 * } // regid = gcm.register("1050595639343");
		 * 
		 * File configurationFile = new File(UMessageApplication.getContext()
		 * .getFilesDir() + "/" + "config.dat"); Configuration configuration =
		 * Utility .loadConfiguration(configurationFile);
		 * configuration.setGcmid(regid);
		 * Utility.saveConfiguration(configuration, configurationFile);
		 * 
		 * return regid; }
		 * 
		 * protected void onPostExecute(String msg) { Toast msgg =
		 * Toast.makeText(UMessageApplication.getContext(), "RegId: " + msg,
		 * Toast.LENGTH_LONG); msgg.show();
		 * 
		 * TextView t = new TextView(context); t.findViewById(R.id.txt0);
		 * t.setText("Sono dentro al onPostExecute dell'AsyncTask");
		 * 
		 * }
		 * 
		 * }
		 */
	}

}
