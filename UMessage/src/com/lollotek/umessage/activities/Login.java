package com.lollotek.umessage.activities;

import java.io.File;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.ConfigurationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Login extends Activity {

	private static final String TAG = Login.class.getName() + ":\n";

	EditText smsCode, emailCode;
	Button b1;

	Bundle request, response;

	private static final String SHARED_PREFS_RESTORE_VALUES = "LOGIN_VALUES";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		smsCode = (EditText) findViewById(R.id.editText1);
		emailCode = (EditText) findViewById(R.id.editText2);
		b1 = (Button) findViewById(R.id.button1);

		request = new Bundle();
		request.putBoolean(ConfigurationManager.SIM_IS_LOGGING, true);
		if (ConfigurationManager.saveValues(request)) {
			Utility.reportError(
					UMessageApplication.getContext(),
					new Exception(
							"Configurazione non scritta: onCreate() Login.java "),
					TAG);
		}

		b1.setOnClickListener(new ButtonClickedListener());

	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences prefs = getSharedPreferences(
				SHARED_PREFS_RESTORE_VALUES, MODE_PRIVATE);

		smsCode.setText(prefs.getString("smsCode", ""));
		emailCode.setText(prefs.getString("emailCode", ""));

	}

	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences prefs = getSharedPreferences(
				SHARED_PREFS_RESTORE_VALUES, MODE_PRIVATE);
		Editor edit = prefs.edit();

		edit.putString("smsCode", smsCode.getText().toString());
		edit.putString("emailCode", emailCode.getText().toString());
		edit.commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset:

			request = new Bundle();
			request.putString(ConfigurationManager.PREFIX, "");
			request.putString(ConfigurationManager.NUM, "");
			request.putString(ConfigurationManager.EMAIL, "");
			request.putString(ConfigurationManager.SESSION_ID, "");
			request.putBoolean(ConfigurationManager.SIM_IS_LOGGING, false);
			if (ConfigurationManager.saveValues(request)) {
				Utility.reportError(
						UMessageApplication.getContext(),
						new Exception(
								"Configurazione non scritta: onOptionsItemSelected(): Login.java"),
						TAG);
			}

			Intent i = new Intent(this,
					com.lollotek.umessage.activities.Main.class);
			startActivity(i);

			break;

		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	private void deleteLocalUserInformation() {
		try {
			Provider p = new Provider(UMessageApplication.getContext());
			p.eraseDatabase();

			File mainFolder = Utility.getMainFolder(UMessageApplication
					.getContext());

			File myProfileImage = new File(mainFolder.toString()
					+ Settings.MY_PROFILE_IMAGE_SRC);

			File contactsProfileImageFolder = new File(mainFolder.toString()
					+ Settings.CONTACT_PROFILE_IMAGES_FOLDER);

			if (myProfileImage.isFile()) {
				myProfileImage.delete();
			}

			if (contactsProfileImageFolder.isDirectory()) {
				File[] contactsProfileImages = contactsProfileImageFolder
						.listFiles();
				if (contactsProfileImages.length > 0) {
					for (int i = 0; i < contactsProfileImages.length; i++) {
						if (contactsProfileImages[i].isFile()) {
							contactsProfileImages[i].delete();
						}
					}
				}
			}
		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ " deleteLocalUserInformation()");
		}
	}

	public void userLogged(JSONObject result) {

		try {
			request = new Bundle();
			request.putString(ConfigurationManager.SESSION_ID,
					result.getString("sessionId"));
			request.putBoolean(ConfigurationManager.SIM_IS_LOGGING, false);
			if (ConfigurationManager.saveValues(request)) {
				Utility.reportError(UMessageApplication.getContext(),
						new Exception(
								"Configurazione non scritta: userLogged1() "),
						TAG);
			}

			request = new Bundle();
			request.putBoolean(ConfigurationManager.OLD_PREFIX, true);
			request.putBoolean(ConfigurationManager.OLD_NUM, true);
			request.putBoolean(ConfigurationManager.PREFIX, true);
			request.putBoolean(ConfigurationManager.NUM, true);
			response = ConfigurationManager.getValues(request);

			if ((!response
					.getString(ConfigurationManager.OLD_PREFIX, "")
					.equals(response.getString(ConfigurationManager.PREFIX, "")))
					|| (!response.getString(ConfigurationManager.OLD_NUM, "")
							.equals(response.getString(
									ConfigurationManager.NUM, "")))) {
				deleteLocalUserInformation();

				request = new Bundle();
				request.putString(ConfigurationManager.OLD_PREFIX,
						response.getString(ConfigurationManager.PREFIX, ""));
				request.putString(ConfigurationManager.OLD_NUM,
						response.getString(ConfigurationManager.NUM, ""));

				if (ConfigurationManager.saveValues(request)) {
					Utility.reportError(
							UMessageApplication.getContext(),
							new Exception(
									"Configurazione non scritta: userLogged2() "),
							TAG);
				}

			}

			Toast.makeText(UMessageApplication.getContext(),
					"Utente loggato!\n", Toast.LENGTH_SHORT).show();

			String myProfileImageUrl = result.getString("imageProfileSrc");

			Intent service = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action", MessageTypes.USER_LOGGED);
			if (myProfileImageUrl.length() > 2) {
				myProfileImageUrl = myProfileImageUrl.substring(2);
			}
			service.putExtra("myImageUrl", myProfileImageUrl);
			startService(service);

			SharedPreferences prefs = getSharedPreferences(
					SHARED_PREFS_RESTORE_VALUES, MODE_PRIVATE);
			Editor edit = prefs.edit();

			edit.remove("smsCode");
			edit.remove("emailCode");
			edit.commit();

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);

		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": userLogged()");
		}

	}

	public class ButtonClickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String s = smsCode.getText().toString();
			String e = emailCode.getText().toString();

			request = new Bundle();
			request.putBoolean(ConfigurationManager.PREFIX, true);
			request.putBoolean(ConfigurationManager.NUM, true);
			response = ConfigurationManager.getValues(request);

			switch (v.getId()) {
			case R.id.button1:
				new LoginUserAsyncTask().execute(
						response.getString(ConfigurationManager.PREFIX, ""),
						response.getString(ConfigurationManager.NUM, ""), s, e);

				break;

			}
		}
	}

	private class LoginUserAsyncTask extends
			AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			b1.setEnabled(false);
			b1.setText("Loading...");
		}

		@Override
		protected JSONObject doInBackground(String... args) {

			JSONObject parameters = new JSONObject();
			JSONObject result = null;

			try {
				parameters.accumulate("action", "LOGIN_USER");
				parameters.accumulate("prefix", args[0]);
				parameters.accumulate("num", args[1]);
				parameters.accumulate("smsCode", args[2]);
				parameters.accumulate("emailCode", args[3]);

				result = Utility.doPostRequest(Settings.SERVER_URL, parameters);

			} catch (HttpException e) {
				result = null;
			} catch (Exception e) {
				result = null;
			}

			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			try {

				if (result == null) {
					b1.setEnabled(true);
					b1.setText("Conferma");

				} else if (result.getString("errorCode").equals("OK")) {
					userLogged(result);
				} else {
					b1.setEnabled(true);
					b1.setText("Conferma");

				}
			} catch (Exception e) {
				Toast.makeText(UMessageApplication.getContext(),
						"Nessuna connessione...", Toast.LENGTH_SHORT).show();
				b1.setEnabled(true);
				b1.setText("Conferma");

			}
		}

	}

}
