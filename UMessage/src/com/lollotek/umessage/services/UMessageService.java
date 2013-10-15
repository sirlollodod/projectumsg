package com.lollotek.umessage.services;

import java.util.Random;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.db.DatabaseHelper;
import com.lollotek.umessage.db.Provider;

public class UMessageService extends Service {

	private Context instance = null;

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

		// return super.onStartCommand(intent, flags, startId);
		return 0;
	}
}
