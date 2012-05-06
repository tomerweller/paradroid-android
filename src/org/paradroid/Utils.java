package org.paradroid;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	
	public final static String DATE_FORMAT = "MM/dd/yyyy ':' HH:mm:ss"; 
	
	public static void showLongToast(Context context, String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static void log(String info){
		Log.v("Paradroid", info);
	}
	
}
