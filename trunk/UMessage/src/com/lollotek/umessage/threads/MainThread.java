package com.lollotek.umessage.threads;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.DumpDB;
import com.lollotek.umessage.classes.ExponentialQueueTime;
import com.lollotek.umessage.classes.HttpResponseUmsg;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class MainThread extends Thread {

	private static final String TAG = MainThread.class.getName() + ":\n";

	private Handler serviceThreadHandler = null,
			lowPriorityThreadHandler = null, newMessageThreadHandler = null;
	private MainThreadHandler mainThreadHandler = null;
	private LowPriorityThread lowPriorityThread = null;
	private NewMessageThread newMessageThread = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;
	private ExponentialQueueTime timeQueue;

	GoogleCloudMessaging gcm = null;

	public MainThread(Handler handler) {
		serviceThreadHandler = handler;

		long[] time = new long[5];
		time[0] = 1;
		time[1] = 10;
		time[2] = TIME_MINUTE;
		time[3] = TIME_HOUR;
		time[4] = TIME_DAY;
		timeQueue = new ExponentialQueueTime(time);
	}

	public void run() {
		Looper.prepare();

		mainThreadHandler = new MainThreadHandler();
		serviceThreadHandler.obtainMessage(
				MessageTypes.RECEIVE_MAIN_THREAD_HANDLER, mainThreadHandler)
				.sendToTarget();
		lowPriorityThread = new LowPriorityThread(mainThreadHandler);
		lowPriorityThread.start();
		newMessageThread = new NewMessageThread(mainThreadHandler);
		newMessageThread.start();

		mainThreadHandler.obtainMessage(MessageTypes.GET_CHATS_VERSION)
				.sendToTarget();
		mainThreadHandler.obtainMessage(MessageTypes.CHECK_MESSAGES_TO_UPLOAD)
				.sendToTarget();

		mainThreadHandler
				.obtainMessage(MessageTypes.CHECK_GOOGLE_PLAY_SERVICES)
				.sendToTarget();

		mainThreadHandler.obtainMessage(MessageTypes.UPDATE_NOTIFICATION)
				.sendToTarget();

		// mainThreadHandler.obtainMessage(MessageTypes.MAKE_DB_DUMP)
		// .sendToTarget();

		Looper.loop();
	}

	private class MainThreadHandler extends Handler {

		private Provider p;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Message m, syncMsg;
			Bundle b, bnd;
			HttpResponseUmsg httpResult = new HttpResponseUmsg();

			JSONObject parameters;

			p = new Provider(UMessageApplication.getContext());

			switch (msg.what) {
			case MessageTypes.RECEIVE_LOW_PRIORITY_THREAD_HANDLER:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "RECEIVE_LOW_PRIORITY_THREAD_HANDLER",
							Toast.LENGTH_LONG).show();
				}
				lowPriorityThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.RECEIVE_NEW_MESSAGE_THREAD_HANDLER:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "RECEIVE_NEW_MESSAGE_THREAD_HANDLER",
							Toast.LENGTH_LONG).show();
				}
				newMessageThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.DESTROY:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DESTROY", Toast.LENGTH_LONG).show();
				}
				lowPriorityThreadHandler.obtainMessage(MessageTypes.DESTROY)
						.sendToTarget();
				newMessageThreadHandler.obtainMessage(MessageTypes.DESTROY)
						.sendToTarget();
				lowPriorityThreadHandler = null;
				newMessageThreadHandler = null;
				serviceThreadHandler = null;
				lowPriorityThread = null;
				newMessageThread = null;
				Looper.myLooper().quit();
				break;

			// ------------------- METODI BASSA PRIORITA' --------------------
			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC",
							Toast.LENGTH_LONG).show();
				}

				try {
					sendToLowPriorityThreadHandler(msg);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "UPLOAD_MY_PROFILE_IMAGE", Toast.LENGTH_LONG)
							.show();
				}

				try {
					sendToLowPriorityThreadHandler(msg);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_USER_IMAGE_FROM_SRC",
							Toast.LENGTH_LONG).show();
				}

				try {
					sendToLowPriorityThreadHandler(msg);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}

				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_ALL_USERS_IMAGES",
							Toast.LENGTH_LONG).show();
				}

				try {
					sendToLowPriorityThreadHandler(msg);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}

				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_USER_IMAGE", Toast.LENGTH_LONG)
							.show();
				}

				try {
					sendToLowPriorityThreadHandler(msg);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}
				break;

			// ----------------- FINE METODI BASSA PRIORITA' -------------------

			case MessageTypes.UPDATE_NOTIFICATION:

				updateNotification();

				break;

			case MessageTypes.NETWORK_CONNECTED:

				m = new Message();
				m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;

				addToQueue(m, 0, 4, true, true);

				break;

			case MessageTypes.MAKE_DB_DUMP:
				p = new Provider(UMessageApplication.getContext());
				Cursor cd = p.makeDumpDB();
				DumpDB dump = new DumpDB(cd, "0", "test", "test");
				if (dump.buildChatsList()) {
					// Toast.makeText(
					// UMessageApplication.getContext(),
					// "dump riuscito " + dump.myPrefix + " - "
					// + dump.myNum + " [" + dump.dataDumpDB + "]",
					// Toast.LENGTH_LONG).show();
				} else {
					// Toast.makeText(UMessageApplication.getContext(),
					// "dump non riuscito", Toast.LENGTH_LONG).show();
				}

				dump.reset();
				while (dump.moveToNextChat()) {
					Bundle infoChat = dump.getInfoChat();
					String destPrefix = infoChat.getString("prefix");
					String destNum = infoChat.getString("num");

					// Toast.makeText(UMessageApplication.getContext(),
					// "Chat " + destPrefix + " - " + destNum,
					// Toast.LENGTH_LONG).show();
					int count = 0;

					while (dump.moveToNextMessageInChat()) {
						Bundle infoMessage = dump.getInfoMessageInChat();
						String direction = infoMessage.getString("direction");
						String data = infoMessage.getString("data");
						String status = infoMessage.getString("status");
						String read = infoMessage.getString("read");
						String type = infoMessage.getString("type");
						String tag = infoMessage.getString("tag");
						String message = infoMessage.getString("message");
						// Toast.makeText(UMessageApplication.getContext(),
						// message + "\n" + tag, Toast.LENGTH_LONG).show();
						count++;
					}
					// Toast.makeText(UMessageApplication.getContext(),
					// "" + count + " messaggi nella chat.",
					// Toast.LENGTH_LONG).show();

				}

				break;

			case MessageTypes.CHECK_GOOGLE_PLAY_SERVICES:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_GOOGLE_PLAY_SERVICES",
							Toast.LENGTH_LONG).show();
				}

				if (!registerGCM()) {
					addToQueue(msg, TIME_HOUR, 4, false, false);
				}

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

						addToQueue(m, 0, 4, false, true);

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

			case MessageTypes.UPLOAD_NEW_MESSAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "UPLOAD_NEW_MESSAGE", Toast.LENGTH_LONG)
							.show();
				}
				try {

					bnd = msg.getData();
					parameters = new JSONObject();
					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());

					Cursor infoChat = p.getChat(bnd.getString("prefix"),
							bnd.getString("num"));

					infoChat.moveToNext();

					parameters.accumulate("action", "SEND_NEW_MESSAGE");
					parameters.accumulate("sessionId",
							configuration.getSessid());
					parameters
							.accumulate("destPrefix", bnd.getString("prefix"));
					parameters.accumulate("destNum", bnd.getString("num"));
					parameters
							.accumulate(
									"localChatVersion",
									infoChat.getString(infoChat
											.getColumnIndex(DatabaseHelper.KEY_VERSION)));

					parameters.accumulate("message",
							bnd.getString("messageText"));
					parameters.accumulate("type", "text");
					parameters.accumulate("messageTag",
							bnd.getString("messageTag"));

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						addToQueue(msg, TIME_MINUTE, 4, false, false);
						break;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);

						break;
					}

					if (!httpResult.result.getBoolean("isDestValid")) {
						// bisognerebbe cancellare il messaggio dal db
						// locale ?
						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "destination not valid",
									Toast.LENGTH_LONG).show();
						}
						break;
					}

					if (httpResult.result.getBoolean("syncChatRequired")) {
						m = new Message();
						m.what = MessageTypes.SYNCHRONIZE_CHAT;
						b = new Bundle();
						b.putString("prefix", bnd.getString("prefix"));
						b.putString("num", bnd.getString("num"));
						b.putBoolean("messagesToUpload", true);
						m.setData(b);

						addToQueue(m, 0, 4, false, true);

						break;
					}

					if (httpResult.result.getBoolean("chatVersionChanged")) {
						p.updateChatVersion(infoChat.getInt(infoChat
								.getColumnIndex(DatabaseHelper.KEY_ID)),
								httpResult.result.getString("newChatVersion"));

					}

					if (p.updateMessage(bnd.getLong("messageId"),
							httpResult.result.getString("statusNewMessage"),
							Long.parseLong(httpResult.result
									.getString("dataNewMessage")))) {

						syncMsg = new Message();
						syncMsg.what = MessageTypes.MESSAGE_UPDATE;
						SynchronizationManager.getInstance()
								.onSynchronizationFinish(syncMsg);

						break;

					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():UPLOAD_NEW_MESSAGE");
					addToQueue(msg, TIME_MINUTE, 4, false, false);
				}

				break;

			// ---------------- DA QUI IN POI AGGIORNATI CON UTILIZZO METODI
			// NUOVI
			case MessageTypes.SYNCHRONIZE_CHAT:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "SYNCHRONIZE_CHAT", Toast.LENGTH_LONG).show();
				}
				try {
					bnd = msg.getData();
					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());
					String prefixDest = bnd.getString("prefix");
					String numDest = bnd.getString("num");
					String sessionId = configuration.getSessid();
					parameters = new JSONObject();
					Cursor infoChat = p.getChat(prefixDest, numDest);
					boolean updatedSomething = false;
					boolean existsLocalChat = true;

					if ((infoChat == null) || (!infoChat.moveToFirst())) {
						existsLocalChat = false;
					} else {
						infoChat.moveToFirst();
					}

					parameters
							.accumulate("action", "GET_CONVERSATION_MESSAGES");
					parameters.accumulate("sessionId", sessionId);
					parameters.accumulate("destPrefix", prefixDest);
					parameters.accumulate("destNum", numDest);
					String localChatVersion = (existsLocalChat ? infoChat
							.getString(infoChat
									.getColumnIndex(DatabaseHelper.KEY_VERSION))
							: "0");
					parameters.accumulate("localChatVersion", localChatVersion);

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						addToQueue(msg, TIME_MINUTE, 4, false, false);
						break;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);

						break;
					}

					if (!httpResult.result.getBoolean("isDestValid")) {
						// bisognerebbe cancellare i messaggi dal db
						// locale ?
						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "destination not valid",
									Toast.LENGTH_LONG).show();
						}
						break;
					}

					String newOnlineChatVersion = httpResult.result
							.getString("onlineChatVersion");

					if (httpResult.result.getInt("numMessages") == 0) {
						if (!existsLocalChat) {
							break;
						}

						p.updateChatVersion(infoChat.getInt(infoChat
								.getColumnIndex(DatabaseHelper.KEY_ID)),
								newOnlineChatVersion);

						if (bnd.getBoolean("messagesToUpload", false)) {
							m = new Message();
							m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;
							addToQueue(m, 0, 4, true, true);
						}

						break;
					}

					if (!existsLocalChat) {
						p.createNewChat(prefixDest, numDest);
						infoChat = p.getChat(prefixDest, numDest);
						infoChat.moveToFirst();
					}

					JSONArray messages = httpResult.result
							.getJSONArray("messages");
					Cursor localMessage;
					ContentValues newIncomingMessage;

					boolean isThereNewMessages = false;

					for (int i = 0; i < messages.length(); i++) {
						JSONObject message = messages.getJSONObject(i);

						boolean isIncomingMessage = message.getString(
								"direction").equals("0") ? false : true;

						localMessage = p.getMessageByTag(prefixDest, numDest,
								message.getString("tag"));

						String onlineMessageStatus = message
								.getString("status");
						long onlineMessageData = message.getLong("data");
						long idLocalMessage;

						if (isIncomingMessage) {
							if ((localMessage == null)
									|| (!localMessage.moveToFirst())) {
								// messaggio ricevuto e non ancora presente
								// localmente, lo inserisco e aggiorno lo stato
								// online come ricevuto

								newIncomingMessage = new ContentValues();
								newIncomingMessage.put(
										DatabaseHelper.KEY_PREFIX, prefixDest);
								newIncomingMessage.put(DatabaseHelper.KEY_NUM,
										numDest);
								newIncomingMessage.put(
										DatabaseHelper.KEY_DIRECTION, 1);
								newIncomingMessage.put(
										DatabaseHelper.KEY_STATUS, "2");
								newIncomingMessage.put(DatabaseHelper.KEY_DATA,
										message.getString("data"));
								newIncomingMessage.put(DatabaseHelper.KEY_TYPE,
										"text");
								newIncomingMessage.put(
										DatabaseHelper.KEY_MESSAGE,
										message.getString("msg"));
								newIncomingMessage.put(
										DatabaseHelper.KEY_TOREAD, "1");
								newIncomingMessage.put(DatabaseHelper.KEY_TAG,
										message.getString("tag"));

								long newMessageId = p
										.insertNewMessage(newIncomingMessage);
								updatedSomething = true;
								isThereNewMessages = true;

								parameters = new JSONObject();
								parameters.accumulate("action",
										"UPDATE_MESSAGE_DOWNLOADED");
								parameters.accumulate("sessionId", sessionId);
								parameters.accumulate("destPrefix", prefixDest);
								parameters.accumulate("destNum", numDest);
								parameters.accumulate("messageData",
										message.getString("data"));
								parameters.accumulate("messageTag",
										message.get("tag"));
								parameters.accumulate("messageDirection", "1");

								httpResult = doRequest(parameters);

								if (!httpResult.error) {
									// update local message status ----> 3
									p.updateMessage(newMessageId, "3",
											onlineMessageData);
								}

							} else {
								// messaggio ricevuto e gia presente localmente,
								// aggiorno solamente lo stato locale e online
								// se necessario

								localMessage.moveToFirst();
								idLocalMessage = localMessage
										.getLong(localMessage
												.getColumnIndex(DatabaseHelper.KEY_ID));
								if (!localMessage
										.getString(
												localMessage
														.getColumnIndex(DatabaseHelper.KEY_STATUS))
										.equals(onlineMessageStatus)) {
									if (onlineMessageStatus.equals("1")) {

										parameters = new JSONObject();
										parameters.accumulate("action",
												"UPDATE_MESSAGE_DOWNLOADED");
										parameters.accumulate("sessionId",
												sessionId);
										parameters.accumulate("destPrefix",
												prefixDest);
										parameters.accumulate("destNum",
												numDest);
										parameters.accumulate("messageData",
												message.getString("data"));
										parameters.accumulate("messageTag",
												message.get("tag"));
										parameters.accumulate(
												"messageDirection", "1");

										httpResult = doRequest(parameters);

										if (!httpResult.error) {
											// update local message status
											// ----> 3
											p.updateMessage(idLocalMessage,
													"3", onlineMessageData);
										}

										updatedSomething = true;
									}
								}

							}
						} else {
							boolean newMyMessage = false;
							if ((localMessage == null)
									|| (!localMessage.moveToFirst())) {
								// mio messaggio presente ancora online ma
								// non localmente... lo devo inserire
								// localmente

								newIncomingMessage = new ContentValues();
								newIncomingMessage.put(
										DatabaseHelper.KEY_PREFIX, prefixDest);
								newIncomingMessage.put(DatabaseHelper.KEY_NUM,
										numDest);
								newIncomingMessage.put(
										DatabaseHelper.KEY_DIRECTION, 0);
								newIncomingMessage.put(
										DatabaseHelper.KEY_STATUS,
										message.getString("tag"));
								newIncomingMessage.put(DatabaseHelper.KEY_DATA,
										message.getString("data"));
								newIncomingMessage.put(DatabaseHelper.KEY_TYPE,
										"text");
								newIncomingMessage.put(
										DatabaseHelper.KEY_MESSAGE,
										message.getString("msg"));
								newIncomingMessage.put(
										DatabaseHelper.KEY_TOREAD, "0");
								newIncomingMessage.put(DatabaseHelper.KEY_TAG,
										message.getString("tag"));

								long newMessageId = p
										.insertNewMessage(newIncomingMessage);
								updatedSomething = true;
								newMyMessage = true;

							}

							idLocalMessage = localMessage.getLong(localMessage
									.getColumnIndex(DatabaseHelper.KEY_ID));

							if ((newMyMessage)
									|| !localMessage
											.getString(
													localMessage
															.getColumnIndex(DatabaseHelper.KEY_STATUS))
											.equals(onlineMessageStatus)) {
								if (onlineMessageStatus.equals("3")) {

									p.updateMessage(idLocalMessage, "4",
											onlineMessageData);

									updatedSomething = true;

									parameters = new JSONObject();
									parameters
											.accumulate("action",
													"UPDATE_MESSAGE_NOTIFICATION_DELIVERED");
									parameters.accumulate("sessionId",
											sessionId);
									parameters.accumulate("destPrefix",
											prefixDest);
									parameters.accumulate("destNum", numDest);
									parameters.accumulate("messageData",
											message.getString("data"));
									parameters.accumulate("messageTag",
											message.get("tag"));
									parameters.accumulate("messageDirection",
											"0");

									httpResult = doRequest(parameters);

								}
							}
						}

					}// fine for

					infoChat.close();
					infoChat = p.getChat(prefixDest, numDest);
					infoChat.moveToFirst();
					p.updateChatVersion(infoChat.getInt(infoChat
							.getColumnIndex(DatabaseHelper.KEY_ID)),
							newOnlineChatVersion);

					if (updatedSomething) {
						syncMsg = new Message();
						syncMsg.what = MessageTypes.MESSAGE_UPDATE;
						SynchronizationManager.getInstance()
								.onSynchronizationFinish(syncMsg);
					}

					if (isThereNewMessages) {
						m = new Message();
						m.what = MessageTypes.UPDATE_NOTIFICATION;
						addToQueue(m, 0, 4, true, true);
					}

					if (bnd.getBoolean("messagesToUpload", false)) {
						m = new Message();
						m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;
						addToQueue(m, 0, 4, true, true);
					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():SYNCHRONIZE_CHAT");
				}

				break;

			case MessageTypes.CHECK_MESSAGES_TO_UPLOAD:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_MESSAGES_TO_UPLOAD", Toast.LENGTH_LONG)
							.show();
				}
				Cursor messagesToUpload = p.getMessagesToUpload();

				if (messagesToUpload == null) {
					break;
				}

				boolean errors = false;
				while (messagesToUpload.moveToNext()) {
					try {
						m = new Message();
						m.what = MessageTypes.UPLOAD_NEW_MESSAGE;
						b = new Bundle();
						b.putString(
								"prefix",
								messagesToUpload.getString(messagesToUpload
										.getColumnIndex(DatabaseHelper.KEY_PREFIX)));
						b.putString(
								"num",
								messagesToUpload.getString(messagesToUpload
										.getColumnIndex(DatabaseHelper.KEY_NUM)));
						b.putString(
								"messageText",
								messagesToUpload.getString(messagesToUpload
										.getColumnIndex(DatabaseHelper.KEY_MESSAGE)));
						b.putString(
								"messageTag",
								messagesToUpload.getString(messagesToUpload
										.getColumnIndex(DatabaseHelper.KEY_TAG)));
						b.putLong("messageId", messagesToUpload
								.getLong(messagesToUpload
										.getColumnIndex(DatabaseHelper.KEY_ID)));
						m.setData(b);

						addToQueue(m, 0, 4, false, true);

					} catch (Exception e) {
						Utility.reportError(
								UMessageApplication.getContext(),
								e,
								TAG
										+ ": handleMessage():CHECK_MESSAGES_TO_UPLOAD");
						errors = true;
					}

				}

				if (errors) {
					addToQueue(msg, TIME_MINUTE, 4, true, false);
				}

				break;

			case MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_CHATS_TO_SYNCHRONIZE",
							Toast.LENGTH_LONG).show();
				}
				try {

					Cursor chats = p.getAllChats();

					if (chats == null) {
						addToQueue(msg, TIME_MINUTE, 4, true, false);
						break;
					}

					while (chats.moveToNext()) {
						m = new Message();
						b = new Bundle();
						b.putString("prefix", chats.getString(chats
								.getColumnIndex(DatabaseHelper.KEY_PREFIXDEST)));
						b.putString("num", chats.getString(chats
								.getColumnIndex(DatabaseHelper.KEY_NUMDEST)));
						m.what = MessageTypes.SYNCHRONIZE_CHAT;
						m.setData(b);

						addToQueue(m, 0, 4, false, true);
					}
				} catch (Exception e) {
					Utility.reportError(
							UMessageApplication.getContext(),
							e,
							TAG
									+ ": handleMessage():CHECK_CHATS_TO_SYNCHRONIZE");
				}

				addToQueue(msg, TIME_HOUR, 4, true, false);
				break;

			case MessageTypes.GET_CHATS_VERSION:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "GET_CHATS_VERSION", Toast.LENGTH_LONG)
							.show();
				}
				try {
					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());
					String sessionId = configuration.getSessid();
					parameters = new JSONObject();
					parameters.accumulate("action", "GET_CHATS_VERSION");
					parameters.accumulate("sessionId", sessionId);

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						addToQueue(msg, TIME_HOUR, 4, true, false);
						break;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);

						break;
					}

					if (httpResult.result.getInt("numChats") == 0) {
						addToQueue(msg, TIME_HOUR, 4, true, false);
						break;
					}

					JSONArray chatsInfo = httpResult.result
							.getJSONArray("chatsInfo");
					JSONObject onlineChat;
					for (int i = 0; i < chatsInfo.length(); i++) {
						onlineChat = chatsInfo.getJSONObject(i);
						String prefixDest, numDest, version;
						prefixDest = onlineChat.getString("prefixDest");
						numDest = onlineChat.getString("numDest");
						version = onlineChat.getString("version");
						Cursor localChat = p.getChat(prefixDest, numDest);
						boolean chatToSync = false;

						if ((localChat == null) || (!localChat.moveToFirst())) {
							chatToSync = true;
						} else {
							localChat.moveToFirst();
							String localVersion = localChat
									.getString(localChat
											.getColumnIndex(DatabaseHelper.KEY_VERSION));
							if (!version.equals(localVersion)) {
								chatToSync = true;
							}
						}

						if (chatToSync) {
							m = new Message();
							b = new Bundle();
							b.putString("prefix", prefixDest);
							b.putString("num", numDest);
							m.what = MessageTypes.SYNCHRONIZE_CHAT;
							m.setData(b);

							addToQueue(m, 0, 4, false, true);
						}

					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():GET_CHATS_VERSION");
				}

				addToQueue(msg, TIME_HOUR, 4, true, false);

				break;

			}
		}

		// Inoltra richiesta http post
		// Se richiesta non è andata a buon fine, ne ritorna il risultato e flag
		// toRepeat=true, altrimenti flag=false.
		private HttpResponseUmsg doRequest(JSONObject parameters) {
			HttpResponseUmsg result = new HttpResponseUmsg();

			try {
				result.result = Utility.doPostRequest(Settings.SERVER_URL,
						parameters);
				if ((result.result == null)
						|| (result.result.getString("errorCode").equals("KO"))) {
					result.error = true;
				}

			} catch (Exception e) {
				Utility.reportError(UMessageApplication.getContext(), e, TAG
						+ "doRequest()");
				result.error = true;
			}

			return result;
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
				mainThreadHandler.removeMessages(msg.what);
			}

			if (timeDelay == -1) {
				if ((++newMsg.arg2) > 4) {
					newMsg.arg2 = 0;
					newMsg.arg1 = timeQueue.toNext(newMsg.arg1);
				}
				if (newMsg.arg1 > maxTimeDelayQueue) {
					newMsg.arg1 = maxTimeDelayQueue;
				}

				mainThreadHandler.sendMessageDelayed(newMsg,
						timeQueue.getTime(newMsg.arg1) * 1000);
			} else if (timeDelay > 0) {
				mainThreadHandler.sendMessageDelayed(newMsg, timeDelay * 1000);
			} else {
				if (atFrontQueue) {
					mainThreadHandler.sendMessageAtFrontOfQueue(newMsg);
				} else {
					mainThreadHandler.sendMessage(newMsg);
				}
			}

		}

		private void sendToLowPriorityThreadHandler(Message msg) {
			Message m = new Message();
			m.what = msg.what;
			m.arg1 = msg.arg1;
			m.arg2 = msg.arg2;
			m.obj = msg.obj;
			m.setData(msg.getData());
			lowPriorityThreadHandler.sendMessage(m);
		}

		private boolean registerGCM() {
			if (Utility.checkPlayServices(UMessageApplication.getContext())) {
				Configuration configuration = Utility
						.getConfiguration(UMessageApplication.getContext());

				String regid;
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(UMessageApplication
							.getContext());
				}

				try {
					regid = gcm.register("1050595639343");
					configuration.setGcmid(regid);
					Utility.setConfiguration(UMessageApplication.getContext(),
							configuration);

					JSONObject parameters = new JSONObject();
					parameters.accumulate("action", "UPDATE_GCM_ID");
					parameters.accumulate("sessionId",
							configuration.getSessid());
					parameters.accumulate("gcmId", configuration.getGcmid());
					HttpResponseUmsg httpResult = new HttpResponseUmsg();

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						return false;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);
						return false;
					}

					if (httpResult.result.getBoolean("isGcmIdUpdated")) {
						return true;
					} else {
						return false;
					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": registerGCM()");
				}

			} else {

			}

			return false;
		}

		// Controlla se ci sono notifiche da dover inviare
		private boolean updateNotification() {
			Provider p = new Provider(UMessageApplication.getContext());

			Cursor messagesNotification = p.getAllNewMessages();

			if (messagesNotification == null) {
				return false;
			}

			int totalNewMessages = messagesNotification.getCount();
			int totalNewConversationMessages = 0;
			boolean firstMessage = true, newDest = false;

			String prefix = "", num = "", name = "", message = "", data = "", type = "";

			if (totalNewMessages == 0) {
				return true;
			}

			messagesNotification.moveToFirst();

			Notification notification;
			Builder b = new Notification.Builder(
					UMessageApplication.getContext());
			String contextText = "";

			do {
				if (firstMessage) {
					firstMessage = false;
					totalNewConversationMessages++;
				} else {
					if ((!prefix
							.equals(messagesNotification.getString(messagesNotification
									.getColumnIndex(DatabaseHelper.KEY_PREFIX))) || (!num
							.equals(messagesNotification.getString(messagesNotification
									.getColumnIndex(DatabaseHelper.KEY_NUM)))))) {
						totalNewConversationMessages++;
						newDest = true;
					} else {
						newDest = false;
					}
				}

				prefix = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_PREFIX));
				num = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_NUM));
				name = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_NAME));
				message = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_MESSAGE));
				data = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_DATA));
				type = messagesNotification.getString(messagesNotification
						.getColumnIndex(DatabaseHelper.KEY_TYPE));

			} while (messagesNotification.moveToNext());

			Intent action;
			PendingIntent pIntent;

			if (totalNewConversationMessages == 1) {
				action = new Intent(
						UMessageApplication.getContext(),
						com.lollotek.umessage.activities.SingleChatContact.class);
				action.putExtra("prefix", prefix);
				action.putExtra("num", num);
				notification = b
						.setContentTitle(
								name.equals("0") ? (prefix + " " + num)
										: (name + " "))
						.setContentText(
								totalNewMessages
										+ (totalNewMessages == 1 ? " nuovo messaggio"
												: " nuovi messaggi"))
						.getNotification();
			} else {
				action = new Intent(
						UMessageApplication.getContext(),
						com.lollotek.umessage.activities.ConversationsList.class);
				notification = b
						.setContentTitle(
								"" + messagesNotification.getCount()
										+ " nuovi messaggi")
						.setContentText(
								"In " + totalNewConversationMessages
										+ " conversazioni").getNotification();
			}

			pIntent = PendingIntent.getActivity(
					UMessageApplication.getContext(), 0, action, 0);

			NotificationManager notificationManager = (NotificationManager) UMessageApplication
					.getContext()
					.getSystemService(
							UMessageApplication.getContext().NOTIFICATION_SERVICE);

			notificationManager.cancel(0);

			notification = b
					.setContentIntent(pIntent)
					.setVibrate(new long[] { 500, 1000 })
					.setSound(
							RingtoneManager
									.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
					.setSmallIcon(R.drawable.ic_launcher).getNotification();

			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, notification);

			return true;

		}

	}

}
