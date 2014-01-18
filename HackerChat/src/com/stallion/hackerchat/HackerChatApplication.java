package com.stallion.hackerchat;

import android.app.Application;
import android.util.Log;

public class HackerChatApplication extends Application {
	
	public static final String TAG = "HackerChatApplication";
	
	// String name for user preferences in shared preferences
	public static final String USER_PREFS = "userPrefs";
	public static final String USER_PREFS_USERNAME = "username";
	
	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate");
	}
}
