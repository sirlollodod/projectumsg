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
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class SingleChatContact extends Activity {

	String[] fromColumns = {};
	int[] toViews = {};
	ListView listView;

	String name, prefix, num, iconSrc;

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

		int previousPosition = messages.getPosition();
		int startPosition = messages.getCount();
		boolean isSomeNewMessages = false;
		if(messages.moveToLast()){
			do{
			if(messages.getString(messages.getColumnIndex(DatabaseHelper.KEY_TOREAD)).equals("1")){
				if(!isSomeNewMessages){
					isSomeNewMessages = true;
				}
				
				startPosition = messages.getPosition();
			}
			else{
				break;
			}
				
			} while(messages.moveToPrevious());
		}
		
		messages.moveToPosition(previousPosition);
		
		SingleChatMessagesAdapter adapter = new SingleChatMessagesAdapter(this,
				R.layout.usercontact, messages, fromColumns, toViews, 0, (isSomeNewMessages ? startPosition : -1));
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
