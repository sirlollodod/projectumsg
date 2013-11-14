package com.lollotek.umessage.activities;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Registration extends Activity {

	TextView emailText;
	EditText prefix, num, email;
	Button b1, b2;
	String serialSim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_registration);
		serialSim = Utility.getSerialSim(UMessageApplication.getContext());
		emailText = (TextView) findViewById(R.id.textView3);
		prefix = (EditText) findViewById(R.id.editText1);
		num = (EditText) findViewById(R.id.editText2);
		email = (EditText) findViewById(R.id.editText3);
		b1 = (Button) findViewById(R.id.button1);
		b2 = (Button) findViewById(R.id.button2);

		b1.setOnClickListener(new ButtonClickedListener());
		b2.setOnClickListener(new ButtonClickedListener());

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Bisognerebbe cancellare il db locale contenente tutti i dati relativi
		// all'account precedentemente loggato

	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}
	

	public void startLoginActivity(JSONObject userInfo) {
		Intent i = new Intent(UMessageApplication.getContext(),
				com.lollotek.umessage.activities.Login.class);

		try {
			String prefix = userInfo.getString("prefix");
			String num = userInfo.getString("num");
			String email = userInfo.getString("email");

			i.putExtra("prefix", prefix);
			i.putExtra("num", num);
			i.putExtra("email", email);

			Configuration configuration = Utility
					.getConfiguration(UMessageApplication.getContext());

			configuration.setPrefix(prefix);
			configuration.setNum(num);
			configuration.setSimIsLogging(true);
			configuration.setSimserial(serialSim);
			configuration.setEmail(email);

			Utility.setConfiguration(UMessageApplication.getContext(),
					configuration);

		} catch (Exception e) {

		}

		startActivity(i);
	}

	public class ButtonClickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String p = prefix.getText().toString();
			String n = num.getText().toString();

			switch (v.getId()) {
			case R.id.button1:
				new CheckUserAlreadyregisteredAsyncTask().execute(p, n);

				break;

			case R.id.button2:
				String e = email.getText().toString();
				new RegisterUserAsyncTask().execute(p, n, e);

				break;
			}
		}
	}

	private class CheckUserAlreadyregisteredAsyncTask extends
			AsyncTask<String, Void, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			prefix.setEnabled(false);
			num.setEnabled(false);
			b1.setEnabled(false);
			b1.setText("Loading...");

		}

		@Override
		protected JSONObject doInBackground(String... args) {
			JSONObject parameters = new JSONObject();
			JSONObject result = null;

			try {
				parameters.accumulate("action", "CHECK_USER_REGISTERED");
				parameters.accumulate("prefix", "+" + args[0]);
				parameters.accumulate("num", args[1]);

				result = Utility.doPostRequest(Settings.SERVER_URL, parameters);
				if ((result.getString("errorCode").equals("OK"))
						&& result.getBoolean("isRegistered")) {
					parameters = new JSONObject();
					parameters.accumulate("action", "REGISTER_USER");
					parameters.accumulate("prefix", "+" + args[0]);
					parameters.accumulate("num", args[1]);
					result = Utility.doPostRequest(Settings.SERVER_URL,
							parameters);

					return result;

				} else {
					return result;
				}
			}
			catch (HttpException e){
				result = null;
			}
			catch (Exception e) {
				result = null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			try {

				if (result == null) {
					Toast msg = Toast.makeText(
							UMessageApplication.getContext(), "Errore:1",
							Toast.LENGTH_SHORT);
					msg.show();
				} else if ((result.getString("errorCode").equals("OK"))
						&& (result.getBoolean("isRegistered"))) {
					// utente gia esistente, codici inviati a mail e per sms dal
					// sistema PHP. Da avviare activity login
					startLoginActivity(result);

				} else {
					// utente non esistente, richiesta anche la mail da
					// associare
					b1.setVisibility(View.GONE);
					emailText.setVisibility(View.VISIBLE);
					email.setVisibility(View.VISIBLE);
					b2.setVisibility(View.VISIBLE);

				}
			} catch (Exception e) {
				Toast msg = Toast.makeText(UMessageApplication.getContext(),
						"Errore:2", Toast.LENGTH_SHORT);
				msg.show();
			}

		}
	}

	private class RegisterUserAsyncTask extends
			AsyncTask<String, Integer, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			email.setEnabled(false);
			b2.setEnabled(false);
			b2.setText("Loading...");
		}

		@Override
		protected JSONObject doInBackground(String... args) {
			JSONObject parameters = new JSONObject();
			JSONObject result = null;

			try {
				parameters.accumulate("action", "REGISTER_USER");
				parameters.accumulate("prefix", "+" + args[0]);
				parameters.accumulate("num", args[1]);
				parameters.accumulate("email", args[2]);

				result = Utility.doPostRequest(Settings.SERVER_URL, parameters);

			}
			catch(HttpException e){
				result = null;
			}
			catch (Exception e) {
				result = null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			try {
				if ((result == null)) {
					Toast msg = Toast.makeText(
							UMessageApplication.getContext(), "Errore:3",
							Toast.LENGTH_SHORT);
					msg.show();
				} else if (result.getString("errorCode").equals("OK")) {
					// utente registrato, codici inviati per sms e mail dal
					// sistema
					// PHP. avviare login activity
					startLoginActivity(result);
				} else {
					b2.setEnabled(true);
					b2.setText("Conferma");

					Toast msg = Toast.makeText(
							UMessageApplication.getContext(),
							"errore registrazione user", Toast.LENGTH_SHORT);
					msg.show();
				}
			} catch (Exception e) {
				Toast msg = Toast.makeText(UMessageApplication.getContext(),
						"Errore:4", Toast.LENGTH_SHORT);
				msg.show();
			}
		}

	}

}
