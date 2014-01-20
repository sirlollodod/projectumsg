package com.lollotek.umessage.threads;

import java.util.Calendar;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.ExponentialQueueTime;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class HighPriorityThread extends Thread {

	private static final String TAG = HighPriorityThread.class.getName()
			+ ":\n";

	private Handler serviceHandler = null;
	private HightPriorityThreadHandler highPriorityThreadHandler = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;
	private ExponentialQueueTime timeQueue;

	public HighPriorityThread(Handler handler) {
		serviceHandler = handler;
	}

	public void run() {
		Looper.prepare();

		highPriorityThreadHandler = new HightPriorityThreadHandler();
		serviceHandler.obtainMessage(
				MessageTypes.RECEIVE_HIGH_PRIORITY_THREAD_HANDLER,
				highPriorityThreadHandler).sendToTarget();

		Looper.loop();

	}

	private class HightPriorityThreadHandler extends Handler {

		Provider p;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Message m, syncMsg;
			Bundle b, bnd;

			p = new Provider(UMessageApplication.getContext());

			switch (msg.what) {
			case MessageTypes.DESTROY:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DESTROY", Toast.LENGTH_LONG).show();
				}
				serviceHandler = null;
				Looper.myLooper().quit();

				break;

			case MessageTypes.SEND_NEW_TEXT_MESSAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "SEND_NEW_TEXT_MESSAGE", Toast.LENGTH_LONG)
							.show();
				}

				m = new Message();
				bnd = msg.getData();
				m.setData(bnd);
				m.what = MessageTypes.SEND_NEW_TEXT_MESSAGE;

				try {
					Calendar c = Calendar.getInstance();
					ContentValues value = new ContentValues();
					value.put(DatabaseHelper.KEY_PREFIX,
							bnd.getString("prefix"));
					value.put(DatabaseHelper.KEY_NUM, bnd.getString("num"));
					value.put(DatabaseHelper.KEY_DIRECTION, 0);
					value.put(DatabaseHelper.KEY_STATUS, "0");
					value.put(DatabaseHelper.KEY_DATA,
							Double.parseDouble("" + c.getTimeInMillis()));
					value.put(DatabaseHelper.KEY_TYPE, "text");
					value.put(DatabaseHelper.KEY_MESSAGE,
							bnd.getString("messageText"));
					value.put(DatabaseHelper.KEY_TOREAD, "0");
					value.put(DatabaseHelper.KEY_TAG,
							bnd.getString("messageTag"));

					long newMessageId = p.insertNewMessage(value);

					if (newMessageId != -1) {
						syncMsg = new Message();
						syncMsg.what = MessageTypes.MESSAGE_UPDATE;
						SynchronizationManager.getInstance()
								.onSynchronizationFinish(syncMsg);

						bnd.putLong("messageId", newMessageId);
						m.what = MessageTypes.UPLOAD_NEW_MESSAGE;

						// addToQueue(m, 0, 4, false, true);
						serviceHandler.sendMessage(m);

					} else {
						Utility.reportError(UMessageApplication.getContext(),
								new Exception("messaggio non inserito nel db"),
								TAG + ": handleMessage():SEND_NEW_TEXT_MESSAGE");
						addToQueue(msg, TIME_MINUTE, 4, false, false);
					}
				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():SEND_NEW_TEXT_MESSAGE");
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}
				break;

			}
		}

		// Aggiunge il messaggio alla coda, specificando il messaggio, il tempo
		// di delay in secondi (se = 0 nessun delay, se = -1 delay deciso dalla
		// scala temporale a seconda del parametro arg1, arg2 conta quante volte
		// si è tentata esecuzione su attuale valore temporale, senza successo)
		// e se rimuovere prima
		// tutti
		// i messaggi
		// dello stesso tipo presenti in coda
		private void addToQueue(Message msg, long timeDelay,
				int maxTimeDelayQueue, boolean removePendingMessages,
				boolean atFrontQueue) {

			Message newMsg = new Message();
			newMsg.what = msg.what;
			newMsg.arg1 = msg.arg1;
			newMsg.arg2 = msg.arg2;
			newMsg.obj = msg.obj;
			newMsg.replyTo = msg.replyTo;
			newMsg.setData(msg.getData());

			if (removePendingMessages) {
				this.removeMessages(msg.what);
			}

			if (timeDelay == -1) {
				if ((++newMsg.arg2) > 4) {
					newMsg.arg2 = 0;
					newMsg.arg1 = timeQueue.toNext(newMsg.arg1);
				}
				if (newMsg.arg1 > maxTimeDelayQueue) {
					newMsg.arg1 = maxTimeDelayQueue;
				}

				this.sendMessageDelayed(newMsg,
						timeQueue.getTime(newMsg.arg1) * 1000);
			} else if (timeDelay > 0) {
				this.sendMessageDelayed(newMsg, timeDelay * 1000);
			} else {
				if (atFrontQueue) {
					this.sendMessageAtFrontOfQueue(newMsg);
				} else {
					this.sendMessage(newMsg);
				}
			}

		}

	}
}
