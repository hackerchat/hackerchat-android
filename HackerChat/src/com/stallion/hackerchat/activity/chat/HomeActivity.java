package com.stallion.hackerchat.activity.chat;

import java.util.ArrayList;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.stallion.hackerchat.HackerChatApplication;
import com.stallion.hackerchat.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends Activity {
	
	public static final String TAG = "HomeActivity";
	
	// Chat List
	private ListView mChatsListView;
	private TextView mWelcomeTextView;
	private TextView mNameTextView;
	private ChatArrayAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		// Initialize views
		mChatsListView = (ListView)findViewById(android.R.id.list);
		mWelcomeTextView = (TextView)findViewById(R.id.welcomeTextView);
		mNameTextView = (TextView)findViewById(R.id.nameTextView);
		
		// Initialize adapter to the list
		mAdapter = new ChatArrayAdapter(this, R.layout.chat_cell);
		mChatsListView.setAdapter(mAdapter);
		
		// Get name from shared preferences
		String name = getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
				.getString(HackerChatApplication.USER_PREFS_USERNAME, null);

		// Display name in a welcome message
		mWelcomeTextView.setText("Welcome, ");
		mNameTextView.setText(name);
		
		// Build user path
		String username = getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
				.getString(HackerChatApplication.USER_PREFS_USERNAME, null);
		String path = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + username;
		
		// Sync to user's chats
		Firebase chatsRef = new Firebase(path + "/chats");
		chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override public void onCancelled(FirebaseError arg0) { }

			@Override
			public void onDataChange(DataSnapshot chatsSnapshot) {
				// Build an ArrayList with the paths to the chats
				ArrayList<String> paths = new ArrayList<String>();
				String baseChatsPath = HackerChatApplication.FIREBASE_BASE_URL + "/chats/";
				
				for (DataSnapshot chat : chatsSnapshot.getChildren()) {
					String chatId = (String)chat.getValue();
					paths.add(baseChatsPath + chatId);
				}
				
				// Set the data and refresh the adapter
				mAdapter.setData(paths);
				mAdapter.notifyDataSetChanged();
			}
		});
		
		final Activity activity = this;
		// Add an item click listener to the chat list to open chats!
		mChatsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Open the chat activity, passing the path of the chat via an intent.
				Intent intent = new Intent(activity, ChatActivity.class);
				intent.putExtra(ChatActivity.CHAT_PATH, mAdapter.getPath(position));
				startActivity(intent);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
	        case R.id.action_create_chat:
	        	// Create a new chat and open it
	        	// Get username
	        	String username = getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
	        			.getString(HackerChatApplication.USER_PREFS_USERNAME, null);

	        	String chatsPath = HackerChatApplication.FIREBASE_BASE_URL + "/chats";
	        	Firebase newChatRef = new Firebase(chatsPath).push();
	        	Firebase usersRef = newChatRef.child("users");

	        	// Add this user to the chat
	        	Firebase userRef = usersRef.push();
	        	userRef.setValue(username);
	        	
	        	// Add the created time to the chat
	        	Long time = System.currentTimeMillis() / 1000;
	        	newChatRef.child("time").setValue(time);
	        	
	        	// Set the priority of the chat to be the negative created time,
	        	// so that it appears at the top when newer things happen
	        	newChatRef.setPriority(-time);
	        	
	        	// Add this chat to the current user's list of chats
	        	String usersChatsPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + username + "/chats";
	        	Firebase newUsersChatsRef = new Firebase(usersChatsPath).push();
	        	newUsersChatsRef.setValue(newChatRef.getName());
	        	
	        	// Open the chat
	        	Intent intent = new Intent(this, ChatActivity.class);
	        	intent.putExtra(ChatActivity.CHAT_PATH, chatsPath + "/" + newChatRef.getName());
	        	startActivity(intent);
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public static class ChatArrayAdapter extends ArrayAdapter<String> {
		
		private Context mContext;
		private int mResource;
		
		// IDs of the chats. Full path is HackerChatApplication.FIREBASE_BASE_URL + "/chats/" + mData.get(i)
		private ArrayList<String> mData;

		public ChatArrayAdapter(Context context, int resource) {
			super(context, resource);
			mContext = context;
			mResource = resource;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// Inflate a new view if the old view is null.
				convertView = LayoutInflater.from(mContext).inflate(mResource, null, false);
			}
			
			// Displays a list of names, for example: "Shane, Jonah, Ford"
			final TextView headingTextView = (TextView)convertView.findViewById(R.id.headingTextView);
			// Displays the most recent message
			final TextView subheadingTextView = (TextView)convertView.findViewById(R.id.subheadingTextView);
			
			// Clear the textviews
			headingTextView.setText("");
			subheadingTextView.setText("");
			
			// Get the current user's global path
			String username = mContext.getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
					.getString(HackerChatApplication.USER_PREFS_USERNAME, null);
			final String currentUserPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + username;
			
			// Get the chat data from Firebase
			String chatPath = mData.get(position).toString();
			Firebase chatRef = new Firebase(chatPath);
			chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override public void onCancelled(FirebaseError error) { }

				@Override
				public void onDataChange(final DataSnapshot chatSnapshot) {

					// Sync to the last message to set up the subheading
					String lastMessageId = (String)chatSnapshot.child("lastMessage").getValue();
					String lastMessagePath = HackerChatApplication.FIREBASE_BASE_URL + "/messages/" + lastMessageId;
					Firebase lastMessageRef = new Firebase(lastMessagePath);
					lastMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override public void onCancelled(FirebaseError error) { }

						@Override
						public void onDataChange(DataSnapshot lastMessageSnapshot) {
							// Set the subheading text
							final String lastMessageText = (String)lastMessageSnapshot.child("text").getValue();
							
							// Check if this is a group chat, and if so then attach the name of the sender
							if (chatSnapshot.child("users").getChildrenCount() > 2) {
								String senderUserPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + 
										(String)lastMessageSnapshot.child("sender").getValue();
								Firebase senderUserRef = new Firebase(senderUserPath);
								senderUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
									@Override public void onCancelled(FirebaseError arg0) { }

									@Override
									public void onDataChange(DataSnapshot senderUserSnapshot) {
										// Check if this is you or someone else
										String senderUserPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + senderUserSnapshot.getName();
                                        String senderName;
										if (senderUserPath.equals(currentUserPath)) {
											// Set the name to be "You"
											senderName = "You";
										} else {
                                            // Set the name to be whatever the other person's name is
                                            senderName = (String)senderUserSnapshot.child("name").getValue();
										}
                                        subheadingTextView.setText(senderName + ": " + lastMessageText);
									}
								});

							} else {
								// If it's a one on one just show the message
								subheadingTextView.setText(lastMessageText);
							}
						}
					});
					
					for (DataSnapshot userIdSnapshot : chatSnapshot.child("users").getChildren()) {

						// Check that this user is not the current user
						String userPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + userIdSnapshot.getValue();
						Firebase userRef = new Firebase(userPath);
						if (!userPath.equals(currentUserPath)) {

                            // Sync to each user to set up the heading
                            // Note: The datastore only stores user ids in a list, so we go ahead and sync to
                            // the actual user object.
							userRef.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override public void onCancelled(FirebaseError error) { }

								@Override
								public void onDataChange(DataSnapshot userSnapshot) {

									// TODO: If we have a chat with more than like 5 people, it might be a worthy optimization
									// to break out of this loop so we don't process all the users for building this string...
									// TODO: Also we could cache the heading title somehow

									// Add the name to the heading.
									String text = headingTextView.getText().toString();
									String name = (String)userSnapshot.child("name").getValue();

									if ("".equals(text)) {
										// This is the first name!
										text = name;
									} else {
										text += ", " + name;
									}
									headingTextView.setText(text);
								}
							});

						} else {
							// Add "You" to the heading
                            String text = headingTextView.getText().toString();
                            String name = "You";

                            if ("".equals(text)) {
                                // This is the first name!
                                text = name;
                            } else {
                                text += ", " + name;
                            }
                            headingTextView.setText(text);
						}
					}
				}
			});

			return convertView;
		}
		
		@Override
		public int getCount() {
			return (mData != null) ? mData.size() : 0;
		}
		
		public void setData(ArrayList<String> data) {
			mData = data;
		}
		
		public String getPath(int position) {
			return mData.get(position);
		}
	}
}
