package com.lollotek.umessage.activities;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.data.c;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
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

		m_configuration = Utility.getConfiguration(UMessageApplication
				.getContext());

		
	}

	@Override
	protected void onResume() {
		super.onResume();

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
