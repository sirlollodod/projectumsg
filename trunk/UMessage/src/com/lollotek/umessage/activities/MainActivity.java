package com.lollotek.umessage.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.Utility;

public class MainActivity extends Activity {

	private Configuration m_configuration;
	private File m_configurationFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		m_configurationFile = new File(this.getFilesDir() + "/" + "config.dat");
		m_configuration = Utility.loadConfiguration(m_configurationFile);
		if (m_configuration == null) {
			m_configuration = new Configuration();
			Utility.saveConfiguration(m_configuration, m_configurationFile);
		}

		if (m_configuration.isFirstExecutionApp()) {
			Toast msg = Toast
					.makeText(
							this,
							"Prima esecuzione app... si consiglia di riavviare il telefono!",
							Toast.LENGTH_SHORT);
			msg.show();
			m_configuration.setFirstExecutionApp(false);
			Utility.saveConfiguration(m_configuration, m_configurationFile);

			Intent serviceIntent = new Intent();
			serviceIntent
					.setAction("com.lollotek.umessage.services.UMessageService");
			this.startService(serviceIntent);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			Provider p = new Provider(UMessageApplication.getContext());
			Cursor users = p.getTotalUser();

			Toast msg = Toast.makeText(getApplicationContext(),
					"Totale user inseriti: " + users.getCount(),
					Toast.LENGTH_LONG);
			msg.show();
		} catch (Exception e) {
			Toast msg = Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
			msg.show();
		}
		
		
		
		
		/*
		 * String elenco = "";
		 * 
		 * while (users.moveToNext()) { elenco += users.getString(users
		 * .getColumnIndex(DatabaseHelper.KEY_NAME)) + " : +" +
		 * users.getString(users .getColumnIndex(DatabaseHelper.KEY_PREFIX)) +
		 * users.getString(users .getColumnIndex(DatabaseHelper.KEY_NUM)) +
		 * "\n"; }
		 * 
		 * msg.setText(elenco); msg.show();
		 */

	}

}
