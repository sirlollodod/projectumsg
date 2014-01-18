package com.lollotek.umessage.classes;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

public class DumpDB {

	private Cursor messagesDump;
	public String myPrefix, myNum, dataDumpDB;
	private ChatDump actualChat, firstChat, lastChat;

	public DumpDB(Cursor messagesDump, String dataDumpDB, String myPrefix,
			String myNum) {
		this.dataDumpDB = dataDumpDB;
		this.messagesDump = messagesDump;
		this.myPrefix = myPrefix;
		this.myNum = myNum;
		this.actualChat = null;
		this.firstChat = null;
		this.lastChat = null;
	}

	public Bundle getInfoChat() {
		Bundle b = null;

		if (actualChat == null) {
			return b;
		}

		b = new Bundle();

		b.putString("prefix", actualChat.destPrefix);
		b.putString("num", actualChat.destNum);

		return b;
	}

	public boolean moveToNextMessageInChat() {
		return actualChat.moveToNextMessage();
	}

	public Bundle getInfoMessageInChat() {
		Bundle b = null;

		if (actualChat == null) {
			return b;
		}

		if (actualChat.actualMessage == null) {
			return b;
		}

		b = new Bundle();

		b.putString("direction", actualChat.actualMessage.direction);
		b.putString("status", actualChat.actualMessage.status);
		b.putString("data", actualChat.actualMessage.data);
		b.putString("read", actualChat.actualMessage.read);
		b.putString("type", actualChat.actualMessage.type);
		b.putString("message", actualChat.actualMessage.msg);
		b.putString("tag", actualChat.actualMessage.tag);

		return b;
	}

	public boolean buildChatsList() {
		messagesDump.moveToFirst();
		this.actualChat = null;
		this.firstChat = null;
		this.lastChat = null;

		String prevDestPrefix = "", prevDestNum = "", actualDestPrefix = "", actualDestNum = "";
		String msgDirection = "", msgStatus = "", msgData = "", msgRead = "", msgType = "", msgMessage = "", msgTag = "";

		do {
			actualDestPrefix = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_PREFIX));
			actualDestNum = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_NUM));
			msgDirection = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_DIRECTION));
			msgStatus = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_STATUS));
			msgData = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_DATA));
			msgRead = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_TOREAD));
			msgType = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_TYPE));
			msgMessage = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
			msgTag = messagesDump.getString(messagesDump
					.getColumnIndex(DatabaseHelper.KEY_TAG));

			if (!prevDestPrefix.equals(actualDestPrefix)
					|| !prevDestNum.equals(actualDestNum)) {
				if (!insertChat(actualDestPrefix, actualDestNum)) {
					return false;
				}
				prevDestPrefix = actualDestPrefix;
				prevDestNum = actualDestNum;

				moveToNextChat();
			}

			if (!actualChat.insertMessage(msgDirection, msgStatus, msgData,
					msgRead, msgType, msgMessage, msgTag)) {
				return false;
			}

		} while (messagesDump.moveToNext());

		return true;
	}

	private boolean insertChat(String destPrefix, String destNum) {
		ChatDump chatToInsert = new ChatDump(destPrefix, destNum);
		if (firstChat == null) {
			firstChat = chatToInsert;
			lastChat = chatToInsert;
		} else {
			lastChat.nextChat = chatToInsert;
			lastChat = chatToInsert;
		}

		return true;

	}

	public boolean moveToFirstChat() {
		if (firstChat != null) {
			actualChat = firstChat;
			return true;
		} else {
			return false;
		}
	}

	public boolean moveToNextChat() {
		if (actualChat == null) {
			if (firstChat != null) {
				actualChat = firstChat;
				// return true;
			} else {
				return false;
			}
		} else {
			if (actualChat.nextChat != null) {
				actualChat = actualChat.nextChat;
				// return true;
			} else {
				return false;
			}
		}
		// caso in cui doveve essere return true, invece azzero anche l'actual
		// message relativo alla chat attuale
		actualChat.reset();

		return true;
	}

	public void reset() {
		actualChat = null;
	}

	private class ChatDump {
		private String destPrefix, destNum;
		private ChatDump nextChat;
		private MessageDump actualMessage, firstMessage, lastMessage;

		public ChatDump(String destPrefix, String destNum) {
			this.destPrefix = destPrefix;
			this.destNum = destNum;
			this.nextChat = null;
			this.actualMessage = null;
			this.firstMessage = null;
			this.lastMessage = null;
		}

		private boolean insertMessage(String direction, String status,
				String data, String read, String type, String msg, String tag) {
			MessageDump messageToInsert = new MessageDump(direction, status,
					data, read, type, msg, tag);

			if (firstMessage == null) {
				firstMessage = messageToInsert;
				lastMessage = messageToInsert;
			} else {
				lastMessage.nextMessage = messageToInsert;
				lastMessage = messageToInsert;
			}

			return true;
		}

		private boolean moveToFirstMessage() {
			if (firstMessage != null) {
				actualMessage = firstMessage;
				return true;
			} else {
				return false;
			}
		}

		private boolean moveToNextMessage() {
			if (actualMessage == null) {
				if (firstMessage != null) {
					actualMessage = firstMessage;
					return true;
				} else {
					return false;
				}
			} else {
				if (actualMessage.nextMessage != null) {
					actualMessage = actualMessage.nextMessage;
					return true;
				} else {
					return false;
				}
			}
		}

		private void reset() {
			actualMessage = null;
		}

	}

	private class MessageDump {
		private String direction, status, data, read, type, msg, tag;
		private MessageDump nextMessage;

		public MessageDump(String direction, String status, String data,
				String read, String type, String msg, String tag) {
			this.direction = direction;
			this.status = status;
			this.data = data;
			this.read = read;
			this.type = type;
			this.msg = msg;
			this.tag = tag;
			this.nextMessage = null;
		}
	}
}
