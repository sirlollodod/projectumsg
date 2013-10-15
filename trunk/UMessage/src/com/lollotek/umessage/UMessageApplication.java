package com.lollotek.umessage;

import android.app.Application;
import android.content.Context;

public class UMessageApplication extends Application{

	private static Context mContext;
	
	public void onCreate(){
		super.onCreate();
		UMessageApplication.mContext = getApplicationContext();
	}
	
	public static Context getContext(){
		return UMessageApplication.mContext;
	}
}
