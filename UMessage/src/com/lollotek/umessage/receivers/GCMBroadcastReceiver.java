package com.lollotek.umessage.receivers;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(UMessageApplication.getContext(),
				"messaggio ricevuto da gcm: " + intent.getStringExtra("value"), Toast.LENGTH_SHORT).show();

		Intent service = new Intent(context,
				com.lollotek.umessage.services.UMessageService.class);

		int actionToPerform = intent.getIntExtra("action",
				MessageTypes.GET_CHATS_VERSION);

		switch (actionToPerform) {
		case MessageTypes.GET_CHATS_VERSION:
			service.putExtra("action", actionToPerform);
			break;

		case MessageTypes.SYNCHRONIZE_CHAT:
			service.putExtra("action", actionToPerform);
			service.putExtra("prefix", intent.getStringExtra("prefix"));
			service.putExtra("num", intent.getStringExtra("num"));
			break;

		default:

			Toast.makeText(UMessageApplication.getContext(),
					"Richiesta errata...", Toast.LENGTH_LONG).show();
		}

		 startWakefulService(context, service);
		 setResultCode(Activity.RESULT_OK);
		//context.startService(service);
	}

}
