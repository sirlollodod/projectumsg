package com.lollotek.umessage.activities;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.app.ActionBar;
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
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.adapters.ContactAdapter;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.ConfigurationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class Contacts extends Activity {

	private static final String TAG = Contacts.class.getName() + ":\n";

	static TextView title, loading;
	ListView listView;
	String[] fromColumns = { DatabaseHelper.KEY_NAME,
			DatabaseHelper.KEY_PREFIX, DatabaseHelper.KEY_NUM };
	int[] toViews = { R.id.textView1, R.id.textView2, R.id.textView3 };
	Context context = null;
	private static int firstContactDisplayed;

	private Bundle request, response;

	static LoadUserContactsAsyncTask loadUserContactAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		context = this;
		firstContactDisplayed = -1;
		title = (TextView) findViewById(R.id.textView1);
		loading = (TextView) findViewById(R.id.textView2);
		listView = (ListView) findViewById(R.id.listView1);
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

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

		if (loadUserContactAsyncTask == null) {
			loading.setVisibility(View.GONE);
		} else {
			loading.setText("Caricamento contatti...");
			loading.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		loadUsers();

	}

	@Override
	protected void onPause() {
		super.onPause();

		firstContactDisplayed = listView.getFirstVisiblePosition();
	}

	@Override
	protected void onStop() {
		super.onStop();

		firstContactDisplayed = listView.getFirstVisiblePosition();
	}

	private void loadUsers() {

		Provider p = new Provider(UMessageApplication.getContext());
		Cursor users = p.getTotalUser();

		title.setText("Totale utenti registrati: " + users.getCount() + "\n\n");

		if (users.getCount() > 0) {

			ContactAdapter adapter = new ContactAdapter(this,
					R.layout.usercontact, users, fromColumns, toViews, 0);
			listView.setAdapter(adapter);
		}

		if (firstContactDisplayed != -1) {
			listView.setSelection(firstContactDisplayed);
		} else {
			listView.setSelection(0);
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

			request = new Bundle();
			request.putBoolean(ConfigurationManager.PREFIX, true);
			request.putBoolean(ConfigurationManager.NUM, true);
			response = ConfigurationManager.getValues(request);

			String myPrefix = response.getString(ConfigurationManager.PREFIX,
					"");
			String myNum = response.getString(ConfigurationManager.NUM, "");

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
				String prefixNum = "+" + num.getCountryCode();
				String phoneNum = (num.isItalianLeadingZero() ? "0" : "")
						+ num.getNationalNumber();

				if (myPrefix.equals(prefixNum)
						&& myNum.equals("" + num.getNationalNumber())) {
					continue;
				}

				if (phoneUtil.getNumberType(num) == PhoneNumberType.MOBILE) {

					// errore: null pointer exception in questo try/catch...
					// forse risolto
					try {
						// Controllo che user non sia gia inserito nel db locale
						Cursor localUser = p.getUserInfo(prefixNum, phoneNum);
						if ((localUser != null) && (localUser.moveToFirst())) {
							continue;
						}

						// User non presente nel db locale, controllo se è
						// registrato online
						parameters
								.accumulate("action", "CHECK_USER_REGISTERED");
						parameters.accumulate("prefix", prefixNum);
						parameters.accumulate("num", phoneNum);
						parameters.accumulate("anonymous", "yes");

						result = Utility.doPostRequest(Settings.SERVER_URL,
								parameters);

						if (result == null) {
							continue;
						}

						if ((!result.getString("errorCode").equals("OK"))
								|| !result.getBoolean("isRegistered")) {
							continue;
						}

						value.put(DatabaseHelper.KEY_PREFIX, prefixNum);
						value.put(DatabaseHelper.KEY_NUM, phoneNum);
						value.put(DatabaseHelper.KEY_NAME, name);
						value.put(DatabaseHelper.KEY_IMGSRC, "0");
						value.put(DatabaseHelper.KEY_IMGDATA, "0");

						if (p.insertNewUser(value)) {
							numMobileContactsLoaded++;
						}

						// Controllo adesso singolo contatto se immagine profilo
						// da scaricare/aggiornare
						String userImageUrl = result
								.getString("imageProfileSrc");
						if (userImageUrl.length() > 2) {
							userImageUrl = userImageUrl.substring(2);
							Intent service = new Intent(
									UMessageApplication.getContext(),
									com.lollotek.umessage.services.UMessageService.class);
							service.putExtra("action",
									MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC);
							service.putExtra("imageUrl", userImageUrl);
							service.putExtra("prefix", prefixNum);
							service.putExtra("num", phoneNum);

							startService(service);

						}

					} catch (HttpException e) {
						return numMobileContactsLoaded;
					} catch (Exception e) {
						Utility.reportError(UMessageApplication.getContext(),
								e, TAG + ": doInBackground()");
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
			loadUserContactAsyncTask = null;
		}

		@Override
		protected void onCancelled(Integer result) {
			super.onCancelled(result);
			loading.setVisibility(View.GONE);
			loading.setText("");
			loadUserContactAsyncTask = null;
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

			try {
				NavUtils.navigateUpFromSameTask(this);
			} catch (Exception e) {
				finish();
			}

			break;

		}

		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contacts, menu);
		return true;
	}

}
