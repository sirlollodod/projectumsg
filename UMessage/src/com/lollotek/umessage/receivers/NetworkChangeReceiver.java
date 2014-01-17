package com.lollotek.umessage.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.lollotek.umessage.UMessageApplication;
import com.lollotek.umessage.utils.MessageTypes;
import com.lollotek.umessage.utils.Settings;

public class NetworkChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isConnected() || mobile.isConnected()) {
			if (Settings.debugMode) {
				Toast.makeText(UMessageApplication.getContext(),
						"Connessione presente....", Toast.LENGTH_SHORT).show();
			}

			Intent service = new Intent(UMessageApplication.getContext(),
					com.lollotek.umessage.services.UMessageService.class);
			service.putExtra("action", MessageTypes.NETWORK_CONNECTED);
			context.startService(service);
		}
	}
}