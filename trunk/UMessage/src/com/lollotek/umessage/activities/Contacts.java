package com.lollotek.umessage.activities;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Contacts extends Activity {

	TextView title, loading;
	ListView listView;
	String[] fromColumns = { DatabaseHelper.KEY_NAME,
			DatabaseHelper.KEY_PREFIX, DatabaseHelper.KEY_NUM };
	int[] toViews = { R.id.textView1, R.id.textView2, R.id.textView3 };
	Context context = null;

	LoadUserContactsAsyncTask loadUserContactAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		context = this;

		title = (TextView) findViewById(R.id.textView1);
		loading = (TextView) findViewById(R.id.textView2);

		listView = (ListView) findViewById(R.id.listView1);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = ((SimpleCursorAdapter) listView.getAdapter())
						.getCursor();

				c.moveToPosition(position);

				Intent i = new Intent(
						context,
						com.lollotek.umessage.activities.SingleChatContact.class);
				i.putExtra("prefix", c.getString(c
						.getColumnIndex(DatabaseHelper.KEY_PREFIX)));
				i.putExtra("num",
						c.getString(c.getColumnIndex(DatabaseHelper.KEY_NUM)));
				i.putExtra("name",
						c.getString(c.getColumnIndex(DatabaseHelper.KEY_NAME)));

				startActivity(i);

			}
		});

		loading.setVisibility(View.GONE);

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadUsers();

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void loadUsers() {

		Provider p = new Provider(UMessageApplication.getContext());
		Cursor users = p.getTotalUser();

		title.setText("Totale utenti registrati: " + users.getCount() + "\n\n");

		if (users.getCount() > 0) {

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
					R.layout.usercontact, users, fromColumns, toViews, 0);
			listView.setAdapter(adapter);
		}

	}

	private class LoadUserContactsAsyncTask extends
			AsyncTask<Void, Integer, Integer> {

		private int totalPhoneContacts;

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			loading.setText("Caricamento contatti... [ " + values[0] + "/"
					+ totalPhoneContacts + " ]");
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);
			loading.setText("Caricamento contatti...");
		}

		@Override
		protected Integer doInBackground(Void... params) {

			Cursor phones = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);

			PhoneNumber num;
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			int numMobileContactsLoaded = 0;

			Provider p = new Provider(UMessageApplication.getContext());
			ContentValues value;
			JSONObject parameters;
			JSONObject result;

			totalPhoneContacts = phones.getCount();
			int actualPhoneContact = 0;

			Configuration configuration = Utility
					.getConfiguration(UMessageApplication.getContext());
			String myPrefix = configuration.getPrefix();
			String myNum = configuration.getNum();

			while (phones.moveToNext()) {
				publishProgress(++actualPhoneContact);

				parameters = new JSONObject();
				result = null;
				value = new ContentValues();

				if (isCancelled()) {
					return numMobileContactsLoaded;
				}

				String name = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String phoneNumber = phones
						.getString(phones
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				num = null;

				try {
					num = phoneUtil.parseAndKeepRawInput(phoneNumber, "IT");
				} catch (NumberParseException e) {
					continue;
				}

				phoneUtil.format(num, PhoneNumberFormat.INTERNATIONAL);

				if (myPrefix.equals("+" + num.getCountryCode())
						&& myNum.equals("" + num.getNationalNumber())) {
					continue;
				}

				if (phoneUtil.getNumberType(num) == PhoneNumberType.MOBILE) {

					try {
						parameters
								.accumulate("action", "CHECK_USER_REGISTERED");
						parameters.accumulate("prefix",
								"+" + num.getCountryCode());
						parameters.accumulate("num", num.getNationalNumber());
						parameters.accumulate("anonymous", "yes");

						result = Utility.doPostRequest(Settings.SERVER_URL,
								parameters);

						if ((!result.getString("errorCode").equals("OK"))
								|| !result.getBoolean("isRegistered")) {
							continue;
						}

						value.put(DatabaseHelper.KEY_PREFIX,
								"+" + num.getCountryCode());
						value.put(
								DatabaseHelper.KEY_NUM,
								(num.isItalianLeadingZero() ? "0" : "")
										+ num.getNationalNumber());
						value.put(DatabaseHelper.KEY_NAME, name);

						if (p.insertNewUser(value)) {
							numMobileContactsLoaded++;
						}
					} catch (HttpException e) {
						return numMobileContactsLoaded;
					} catch (Exception e) {

					}

				} else {
					continue;
				}

			}
			phones.close();

			// Dovrebbe anche controllare tra tutti quelli attualmente inseriti
			// nel db locale quali non sono registrati al servizio, e rimuoverli
			// eventualmente

			return numMobileContactsLoaded;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			Toast msg = Toast.makeText(UMessageApplication.getContext(),
					"Importati " + result + " contatti.", Toast.LENGTH_SHORT);
			msg.show();
			loading.setVisibility(View.GONE);
			loading.setText("");
			loadUsers();
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
			loading.setVisibility(View.GONE);
			loading.setText("");
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:

			if (loadUserContactAsyncTask == null) {
				loadUserContactAsyncTask = new LoadUserContactsAsyncTask();
				loadUserContactAsyncTask.execute();
			}

			break;

		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;

		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}

}
