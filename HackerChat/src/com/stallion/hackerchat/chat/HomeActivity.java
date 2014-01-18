package com.stallion.hackerchat.chat;

import com.stallion.hackerchat.HackerChatApplication;
import com.stallion.hackerchat.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity {
	
	// Chat List
	private ListView mList;
	private TextView mWelcomeTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		// Initialize views
		mList = (ListView)findViewById(android.R.id.list);
		mWelcomeTextView = (TextView)findViewById(R.id.welcomeTextView);
		
		// Get name from shared preferences
		String name = getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
				.getString(HackerChatApplication.USER_PREFS_USERNAME, null);

		// Display name in a welcome message
		mWelcomeTextView.setText(String.format("Welcome, %s.", name));
	}
}
