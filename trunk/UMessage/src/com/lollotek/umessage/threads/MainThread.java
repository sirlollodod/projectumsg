package com.lollotek.umessage.threads;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
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

				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());

				myNewProfileImage = new File(mainFolder.toString()
						+ Settings.MY_PROFILE_IMAGE_SRC);

				try {
					Utility.uploadImageProfile(
							UMessageApplication.getContext(),
							Settings.SERVER_URL,
							myNewProfileImage,
							Utility.getConfiguration(
									UMessageApplication.getContext())
									.getSessid());
				} catch (Exception e) {
					m = new Message();
					m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
					this.sendMessageDelayed(m, 60000);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				mainFolder = Utility.getMainFolder(UMessageApplication
						.getContext());
				
				Bundle data = msg.getData();

				String userImageUrl = (String) msg.obj;
				String newUserImageFileName = userImageUrl.substring(userImageUrl.indexOf("+"));
				int posNumDataSeparator = newUserImageFileName.indexOf("_");
				String newImageName = "/" + newUserImageFileName.substring(0, posNumDataSeparator) + ".jpg";
				long newImageData = Long.parseLong(newUserImageFileName.substring(posNumDataSeparator+1, newUserImageFileName.length()-4));
				File userImage = new File(mainFolder.toString() + Settings.CONTACT_PROFILE_IMAGES_FOLDER + newImageName);
				
				try{
					Utility.downloadFileFromUrl(UMessageApplication.getContext(), userImage, userImageUrl);
					p = new Provider(UMessageApplication.getContext());
					p.updateUserImage(data.getString("prefix"), data.getString("num"), newImageName, newImageData);
					
				}
				catch(Exception e){
					
				}

				break;
			}
		}

	}
}
