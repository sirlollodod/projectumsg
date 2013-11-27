package com.lollotek.umessage.activities;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.adapters.SingleChatMessagesAdapter;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class SingleChatContact extends Activity {

	String[] fromColumns = {};
	int[] toViews = {};
	ListView listView;

	String name, prefix, num, iconSrc;

	Provider p;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_singlechatcontact);
		Intent parameter = getIntent();
		ActionBar ab = getActionBar();
		name = parameter.getStringExtra("name");
		prefix = parameter.getStringExtra("prefix");
		num = parameter.getStringExtra("num");
		iconSrc = parameter.getStringExtra("iconSrc");

		p = new Provider(UMessageApplication.getContext());
		Cursor userInfo = p.getUserInfo(prefix, num);

		if ((userInfo != null) && (userInfo.moveToNext())) {
			String imageSrc = userInfo.getString(userInfo
					.getColumnIndex(DatabaseHelper.KEY_IMGSRC));
			long imageData = Long.parseLong(userInfo.getString(userInfo
					.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));

		}

		if (name.equals("0")) {
			ab.setTitle("Sconosciuto");
		} else {
			ab.setTitle(name);
		}
		ab.setSubtitle(prefix + " " + num);

		ab.setDisplayHomeAsUpEnabled(true);

		listView = (ListView) findViewById(R.id.listView1);

	}

	@Override
	protected void onResume() {
		super.onResume();
		loadMessages(name, prefix, num);

	}

	private void loadMessages(String name, String prefix, String num) {

		p = new Provider(UMessageApplication.getContext());

		Cursor messages = p.getMessages(prefix, num);

		int previousPosition = messages.getPosition();
		int startPosition = messages.getCount();
		boolean isSomeNewMessages = false;
		if (messages.moveToLast()) {
			do {
				if (messages.getString(
						messages.getColumnIndex(DatabaseHelper.KEY_TOREAD))
						.equals("1")) {
					if (!isSomeNewMessages) {
						isSomeNewMessages = true;
					}

					startPosition = messages.getPosition();
				} else {
					break;
				}

			} while (messages.moveToPrevious());
		}

		messages.moveToPosition(previousPosition);

		//
		Calendar c = Calendar.getInstance();
		c.get(Calendar.DAY_OF_MONTH);
		int lastYear, lastMonth, lastDay, lastPosition, actualYear, actualMonth, actualDay;
		Bundle indexOfFirstOfDay = new Bundle();

		if (messages.moveToLast()) {
			c.setTimeInMillis(Long.parseLong(messages.getString(messages
					.getColumnIndex(DatabaseHelper.KEY_DATA))));
			lastYear = c.get(Calendar.YEAR);
			lastMonth = c.get(Calendar.MONTH);
			lastDay = c.get(Calendar.DAY_OF_MONTH);
			lastPosition = messages.getPosition();

			while (messages.moveToPrevious()) {
				c.setTimeInMillis(Long.parseLong(messages.getString(messages
						.getColumnIndex(DatabaseHelper.KEY_DATA))));
				actualYear = c.get(Calendar.YEAR);
				actualMonth = c.get(Calendar.MONTH);
				actualDay = c.get(Calendar.DAY_OF_MONTH);

				if ((actualDay != lastDay) || (actualMonth != lastMonth)
						|| (actualYear != lastYear)) {
					indexOfFirstOfDay.putBoolean("" + lastPosition, true);
					lastYear = actualYear;
					lastMonth = actualMonth;
					lastDay = actualDay;

				}

				lastPosition = messages.getPosition();

			}

			indexOfFirstOfDay.putBoolean("" + lastPosition, true);

		}

		messages.moveToPosition(previousPosition);

		//

		SingleChatMessagesAdapter adapter = new SingleChatMessagesAdapter(this,
				R.layout.usercontact, messages, fromColumns, toViews, 0,
				(isSomeNewMessages ? startPosition : -1));
		adapter.setIndexFirstOfDay(indexOfFirstOfDay);

		listView.setAdapter(adapter);

		listView.setSelection(startPosition);

		p.markMessagesAsRed(prefix, num);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.singlechatcontact, menu);
		return true;
	}

}
