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

	private long insert(String table, String nullColumnHack,
			ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		long idLastInserted;
		try {
			idLastInserted = db.insertOrThrow(table, nullColumnHack, values);
		} catch (Exception e) {
			idLastInserted = -1;
		}

		return idLastInserted;
	}

	private int delete(String table, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		int totalRowsRemoved;
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

	private int update(String table, ContentValues value, String whereClause,
			String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		int totalRowsUpdated;
		try {
			totalRowsUpdated = db.update(table, value, whereClause, whereArgs);
		} catch (Exception e) {
			totalRowsUpdated = -1;
		}

		return totalRowsUpdated;
	}

	public synchronized boolean insertNewMessage(ContentValues message) {
		String prefix = message.getAsString(DatabaseHelper.KEY_PREFIX);
		String num = message.getAsString(DatabaseHelper.KEY_NUM);

		Cursor chat = getIdChatDest(prefix, num);
		long idChat;
		if (!chat.moveToNext()) {
			ContentValues newChat = new ContentValues();
			newChat.put(DatabaseHelper.KEY_PREFIXDEST, prefix);
			newChat.put(DatabaseHelper.KEY_NUMDEST, num);
			newChat.put(DatabaseHelper.KEY_VERSION, "");
			newChat.put(DatabaseHelper.KEY_IDLASTMESSAGE, "");

			idChat = insert(DatabaseHelper.TABLE_SINGLECHAT, null, newChat);
		} else {
			idChat = chat.getLong(chat.getColumnIndex(DatabaseHelper.KEY_ID));
		}

		if ((idChat == -1) || (idChat == 0)) {
			return false;
		}

		message.put(DatabaseHelper.KEY_IDCHAT, idChat);

		message.remove(DatabaseHelper.KEY_PREFIX);
		message.remove(DatabaseHelper.KEY_NUM);

		long idNewMessage;

		idNewMessage = insert(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null,
				message);

		if ((idNewMessage == -1) || (idNewMessage == 0)) {
			return false;
		}

		int affectedRows;

		ContentValues chatToUpdate = new ContentValues();

		chatToUpdate.put(DatabaseHelper.KEY_IDLASTMESSAGE, idNewMessage);
		affectedRows = update(DatabaseHelper.TABLE_SINGLECHAT, chatToUpdate,
				DatabaseHelper.KEY_ID + "=?",
				new String[] { String.valueOf(idChat) });

		return true;
	}

	public synchronized Cursor getTotalUser() {
		String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER
				+ " ORDER BY " + DatabaseHelper.KEY_NAME;

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		return c;
	}

	public synchronized Cursor getMessages(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = db.query(DatabaseHelper.TABLE_SINGLECHAT,
				new String[] { DatabaseHelper.KEY_ID },
				DatabaseHelper.KEY_PREFIXDEST + "=? and "
						+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
						prefix, num }, null, null, null);

		if (!chat.moveToNext()) {
			return chat;
		}

		String idChat = chat.getString(chat
				.getColumnIndex(DatabaseHelper.KEY_ID));

		Cursor messages = db.query(DatabaseHelper.TABLE_SINGLECHATMESSAGES,
				new String[] { DatabaseHelper.KEY_ID,
						DatabaseHelper.KEY_DIRECTION, DatabaseHelper.KEY_DATA,
						DatabaseHelper.KEY_MESSAGE }, DatabaseHelper.KEY_IDCHAT
						+ "=?", new String[] { idChat }, null, null,
				DatabaseHelper.KEY_DATA);

		return messages;
	}

	private Cursor getIdChatDest(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = db.query(DatabaseHelper.TABLE_SINGLECHAT,
				new String[] { DatabaseHelper.KEY_ID },
				DatabaseHelper.KEY_PREFIXDEST + "=? AND "
						+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
						prefix, num }, null, null, null);
		return chat;
	}

	public synchronized boolean insertNewUser(ContentValues value) {
		if (insert(DatabaseHelper.TABLE_USER, null, value) != -1) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized Cursor getConversations() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "";
		Cursor conversations = null;
		try {

			query = "SELECT " + DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_PREFIXDEST + " AS "
					+ DatabaseHelper.KEY_PREFIX + ", "
					+ DatabaseHelper.TABLE_SINGLECHAT + "." + DatabaseHelper.KEY_NUMDEST
					+ " AS " + DatabaseHelper.KEY_NUM + ", IFNULL("
					+ DatabaseHelper.TABLE_USER + "." + DatabaseHelper.KEY_NAME
					+ ", 0)" + " AS " + DatabaseHelper.KEY_NAME + ", "
					+ DatabaseHelper.TABLE_USER + "." + DatabaseHelper.KEY_ID
					+ " AS " + DatabaseHelper.KEY_ID + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_DATA + " AS "
					+ DatabaseHelper.KEY_DATA + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_MESSAGE + " AS "
					+ DatabaseHelper.KEY_MESSAGE + " FROM "
					+ DatabaseHelper.TABLE_SINGLECHAT + " LEFT JOIN  "
					+ DatabaseHelper.TABLE_USER + " ON "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_PREFIXDEST + "="
					+ DatabaseHelper.TABLE_USER + "."
					+ DatabaseHelper.KEY_PREFIX + " AND "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_NUMDEST + "="
					+ DatabaseHelper.TABLE_USER + "." + DatabaseHelper.KEY_NUM
					+ " LEFT JOIN " + DatabaseHelper.TABLE_SINGLECHATMESSAGES
					+ " ON " + DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_IDLASTMESSAGE + "="
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_ID + " ORDER BY " + DatabaseHelper.TABLE_SINGLECHATMESSAGES + "." + DatabaseHelper.KEY_DATA + " DESC";
			conversations = db.rawQuery(query, null);

		} catch (Exception e) {

		}
		return conversations;
	}

	public synchronized Cursor getAllChats() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chats = db.query(DatabaseHelper.TABLE_SINGLECHAT, null, null,
				null, null, null, null);
		return chats;
	}
}
