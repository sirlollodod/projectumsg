package com.lollotek.umessage.adapters;

import java.util.Calendar;

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

	private static int firstToReadPosition;

	public SingleChatMessagesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, int firstToReadPosition) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.cursor = c;
		SingleChatMessagesAdapter.firstToReadPosition = firstToReadPosition;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView;
		TextView data, message, newMessages;

		cursor.moveToPosition(position);

		String directionValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_DIRECTION));
		String messageValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
		String dataValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_DATA));

		if (directionValue.equals("0")) {
			rowView = inflater
					.inflate(R.layout.chatmessage_sent, parent, false);
		} else {
			rowView = inflater.inflate(R.layout.chatmessage_received, parent,
					false);
		}

		data = (TextView) rowView.findViewById(R.id.textView1);
		message = (TextView) rowView.findViewById(R.id.textView2);

		if (position == firstToReadPosition) {
			newMessages = (TextView) rowView.findViewById(R.id.textView3);
			newMessages.setVisibility(View.VISIBLE);
		}
		Calendar c = Calendar.getInstance();

		c.setTimeInMillis(Long.parseLong(dataValue));
		boolean isMessageOfToday = (Calendar.getInstance().get(
				Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH));
		String dataFormattedValue;

		if (isMessageOfToday) {
			dataFormattedValue = ""
					+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0"
							+ c.get(Calendar.HOUR_OF_DAY) : c
							.get(Calendar.HOUR_OF_DAY))
					+ ":"
					+ (c.get(Calendar.MINUTE) < 10 ? "0"
							+ c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE));
			;
		} else {
			dataFormattedValue = ""
					+ (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0"
							+ c.get(Calendar.DAY_OF_MONTH) : c
							.get(Calendar.DAY_OF_MONTH))
					+ "/"
					+ ((c.get(Calendar.MONTH) + 1) < 10 ? "0"
							+ (c.get(Calendar.MONTH) + 1) : (c
							.get(Calendar.MONTH) + 1)) + "/"
					+ c.get(Calendar.YEAR);

		}

		if (position % 3 == 0) {
			data.setText(dataFormattedValue);
		} else {
			data.setVisibility(View.GONE);
		}

		message.setText(messageValue);

		return rowView;
	}

}