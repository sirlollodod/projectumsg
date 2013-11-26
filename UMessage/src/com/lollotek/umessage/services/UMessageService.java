package com.lollotek.umessage.services;

import java.io.File;

import org.apache.http.HttpException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

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

		switch (actionRequest) {

		case MessageTypes.DOWNLOAD_PROFILE_IMAGE_FROM_SRC:
			String imageUrl = intent.getStringExtra("imageSrc");
			imageUrl = imageUrl.substring(2);

			Message m = new Message();
			m.what = MessageTypes.DOWNLOAD_PROFILE_IMAGE_FROM_SRC;
			m.obj = Settings.SERVER_URL + imageUrl;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.ERROR:

			break;

		}
		return 0;
	}

	private class ServiceHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MessageTypes.RECEIVE_MAIN_THREAD_HANDLER:
				mainThreadHandler = (Handler) msg.obj;
				break;

			case MessageTypes.DOWNLOAD_PROFILE_IMAGE_FROM_SRC:

				Message m = new Message();
				m.what = MessageTypes.DOWNLOAD_PROFILE_IMAGE_FROM_SRC;
				m.obj = msg.obj;
				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 10000);
				}
				break;
			}
		}

	}

}
