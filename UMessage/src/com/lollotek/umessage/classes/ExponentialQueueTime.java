package com.lollotek.umessage.classes;

public class ExponentialQueueTime {

	// Scala temporale d'attesa [secondi]
	public long[] toWait;

	public ExponentialQueueTime(long[] timeQueue) {
		this.toWait = new long[timeQueue.length];
		for (int i = 0; i < timeQueue.length; i++) {
			this.toWait[i] = timeQueue[i];
		}
	}

	public int toNext(int actual) {
		if ((actual + 1) < toWait.length) {
			return (actual + 1);
		} else {
			return actual;
		}
	}

	public int toPrevious(int actual) {
		if ((actual - 1) > 0) {
			return (actual - 1);
		} else {
			return actual;
		}
	}
	
	public long getTime(int actual){
		return toWait[actual];
	}
}
