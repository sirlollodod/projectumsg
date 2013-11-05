package com.lollotek.umessage.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lollotek.umessage.R;



public class UMessageSettings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);

	}
}
