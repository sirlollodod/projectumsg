package com.lollotek.umessage.activities;

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
		email = params.getStringExtra("email");

		Toast msg = Toast.makeText(UMessageApplication.getContext(), "email:"
				+ email + "\nprefix:" + prefix + "\nnum:" + num,
				Toast.LENGTH_SHORT);
		msg.show();

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

			Configuration configuration = new Configuration();
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

	public void userLogged(JSONObject result) {
		Configuration configuration = Utility
				.getConfiguration(UMessageApplication.getContext());
		try {
			configuration.setSessid(result.getString("sessionId"));
			configuration.setSimIsLogging(false);
			Utility.setConfiguration(UMessageApplication.getContext(),
					configuration);
			Toast msg = Toast.makeText(UMessageApplication.getContext(),
					"Utente loggato!", Toast.LENGTH_SHORT);
			msg.show();

			Intent i = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.activities.ConversationsList.class);
			startActivity(i);

		} catch (Exception e) {

		}

	}

	public class ButtonClickedListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String s = smsCode.getText().toString();
			String e = emailCode.getText().toString();

			switch (v.getId()) {
			case R.id.button1:
				new LoginUserAsyncTask().execute(prefix, num, s, e);

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
