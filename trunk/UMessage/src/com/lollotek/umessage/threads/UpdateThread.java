package com.lollotek.umessage.threads;

import com.lollotek.umessage.utils.MessageTypes;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class UpdateThread extends Thread {

	private UpdateThreadHandler updateThreadHandler = null;
	private Handler mainThreadHandler = null;

	public UpdateThread(Handler handler) {
		mainThreadHandler = handler;
	}

	public void run() {
		Looper.prepare();

		updateThreadHandler = new UpdateThreadHandler();
		mainThreadHandler
				.obtainMessage(MessageTypes.RECEIVE_UPDATE_THREAD_HANDLER,
						updateThreadHandler).sendToTarget();

		Looper.loop();
	}

	private class UpdateThreadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MessageTypes.DESTROY:
				mainThreadHandler = null;
				updateThreadHandler = null;
				Looper.myLooper().quit();
				
				break;
			}
		}

	}
}
