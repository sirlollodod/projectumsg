package com.lollotek.umessage.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.managers.ConfigurationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Main extends Activity {

	private static final String TAG = Main.class.getName() + ":\n";
	
	private static final String SHARED_PREFS_MAIN = "MAIN_DELETE_CONFIG_FILE";
	
	Bundle request, response;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// debug, solo fino a versione 9
        File configurationFile = new File(UMessageApplication.getContext()
                        .getFilesDir() + "/" + Settings.CONFIG_FILE_NAME);

        if (configurationFile.isFile()) {
                configurationFile.delete();
        }

        File sharedPrefsMain = new File(
                        "/data/data/com.lollotek.umessage/shared_prefs/"
                                        + SHARED_PREFS_MAIN + ".xml");
        if (sharedPrefsMain.isFile()) {
                sharedPrefsMain.delete();
        }
        // fine debug
        
		Intent service = new Intent(this,
				com.lollotek.umessage.services.UMessageService.class);

		request = new Bundle();
		request.putBoolean(ConfigurationManager.SESSION_ID, true);
		response = ConfigurationManager.getValues(request);

		if (response.getString(ConfigurationManager.SESSION_ID, "").equals("")) {
			stopService(service);
		} else {
			service.putExtra("action",
					MessageTypes.STARTED_FOR_INITIALIZE_SERVICE);
			startService(service);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		request = new Bundle();
		request.putBoolean(ConfigurationManager.SIM_SERIAL, true);
		request.putBoolean(ConfigurationManager.SESSION_ID, true);
		request.putBoolean(ConfigurationManager.SIM_IS_LOGGING, true);
		request.putBoolean(ConfigurationManager.PREFIX, true);
		request.putBoolean(ConfigurationManager.NUM, true);
		response = ConfigurationManager.getValues(request);

		String actualSerialSim = Utility.getSerialSim(UMessageApplication
				.getContext());

		if ((response.getString(ConfigurationManager.SIM_SERIAL, "").equals(""))
				|| (response.getString(ConfigurationManager.SIM_SERIAL, "") == null)
				|| !(actualSerialSim.equals(response.getString(
						ConfigurationManager.SIM_SERIAL, "")))) { // Registrazione

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Registration.class);
			startActivity(i);

		} else if (response.getBoolean(ConfigurationManager.SIM_IS_LOGGING,
				false)) { // Login

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.Login.class);

			startActivity(i);

		} else if ((!response.getString(ConfigurationManager.PREFIX, "")
				.equals(""))
				&& (!response.getString(ConfigurationManager.NUM, "")
						.equals(""))
				&& (!response.getString(ConfigurationManager.SESSION_ID, "")
						.equals(""))) {
			// Utente presumibilmente loggato, bisognerebbe assicurarsi che
			// sessionId valida ed associata a numero attuale
			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);
		} else { // Default: Registrazione

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
