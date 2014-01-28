package com.lollotek.umessage.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper instance = null;
	private Context mContext;

	// Logcat tag
	private static final String TAG = DatabaseHelper.class.getName() + ":\n";

	// Database Version
	private static final int DATABASE_VERSION = 5;

	// Database Name
	public static final String DATABASE_NAME = "UMessage";

	// Table names
	public static final String TABLE_SINGLECHAT = "singlechat",
			TABLE_SINGLECHATMESSAGES = "singlechatmessages",
			TABLE_USER = "user",
			TABLE_TEMPSINGLECHATMESSAGES = "tempsinglechatmessages";

	// Table singlechat columns names
	public static final String // KEY_IDCHAT = "idChat",
	KEY_VERSION = "version",
			KEY_PREFIXDEST = "prefixDest",
			KEY_NUMDEST = "numDest",
			KEY_IDLASTMESSAGE = "idLastMessage",
			KEY_DATALASTMESSAGE = "dataLastMessage";

	// Table singlechatmessages columns names
	public static final String // KEY_IDMESSAGE = "idMessage",
			KEY_IDCHAT = "idChat",
			KEY_DIRECTION = "direction",
			KEY_STATUS = "status",
			KEY_DATA = "data", KEY_TYPE = "type",
			KEY_MESSAGE = "message",
			KEY_TOREAD = "read", KEY_TAG = "tag";

	// Table User columns names
	public static final String KEY_PREFIX = "prefix", KEY_NUM = "num",
			KEY_NAME = "name", KEY_ID = "_id", KEY_IMGSRC = "imgSrc",
			KEY_IMGDATA = "imgData";

	// Create table singlechat
	public static final String CREATE_TABLE_SINGLECHAT = TABLE_SINGLECHAT
			+ "("
			+ KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"// + KEY_IDCHAT +
													// " INTEGER UNIQUE,"
			+ KEY_VERSION + " TEXT," + KEY_PREFIXDEST + " TEXT," + KEY_NUMDEST
			+ " TEXT," + KEY_IDLASTMESSAGE + " INTEGER," + KEY_DATALASTMESSAGE
			+ " INTEGER, UNIQUE(" + KEY_PREFIXDEST + ", " + KEY_NUMDEST + "))";

	// Create table singlechatmessages
	public static final String CREATE_TABLE_SINGLECHATMESSAGES = TABLE_SINGLECHATMESSAGES
			+ "("
			+ KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			// + KEY_IDMESSAGE
			// + " INTEGER NOT NULL,"
			+ KEY_IDCHAT
			+ " INTEGER NOT NULL,"
			+ KEY_DIRECTION
			+ " TEXT,"
			+ KEY_STATUS
			+ " TEXT,"
			+ KEY_DATA
			+ " INTEGER,"
			+ KEY_TYPE
			+ " TEXT,"
			+ KEY_MESSAGE
			+ " TEXT,"
			+ KEY_TOREAD
			+ " TEXT,"
			+ KEY_TAG + " TEXT)";
	// , UNIQUE ("
	// + KEY_IDMESSAGE + ", " + KEY_IDCHAT + "))";

	// Create table tempsinglechatmessages
	public static final String CREATE_TABLE_TEMPSINGLECHATMESSAGES = TABLE_TEMPSINGLECHATMESSAGES
			+ "("
			+ KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			// + KEY_IDMESSAGE
			// + " INTEGER NOT NULL,"
			+ KEY_IDCHAT
			+ " INTEGER NOT NULL UNIQUE,"
			+ KEY_DATA
			+ " INTEGER,"
			+ KEY_TYPE + " TEXT," + KEY_MESSAGE + " TEXT)";
	// ", UNIQUE ("
	// + KEY_IDMESSAGE
	// + ", "
	// + KEY_IDCHAT + "))";

	// Create table user
	public static final String CREATE_TABLE_USER = TABLE_USER + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_PREFIX
			+ " TEXT NOT NULL," + KEY_NUM + " TEXT NOT NULL," + KEY_NAME
			+ " TEXT NOT NULL," + KEY_IMGSRC + " TEXT," + KEY_IMGDATA
			+ " TEXT, UNIQUE(" + KEY_PREFIX + ", " + KEY_NUM + "))";

	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context.getApplicationContext());
		}
		return instance;
	}

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		mContext = context;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CREATE_TABLE_SINGLECHAT);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ CREATE_TABLE_SINGLECHATMESSAGES);
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CREATE_TABLE_USER);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ CREATE_TABLE_TEMPSINGLECHATMESSAGES);
		
		//tabella debug messaggi di errore
		db.execSQL("CREATE TABLE IF NOT EXISTS errori(_id INTEGER PRIMARY KEY AUTOINCREMENT, tag TEXT, errore TEXT)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SINGLECHAT + ";");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SINGLECHATMESSAGES + ";");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER + ";");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPSINGLECHATMESSAGES + ";");

		onCreate(db);
	}

	// closing database
	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();
	}

}