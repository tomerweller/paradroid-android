package org.paradroid;

import org.paradroid.common.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.preference.PreferenceManager;


public class SettingsFactory {

	public static Settings getSettingsFromPreferences(Context context){
		Settings retval = new Settings();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		retval.diverName = prefs.getString("diverName", "Some Diver");
		retval.provider = getProviderFromString(prefs.getString("provider", "GPS"));		
		retval.minTimeInterval = Long.parseLong(prefs.getString("timeRefreshIntervalPreference", "0"));
		retval.minDistanceInterval = Float.parseFloat(prefs.getString("distanceRefreshIntervalPreference", "0"));
		retval.locationAccuracy = getFineCoarseAccuracy(prefs.getString("locationAccuracyPreference", "Fine"));
		retval.altitudeRequired = prefs.getBoolean("altitudeRequiredPreference", true);
//		retval.altitudeAccuracy = getHighLowAccuracy(prefs.getString("altitudeAccuracyPreference", "High"));
		retval.bearingRequired = prefs.getBoolean("bearingRequiredPreference", true);
//		retval.bearingAccuracy = getHighLowAccuracy(prefs.getString("bearingAccuracyPreference", "High"));
		retval.speedRequired = prefs.getBoolean("speedRequiredPreference", true);
//		retval.speedAccuracy = getHighLowAccuracy(prefs.getString("speedAccuracyPreference", "High"));
		
		return retval;
	}
	private static String getProviderFromString(String provider){
		if (provider.equalsIgnoreCase("GPS"))
			return LocationManager.GPS_PROVIDER;
		else
			return LocationManager.NETWORK_PROVIDER;
	}
	private static int getFineCoarseAccuracy(String string) {
		if (string.toLowerCase().equals("fine"))
			return Criteria.ACCURACY_FINE;
		else 
			return Criteria.ACCURACY_COARSE;
	}

//	private static int getHighLowAccuracy(String string) {
//		if (string.toLowerCase().equals("high"))
//			return Criteria.ACCURACY_HIGH;
//		else 
//			return Criteria.ACCURACY_LOW;
//	}

	
}
