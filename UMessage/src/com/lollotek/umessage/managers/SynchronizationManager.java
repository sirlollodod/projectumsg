package com.lollotek.umessage.managers;

import java.util.List;

import com.lollotek.umessage.listeners.SynchronizationListener;
import com.lollotek.umessage.utils.Lists;

public class SynchronizationManager {
	private static final String TAG = SynchronizationManager.class.getName();

	private static SynchronizationManager instance;
	private Object synRoot = new Object();
	private boolean synchronizing = false;
	private List<SynchronizationListener> synchronizationListeners;

	public SynchronizationManager() {
		synchronizationListeners = Lists.newArrayList();
	}

	static {
		instance = new SynchronizationManager();
	}

	public static SynchronizationManager getInstance() {
		return instance;
	}

	public boolean isSynchronizing() {
		synchronized (synRoot) {
			return synchronizing;
		}
	}


	public void stopSynchronizing() {
		synchronized (synRoot) {
			synchronizing = false;
		}
	}

	public synchronized void registerSynchronizationListener(
			SynchronizationListener listener) {
		if (!synchronizationListeners.contains(listener)) {
			synchronizationListeners.add(listener);
		}
	}

	public synchronized void unregisterSynchronizationListener(
			SynchronizationListener listener) {
		if (synchronizationListeners.contains(listener)) {
			synchronizationListeners.remove(listener);
		}
	}

	public void onSynchronizationStart() {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onStart();
		}
	}

	protected void onSynchronizationProgress() {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onProgress();
		}
	}

	protected void onSynchronizationFinish() {
		for (SynchronizationListener listener : synchronizationListeners) {
			listener.onFinish();
		}
	}

}
