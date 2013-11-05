package com.lollotek.umessage.services;

import android.os.Handler;

public class TestThread extends Thread {

	private Handler serviceHandler;

	public TestThread(Handler handler) {
		serviceHandler = handler;
	}

	public void run() {
		// Looper.prepare();
		while (true) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {

			}

			serviceHandler.obtainMessage(0, "sono dentro TestThread")
					.sendToTarget();
		}
		// Looper.loop();

	}

}
