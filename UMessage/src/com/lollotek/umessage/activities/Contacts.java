package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class Contacts extends Activity {

	TextView list1, list2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		list1 = (TextView) findViewById(R.id.textView1);
		list2 = (TextView) findViewById(R.id.textView2);

	}

	@Override
	protected void onResume() {
		super.onResume();
		loadUsers();

	}

	private void loadUsers() {
		Provider p = new Provider(UMessageApplication.getContext());
		Cursor users = p.getTotalUser();

		list1.setText("Totale utenti importati: " + users.getCount() + "\n\n");
		list2.setText("");

		while (users.moveToNext()) {
			list2.append(users.getString(users
					.getColumnIndex(DatabaseHelper.KEY_NAME))
					+ ": "
					+ users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_PREFIX))
					+ " "
					+ users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_NUM)) + "\n");

		}
	}

	private class LoadUserContactsAsyncTask extends
			AsyncTask<Void, Exception, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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

			while (phones.moveToNext()) {
				value = new ContentValues();

				if (isCancelled()) {
					return null;
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

				if (phoneUtil.getNumberType(num) == PhoneNumberType.MOBILE) {
					// Bisogna controllare qui che il numero selezionato sia
					// registrato al servizio UMessage

					value.put(DatabaseHelper.KEY_PREFIX,
							"+" + num.getCountryCode());
					value.put(
							DatabaseHelper.KEY_NUM,
							(num.isItalianLeadingZero() ? "0" : "")
									+ num.getNationalNumber());
					value.put(DatabaseHelper.KEY_NAME, name);

					try {
						if (p.insert(DatabaseHelper.TABLE_USER, null, value) != -1) {
							numMobileContactsLoaded++;
						}
					} catch (Exception e) {
						continue;
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
			loadUsers();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:

			new LoadUserContactsAsyncTask().execute();

			break;
		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}

}
