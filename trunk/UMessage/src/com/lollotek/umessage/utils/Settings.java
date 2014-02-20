package com.lollotek.umessage.utils;

import com.dropbox.client2.session.Session.AccessType;

public class Settings {

	public static final String CONFIG_FILE_NAME = "config.dat";
	public static final String SERVER_URL = "http://umsg.netsons.org/";
	public static final String CONTACT_PROFILE_IMAGES_FOLDER = "/contacts";
	public static final String MY_PROFILE_IMAGE_SRC = "/me.jpg";
	public static final String MY_PROFILE_IMAGE_SRC_TEMP = "/temp_me.jpg";
	public static final String DUMP_DB_BASE_FILE_NAME = "/dumpDB";
	public static final String DROPBOX_INDEX_LOCAL_FILE_TEMP = "/dropbox_index_temp";
	public static final String DROPBOX_INDEX_FILE = "/indexUsersBk";
	public static final String DROPBOX_USER_BK = "/dropbox_user_bk";

	public static final boolean debugMode = false;

	// Google vars
	public static final String GOOGLE_PROJECT_NUMBER = "990058189573";

	// Dropbox vars
	public static final String APP_KEY = "hkkh7cx5dxb7f92";
	public static final String APP_SECRET = "qbvaquyudpj6uwz";
	public static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	public static final String SHARED_PREFS_DROPBOX = "DROPBOX_VALUES";
	public static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	public static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

}
