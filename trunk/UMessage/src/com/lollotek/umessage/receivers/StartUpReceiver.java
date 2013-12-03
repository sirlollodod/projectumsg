package com.lollotek.umessage.receivers;

import com.lollotek.umessage.utils.MessageTypes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, com.lollotek.umessage.services.UMessageService.class);
		serviceIntent.putExtra("action", MessageTypes.STARTED_FROM_BOOT_RECEIVER);
		context.startService(serviceIntent);

	}

}
