package com.lollotek.umessage.threads;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.managers.SynchronizationManager;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class MainThread extends Thread {

	private Handler serviceThreadHandler = null, updateThreadHandler = null,
			newMessageThreadHandler = null;
	private MainThreadHandler mainThreadHandler = null;
	private UpdateThread updateThread = null;
	private NewMessageThread newMessageThread = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;

	GoogleCloudMessaging gcm = null;

	public MainThread(Handler handler) {
		serviceThreadHandler = handler;
	}

	public void run() {
		Looper.prepare();

		mainThreadHandler = new MainThreadHandler();
		serviceThreadHandler.obtainMessage(
				MessageTypes.RECEIVE_MAIN_THREAD_HANDLER, mainThreadHandler)
				.sendToTarget();
		updateThread = new UpdateThread(mainThreadHandler);
		updateThread.start();
		newMessageThread = new NewMessageThread(mainThreadHandler);
		newMessageThread.start();

		mainThreadHandler
				.obtainMessage(MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE)
				.sendToTarget();
		mainThreadHandler.obtainMessage(MessageTypes.CHECK_MESSAGES_TO_UPLOAD)
				.sendToTarget();

		Looper.loop();
	}

	private void registerGCM() {
		if (Utility.checkPlayServices(UMessageApplication.getContext())) {
			Configuration configuration = Utility
					.getConfiguration(UMessageApplication.getContext());

			String gcmid = configuration.getGcmid();
			if ((gcmid == "") || (gcmid == null)) {

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

				} catch (IOException e) {

				}

			} else {

			}
		} else {

		}
	}

	private class MainThreadHandler extends Handler {

		private File mainFolder, myNewProfileImage;
		private Provider p;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Message m, syncMsg;
			Bundle b, bnd;

			p = new Provider(UMessageApplication.getContext());

			switch (msg.what) {
			case MessageTypes.RECEIVE_UPDATE_THREAD_HANDLER:
				updateThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.RECEIVE_NEW_MESSAGE_THREAD_HANDLER:
				newMessageThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.DESTROY:
				updateThreadHandler.obtainMessage(MessageTypes.DESTROY)
						.sendToTarget();
				newMessageThreadHandler.obtainMessage(MessageTypes.DESTROY)
						.sendToTarget();
				updateThreadHandler = null;
				newMessageThreadHandler = null;
				serviceThreadHandler = null;
				updateThread = null;
				newMessageThread = null;
				Looper.myLooper().quit();
				break;

			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				String imageUrl = (String) msg.obj;

				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());
				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);
				try {
					Utility.downloadFileFromUrl(
							UMessageApplication.getContext(),
							myNewProfileImage, imageUrl);
				} catch (HttpException e) {
					this.sendEmptyMessageDelayed(
							MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC,
							TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);

				try {

					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());

					JSONObject result = Utility.uploadImageProfile(
							UMessageApplication.getContext(),
							Settings.SERVER_URL, myNewProfileImage,
							configuration.getSessid());

					Toast.makeText(UMessageApplication.getContext(),
							result.toString(), Toast.LENGTH_LONG).show();
					if ((result == null)
							|| (result.getString("errorCode").equals("KO"))) {
						m = new Message();
						m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
						this.sendMessageDelayed(m, TIME_MINUTE * 1000);
					} else if (result.getString("errorCode").equals("OK")
							&& result.getBoolean("isSessionValid")) {
						configuration.setProfileImageToUpload(false);
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);

					} else if (!result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);
						Toast.makeText(UMessageApplication.getContext(),
								"sessione non valida... azzerata!",
								Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_LONG).show();
					m = new Message();
					m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
					this.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				bnd = msg.getData();

				String userImageUrl = (String) msg.obj;
				String newUserImageFileName = userImageUrl
						.substring(userImageUrl.indexOf("+"));
				int posNumDataSeparator = newUserImageFileName.indexOf("_");
				String newImageName = "/"
						+ newUserImageFileName
								.substring(0, posNumDataSeparator) + ".jpg";
				long newImageData = Long.parseLong(newUserImageFileName
						.substring(posNumDataSeparator + 1,
								newUserImageFileName.length() - 4));
				File userImage = new File(mainFolder.toString()
						+ Settings.CONTACT_PROFILE_IMAGES_FOLDER + newImageName);

				try {
					Cursor userInfo = p.getUserInfo(bnd.getString("prefix"),
							bnd.getString("num"));

					if (!userInfo.moveToNext()) {
						break;
					}

					String imageSrc = userInfo.getString(userInfo
							.getColumnIndex(DatabaseHelper.KEY_IMGSRC));

					long imageData = Long.parseLong(userInfo.getString(userInfo
							.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));

					if ((imageSrc.equals("0")) || (imageData == 0)
							|| (imageData != newImageData)) {
						if (Utility.downloadFileFromUrl(
								UMessageApplication.getContext(), userImage,
								userImageUrl)) {
							p.updateUserImage(bnd.getString("prefix"),
									bnd.getString("num"), newImageName,
									newImageData);
						}
					}
				} catch (Exception e) {

				}

				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				Cursor users = p.getTotalUser();

				while (users.moveToNext()) {

					String prefix = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_PREFIX));
					String num = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_NUM));
					String imageSrc = users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_IMGSRC));

					long imageData = Long.parseLong(users.getString(users
							.getColumnIndex(DatabaseHelper.KEY_IMGDATA)));

					try {

						JSONObject parameters = new JSONObject();
						JSONObject result = new JSONObject();

						parameters
								.accumulate("action", "CHECK_USER_REGISTERED");
						parameters.accumulate("prefix", prefix);
						parameters.accumulate("num", num);
						parameters.accumulate("anonymous", "yes");

						result = Utility.doPostRequest(Settings.SERVER_URL,
								parameters);

						String newUserImageUrl;
						if (!result.getString("errorCode").equals("OK")) {
							continue;
						}

						newUserImageUrl = result.getString("imageProfileSrc");

						if (newUserImageUrl.length() == 0) {
							continue;
						}

						newUserImageUrl = newUserImageUrl.substring(2);

						newUserImageFileName = newUserImageUrl
								.substring(newUserImageUrl.indexOf("+"));

						posNumDataSeparator = newUserImageFileName.indexOf("_");
						newImageName = "/"
								+ newUserImageFileName.substring(0,
										posNumDataSeparator) + ".jpg";
						newImageData = Long.parseLong(newUserImageFileName
								.substring(posNumDataSeparator + 1,
										newUserImageFileName.length() - 4));
						userImage = new File(mainFolder.toString()
								+ Settings.CONTACT_PROFILE_IMAGES_FOLDER
								+ newImageName);

						if ((imageSrc.equals("0")) || (imageData == 0)
								|| (imageData != newImageData)) {
							if (Utility.downloadFileFromUrl(
									UMessageApplication.getContext(),
									userImage, Settings.SERVER_URL
											+ newUserImageUrl)) {
								p.updateUserImage(prefix, num, newImageName,
										newImageData);
							}

						}

					} catch (Exception e) {

					}

				}

				// Schedulo aggiornamento immagini tra 1gg
				this.sendEmptyMessageDelayed(
						MessageTypes.DOWNLOAD_ALL_USERS_IMAGES, TIME_DAY * 1000);

				break;

			case MessageTypes.SEND_NEW_TEXT_MESSAGE:
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

						mainThreadHandler.sendMessage(m);

					} else {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);
					}
				} catch (Exception e) {
					mainThreadHandler.sendMessageDelayed(msg,
							TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.UPLOAD_NEW_MESSAGE:

				try {

					bnd = msg.getData();
					JSONObject parameters = new JSONObject();
					JSONObject result = null;
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

					result = Utility.doPostRequest(Settings.SERVER_URL,
							parameters);

					if (result == null) {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);
					} else if (result.getString("errorCode").equals("KO")) {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);
					} else if (result.getString("errorCode").equals("OK")) {
						if (!result.getBoolean("isSessionValid")) {
							configuration.setSessid("");
							Utility.setConfiguration(
									UMessageApplication.getContext(),
									configuration);

							break;
						}

						if (!result.getBoolean("isDestValid")) {
							// bisognerebbe cancellare il messaggio dal db
							// locale ?
							Toast.makeText(UMessageApplication.getContext(),
									"destination not valid", Toast.LENGTH_LONG)
									.show();

							break;
						}

						if (result.getBoolean("syncChatRequired")) {
							m = new Message();
							m.what = MessageTypes.SYNCHRONIZE_CHAT;
							b = new Bundle();
							b.putString("prefix", bnd.getString("prefix"));
							b.putString("num", bnd.getString("num"));
							b.putBoolean("messagesToUpload", true);
							m.setData(b);

							mainThreadHandler.sendMessageAtFrontOfQueue(m);

							break;
						}

						if (result.getBoolean("chatVersionChanged")) {
							p.updateChatVersion(infoChat.getInt(infoChat
									.getColumnIndex(DatabaseHelper.KEY_ID)),
									result.getString("newChatVersion"));

						}

						if (p.updateMessage(bnd.getLong("messageId"), result
								.getString("statusNewMessage"), Long
								.parseLong(result.getString("dataNewMessage")))) {

							syncMsg = new Message();
							syncMsg.what = MessageTypes.MESSAGE_UPLOADED;
							SynchronizationManager.getInstance()
									.onSynchronizationFinish(syncMsg);

							break;

						}

					} else {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);
					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_LONG).show();
					mainThreadHandler.sendMessageDelayed(msg,
							TIME_MINUTE * 1000);
				}

				break;

			case MessageTypes.SYNCHRONIZE_CHAT:
				try {
					bnd = msg.getData();
					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());
					String prefixDest = bnd.getString("prefix");
					String numDest = bnd.getString("num");
					String sessionId = configuration.getSessid();
					JSONObject parameters = new JSONObject();
					JSONObject result = null;
					Cursor infoChat = p.getChat(prefixDest, numDest);

					infoChat.moveToFirst();

					parameters
							.accumulate("action", "GET_CONVERSATION_MESSAGES");
					parameters.accumulate("sessionId", sessionId);
					parameters.accumulate("destPrefix", prefixDest);
					parameters.accumulate("destNum", numDest);
					parameters
							.accumulate(
									"localChatVersion",
									infoChat.getString(infoChat
											.getColumnIndex(DatabaseHelper.KEY_VERSION)));

					result = Utility.doPostRequest(Settings.SERVER_URL,
							parameters);

					if (result == null) {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);

						break;
					}

					String newOnlineChatVersion = result
							.getString("onlineChatVersion");

					if (result.getString("errorCode").equals("KO")) {
						mainThreadHandler.sendMessageDelayed(msg,
								TIME_MINUTE * 1000);

						break;
					}

					if (result.getString("errorCode").equals("OK")) {
						if (!result.getBoolean("isSessionValid")) {
							configuration.setSessid("");
							Utility.setConfiguration(
									UMessageApplication.getContext(),
									configuration);

							break;
						}

						if (!result.getBoolean("isDestValid")) {
							// bisognerebbe cancellare i messaggi dal db
							// locale ?
							Toast.makeText(UMessageApplication.getContext(),
									"destination not valid", Toast.LENGTH_LONG)
									.show();

							break;
						}

						if (result.getInt("numMessages") == 0) {
							infoChat = p.getChat(prefixDest, numDest);
							infoChat.moveToFirst();
							p.updateChatVersion(infoChat.getInt(infoChat
									.getColumnIndex(DatabaseHelper.KEY_ID)),
									newOnlineChatVersion);

							if (bnd.getBoolean("messagesToUpload", false)) {
								m = new Message();
								m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;
								mainThreadHandler.sendMessage(m);
							}

							break;
						}

						boolean updatedSomething = false;
						JSONArray messages = result.getJSONArray("messages");
						Cursor localMessage;

						for (int i = 0; i < messages.length(); i++) {
							JSONObject message = messages.getJSONObject(i);

							boolean isIncomingMessage = message.getString(
									"direction").equals("0") ? false : true;

							localMessage = p.getMessageByTag(prefixDest,
									numDest, message.getString("tag"));

							String onlineMessageStatus = message
									.getString("status");
							long onlineMessageData = message.getLong("data");
							long idLocalMessage;

							if (isIncomingMessage) {
								if ((localMessage == null)
										|| (!localMessage.moveToFirst())) {

									ContentValues newIncomingMessage = new ContentValues();
									newIncomingMessage.put(
											DatabaseHelper.KEY_PREFIX,
											prefixDest);
									newIncomingMessage.put(
											DatabaseHelper.KEY_NUM, numDest);
									newIncomingMessage.put(
											DatabaseHelper.KEY_DIRECTION, 1);
									newIncomingMessage.put(
											DatabaseHelper.KEY_STATUS, "2");
									newIncomingMessage.put(
											DatabaseHelper.KEY_DATA,
											message.getString("data"));
									newIncomingMessage.put(
											DatabaseHelper.KEY_TYPE, "text");
									newIncomingMessage.put(
											DatabaseHelper.KEY_MESSAGE,
											message.getString("msg"));
									newIncomingMessage.put(
											DatabaseHelper.KEY_TOREAD, "1");
									newIncomingMessage.put(
											DatabaseHelper.KEY_TAG,
											message.getString("tag"));

									long newMessageId = p
											.insertNewMessage(newIncomingMessage);
									updatedSomething = true;

									parameters = new JSONObject();
									parameters.accumulate("action",
											"UPDATE_MESSAGE_DOWNLOADED");
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
											"1");

									result = null;
									result = Utility.doPostRequest(
											Settings.SERVER_URL, parameters);

									if (result == null) {
										// niente
									} else if (result.getString("errorCode")
											.equals("KO")) {
										// niente
									} else if (result.getString("errorCode")
											.equals("OK")) {
										// update local message status ----> 3
										p.updateMessage(newMessageId, "3",
												onlineMessageData);
									}

								} else {
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
											parameters
													.accumulate("action",
															"UPDATE_MESSAGE_DOWNLOADED");
											parameters.accumulate("sessionId",
													sessionId);
											parameters.accumulate("destPrefix",
													prefixDest);
											parameters.accumulate("destNum",
													numDest);
											parameters.accumulate(
													"messageData",
													message.getString("data"));
											parameters.accumulate("messageTag",
													message.get("tag"));
											parameters.accumulate(
													"messageDirection", "1");

											result = null;
											result = Utility.doPostRequest(
													Settings.SERVER_URL,
													parameters);

											if (result == null) {
												// niente
											} else if (result.getString(
													"errorCode").equals("KO")) {
												// niente
											} else if (result.getString(
													"errorCode").equals("OK")) {
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
								if (!localMessage.moveToFirst()) {
									// mio messaggio presente ancora online ma
									// non localmente... lo devo inserire
									// localmente

								}

								idLocalMessage = localMessage
										.getLong(localMessage
												.getColumnIndex(DatabaseHelper.KEY_ID));

								if (!localMessage
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
										parameters.accumulate("destNum",
												numDest);
										parameters.accumulate("messageData",
												message.getString("data"));
										parameters.accumulate("messageTag",
												message.get("tag"));
										parameters.accumulate(
												"messageDirection", "0");

										result = null;
										result = Utility
												.doPostRequest(
														Settings.SERVER_URL,
														parameters);

										if (result == null) {
											// niente
										} else if (result
												.getString("errorCode").equals(
														"KO")) {
											// niente
										} else if (result
												.getString("errorCode").equals(
														"OK")) {
											// niente, messaggio locale gia
											// segnato come notifica lettura

										}

									}

								}

							}

						}// fine for

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

						if (bnd.getBoolean("messagesToUpload", false)) {
							m = new Message();
							m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;
							mainThreadHandler.sendMessage(m);
						}

					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_LONG).show();
				}

				break;

			case MessageTypes.CHECK_MESSAGES_TO_UPLOAD:

				Cursor messagesToUpload = p.getMessagesToUpload();

				if (messagesToUpload == null) {
					break;
				}

				int errorCount = 0;
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

						mainThreadHandler.sendMessage(m);

					} catch (Exception e) {
						errorCount++;

					}

				}

				if (errorCount > 0) {
					m = new Message();
					m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;
					mainThreadHandler.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}

				break;

			case MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE:
				try {

					Cursor chats = p.getAllChats();

					if (chats == null) {
						mainThreadHandler
								.removeMessages(MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE);
						m = new Message();
						m.what = MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE;
						mainThreadHandler.sendMessageDelayed(m,
								TIME_MINUTE * 1000);

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

						mainThreadHandler.sendMessageAtFrontOfQueue(m);

					}

				} catch (Exception e) {
					Toast.makeText(UMessageApplication.getContext(),
							e.toString(), Toast.LENGTH_LONG).show();
				}

				mainThreadHandler
						.removeMessages(MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE);
				m = new Message();
				m.what = MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE;
				mainThreadHandler.sendMessageDelayed(m, TIME_MINUTE * 1000);

				break;

			}
		}
	}
}
