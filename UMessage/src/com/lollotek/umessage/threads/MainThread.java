package com.lollotek.umessage.threads;

import java.io.File;

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
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.R;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.ExponentialQueueTime;
import com.lollotek.umessage.classes.HttpResponseUmsg;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.ConfigurationManager;
import com.lollotek.umessage.managers.DropboxManager;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class MainThread extends Thread {

	private static final String TAG = MainThread.class.getName() + ":\n";

	private Handler serviceThreadHandler = null,
			lowPriorityThreadHandler = null;
	private MainThreadHandler mainThreadHandler = null;
	private LowPriorityThread lowPriorityThread = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400,
			TIME_SYNC_POLLING = 300, TIME_WAIT_PING_GCM = 600;
	private ExponentialQueueTime timeQueue;

	private Bundle request, response;

	GoogleCloudMessaging gcm = null;

	// test
	public static String prefixDisplayed = "", numDisplayed = "";

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

		Message m;
		Bundle b;

		mainThreadHandler.obtainMessage(MessageTypes.GET_CHATS_VERSION)
				.sendToTarget();
		mainThreadHandler.obtainMessage(MessageTypes.CHECK_MESSAGES_TO_UPLOAD)
				.sendToTarget();

		m = new Message();
		m.what = MessageTypes.CHECK_GOOGLE_PLAY_SERVICES;
		b = new Bundle();
		b.putBoolean("calledFromThreadStarted", true);
		m.setData(b);
		mainThreadHandler.sendMessage(m);

		m = new Message();
		m.what = MessageTypes.UPDATE_NOTIFICATION;
		b = new Bundle();
		b.putBoolean("calledFromThreadStarted", true);
		m.setData(b);
		mainThreadHandler.sendMessage(m);

		mainThreadHandler.obtainMessage(MessageTypes.MAKE_DB_DUMP)
				.sendToTarget();

		Looper.loop();

	}

	private void checkHandlerThreadStarted() {
		if (lowPriorityThread == null) {
			if (Settings.debugMode) {
				Toast.makeText(UMessageApplication.getContext(),
						"LowPriorityThread null, reinizializzo... ",
						Toast.LENGTH_SHORT).show();
			}

			lowPriorityThread = new LowPriorityThread(mainThreadHandler);
			lowPriorityThread.start();
		}
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

			AndroidAuthSession sessionDropbox;
			DropboxAPI<AndroidAuthSession> mApi;

			checkHandlerThreadStarted();

			switch (msg.what) {
			case MessageTypes.RECEIVE_LOW_PRIORITY_THREAD_HANDLER:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "RECEIVE_LOW_PRIORITY_THREAD_HANDLER",
							Toast.LENGTH_SHORT).show();
				}
				lowPriorityThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.DESTROY:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DESTROY", Toast.LENGTH_SHORT).show();
				}
				lowPriorityThreadHandler.obtainMessage(MessageTypes.DESTROY)
						.sendToTarget();
				lowPriorityThreadHandler = null;
				serviceThreadHandler = null;
				lowPriorityThread = null;
				Looper.myLooper().quit();
				break;

			// ------------------- METODI BASSA PRIORITA' --------------------
			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC",
							Toast.LENGTH_SHORT).show();
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
							TAG + "UPLOAD_MY_PROFILE_IMAGE", Toast.LENGTH_SHORT)
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
							Toast.LENGTH_SHORT).show();
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
							Toast.LENGTH_SHORT).show();
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
							TAG + "DOWNLOAD_USER_IMAGE", Toast.LENGTH_SHORT)
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
				bnd = msg.getData();

				boolean calledFromSingleChatContact = bnd.getBoolean(
						"calledFromSingleChatContact", false);

				boolean calledFromThreadStarted = bnd.getBoolean(
						"calledFromThreadStarted", false);
				updateNotification(!calledFromSingleChatContact
						&& !calledFromThreadStarted);

				break;

			case MessageTypes.NETWORK_CONNECTED:

				m = new Message();
				m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;

				addToQueue(m, 0, 4, true, true);

				break;

			case MessageTypes.MAKE_DB_DUMP:
				try {
					m = new Message();
					m.what = MessageTypes.MAKE_DB_DUMP;
					m.setData(msg.getData());
					sendToLowPriorityThreadHandler(m);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, true, false);
				}
				break;

			case MessageTypes.GET_LAST_LOCAL_DB_BK_DATA:
				mainThreadHandler
						.removeMessages(MessageTypes.GET_LAST_LOCAL_DB_BK_DATA);

				File mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				File[] listDBDumpFiles = mainFolder.listFiles();

				String dataLastLocalDBDump = "0";

				if (listDBDumpFiles.length > 0) {
					for (int i = 0; i < listDBDumpFiles.length; i++) {
						if (listDBDumpFiles[i].isFile()) {
							String fileName = listDBDumpFiles[i].getName();
							if (fileName
									.startsWith(Settings.DUMP_DB_BASE_FILE_NAME
											.substring(1))) {
								String data = fileName.substring(fileName
										.lastIndexOf("_") + 1);
								if (Long.parseLong(dataLastLocalDBDump) < Long
										.parseLong(data)) {
									dataLastLocalDBDump = data;
								}
							} else {
								continue;
							}

						}
					}
				}

				syncMsg = new Message();
				syncMsg.what = MessageTypes.DROPBOX_REFRESH;
				b = new Bundle();
				b.putString("lastLocalBkData", dataLastLocalDBDump);
				syncMsg.setData(b);
				SynchronizationManager.getInstance().onSynchronizationFinish(
						syncMsg);

				break;

			case MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA:
				mainThreadHandler
						.removeMessages(MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA);

				// Dropbox authentication
				sessionDropbox = DropboxManager.getInstance(
						UMessageApplication.getContext()).buildSession();
				mApi = new DropboxAPI<AndroidAuthSession>(sessionDropbox);
				// Fine Dropbox authentication

				// Check user logged
				if (!mApi.getSession().isLinked()) {
					Toast.makeText(UMessageApplication.getContext(),
							"Dropbox user not logged in...", Toast.LENGTH_SHORT)
							.show();
					break;
				}
				// Fine check user logged

				// Download last dropbox db dump, se file non gia esistente
				// localmente
				try {
					DropboxAPI.Entry entries = mApi.metadata("/", 100, null,
							true, null);
					String dataLastDropboxDBDump = "0", fileToDownload = "";
					for (DropboxAPI.Entry e : entries.contents) {
						String fileName = e.fileName();
						if (fileName.startsWith(Settings.DUMP_DB_BASE_FILE_NAME
								.substring(1))) {
							String data = fileName.substring(fileName
									.lastIndexOf("_") + 1);
							if (Long.parseLong(dataLastDropboxDBDump) < Long
									.parseLong(data)) {
								dataLastDropboxDBDump = data;
								fileToDownload = fileName;
							}
						} else {
							continue;
						}
					}

					syncMsg = new Message();
					syncMsg.what = MessageTypes.DROPBOX_REFRESH;
					b = new Bundle();
					b.putString("lastOnlineBkData", dataLastDropboxDBDump);
					syncMsg.setData(b);
					SynchronizationManager.getInstance()
							.onSynchronizationFinish(syncMsg);

				} catch (Exception e) {

				}

				break;

			case MessageTypes.GET_DROPBOX_ACCOUNT_INFO:
				mainThreadHandler
						.removeMessages(MessageTypes.GET_DROPBOX_ACCOUNT_INFO);

				// Dropbox authentication
				sessionDropbox = DropboxManager.getInstance(
						UMessageApplication.getContext()).buildSession();
				mApi = new DropboxAPI<AndroidAuthSession>(sessionDropbox);
				// Fine Dropbox authentication

				try {
					syncMsg = new Message();
					syncMsg.what = MessageTypes.DROPBOX_REFRESH;
					b = new Bundle();
					b.putString("userLoggedIn", mApi.accountInfo().displayName);
					syncMsg.setData(b);
					SynchronizationManager.getInstance()
							.onSynchronizationFinish(syncMsg);
				} catch (Exception e) {
				}

				break;

			case MessageTypes.START_DROPBOX_SYNCHRONIZATION:
				mainThreadHandler
						.removeMessages(MessageTypes.START_DROPBOX_SYNCHRONIZATION);

				try {
					m = new Message();
					m.what = MessageTypes.START_DROPBOX_SYNCHRONIZATION;
					m.setData(msg.getData());
					lowPriorityThreadHandler.sendMessage(m);
				} catch (Exception e) {
					addToQueue(msg, TIME_MINUTE, 4, true, false);
				}

				break;

			case MessageTypes.PING_FROM_GCM:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "PING_FROM_GCM", Toast.LENGTH_SHORT).show();
				}

				// ping gcm funzionante, rimuovo check per registrazione gcm...
				mainThreadHandler
						.removeMessages(MessageTypes.CHECK_GOOGLE_PLAY_SERVICES);
				// ... e polling
				mainThreadHandler
						.removeMessages(MessageTypes.GET_CHATS_VERSION);

				break;

			case MessageTypes.CHECK_GOOGLE_PLAY_SERVICES:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_GOOGLE_PLAY_SERVICES",
							Toast.LENGTH_SHORT).show();
				}

				bnd = msg.getData();

				request = new Bundle();
				request.putBoolean(ConfigurationManager.GCM_ID, true);
				request.putBoolean(ConfigurationManager.SESSION_ID, true);
				response = ConfigurationManager.getValues(request);

				if (!response.getString(ConfigurationManager.GCM_ID, "")
						.equals("")
						&& bnd.getBoolean("calledFromThreadStarted", true)) {
					try {
						parameters = new JSONObject();
						parameters.accumulate("action", "PING_ME_GCM");
						parameters.accumulate("sessionId", response.getString(
								ConfigurationManager.SESSION_ID, ""));

						httpResult = doRequest(parameters);

						if (httpResult.error) {
							addToQueue(msg, TIME_MINUTE, 4, false, false);
							break;
						}

						if (!httpResult.result.getBoolean("isSessionValid")) {
							request = new Bundle();
							request.putString(ConfigurationManager.SESSION_ID,
									"");
							if (!ConfigurationManager.saveValues(request)) {
								Utility.reportError(
										UMessageApplication.getContext(),
										new Exception(
												"Configurazione non scritta: onHandleMessage() CHECK_GOOGLE_PLAY_SERVICE MainThread.java "),
										TAG);
							}

							break;
						}

						m = new Message();
						m.what = MessageTypes.CHECK_GOOGLE_PLAY_SERVICES;
						b = new Bundle();
						b.putBoolean("calledFromThreadStarted", false);
						m.setData(b);
						addToQueue(m, TIME_WAIT_PING_GCM, 4, true, false);
						break;

					} catch (Exception e) {
						Utility.reportError(UMessageApplication.getContext(),
								e, TAG + "CHECK_GOOGLE_PLAY_SERVICES");
					}
					break;
				}

				if (!registerGCM()) {
					addToQueue(msg, TIME_HOUR, 4, false, false);
				} else {
					// gcm registrato correttamente, rimuovo polling
					mainThreadHandler
							.removeMessages(MessageTypes.GET_CHATS_VERSION);
				}

				break;

			case MessageTypes.UPLOAD_NEW_MESSAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "UPLOAD_NEW_MESSAGE", Toast.LENGTH_SHORT)
							.show();
				}

				Cursor infoChat = null;
				try {

					bnd = msg.getData();
					parameters = new JSONObject();

					request = new Bundle();
					request.putBoolean(ConfigurationManager.SESSION_ID, true);
					response = ConfigurationManager.getValues(request);

					infoChat = p.getChat(bnd.getString("prefix"),
							bnd.getString("num"));

					infoChat.moveToNext();

					parameters.accumulate("action", "SEND_NEW_MESSAGE");
					parameters.accumulate("sessionId", response.getString(
							ConfigurationManager.SESSION_ID, ""));
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
						request = new Bundle();
						request.putString(ConfigurationManager.SESSION_ID, "");
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: onHandleMessage() UPLOAD_NEW_MESSAGE MainThread.java "),
									TAG);
						}

						break;
					}

					if (!httpResult.result.getBoolean("isDestValid")) {
						// bisognerebbe cancellare il messaggio dal db
						// locale ?
						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "destination not valid",
									Toast.LENGTH_SHORT).show();
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
				} finally {
					if (infoChat != null) {
						infoChat.close();
					}
				}

				break;

			// ---------------- DA QUI IN POI AGGIORNATI CON UTILIZZO METODI
			// NUOVI

			// da cotrollare, ERRORE indexoutofboundexception, quando localmente
			// no chat/messaggi e qualche messaggio da scaricare/sync da db
			// online
			case MessageTypes.SYNCHRONIZE_CHAT:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "SYNCHRONIZE_CHAT", Toast.LENGTH_SHORT)
							.show();
				}

				Cursor localMessage = null;
				infoChat = null;
				try {
					bnd = msg.getData();
					request = new Bundle();
					request.putBoolean(ConfigurationManager.SESSION_ID, true);
					response = ConfigurationManager.getValues(request);

					String prefixDest = bnd.getString("prefix");
					String numDest = bnd.getString("num");
					String sessionId = response.getString(
							ConfigurationManager.SESSION_ID, "");
					parameters = new JSONObject();
					infoChat = p.getChat(prefixDest, numDest);
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
						request = new Bundle();
						request.putString(ConfigurationManager.SESSION_ID, "");
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: onHandleMessage() SYNCHRONIZE_CHAT MainThread.java "),
									TAG);
						}

						break;
					}

					if (!httpResult.result.getBoolean("isDestValid")) {
						// bisognerebbe cancellare i messaggi dal db
						// locale ?
						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "destination not valid",
									Toast.LENGTH_SHORT).show();
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
										message.getString("status"));
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

							// ERRORE QUI??? indexoutofboundexception??? forse
							// risolto ricaricando il messaggio dal db in caso
							// non fosse presente e appena inserito nel db
							// locale
							localMessage = p.getMessageByTag(prefixDest,
									numDest, message.getString("tag"));
							localMessage.moveToFirst();

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
				} finally {
					if (infoChat != null) {
						infoChat.close();
					}
					if (localMessage != null) {
						localMessage.close();
					}
				}

				break;

			case MessageTypes.CHECK_MESSAGES_TO_UPLOAD:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_MESSAGES_TO_UPLOAD",
							Toast.LENGTH_SHORT).show();
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
				if (messagesToUpload != null) {
					messagesToUpload.close();
				}

				if (errors) {
					addToQueue(msg, TIME_MINUTE, 4, true, false);
				}

				break;

			case MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "CHECK_CHATS_TO_SYNCHRONIZE",
							Toast.LENGTH_SHORT).show();
				}

				Cursor chats = null;
				try {

					chats = p.getAllChats();

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
				} finally {
					if (chats != null) {
						chats.close();
					}
				}

				addToQueue(msg, TIME_HOUR, 4, true, false);
				break;

			case MessageTypes.GET_CHATS_VERSION:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "GET_CHATS_VERSION", Toast.LENGTH_SHORT)
							.show();
				}

				Cursor localChat = null;
				try {

					request = new Bundle();
					request.putBoolean(ConfigurationManager.SESSION_ID, true);
					response = ConfigurationManager.getValues(request);

					String sessionId = response.getString(
							ConfigurationManager.SESSION_ID, "");
					parameters = new JSONObject();
					parameters.accumulate("action", "GET_CHATS_VERSION");
					parameters.accumulate("sessionId", sessionId);

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						addToQueue(msg, TIME_SYNC_POLLING, 4, true, false);
						break;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						request = new Bundle();
						request.putString(ConfigurationManager.SESSION_ID, "");
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: onHandleMessage() GET_CHATS_VERSION MainThread.java "),
									TAG);
						}

						break;
					}

					if (httpResult.result.getInt("numChats") == 0) {
						addToQueue(msg, TIME_SYNC_POLLING, 4, true, false);
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
						localChat = p.getChat(prefixDest, numDest);
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
				} finally {
					if (localChat != null) {
						localChat.close();
					}
				}

				addToQueue(msg, TIME_SYNC_POLLING, 4, true, false);

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

				request = new Bundle();
				request.putBoolean(ConfigurationManager.SESSION_ID, true);
				response = ConfigurationManager.getValues(request);

				String regid;
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(UMessageApplication
							.getContext());
				}

				try {
					regid = gcm.register(Settings.GOOGLE_PROJECT_NUMBER);

					JSONObject parameters = new JSONObject();
					parameters.accumulate("action", "UPDATE_GCM_ID");
					parameters.accumulate("sessionId", response.getString(
							ConfigurationManager.SESSION_ID, ""));
					parameters.accumulate("gcmId", regid);
					HttpResponseUmsg httpResult = new HttpResponseUmsg();

					httpResult = doRequest(parameters);

					if (httpResult.error) {
						return false;
					}

					if (!httpResult.result.getBoolean("isSessionValid")) {
						request = new Bundle();
						request.putString(ConfigurationManager.SESSION_ID, "");
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: registerGcm1() MainThread.java "),
									TAG);
						}

						return false;
					}

					if (httpResult.result.getBoolean("isGcmIdUpdated")) {
						request = new Bundle();
						request.putString(ConfigurationManager.GCM_ID, regid);
						if (!ConfigurationManager.saveValues(request)) {
							Utility.reportError(
									UMessageApplication.getContext(),
									new Exception(
											"Configurazione non scritta: registerGcm2() MainThread.java "),
									TAG);
						}

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
		private boolean updateNotification(boolean playSoundAndVibrate) {
			Provider p = new Provider(UMessageApplication.getContext());

			Cursor messagesNotification = p.getAllNewMessages();

			if (messagesNotification == null) {
				return false;
			}

			int totalNewMessages = messagesNotification.getCount();
			int totalNewMessagesToShow = 0;
			int totalNewConversationMessages = 0;
			boolean firstMessage = true, newDest = false;

			NotificationManager notificationManager = (NotificationManager) UMessageApplication
					.getContext()
					.getSystemService(
							UMessageApplication.getContext().NOTIFICATION_SERVICE);

			String prefix = "", num = "", name = "", message = "", data = "", type = "";

			if (totalNewMessages == 0) {
				notificationManager.cancelAll();
				return true;
			}

			messagesNotification.moveToFirst();

			Notification notification;
			Builder b = new Notification.Builder(
					UMessageApplication.getContext());
			String contextText = "";
			int count = 0;
			do {
				// se attualmente aperta activity relativa a user con nuovi
				// messaggi non li conteggio per la nuova notifica
				if (MainThread.prefixDisplayed.equals(messagesNotification
						.getString(messagesNotification
								.getColumnIndex(DatabaseHelper.KEY_PREFIX)))
						&& MainThread.numDisplayed
								.equals(messagesNotification.getString(messagesNotification
										.getColumnIndex(DatabaseHelper.KEY_NUM)))) {
					continue;
				}

				totalNewMessagesToShow++;

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

			if (totalNewMessagesToShow == 0) {
				notificationManager.cancelAll();
				return true;
			}

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
								totalNewMessagesToShow
										+ (totalNewMessagesToShow == 1 ? " nuovo messaggio"
												: " nuovi messaggi"))
						.getNotification();
			} else {
				action = new Intent(
						UMessageApplication.getContext(),
						com.lollotek.umessage.activities.ConversationsList.class);
				notification = b
						.setContentTitle(
								"" + totalNewMessagesToShow + " nuovi messaggi")
						.setContentText(
								"In " + totalNewConversationMessages
										+ " conversazioni").getNotification();
			}

			pIntent = PendingIntent.getActivity(
					UMessageApplication.getContext(), 0, action,
					PendingIntent.FLAG_CANCEL_CURRENT);

			notificationManager.cancel(0);
			notificationManager.cancelAll();

			notification = b.setContentIntent(pIntent)
					.setSmallIcon(R.drawable.ic_launcher).getNotification();

			if (playSoundAndVibrate) {
				notification = b
						.setVibrate(new long[] { 500, 1000 })
						.setSound(
								RingtoneManager
										.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
						.getNotification();

			}

			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(0, notification);

			if (messagesNotification != null) {
				messagesNotification.close();
			}

			return true;

		}

	}

}
