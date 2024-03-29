package com.lollotek.umessage.services;

import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.threads.HighPriorityThread;
import com.lollotek.umessage.threads.MainThread;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;
import com.lollotek.umessage.utils.Utility;

public class UMessageService extends Service {

	private static final String TAG = UMessageService.class.getName() + ":\n";

	private Context instance = null;
	private ServiceHandler serviceHandler = null;
	private MainThread mainThread = null;
	private Handler mainThreadHandler = null, highPriorityThreadHandler = null;
	private HighPriorityThread highPriorityThread = null;

	private final long TIME_MINUTE = 60, TIME_HOUR = 3600, TIME_DAY = 86400;

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			Utility.prepareDirectory(UMessageApplication.getContext());

			if (instance == null) {
				instance = this;
			}

			checkHandlerThreadStarted();

		} catch (Exception e) {
			Utility.reportError(UMessageApplication.getContext(), e, TAG
					+ ": onCreate()");
		}

	}

	private void checkHandlerThreadStarted() {
		if (serviceHandler == null) {
			serviceHandler = new ServiceHandler();
		}

		if (mainThread == null) {
			if (Settings.debugMode) {
				Toast.makeText(UMessageApplication.getContext(),
						"MainThread null, reinizializzo... ", Toast.LENGTH_SHORT)
						.show();
			}

			mainThread = new MainThread(serviceHandler);
			mainThread.start();
		}

		if (highPriorityThread == null) {
			if (Settings.debugMode) {
				Toast.makeText(UMessageApplication.getContext(),
						"HighPriorityThread null, reinizializzo... ",
						Toast.LENGTH_SHORT).show();
			}

			highPriorityThread = new HighPriorityThread(serviceHandler);
			highPriorityThread.start();

		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mainThreadHandler.obtainMessage(MessageTypes.DESTROY).sendToTarget();
		mainThread = null;
		highPriorityThreadHandler.obtainMessage(MessageTypes.DESTROY)
				.sendToTarget();
		highPriorityThread = null;

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		int actionRequest;
		Bundle b;

		checkHandlerThreadStarted();

		try {
			actionRequest = intent.getIntExtra("action", MessageTypes.ERROR);
		} catch (Exception e) {
			actionRequest = MessageTypes.ERROR;
		}

		if (Settings.debugMode) {
			Toast.makeText(UMessageApplication.getContext(),
					TAG + actionRequest, Toast.LENGTH_SHORT).show();
		}

		Message m;

		switch (actionRequest) {

		case MessageTypes.STARTED_FOR_INITIALIZE_SERVICE:

			break;

		case MessageTypes.STARTED_FROM_BOOT_RECEIVER:

			break;

		case MessageTypes.NETWORK_CONNECTED:
			m = new Message();
			m.what = MessageTypes.NETWORK_CONNECTED;
			serviceHandler.sendMessageAtFrontOfQueue(m);

			break;

		case MessageTypes.MAKE_DB_DUMP:
			m = new Message();
			m.what = MessageTypes.MAKE_DB_DUMP;
			b = new Bundle();
			b.putBoolean("forceDBDump",
					intent.getBooleanExtra("forceDBDump", false));
			m.setData(b);
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.GET_LAST_LOCAL_DB_BK_DATA:
			m = new Message();
			m.what = MessageTypes.GET_LAST_LOCAL_DB_BK_DATA;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA:
			m = new Message();
			m.what = MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.GET_DROPBOX_ACCOUNT_INFO:
			m = new Message();
			m.what = MessageTypes.GET_DROPBOX_ACCOUNT_INFO;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.START_DROPBOX_SYNCHRONIZATION:
			m = new Message();
			m.what = MessageTypes.START_DROPBOX_SYNCHRONIZATION;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.USER_LOGGED:
			String myProfileImageUrl = intent.getStringExtra("myImageUrl");

			if ((myProfileImageUrl.length() <= 2)
					|| (myProfileImageUrl.equals("") || (myProfileImageUrl == null))) {
				break;
			}

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

		case MessageTypes.DOWNLOAD_USER_IMAGE:
			b = new Bundle();
			b.putString("prefix", intent.getStringExtra("prefix"));
			b.putString("num", intent.getStringExtra("num"));

			m = new Message();
			m.setData(b);
			m.what = MessageTypes.DOWNLOAD_USER_IMAGE;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.DOWNLOAD_ALL_USERS_IMAGES:
			serviceHandler
					.obtainMessage(MessageTypes.DOWNLOAD_ALL_USERS_IMAGES)
					.sendToTarget();

			break;

		case MessageTypes.SEND_NEW_TEXT_MESSAGE:
			b = new Bundle();
			b.putString("messageText", intent.getStringExtra("messageText"));
			b.putString("prefix", intent.getStringExtra("prefix"));
			b.putString("num", intent.getStringExtra("num"));
			Calendar c = Calendar.getInstance();
			String messageTag = Utility.md5(intent.getStringExtra("num")
					+ intent.getStringExtra("messageText")
					+ c.getTimeInMillis());
			b.putString("messageTag", messageTag);

			m = new Message();
			m.setData(b);
			m.what = MessageTypes.SEND_NEW_TEXT_MESSAGE;

			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.SYNCHRONIZE_CHAT:
			b = new Bundle();
			b.putString("prefix", intent.getStringExtra("prefix"));
			b.putString("num", intent.getStringExtra("num"));
			m = new Message();
			m.setData(b);
			m.what = MessageTypes.SYNCHRONIZE_CHAT;
			serviceHandler.sendMessage(m);

			break;

		case MessageTypes.UPDATE_NOTIFICATION:
			m = new Message();
			m.what = MessageTypes.UPDATE_NOTIFICATION;
			b = new Bundle();
			b.putBoolean("calledFromSingleChatContact", intent.getBooleanExtra(
					"calledFromSingleChatContact", false));
			m.setData(b);
			serviceHandler.sendMessageAtFrontOfQueue(m);

			break;

		case MessageTypes.PING_FROM_GCM:
			m = new Message();
			m.what = MessageTypes.PING_FROM_GCM;
			serviceHandler.sendMessage(m);

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
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "RECEIVE_MAIN_THREAD_HANDLER",
							Toast.LENGTH_SHORT).show();
				}

				mainThreadHandler = (Handler) msg.obj;

				// Adesso mainThreadHandler � funzionante, dovrei svuotare dalla
				// coda messaggi del service i messaggi delayed e inoltrarli
				// nuovamente alla MainThread????

				break;

			case MessageTypes.RECEIVE_HIGH_PRIORITY_THREAD_HANDLER:
				if (Settings.debugMode) {
					Toast.makeText(UMessageApplication.getContext(),
							TAG + "RECEIVE_HIGH_PRIORITY_THREAD_HANDLER",
							Toast.LENGTH_SHORT).show();
				}

				highPriorityThreadHandler = (Handler) msg.obj;

				// Adesso highPriorityThreadHandler � funzionante.

				break;

			case MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC:
				m = new Message();
				m.what = MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC;
				m.obj = msg.obj;
				try {
					mainThreadHandler
							.removeMessages(MessageTypes.UPLOAD_MY_PROFILE_IMAGE);
					mainThreadHandler
							.removeMessages(MessageTypes.DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC);
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
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
					this.sendMessageDelayed(m, 1 * 1000);
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
					this.sendMessageDelayed(m, 1 * 1000);
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
							MessageTypes.DOWNLOAD_ALL_USERS_IMAGES, 1 * 1000);
				}
				break;

			case MessageTypes.SEND_NEW_TEXT_MESSAGE:
				m = new Message();
				m.setData(msg.getData());
				m.what = MessageTypes.SEND_NEW_TEXT_MESSAGE;

				try {
					highPriorityThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}
				break;

			case MessageTypes.UPLOAD_NEW_MESSAGE:
				m = new Message();
				m.what = msg.what;
				m.arg1 = msg.arg1;
				m.arg2 = msg.arg2;
				m.obj = msg.obj;
				m.setData(msg.getData());

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE:
				m = new Message();
				m.what = MessageTypes.CHECK_CHATS_TO_SYNCHRONIZE;

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.CHECK_MESSAGES_TO_UPLOAD:
				m = new Message();
				m.what = MessageTypes.CHECK_MESSAGES_TO_UPLOAD;

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.SYNCHRONIZE_CHAT:
				m = new Message();
				m.what = MessageTypes.SYNCHRONIZE_CHAT;
				m.setData(msg.getData());

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}
				break;

			case MessageTypes.DOWNLOAD_USER_IMAGE:
				m = new Message();
				m.setData(msg.getData());
				m.what = MessageTypes.DOWNLOAD_USER_IMAGE;

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.NETWORK_CONNECTED:
				m = new Message();
				m.what = MessageTypes.NETWORK_CONNECTED;

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.MAKE_DB_DUMP:
				m = new Message();
				m.what = MessageTypes.MAKE_DB_DUMP;
				m.setData(msg.getData());

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.GET_LAST_LOCAL_DB_BK_DATA:
				m = new Message();
				m.what = MessageTypes.GET_LAST_LOCAL_DB_BK_DATA;

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA:
				m = new Message();
				m.what = MessageTypes.GET_LAST_DROPBOX_DB_BK_DATA;

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.GET_DROPBOX_ACCOUNT_INFO:
				m = new Message();
				m.what = MessageTypes.GET_DROPBOX_ACCOUNT_INFO;

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.START_DROPBOX_SYNCHRONIZATION:
				m = new Message();
				m.what = MessageTypes.START_DROPBOX_SYNCHRONIZATION;

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.UPDATE_NOTIFICATION:
				m = new Message();
				m.what = MessageTypes.UPDATE_NOTIFICATION;
				m.setData(msg.getData());

				try {
					mainThreadHandler.sendMessageAtFrontOfQueue(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;

			case MessageTypes.PING_FROM_GCM:
				m = new Message();
				m.what = MessageTypes.PING_FROM_GCM;
				m.setData(msg.getData());

				try {
					mainThreadHandler.sendMessage(m);
				} catch (Exception e) {
					this.sendMessageDelayed(m, 1 * 1000);
				}

				break;
			}
		}

	}

}
