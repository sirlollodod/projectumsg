package com.lollotek.umessage.activities;

import java.io.File;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Login extends Activity {

	EditText smsCode, emailCode;
	Button b1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		smsCode = (EditText) findViewById(R.id.editText1);
		emailCode = (EditText) findViewById(R.id.editText2);
		b1 = (Button) findViewById(R.id.button1);

		Configuration configuration = Utility
				.getConfiguration(UMessageApplication.getContext());
		configuration.setSimIsLogging(true);
		Utility.setConfiguration(UMessageApplication.getContext(),
				configuration);

		b1.setOnClickListener(new ButtonClickedListener());

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset:

			Configuration configuration = Utility
					.getConfiguration(UMessageApplication.getContext());
			configuration.setSimIsLogging(false);
			configuration.setEmail("");
			configuration.setPrefix("");
			configuration.setNum("");
			configuration.setSessid("");
			Utility.setConfiguration(UMessageApplication.getContext(),
					configuration);

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

		File[] contactsProfileImages = contactsProfileImageFolder.listFiles();
		if (contactsProfileImages.length > 0) {
			for (int i = 0; i < contactsProfileImages.length; i++) {
				if (contactsProfileImages[i].isFile()) {
					contactsProfileImages[i].delete();
				}
			}
		}
	}

	public void userLogged(JSONObject result) {
		Configuration configuration = Utility
				.getConfiguration(UMessageApplication.getContext());
		try {

			configuration.setSessid(result.getString("sessionId"));
			configuration.setSimIsLogging(false);
			if ((!configuration.getOldPrefix()
					.equals(configuration.getPrefix()))
					|| (!configuration.getOldNum().equals(
							configuration.getNum()))) {
				deleteLocalUserInformation();
				configuration.setOldPrefix(configuration.getPrefix());
				configuration.setOldNum(configuration.getNum());

			}

			Utility.setConfiguration(UMessageApplication.getContext(),
					configuration);

			Toast.makeText(UMessageApplication.getContext(),
					"Utente loggato!\n", Toast.LENGTH_SHORT).show();

			String myProfileImageUrl = result.getString("imageProfileSrc");
			Intent service = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.services.UMessageService.class);
			if (myProfileImageUrl.length() > 2) {
				service.putExtra("action",
						MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC);
				service.putExtra("imageUrl", myProfileImageUrl.substring(2));
			}

			startService(service);

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(), e.toString(),
					Toast.LENGTH_LONG).show();
		}

	}

	public class ButtonClickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String s = smsCode.getText().toString();
			String e = emailCode.getText().toString();

			Configuration configuration = Utility
					.getConfiguration(UMessageApplication.getContext());

			switch (v.getId()) {
			case R.id.button1:
				new LoginUserAsyncTask().execute(configuration.getPrefix(),
						configuration.getNum(), s, e);

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
				b1.setEnabled(true);
				b1.setText("Conferma");

			}
		}

	}
}
