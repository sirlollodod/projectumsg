package com.lollotek.umessage.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.db.DatabaseHelper;

public class SingleChatMessagesAdapter extends SimpleCursorAdapter {

	private Cursor cursor;
	private Context context;

	private static long dataLastMessage;

	public SingleChatMessagesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.cursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView;
		TextView data, message;

		cursor.moveToPosition(position);

		String direction = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_DIRECTION));
		String messageValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
		String dataValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_DATA));

		if (direction.equals("0")) {
			rowView = inflater
					.inflate(R.layout.chatmessage_sent, parent, false);
		} else {
			rowView = inflater.inflate(R.layout.chatmessage_received, parent,
					false);
		}

		data = (TextView) rowView.findViewById(R.id.textView1);
		message = (TextView) rowView.findViewById(R.id.textView2);

		if (position % 3 == 0) {
			data.setText(dataValue);
		} else {
			data.setVisibility(View.GONE);
		}

		message.setText(messageValue);

		return rowView;
	}

}
