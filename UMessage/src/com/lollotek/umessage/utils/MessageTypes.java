package com.lollotek.umessage.utils;

public class MessageTypes {

	// Tipi messaggio ricevuti da ServiceHandler
	public static final int RECEIVE_MAIN_THREAD_HANDLER = 0;

	// Tipi messaggio ricevuti da MainThreadHandler
	public static final int RECEIVE_UPDATE_THREAD_HANDLER = 10;
	public static final int RECEIVE_NEW_MESSAGE_THREAD_HANDLER = 11;

	// Tipi messaggio ricevuti da UpdateThreadHandler

	// Tipi messaggio ricevuti da NewMessageThreadHandler
	
	
	// Tipi messaggio generali
	public static final int DESTROY = 50;
	public static final int ERROR = 51;
	
	// Tipi messaggio ricevuti da Service
	public static final int DOWNLOAD_MY_PROFILE_IMAGE_FROM_SRC = 70;
	public static final int UPLOAD_MY_PROFILE_IMAGE = 71;
	public static final int DOWNLOAD_USER_IMAGE_FROM_SRC = 72;
	public static final int DOWNLOAD_ALL_USERS_IMAGES = 73;
	public static final int STARTED_FROM_BOOT_RECEIVER = 74;
	public static final int SEND_NEW_TEXT_MESSAGE = 75;
	
	
}
