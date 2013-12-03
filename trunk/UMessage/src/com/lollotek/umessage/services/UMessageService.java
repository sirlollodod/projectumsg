package com.lollotek.umessage.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.threads.MainThread;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class UMessageService extends Service {

	private Context instance = null;
	private ServiceHandler serviceHandler = null;
	private MainThread mainThread = null;
	private Handler mainThreadHandler = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;

	@Override
	public void onCreate() {
		super.onCreate();

		Utility.prepareDirectory(UMessageApplication.getContext());

		if (instance == null) {
			instance = this;
		}

		if (serviceHandler == null) {
			serviceHandler = new ServiceHandler();
		}

		if (mainThread == null) {
			mainThread = new MainThread(serviceHandler);
			mainThread.start();
		}

	}

	// Inizializzazione base del service
	private void scheduleDownloadAllProfileImages() {
		Configuration configuration = Utility.getConfiguration(instance);

		if (configuration.isProfileImageToUpload()) {
			serviceHandler
					.sendEmptyMessage(MessageTypes.UPLOAD_MY_PROFILE_IMAGE);
		}

		serviceHandler.sendEmptyMessage(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mainThreadHandler.obtainMessage(MessageTypes.DESTROY).sendToTarget();
		mainThread = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		int actionRequest;

		actionRequest = intent.getIntExtra("action", MessageTypes.ERROR);

		Message m;

		switch (actionRequest) {

		case MessageTypes.STARTED_FROM_BOOT_RECEIVER:
			scheduleDownloadAllProfileImages();

			break;

		case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
			String myProfileImageUrl = intent.getStringExtra("imageUrl");

			m = new Message();
			m.what = MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC;
			m.obj = Settings.SERVER_URL + myProfileImageUrl;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:

			serviceHandler.obtainMessage(MessageTypes.UPLOAD_MY_PROFILE_IMAGE)
					.sendToTarget();

			break;

		case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:

			String userImageUrl = intent.getStringExtra("imageUrl");
			Bundle data = new Bundle();
			data.putString("prefix", intent.getStringExtra("prefix"));
			data.putString("num", intent.getStringExtra("num"));

			m = new Message();
			m.setData(data);
			m.what = MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC;
			m.obj = Settings.SERVER_URL + userImageUrl;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
			serviceHandler
					.obtainMessage(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES)
					.sendToTarget();

			break;
		case MessageTypes.ERROR:

			break;

		}

		return Service.START_STICKY;

	}

	private class ServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Message m;

			switch (msg.what) {
			case MessageTypes.RECEIVE_MAIN_THREAD_HANDLER:
				mainThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				m = new Message();
				m.what = MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC;
				m.obj = msg.obj;
				try {
					mainThreadHandler
							.removeMessages(MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC);
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.UPLOAD_MY_PROFILE_IMAGE:
				m = new Message();
				m.what = MessageTypes.UPLOAD_MY_PROFILE_IMAGE;
				try {
					mainThreadHandler
							.removeMessages(MessageTypes.UPLOAD_MY_PROFILE_IMAGE);
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC:
				m = new Message();
				m.setData(msg.getData());
				m.what = MessageTypes.DOWNLOAD_USER_IMAGE_FROM_SRC;
				m.obj = msg.obj;
				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, TIME_MINUTE * 1000);
				}

				break;

			case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
				this.removeMessages(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES);
				try {
					mainThreadHandler
							.removeMessages(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES);
					mainThreadHandler
							.sendEmptyMessage(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES);
				} catch (Exception e) {
					this.sendEmptyMessageDelayed(
							MessageTypes.DOWNLOAD_ALL_USERS_IMAGES,
							TIME_MINUTE * 1000);
				}
				break;
			}
		}

	}

}
