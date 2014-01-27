package com.lollotek.umessage.utils;

public class MessageTypes {

	// Tipi messaggio ricevuti da ServiceHandler
	public static final int RECEIVE_MAIN_THREAD_HANDLER = 0;

	// Tipi messaggio ricevuti da MainThreadHandler
	public static final int RECEIVE_LOW_PRIORITY_THREAD_HANDLER = 10;
	public static final int RECEIVE_HIGH_PRIORITY_THREAD_HANDLER = 11;

	// Tipi messaggio ricevuti da LowPriorityThreadHandler

	// Tipi messaggio ricevuti da NewMessageThreadHandler

	// Tipi messaggio generali
	public static final int DESTROY = 50;
	public static final int ERROR = 51;

	// Tipi messaggio ricevuti da Service e/o Thread
	public static final int DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC = 70;
	public static final int UPLOAD_MY_PROFILE_IMAGE = 71;
	public static final int DOWNLOAD_USER_IMAGE_FROM_SRC = 72;
	public static final int DOWNLOAD_ALL_USERS_IMAGES = 73;
	public static final int STARTED_FROM_BOOT_RECEIVER = 74;
	public static final int SEND_NEW_TEXT_MESSAGE = 75;
	public static final int UPLOAD_NEW_MESSAGE = 76;
	public static final int STARTED_FOR_INITIALIZE_SERVICE = 77;
	public static final int SYNCHRONIZE_CHAT = 78;
	public static final int CHECK_MESSAGES_TO_UPLOAD = 79;
	public static final int CHECK_CHATS_TO_SYNCHRONIZE = 80;
	public static final int GET_CHATS_VERSION = 81;
	public static final int USER_LOGGED = 82;
	public static final int DOWNLOAD_USER_IMAGE = 83;
	public static final int CHECK_GOOGLE_PLAY_SERVICES = 84;
	public static final int UPDATE_NOTIFICATION = 85;
	public static final int NETWORK_CONNECTED = 86;
	public static final int MAKE_DB_DUMP = 87;
	public static final int GET_LAST_LOCAL_DB_BK_DATA = 88;
	public static final int GET_LAST_DROPBOX_DB_BK_DATA = 89;

	// Tipi messaggio ricevuti da SynchronizationManager e
	// SynchronizationListeners
	public static final int MESSAGE_UPDATE = 100;
	public static final int MESSAGE_UPLOADED = 101;
	public static final int DROPBOX_REFRESH = 102;

}
