package com.lollotek.umessage.receivers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.lollotek.umessage.utils.MessageTypes;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context,
				com.lollotek.umessage.services.UMessageService.class);

		int actionToPerform = Integer.parseInt(intent.getStringExtra("action"));

		switch (actionToPerform) {
		case MessageTypes.GET_CHATS_VERSION:
			service.putExtra("action", actionToPerform);
			break;

		case MessageTypes.SYNCHRONIZE_CHAT:
			service.putExtra("action", actionToPerform);
			service.putExtra("prefix", intent.getStringExtra("prefix"));
			service.putExtra("num", intent.getStringExtra("num"));
			break;

		case MessageTypes.PING_FROM_GCM:
			service.putExtra("action", actionToPerform);
			break;
			
		default:

		}

		startWakefulService(context, service);
		setResultCode(Activity.RESULT_OK);
	}

}
