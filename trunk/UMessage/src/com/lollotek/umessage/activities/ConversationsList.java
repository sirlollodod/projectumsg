package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.adapters.PreviewChatAdapter;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.listeners.SynchronizationListener;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Utility;

public class ConversationsList extends Activity {

	private static final String TAG = ConversationsList.class.getName() + ":\n";

	private Context context = null;

	ListView listView;
	String[] fromColumns = { DatabaseHelper.KEY_IMGSRC,
			DatabaseHelper.KEY_NAME, DatabaseHelper.KEY_MESSAGE,
			DatabaseHelper.KEY_DATA };

	int[] toViews = { R.id.imageView1, R.id.textView1, R.id.textView2,
			R.id.textView3 };

	private static int firstConversationDisplayed;

	private SynchronizationListener syncListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_conversationslist);

		context = this;

		syncListener = new SynchronizationListener() {

			@Override
			public void onStart(Message msg) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgress(Message msg) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish(final Message msg) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						switch (msg.what) {
						case MessageTypes.MESSAGE_UPDATE:
							loadConversations();
							break;
						}

					}

				});

			}

		};

		listView = (ListView) findViewById(R.id.listView1);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				view.setBackgroundColor(Color.rgb(247, 250, 167));

				Cursor c = ((PreviewChatAdapter) listView.getAdapter())
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
				i.putExtra("iconSrc", c.getString(c
						.getColumnIndex(DatabaseHelper.KEY_IMGSRC)));

				startActivity(i);
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		SynchronizationManager.getInstance().registerSynchronizationListener(
				syncListener);

		loadConversations();

	}

	private void loadConversations() {

		Provider p = new Provider(UMessageApplication.getContext());
		Cursor users = p.getConversations();
		Cursor newMessagesCount = p.getConversationsNewMessages();

		Bundle b = new Bundle();

		while (newMessagesCount.moveToNext()) {
			b.putInt(newMessagesCount.getString(newMessagesCount
					.getColumnIndex(DatabaseHelper.KEY_IDCHAT)), Integer
					.parseInt(newMessagesCount.getString(newMessagesCount
							.getColumnIndex("count"))));
		}

		PreviewChatAdapter adapter = new PreviewChatAdapter(this,
				R.layout.chatpreview, users, fromColumns, toViews, 0);
		adapter.setNewMessagesCount(b);
		listView.setAdapter(adapter);

		try {
			listView.setSelection(firstConversationDisplayed);
		} catch (Exception e) {
			listView.setSelection(0);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		SynchronizationManager.getInstance().unregisterSynchronizationListener(
				syncListener);

		firstConversationDisplayed = listView.getFirstVisiblePosition();
	}

	@Override
	protected void onStop() {
		super.onStop();

		firstConversationDisplayed = listView.getFirstVisiblePosition();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {

		case R.id.contacts:

			i = new Intent(this,
					com.lollotek.umessage.activities.Contacts.class);
			startActivity(i);

			break;

		case R.id.settings:

			i = new Intent(this,
					com.lollotek.umessage.activities.UMessageSettings.class);
			startActivity(i);

			break;

		case R.id.profile:

			i = new Intent(this, com.lollotek.umessage.activities.Profile.class);
			startActivity(i);

			break;

		case R.id.logout:

			Configuration configuration = Utility.getConfiguration(context);
			configuration.setSessid("");
			Utility.setConfiguration(context, configuration);

			Intent service = new Intent(this,
					com.lollotek.umessage.services.UMessageService.class);
			stopService(service);

			i = new Intent(this, com.lollotek.umessage.activities.Main.class);
			startActivity(i);
			finish();

			break;

		case R.id.map:
			i = new Intent(this, com.lollotek.umessage.activities.Map.class);
			startActivity(i);
			break;

		case R.id.dropbox:
			i = new Intent(this, com.lollotek.umessage.activities.Dropbox.class);
			startActivity(i);

			break;
		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversationslist, menu);
		
		//elementi di prova
		menu.removeItem(R.id.map);
		menu.removeItem(R.id.settings);
		return true;
	}

}
