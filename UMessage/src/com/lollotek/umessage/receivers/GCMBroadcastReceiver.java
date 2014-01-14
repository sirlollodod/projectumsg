package com.lollotek.umessage.receivers;

import com.lollotek.umessage.UMessageApplication;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, com.lollotek.umessage.services.UMessageService.class);
		
		//service.putExtra("action", );
		
		Toast.makeText(UMessageApplication.getContext(), "messaggio ricevuto da gcm", Toast.LENGTH_SHORT).show();
		startWakefulService(context, service);
		
	}

}
