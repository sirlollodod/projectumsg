package com.lollotek.umessage.threads;

import java.io.File;
import java.io.IOException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lollotek.umessage.Configuration;
import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.MessageTypes;
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

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MessageTypes.RECEIVE_UPDATE_THREAD_HANDLER:
				updateThreadHandler = (Handler) msg.obj;
				break;
				
			case MessageTypes.RECEIVE_NEW_MESSAGE_THREAD_HANDLER:
				newMessageThreadHandler = (Handler) msg.obj;
				break;
				
			case MessageTypes.DESTROY:
				updateThreadHandler.obtainMessage(MessageTypes.DESTROY).sendToTarget();
				newMessageThreadHandler.obtainMessage(MessageTypes.DESTROY).sendToTarget();
				updateThreadHandler = null;
				newMessageThreadHandler = null;
				serviceThreadHandler = null;
				updateThread = null;
				newMessageThread = null;
				Looper.myLooper().quit();
				
			}
		}

	}
}
