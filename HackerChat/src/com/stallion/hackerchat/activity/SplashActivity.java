package com.stallion.hackerchat.activity;

import com.stallion.hackerchat.HackerChatApplication;
import com.stallion.hackerchat.R;
import com.stallion.hackerchat.activity.chat.HomeActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class SplashActivity extends Activity {
	
	private Button mLogInButton;
	private EditText mNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Check shared preferences for user being initialized already
        SharedPreferences preferences = getSharedPreferences(HackerChatApplication.USER_PREFS, 0);
        String name = preferences.getString(HackerChatApplication.USER_PREFS_USERNAME, null);
        
        if (name != null) {
        	// User is authenticated, so launch home activity.
        	Intent intent = new Intent(this, HomeActivity.class);
        	startActivity(intent);

        } else {
        	initializeAuthControls();
        	displayAuthControls();
        }
    }
    
    private void initializeAuthControls() {

        // Reference activity for displaying toasts and opening activities
        final Activity activity = this;
        
        // Initialize view components
        mNameEditText = (EditText)findViewById(R.id.nameEditText);
        mLogInButton = (Button)findViewById(R.id.getStartedButton);

        // Set up log in button click handler
        mLogInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                
                String name = mNameEditText.getText().toString();

                if ("".equals(name)) {
                    Toast.makeText(activity, "Please enter a name!", Toast.LENGTH_SHORT).show();

                } else {
                    // Put username in SharedPreferences
                    SharedPreferences.Editor editor = activity.getSharedPreferences(HackerChatApplication.USER_PREFS, 0).edit();
                    editor.putString(HackerChatApplication.USER_PREFS_USERNAME, name);
                    editor.commit();
                    
                    // Launch home screen
                    Intent intent = new Intent(activity, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
    
    private void displayAuthControls() {
        // TODO: Fade in auth controls here for sexiness
    }
}