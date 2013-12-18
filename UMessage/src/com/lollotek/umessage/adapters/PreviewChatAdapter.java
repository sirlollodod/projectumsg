package com.lollotek.umessage.adapters;

import java.io.File;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lollotek.umessage.R;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class PreviewChatAdapter extends SimpleCursorAdapter {

	private static final String TAG = PreviewChatAdapter.class.getName() + ":\n";
	
	private Cursor cursor;
	private Context context;

	private File mainFolder;
	private String contactProfileImagesFolder;

	private Bundle newMessagesCount;

	public PreviewChatAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.context = context;
		this.cursor = c;
		this.mainFolder = Utility.getMainFolder(context);
		this.contactProfileImagesFolder = Settings.CONTACT_PROFILE_IMAGES_FOLDER;
	}

	public void setNewMessagesCount(Bundle b) {
		this.newMessagesCount = b;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View newRow;

		newRow = inflater.inflate(R.layout.chatpreview, parent, false);
		TextView name, message, time, news;
		ImageView icon;

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
		// boolean isSomeNewMessage = (cursor.getString(cursor
		// .getColumnIndex(DatabaseHelper.KEY_TOREAD))).equals("1");

		int newMessages = newMessagesCount.getInt(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_IDCHAT)), 0);
		if (newMessages > 0) {
			news = (TextView) newRow.findViewById(R.id.textView4);
			news.setText("" + newMessages);
			news.setVisibility(View.VISIBLE);

		}

		String iconSrc = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.KEY_IMGSRC));

		if (!iconSrc.equals("") && !iconSrc.equals("0")) {
			icon = (ImageView) newRow.findViewById(R.id.imageView1);
			File iconFile = new File(mainFolder.toString()
					+ contactProfileImagesFolder + iconSrc);
			icon.setImageURI(Uri.fromFile(iconFile));
		}
		message.setText(messageValue);
		if (nameValue.equals("0")) {
			String prefix = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.KEY_PREFIX));
			String num = cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.KEY_NUM));
			name.setText(prefix + "  " + num);
		} else {
			name.setText(nameValue);
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

		time.setText(dataFormattedValue);

		/*
		 * if (position % 2 == 0) { newRow.setBackgroundColor(Color.rgb(247,
		 * 247, 247)); } else { newRow.setBackgroundColor(Color.rgb(229, 229,
		 * 229)); }
		 */
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
