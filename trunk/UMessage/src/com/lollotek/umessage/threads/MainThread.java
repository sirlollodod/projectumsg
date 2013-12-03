package com.lollotek.umessage.threads;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;
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

			Message m;

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
					JSONObject result = Utility.uploadImageProfile(
							UMessageApplication.getContext(),
							Settings.SERVER_URL,
							myNewProfileImage,
							Utility.getConfiguration(
									UMessageApplication.getContext())
									.getSessid());
					Configuration configuration = Utility
							.getConfiguration(UMessageApplication.getContext());

					if ((result == null)
							|| (result.getString("errorCode").equals("KO"))) {
						m = new Message();
						m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
						this.sendMessageDelayed(m, TIME_MINUTE * 1000);
					} else if (!result.getBoolean("isSessionValid")) {
						configuration.setSessid("");
					} else {
						configuration.setProfileImageToUpload(false);
					}

					Utility.setConfiguration(UMessageApplication.getContext(),
							configuration);

				} catch (Exception e) {
					m = new Message();
					m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
					this.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				Bundle data = msg.getData();

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
					p = new Provider(UMessageApplication.getContext());
					Cursor userInfo = p.getUserInfo(data.getString("prefix"),
							data.getString("num"));

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
							p.updateUserImage(data.getString("prefix"),
									data.getString("num"), newImageName,
									newImageData);
						}
					}
				} catch (Exception e) {

				}

				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				p = new Provider(UMessageApplication.getContext());
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
								p = new Provider(
										UMessageApplication.getContext());
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
				Bundle b = msg.getData();
				m.setData(b);
				m.what = MessageTypes.SEND_NEW_TEXT_MESSAGE;

				try {
					p = new Provider(UMessageApplication.getContext());
					Calendar c = Calendar.getInstance();
					ContentValues value = new ContentValues();
					value.put(DatabaseHelper.KEY_PREFIX, b.getString("prefix"));
					value.put(DatabaseHelper.KEY_NUM, b.getString("num"));
					value.put(DatabaseHelper.KEY_DIRECTION, 0);
					value.put(DatabaseHelper.KEY_STATUS, "0");
					value.put(DatabaseHelper.KEY_DATA,
							Double.parseDouble("" + c.getTimeInMillis()));
					value.put(DatabaseHelper.KEY_TYPE, "text");
					value.put(DatabaseHelper.KEY_MESSAGE,
							b.getString("messageText"));
					value.put(DatabaseHelper.KEY_TOREAD, "0");
					value.put(DatabaseHelper.KEY_TAG, b.getString("messageTag"));

					long newMessageId = p.insertNewMessage(value);

					if (newMessageId != -1) {
						b.putLong("messageId", newMessageId);
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

				break;

			}
		}
	}
}
