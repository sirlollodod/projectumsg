package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.adapters.PreviewChatAdapter;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class ConversationsList extends Activity {

	private Context context = null;

	ListView listView;
	String[] fromColumns = { DatabaseHelper.KEY_NAME,
			DatabaseHelper.KEY_MESSAGE, DatabaseHelper.KEY_DATA };

	int[] toViews = { R.id.textView1, R.id.textView2, R.id.textView3 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_conversationslist);

		context = this;

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

				startActivity(i);
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadConversations();
	}

	private void loadConversations() {

		Provider p = new Provider(UMessageApplication.getContext());
		Cursor users = p.getConversations();

		PreviewChatAdapter adapter = new PreviewChatAdapter(this,
				R.layout.chatpreview, users, fromColumns, toViews, 0);
		listView.setAdapter(adapter);

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

		}

		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversationslist, menu);
		return true;
	}

}
