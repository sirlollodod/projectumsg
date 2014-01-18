package com.lollotek.umessage.threads;

import java.io.File;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.classes.ExponentialQueueTime;
import com.lollotek.umessage.classes.HttpResponseUmsg;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class LowPriorityThread extends Thread {

	private static final String TAG = LowPriorityThread.class.getName() + ":\n";

	private LowPriorityThreadHandler lowPriorityThreadHandler = null;
	private Handler mainThreadHandler = null;
	private ExponentialQueueTime timeQueue;
	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;

	public LowPriorityThread(Handler handler) {
		mainThreadHandler = handler;

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

		lowPriorityThreadHandler = new LowPriorityThreadHandler();
		mainThreadHandler.obtainMessage(
				MessageTypes.RECEIVE_LOW_PRIORITY_THREAD_HANDLER,
				lowPriorityThreadHandler).sendToTarget();

		Looper.loop();
	}

	private class LowPriorityThreadHandler extends Handler {

		private File mainFolder, myNewProfileImage;
		private Provider p;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Message m, syncMsg;
			Bundle b, bnd;
			HttpResponseUmsg httpResult = new HttpResponseUmsg();

			JSONObject parameters, result;

			p = new Provider(UMessageApplication.getContext());

			switch (msg.what) {
			case MessageTypes.DESTROY:
				mainThreadHandler = null;
				lowPriorityThreadHandler = null;
				Looper.myLooper().quit();

				break;

			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC",
							Toast.LENGTH_LONG).show();
				}
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
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():DOWNLOAD_MY_PROFILE_IMAGE");

					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "UPLOAD_MY_PROFILE_IMAGE", Toast.LENGTH_LONG)
							.show();
				}

				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);

				try {

					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());

					result = Utility.uploadImageProfile(
							UMessageApplication.getContext(),
							Settings.SERVER_URL, myNewProfileImage,
							configuration.getSessid());

					if (Settings.debugMode) {
						Toast.makeText(UMessageApplication.getContext(),
								TAG + result.toString(), Toast.LENGTH_LONG)
								.show();
					}
					if ((result == null)
							|| (result.getString("errorCode").equals("KO"))) {
						addToQueue(msg, TIME_MINUTE, 4, false, false);
					} else if (result.getString("errorCode").equals("OK")
							&& result.getBoolean("isSessionValid")) {
						configuration.setProfileImageToUpload(false);
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);

					} else if (!result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
						Utility.setConfiguration(
								UMessageApplication.getContext(), configuration);
						if (Settings.debugMode) {
							Toast.makeText(UMessageApplication.getContext(),
									TAG + "sessione non valida... azzerata!",
									Toast.LENGTH_LONG).show();
						}

					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():UPLOAD_MY_PROFILE_IMAGE");

					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_USER_IMAGE_FROM_SRC",
							Toast.LENGTH_LONG).show();
				}
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
					Utility.reportError(
							UMessageApplication.getContext(),
							e,
							TAG
									+ ": handleMessage():DOWNLOAD_USER_IMAGE_FROM_SRC");
					addToQueue(msg, TIME_HOUR, 4, false, false);
				}

				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_USER_IMAGE", Toast.LENGTH_LONG)
							.show();
				}
				bnd = msg.getData();

				try {

					parameters = new JSONObject();
					parameters.accumulate("action", "CHECK_USER_REGISTERED");
					parameters.accumulate("prefix", bnd.getString("prefix"));
					parameters.accumulate("num", bnd.getString("num"));
					parameters.accumulate("anonymous", "yes");

					result = Utility.doPostRequest(Settings.SERVER_URL,
							parameters);

					if ((!result.getString("errorCode").equals("OK"))
							|| !result.getBoolean("isRegistered")) {
						break;
					}

					// Controllo adesso singolo contatto se immagine profilo
					// da scaricare/aggiornare
					userImageUrl = result.getString("imageProfileSrc");
					if (userImageUrl.length() > 2) {
						userImageUrl = userImageUrl.substring(2);
						m = new Message();
						m.what = MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC;
						m.obj = Settings.SERVER_URL + userImageUrl;
						m.setData(bnd);

						lowPriorityThreadHandler.sendMessage(m);
					}

				} catch (Exception e) {
					Utility.reportError(UMessageApplication.getContext(), e,
							TAG + ": handleMessage():DOWNLOAD_USER_IMAGE");
					addToQueue(msg, TIME_HOUR, 4, false, false);
				}
				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "DOWNLOAD_ALL_USERS_IMAGES",
							Toast.LENGTH_LONG).show();
				}
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

						parameters = new JSONObject();
						result = new JSONObject();

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
						Utility.reportError(
								UMessageApplication.getContext(),
								e,
								TAG
										+ ": handleMessage():DOWNLOAD_ALL_USERS_IMAGES");

					}

				}

				//addToQueue(msg, TIME_DAY, 4, true, false);

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
				lowPriorityThreadHandler.removeMessages(msg.what);
			}

			if (timeDelay == -1) {
				if ((++newMsg.arg2) > 4) {
					newMsg.arg2 = 0;
					newMsg.arg1 = timeQueue.toNext(newMsg.arg1);
				}
				if (newMsg.arg1 > maxTimeDelayQueue) {
					newMsg.arg1 = maxTimeDelayQueue;
				}

				lowPriorityThreadHandler.sendMessageDelayed(newMsg,
						timeQueue.getTime(newMsg.arg1) * 1000);
			} else if (timeDelay > 0) {
				lowPriorityThreadHandler.sendMessageDelayed(newMsg,
						timeDelay * 1000);
			} else {
				if (atFrontQueue) {
					lowPriorityThreadHandler.sendMessageAtFrontOfQueue(newMsg);
				} else {
					lowPriorityThreadHandler.sendMessage(newMsg);
				}
			}

		}

	}
}
