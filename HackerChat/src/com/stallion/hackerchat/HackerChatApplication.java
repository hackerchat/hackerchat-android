package com.stallion.hackerchat;

import android.app.Application;
import android.util.Log;

public class HackerChatApplication extends Application {
	
	public static final String TAG = "HackerChatApplication";
	
	// Firebase stuff
	public static final String FIREBASE_BASE_URL = "https://hacker-chat-app.firebaseIO.com";
	
	// String name for user preferences in shared preferences
	public static final String USER_PREFS = "userPrefs";
	public static final String USER_PREFS_USERNAME = "username";
	
	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate");
	}
}
