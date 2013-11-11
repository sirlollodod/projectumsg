package com.lollotek.umessage.threads;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lollotek.umessage.utils.MessageTypes;

public class NewMessageThread extends Thread {

	private Handler mainThreadHandler = null;
	private NewMessageThreadHandler newMessageThreadHandler = null;

	public NewMessageThread(Handler handler) {
		mainThreadHandler = handler;
	}

	public void run() {
		Looper.prepare();

		newMessageThreadHandler = new NewMessageThreadHandler();
		mainThreadHandler.obtainMessage(
				MessageTypes.RECEIVE_NEW_MESSAGE_THREAD_HANDLER,
				newMessageThreadHandler).sendToTarget();

		Looper.loop();

	}

	private class NewMessageThreadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MessageTypes.DESTROY:
				mainThreadHandler = null;
				newMessageThreadHandler = null;
				Looper.myLooper().quit();

				break;
			}
		}

	}
}
