package com.lollotek.umessage.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Login extends Activity {

	EditText smsCode, emailCode;
	Button b1;
	String prefix, num, email, serialSim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		smsCode = (EditText) findViewById(R.id.editText1);
		emailCode = (EditText) findViewById(R.id.editText2);
		b1 = (Button) findViewById(R.id.button1);

		Intent params = getIntent();
		prefix = params.getStringExtra("prefix");
		num = params.getStringExtra("num");
		serialSim = params.getStringExtra("serialSim");

		b1.setOnClickListener(new ButtonClickedListener());

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void userLogged(JSONObject result) {
		Configuration configuration = Utility
				.getConfiguration(UMessageApplication.getContext());
		try {
			configuration.setPrefix(result.getString("prefix"));
			configuration.setNum(result.getString("num"));
			configuration.setSimserial(result.getString("serialSim"));
			configuration.setSessid(result.getString("sessId"));
			configuration.setSimIsLogging(false);
			Utility.setConfiguration(UMessageApplication.getContext(),
					configuration);

			// utente loggato e configurazione impostata con le info necessarie,
			// da lanciare l'activity che mostra conversazioni

		} catch (Exception e) {

		}

	}

	public class ButtonClickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.button1:
				new LoginUserAsyncTask().execute(prefix, num, serialSim);

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
				parameters.accumulate("serialSim", args[2]);

				result = Utility.doPostRequest(Settings.SERVER_URL, parameters);

			} catch (Exception e) {

			}

			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			try {
				if (result == null) {

				} else if (result.getString("errorCode") == "OK") {
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
