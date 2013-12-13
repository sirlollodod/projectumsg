package com.lollotek.umessage.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lollotek.umessage.R;

public class Map extends Activity {

	private String provider;
	private LocationManager locationManager;

	private LatLng myPosition, destPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		try {
			GoogleMap map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.fragment1)).getMap();

			locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, false);
			Location location = locationManager.getLastKnownLocation(provider);

			if (location != null) {
				myPosition = new LatLng(location.getLatitude(),
						location.getLongitude());
				map.addMarker(new MarkerOptions().title("Io")
						.snippet("Eccomi qui...").position(myPosition));
			} else {
				myPosition = new LatLng(-33.867, 151.206);
				map.addMarker(new MarkerOptions().title("Sydney")
						.snippet("The most populous city in Australia.")
						.position(myPosition));
			}

			destPosition = new LatLng(45.278865, 11.02787);

			map.setMyLocationEnabled(true);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 13));

		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;

		switch (item.getItemId()) {
		case R.id.path:
			try {
				i = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?saddr="
								+ myPosition.latitude + ","
								+ myPosition.longitude + "&daddr="
								+ destPosition.latitude + ","
								+ destPosition.longitude));
				startActivity(i);
			} catch (Exception e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			}
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
