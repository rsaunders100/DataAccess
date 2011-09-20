package com.mig.dataaccess;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;



/**
 * @author rob
 *
 */
public class DataAccessHelpers {


	/**
	 * Util method for prining a stack trace with a tag It means that we can
	 * still see the exceptions if we have a filter set on logcat.
	 */
	static void printStackTraceWithTag(Exception e, String tag) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		Log.e(tag, stringWriter.toString());
	}
	
	
	/**
	 * A nice helper function to check if the intent connection is
	 * available <p>
	 * 
	 * NEEDS PERMISSION :<br>
	 * android.permission.ACCESS_NETWORK_STATE
	 */
	static boolean isOnline(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
}
