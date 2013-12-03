package com.lollotek.umessage.activities;

import java.io.File;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.adapters.SingleChatMessagesAdapter;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class SingleChatContact extends Activity {

	String[] fromColumns = {};
	int[] toViews = {};
	ListView listView;

	String name, prefix, num, iconSrc;

	Provider p;
	Context context;

	private int firstMessageDisplayed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;

		firstMessageDisplayed = -1;
		setContentView(R.layout.activity_singlechatcontact);
		Intent parameter = getIntent();
		ActionBar ab = getActionBar();
		ab.setCustomView(R.layout.actionbar_singlechatcontact);
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_HOME_AS_UP);

		ab.setDisplayHomeAsUpEnabled(true);

		prefix = parameter.getStringExtra("prefix");
		num = parameter.getStringExtra("num");

		TextView nameView, prefixView, numView;
		nameView = (TextView) ab.getCustomView().findViewById(R.id.textView1);
		prefixView = (TextView) ab.getCustomView().findViewById(R.id.textView2);
		numView = (TextView) ab.getCustomView().findViewById(R.id.textView3);

		ImageView iconView = (ImageView) ab.getCustomView().findViewById(
				R.id.imageView2);

		Button sendButton = (Button) findViewById(R.id.button1);

		final EditText messageEditText = (EditText) findViewById(R.id.editText1);

		final ImageView backImageView = (ImageView) ab.getCustomView()
				.findViewById(R.id.imageView1);

		p = new Provider(UMessageApplication.getContext());
		Cursor userInfo = p.getUserInfo(prefix, num);

		if ((userInfo != null) && (userInfo.moveToNext())) {
			name = userInfo.getString(userInfo
					.getColumnIndex(DatabaseHelper.KEY_NAME));
			iconSrc = userInfo.getString(userInfo
					.getColumnIndex(DatabaseHelper.KEY_IMGSRC));
			long imageData = Long.parseLong(userInfo.getString(userInfo
					.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));
			if (!iconSrc.equals("0")) {
				File icon = new File(Utility.getMainFolder(UMessageApplication
						.getContext())
						+ Settings.CONTACT_PROFILE_IMAGES_FOLDER
						+ iconSrc);
				iconView.setImageURI(Uri.fromFile(icon));
			}
			nameView.setText(name);

		} else {
			nameView.setText("Sconosciuto");
		}

		prefixView.setText(prefix);
		numView.setText(num);

		backImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				p.markMessagesAsRed(prefix, num);

				backImageView.setAlpha((float) 0.8);
				try {
					NavUtils.navigateUpFromSameTask((Activity) context);
				} catch (Exception e) {
					finish();
				}

			}
		});

		listView = (ListView) findViewById(R.id.listView1);

		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String messageText = messageEditText.getText().toString();

				if (messageText.length() > 0) {
					messageEditText.setText("");
					Intent service = new Intent(
							context,
							com.lollotek.umessage.services.UMessageService.class);

					service.putExtra("action",
							MessageTypes.SEND_NEW_TEXT_MESSAGE);
					service.putExtra("messageText", messageText);
					service.putExtra("prefix", prefix);
					service.putExtra("num", num);

					startService(service);
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		loadMessages(name, prefix, num);

	}

	@Override
	protected void onPause() {
		super.onPause();

		firstMessageDisplayed = listView.getFirstVisiblePosition();
	}

	@Override
	protected void onStop() {
		super.onStop();

		firstMessageDisplayed = listView.getFirstVisiblePosition();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		p.markMessagesAsRed(prefix, num);
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

		if (firstMessageDisplayed != -1) {
			listView.setSelection(firstMessageDisplayed);
		} else {
			listView.setSelection(startPosition);
		}

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
