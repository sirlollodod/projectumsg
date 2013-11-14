package com.lollotek.umessage.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class PreviewChatAdapter extends SimpleCursorAdapter {

	private Cursor cursor;

	public PreviewChatAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

		this.cursor = c;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View newView = super.getView(position, convertView, parent);

		cursor.moveToPosition(position);

		if (position % 2 == 0) {
			newView.setBackgroundColor(Color.rgb(247, 247, 247));
		} else {
			newView.setBackgroundColor(Color.rgb(229, 229, 229));
		}

		return newView;
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
