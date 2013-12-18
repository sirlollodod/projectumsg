package com.lollotek.umessage.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;

public class Provider {

	private static final String TAG = Provider.class.getName();

	private static DatabaseHelper dbHelper = null;

	public Provider(Context context) {
		if (dbHelper == null) {
			dbHelper = DatabaseHelper.getInstance(context
					.getApplicationContext());
		}
	}

	// -------------- private methods ------------------------------

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

	private Cursor getInfoChat(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = null;

		try {

			chat = db.query(DatabaseHelper.TABLE_SINGLECHAT, null,
					DatabaseHelper.KEY_PREFIXDEST + "=? AND "
							+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
							prefix, num }, null, null, null);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}
		return chat;
	}

	// -------------- public synchronized methods ------------------

	// -------------- USER -----------------------------------------

	public synchronized Cursor getTotalUser() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor c = null;
		try {
			c = db.query(DatabaseHelper.TABLE_USER, null, null, null, null,
					null, DatabaseHelper.KEY_NAME);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}
		return c;
	}

	public synchronized boolean insertNewUser(ContentValues value) {
		if (insert(DatabaseHelper.TABLE_USER, null, value) != -1) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean updateUserImage(String prefix, String num,
			String fileSrc, long data) {
		ContentValues userToUpdate = new ContentValues();

		userToUpdate.put(DatabaseHelper.KEY_IMGSRC, fileSrc);
		userToUpdate.put(DatabaseHelper.KEY_IMGDATA, data);
		int affectedRows = update(DatabaseHelper.TABLE_USER, userToUpdate,
				DatabaseHelper.KEY_PREFIX + "=? AND " + DatabaseHelper.KEY_NUM
						+ "=?", new String[] { prefix, num });

		if (affectedRows > 0) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized Cursor getUserInfo(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		ContentValues user = new ContentValues();

		user.put(DatabaseHelper.KEY_PREFIX, prefix);
		user.put(DatabaseHelper.KEY_NUM, num);

		Cursor userInfo = null;

		try {
			userInfo = db.query(DatabaseHelper.TABLE_USER, null,
					DatabaseHelper.KEY_PREFIX + "=? AND "
							+ DatabaseHelper.KEY_NUM + "=?", new String[] {
							prefix, num }, null, null, null);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return userInfo;
	}

	// -------------- SINGLE CHAT ----------------------------------

	public synchronized Cursor getChat(String prefix, String num) {
		return getInfoChat(prefix, num);
	}

	public synchronized boolean updateChatVersion(int idChat,
			String newChatVersion) {
		ContentValues chatToUpdate = new ContentValues();

		chatToUpdate.put(DatabaseHelper.KEY_VERSION, newChatVersion);

		int affectedRows = update(DatabaseHelper.TABLE_SINGLECHAT,
				chatToUpdate, DatabaseHelper.KEY_ID + "=?",
				new String[] { String.valueOf(idChat) });

		if (affectedRows > 0) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized Cursor getAllChats() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chats = null;
		try {
			chats = db.query(DatabaseHelper.TABLE_SINGLECHAT, null, null, null,
					null, null, null);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}
		return chats;

	}

	public boolean createNewChat(String prefix, String num) {
		ContentValues newChat = new ContentValues();
		newChat.put(DatabaseHelper.KEY_PREFIXDEST, prefix);
		newChat.put(DatabaseHelper.KEY_NUMDEST, num);
		newChat.put(DatabaseHelper.KEY_VERSION, "0");
		newChat.put(DatabaseHelper.KEY_IDLASTMESSAGE, "");
		newChat.put(DatabaseHelper.KEY_DATALASTMESSAGE, 0);

		if (insert(DatabaseHelper.TABLE_SINGLECHAT, null, newChat) != -1) {
			return true;
		} else {
			return false;
		}
	}

	// -------------- SINGLE CHAT MESSAGES -------------------------

	public synchronized long insertNewMessage(ContentValues message) {
		String prefix = message.getAsString(DatabaseHelper.KEY_PREFIX);
		String num = message.getAsString(DatabaseHelper.KEY_NUM);

		Cursor chat = getInfoChat(prefix, num);
		long idChat;
		if ((chat == null) || (!chat.moveToFirst())) {
			ContentValues newChat = new ContentValues();
			newChat.put(DatabaseHelper.KEY_PREFIXDEST, prefix);
			newChat.put(DatabaseHelper.KEY_NUMDEST, num);
			newChat.put(DatabaseHelper.KEY_VERSION, "0");
			newChat.put(DatabaseHelper.KEY_IDLASTMESSAGE, "");
			newChat.put(DatabaseHelper.KEY_DATALASTMESSAGE, 0);

			idChat = insert(DatabaseHelper.TABLE_SINGLECHAT, null, newChat);
		} else {
			chat.moveToFirst();
			idChat = chat.getLong(chat.getColumnIndex(DatabaseHelper.KEY_ID));
		}

		if ((idChat == -1) || (idChat == 0)) {
			return -1;
		}

		chat.close();
		chat = getInfoChat(prefix, num);
		chat.moveToFirst();

		message.put(DatabaseHelper.KEY_IDCHAT, idChat);

		message.remove(DatabaseHelper.KEY_PREFIX);
		message.remove(DatabaseHelper.KEY_NUM);

		long idNewMessage;

		idNewMessage = insert(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null,
				message);
		long dataNewMessage = message.getAsLong(DatabaseHelper.KEY_DATA);

		if ((idNewMessage == -1) || (idNewMessage == 0)) {
			return -1;
		}

		int affectedRows;

		if (dataNewMessage > chat.getLong(chat
				.getColumnIndex(DatabaseHelper.KEY_DATALASTMESSAGE))) {
			ContentValues chatToUpdate = new ContentValues();

			chatToUpdate.put(DatabaseHelper.KEY_IDLASTMESSAGE, idNewMessage);
			chatToUpdate
					.put(DatabaseHelper.KEY_DATALASTMESSAGE, dataNewMessage);
			affectedRows = update(DatabaseHelper.TABLE_SINGLECHAT,
					chatToUpdate, DatabaseHelper.KEY_ID + "=?",
					new String[] { String.valueOf(idChat) });

		}
		return idNewMessage;
	}

	public synchronized Cursor getMessages(String prefix, String num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = null;

		try {
			chat = db.query(DatabaseHelper.TABLE_SINGLECHAT,
					new String[] { DatabaseHelper.KEY_ID },
					DatabaseHelper.KEY_PREFIXDEST + "=? and "
							+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
							prefix, num }, null, null, null);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
			return null;
		}

		if ((chat == null) || (!chat.moveToFirst())) {
			return null;
		}

		chat.moveToFirst();
		String idChat = chat.getString(chat
				.getColumnIndex(DatabaseHelper.KEY_ID));

		Cursor messages = null;

		try {
			messages = db.query(DatabaseHelper.TABLE_SINGLECHATMESSAGES,
					new String[] { DatabaseHelper.KEY_ID,
							DatabaseHelper.KEY_DIRECTION,
							DatabaseHelper.KEY_DATA,
							DatabaseHelper.KEY_MESSAGE,
							DatabaseHelper.KEY_STATUS,
							DatabaseHelper.KEY_TOREAD },
					DatabaseHelper.KEY_IDCHAT + "=?", new String[] { idChat },
					null, null, DatabaseHelper.KEY_DATA);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return messages;
	}

	public synchronized boolean markMessagesAsRed(String prefix, String num) {

		Cursor chat = getInfoChat(prefix, num);

		if ((chat == null) || (!chat.moveToFirst())) {
			return false;
		}

		chat.moveToFirst();

		Long idChat = chat.getLong(chat.getColumnIndex(DatabaseHelper.KEY_ID));

		ContentValues value = new ContentValues();

		value.put(DatabaseHelper.KEY_TOREAD, "0");

		if (update(DatabaseHelper.TABLE_SINGLECHATMESSAGES, value,
				DatabaseHelper.KEY_IDCHAT + "=? AND "
						+ DatabaseHelper.KEY_TOREAD + "=?", new String[] {
						String.valueOf(idChat), "1" }) != -1) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean updateMessage(long idMessage, String newStatus,
			long newData) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor messageInfo = null;
		try {
			messageInfo = db.query(DatabaseHelper.TABLE_SINGLECHATMESSAGES,
					null, DatabaseHelper.KEY_ID + "=?",
					new String[] { String.valueOf(idMessage) }, null, null,
					null);
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
			return false;
		}

		if ((messageInfo == null) || (!messageInfo.moveToFirst())) {
			return false;
		}

		messageInfo.moveToFirst();
		int idChat = messageInfo.getInt(messageInfo
				.getColumnIndex(DatabaseHelper.KEY_IDCHAT));

		Cursor chatInfo = null;
		try {
			chatInfo = db.query(DatabaseHelper.TABLE_SINGLECHAT, null,
					DatabaseHelper.KEY_ID + "=?",
					new String[] { String.valueOf(idChat) }, null, null, null);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
			return false;
		}

		if ((chatInfo == null) || (!chatInfo.moveToFirst())) {
			return false;
		}

		chatInfo.moveToFirst();
		if (newData > chatInfo.getLong(chatInfo
				.getColumnIndex(DatabaseHelper.KEY_DATALASTMESSAGE))) {
			ContentValues chatToUpdate = new ContentValues();
			chatToUpdate.put(DatabaseHelper.KEY_IDLASTMESSAGE, idMessage);
			chatToUpdate.put(DatabaseHelper.KEY_DATALASTMESSAGE, newData);

			update(DatabaseHelper.TABLE_SINGLECHAT, chatToUpdate,
					DatabaseHelper.KEY_ID + "=?",
					new String[] { String.valueOf(idChat) });

		}

		ContentValues messageToUpdate = new ContentValues();

		messageToUpdate.put(DatabaseHelper.KEY_STATUS, newStatus);
		messageToUpdate.put(DatabaseHelper.KEY_DATA, newData);

		int affectedRows = update(DatabaseHelper.TABLE_SINGLECHATMESSAGES,
				messageToUpdate, DatabaseHelper.KEY_ID + "=?",
				new String[] { String.valueOf(idMessage) });

		if (affectedRows > 0) {
			return true;
		} else {
			return false;
		}

	}

	public synchronized Cursor getMessageByTag(String prefix, String num,
			String tag) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor chat = null;

		try {
			chat = db.query(DatabaseHelper.TABLE_SINGLECHAT, null,
					DatabaseHelper.KEY_PREFIXDEST + "=? AND "
							+ DatabaseHelper.KEY_NUMDEST + "=?", new String[] {
							prefix, num }, null, null, null);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		if ((chat == null) || (!chat.moveToFirst())) {
			return null;
		}

		chat.moveToFirst();

		int idChat = chat.getInt(chat.getColumnIndex(DatabaseHelper.KEY_ID));

		Cursor message = null;

		try {
			message = db.query(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null,
					DatabaseHelper.KEY_IDCHAT + "=? AND "
							+ DatabaseHelper.KEY_TAG + "=?", new String[] {
							String.valueOf(idChat), tag }, null, null,
					DatabaseHelper.KEY_DATA + " DESC", "0, 1");
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return message;
	}

	// -------------- MIXED ----------------------------------------

	public synchronized Cursor getConversations() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "";
		Cursor conversations = null;
		try {

			query = "SELECT " + DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_ID + " AS "
					+ DatabaseHelper.KEY_IDCHAT + ", "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_PREFIXDEST + " AS "
					+ DatabaseHelper.KEY_PREFIX + ", "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_NUMDEST + " AS "
					+ DatabaseHelper.KEY_NUM + ", IFNULL("
					+ DatabaseHelper.TABLE_USER + "." + DatabaseHelper.KEY_NAME
					+ ", 0)" + " AS " + DatabaseHelper.KEY_NAME + ", "
					+ DatabaseHelper.TABLE_USER + "." + DatabaseHelper.KEY_ID
					+ " AS " + DatabaseHelper.KEY_ID + ", IFNULL("
					+ DatabaseHelper.TABLE_USER + "."
					+ DatabaseHelper.KEY_IMGSRC + ", 0)" + " AS "
					+ DatabaseHelper.KEY_IMGSRC + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_DATA + " AS "
					+ DatabaseHelper.KEY_DATA + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_MESSAGE + " AS "
					+ DatabaseHelper.KEY_MESSAGE + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_TOREAD + " AS "
					+ DatabaseHelper.KEY_TOREAD + " FROM "
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
					+ DatabaseHelper.KEY_ID + " ORDER BY "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_DATA + " DESC";
			conversations = db.rawQuery(query, null);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return conversations;
	}

	public synchronized Cursor getConversationsNewMessages() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "";
		Cursor conversationsNewMessages = null;

		try {
			query = "SELECT " + DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_ID + " AS "
					+ DatabaseHelper.KEY_IDCHAT + ", COUNT(*) AS count FROM "
					+ DatabaseHelper.TABLE_SINGLECHAT + " JOIN "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + " ON "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_ID + "="
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_IDCHAT + " WHERE "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_TOREAD + "=? GROUP BY "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_ID;
			conversationsNewMessages = db.rawQuery(query, new String[] { "1" });

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return conversationsNewMessages;
	}

	public synchronized Cursor getMessagesToUpload() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor messagesToUpload = null;

		try {
			String query = "SELECT " + DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_PREFIXDEST + " AS "
					+ DatabaseHelper.KEY_PREFIX + ", "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_NUMDEST + " AS "
					+ DatabaseHelper.KEY_NUM + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_ID + " AS " + DatabaseHelper.KEY_ID
					+ ", " + DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_MESSAGE + " AS "
					+ DatabaseHelper.KEY_MESSAGE + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_TAG + " AS " + DatabaseHelper.KEY_TAG
					+ " FROM " + DatabaseHelper.TABLE_SINGLECHAT + ", "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + " WHERE "
					+ DatabaseHelper.TABLE_SINGLECHAT + "."
					+ DatabaseHelper.KEY_ID + "="
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_IDCHAT + " AND "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_STATUS + "=? AND "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_DIRECTION + "=? ORDER BY "
					+ DatabaseHelper.TABLE_SINGLECHATMESSAGES + "."
					+ DatabaseHelper.KEY_DATA;

			messagesToUpload = db.rawQuery(query, new String[] { "0", "0" });
		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
		}

		return messagesToUpload;
	}

	// -------------- DATABASE -------------------------------------

	public synchronized boolean eraseDatabase() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		try {
			this.delete(DatabaseHelper.TABLE_SINGLECHAT, null, null);
			this.delete(DatabaseHelper.TABLE_SINGLECHATMESSAGES, null, null);
			this.delete(DatabaseHelper.TABLE_USER, null, null);
			this.delete(DatabaseHelper.TABLE_TEMPSINGLECHATMESSAGES, null, null);

		} catch (Exception e) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + e.toString(), Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

}