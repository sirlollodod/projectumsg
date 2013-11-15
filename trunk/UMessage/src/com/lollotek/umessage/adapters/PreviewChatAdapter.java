package com.lollotek.umessage.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.db.DatabaseHelper;

public class PreviewChatAdapter extends SimpleCursorAdapter {

	private Cursor cursor;
	private Context context;

	public PreviewChatAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.cursor = c;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View newRow;

		newRow = inflater.inflate(R.layout.chatpreview, parent, false);
		TextView name, message, time;

		cursor.moveToPosition(position);

		name = (TextView) newRow.findViewById(R.id.textView1);
		message = (TextView) newRow.findViewById(R.id.textView2);
		time = (TextView) newRow.findViewById(R.id.textView3);

		String nameValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_NAME));
		String messageValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
		String dataValue = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_DATA));

		message.setText(messageValue);
		if (nameValue.equals("0")) {
			name.setText("Sconosciuto");
		} else {
			name.setText(nameValue);
		}

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(Long.parseLong(dataValue));
		String dataFormattedValue = "" + c.get(Calendar.DAY_OF_MONTH) + "/"
				+ (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR) + "  " + c.get(Calendar.HOUR_OF_DAY) + ":" + (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE): c.get(Calendar.MINUTE));

		time.setText(dataFormattedValue);
		if (position % 2 == 0) {
			newRow.setBackgroundColor(Color.rgb(247, 247, 247));
		} else {
			newRow.setBackgroundColor(Color.rgb(229, 229, 229));
		}

		return newRow;
	}

	/*
	 * @Override public void bindView(View view, Context context, Cursor c) {
	 * super.bindView(view, context, c);
	 * 
	 * int position = c.getPosition(); if (position % 2 == 0) {
	 * view.setBackgroundColor(Color.rgb(247, 247, 247)); } else {
	 * view.setBackgroundColor(Color.rgb(229, 229, 229)); }
	 * 
	 * }
	 */

}
