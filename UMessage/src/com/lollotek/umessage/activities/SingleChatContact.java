package com.lollotek.umessage.activities;

import android.app.ActionBar;
import android.app.Activity;
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
import com.lollotek.umessage.db.Provider;

public class SingleChatContact extends Activity {

	String[] fromColumns = {};
	int[] toViews = {};
	ListView listView;

	String name, prefix, num;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_singlechatcontact);
		Intent parameter = getIntent();
		ActionBar ab = getActionBar();
		name = parameter.getStringExtra("name");
		prefix = parameter.getStringExtra("prefix");
		num = parameter.getStringExtra("num");
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

		Provider p = new Provider(UMessageApplication.getContext());

		Cursor messages = p.getMessages(prefix, num);

		SingleChatMessagesAdapter adapter = new SingleChatMessagesAdapter(this,
				R.layout.usercontact, messages, fromColumns, toViews, 0);
		listView.setAdapter(adapter);

		listView.setSelection(messages.getCount() - 15);

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
