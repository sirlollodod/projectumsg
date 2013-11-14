package com.lollotek.umessage.db;

import com.lollotek.umessage.UMessageApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class Provider {

	private static DatabaseHelper dbHelper = null;

	public Provider(Context context) {
		if (dbHelper == null) {
			dbHelper = DatabaseHelper.getInstance(context
					.getApplicationContext());
		}
	}

	public long insert(String table, String nullColumnHack, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long idLastInserted = -1;
		try {
			idLastInserted = db.insertOrThrow(table, nullColumnHack, values);
		} catch (Exception e) {

		}

		return idLastInserted;
	}

	public long delete(String table, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long totalRowsRemoved;
		try {
			totalRowsRemoved = db.delete(table, whereClause, whereArgs);
		} catch (Exception e) {
			totalRowsRemoved = -1;
		}

		if ((whereClause == null) && (whereArgs == null)) {
			totalRowsRemoved = -2;
		}

		return totalRowsRemoved;

	}

	/*
	 * public int update() {
	 * 
	 * }
	 * 
	 * public Cursor query() {
	 * 
	 * }
	 */
	public Cursor getTotalUser() {
		String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER
				+ " ORDER BY " + DatabaseHelper.KEY_NAME;

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		return c;
	}

	public Cursor getMessages(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = db.query(DatabaseHelper.TABLE_SINGLECHAT, new String[] {
				DatabaseHelper.KEY_ID, DatabaseHelper.KEY_IDCHAT },
				DatabaseHelper.KEY_PREFIXDEST + "=? and "
						+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
						prefix, num }, null, null, null);

		chat.moveToFirst();
		String idChat = chat.getString(chat
				.getColumnIndex(DatabaseHelper.KEY_IDCHAT));

		Cursor messages = db.query(DatabaseHelper.TABLE_SINGLECHATMESSAGES,
				new String[] { DatabaseHelper.KEY_ID,
						DatabaseHelper.KEY_DIRECTION, DatabaseHelper.KEY_DATA,
						DatabaseHelper.KEY_MESSAGE }, DatabaseHelper.KEY_IDCHAT
						+ "=?", new String[] { idChat }, null, null,
				DatabaseHelper.KEY_IDMESSAGE);

		return messages;
	}
}
