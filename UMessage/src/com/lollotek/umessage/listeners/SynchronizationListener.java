package com.lollotek.umessage.listeners;

import android.os.Message;

public interface SynchronizationListener {

	void onStart(Message msg);

	void onProgress(Message msg);

	void onFinish(Message msg);
}
