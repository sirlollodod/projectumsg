package com.lollotek.umessage.services;

import java.util.Random;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class UMessageService extends Service {

	private Context instance = null;
	private ServiceTestThreadHandler stth;
	private boolean inUse = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if (instance == null) {
			instance = this;
			stth = new ServiceTestThreadHandler();
			
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Toast msg = Toast.makeText(this, "SONO DENTRO IL SERVICE!",
				Toast.LENGTH_LONG);
		msg.show();

		if(!inUse){
			inUse = true;
			new Thread(new Runnable() {
				public void run() {
					Provider p = new Provider(UMessageApplication.getContext());
					Cursor user;
					// Toast msg;
					ContentValues values;
					while (true) {
						values = new ContentValues();

						values.put(DatabaseHelper.KEY_PREFIX, "" + randomBox());
						values.put(DatabaseHelper.KEY_NUM, "3494566596");
						values.put(DatabaseHelper.KEY_NAME, "Davide");

						p.insert(DatabaseHelper.TABLE_USER, null, values);

						// msg = Toast.makeText(getApplicationContext(),
						// "Totale utenti db dal service: ", Toast.LENGTH_LONG);
						// msg.show();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

				public int randomBox() {

					Random rand = new Random();
					int pickedNumber = rand.nextInt(1000);
					return pickedNumber;

				}

			}, "prova").start();
			
		}
		else{
			stth.obtainMessage(1, "Messaggio da inviare").sendToTarget();
		}
		
		
		

		TestThread td = new TestThread(stth);
		td.start();
		
		// return super.onStartCommand(intent, flags, startId);
		return 0;
	}
	
	private class ServiceTestThreadHandler extends Handler{
		
		public void handleMessage(Message msg){
			
			Toast tst;
			switch(msg.what){
			
			case 0:
				tst = Toast.makeText(instance, "Dentro servicetestthreadhandler: " + (String) msg.obj, Toast.LENGTH_SHORT);
				tst.show();
				break;
				
			case 1:
				tst = Toast.makeText(instance, "Dentro servicetestthreadhandler: " + (String) msg.obj, Toast.LENGTH_SHORT);
				tst.show();
				break;
			
				
			}
		}
	}
}
