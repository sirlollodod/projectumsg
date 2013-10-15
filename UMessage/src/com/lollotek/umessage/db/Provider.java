package com.lollotek.umessage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Provider {

	private static DatabaseHelper dbHelper = null;

	public Provider(Context context) {
		if (dbHelper == null) {
			dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
		}
	}

	public long insert(String table, String nullColumnHack, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long idLastInserted = db.insert(table, nullColumnHack, values);

		return idLastInserted;
	}

	/*
	 * public int delete() {
	 * 
	 * }
	 * 
	 * public int update() {
	 * 
	 * }
	 * 
	 * public Cursor query() {
	 * 
	 * }
	 */
	public Cursor getTotalUser() {
		String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER;

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		return c;
	}
}
