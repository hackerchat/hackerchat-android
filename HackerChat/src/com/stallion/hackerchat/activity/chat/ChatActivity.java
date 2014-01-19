package com.stallion.hackerchat.activity.chat;

import java.util.ArrayList;
import java.util.HashMap;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.stallion.hackerchat.HackerChatApplication;
import com.stallion.hackerchat.R;
import com.stallion.hackerchat.model.HackerMessage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	
	public static final String TAG = "ChatActivity";
	public static final String CHAT_PATH = "chatPath";
	
	private EditText mMessageEditText;
	private Button mSendButton;
	private ListView mMessagesListView;
	private ChatAdapter mAdapter;
	
	// This string holds the path to this chat in Firebase
	private String chatPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chat);
		
		// Initialize UI components and adapter
		mMessageEditText = (EditText)findViewById(R.id.messageEditText);
		mSendButton = (Button)findViewById(R.id.sendButton);
		mMessagesListView = (ListView)findViewById(android.R.id.list);
		mAdapter = new ChatAdapter(this, R.layout.message_cell);
		
		// Open the chat from the path given in the intent
		Bundle data = getIntent().getExtras();
		chatPath = data.getString(CHAT_PATH, null);
		
		if (chatPath == null) {
			// Shouldn't ever happen, but hey
			Toast.makeText(this, "Couldn't open chat...", Toast.LENGTH_SHORT).show();
			finish();

		} else {
			// Set up the list with the chat messages
			mMessagesListView.setAdapter(mAdapter);
			
			Firebase chatRef = new Firebase(chatPath + "/messages");
			// Limit to 50 messages
			chatRef.limit(50);
			chatRef.addChildEventListener(new ChildEventListener() {
				
				// Unused methods, stuff won't move or change... and we hope it won't cancel out
				@Override public void onCancelled(FirebaseError error) { }
				@Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) { }
				@Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) { }

				@Override
				public void onChildAdded(DataSnapshot messageSnapshot, String previousMessageName) {
					String messageId = (String)messageSnapshot.getValue();
					mAdapter.addMessageId(messageId);
					mMessagesListView.setSelection(mAdapter.getCount() - 1);
				}

				@Override public void onChildRemoved(DataSnapshot messageSnapshot) {
					// Messages won't ever be removed by us, but this method *will* get called if new messages come in.
					// We can assume that it's removing the last element, so we just remove it.
					mAdapter.removeFromEnd();
				}
			});
		}
		
		// Set up send button
		final Activity activity = this;
		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
				String messageText = mMessageEditText.getText().toString();

				// Check that the message isn't empty
				if (!"".equals(messageText)) {
					// Get the username of the current user
					String username = activity.getSharedPreferences(HackerChatApplication.USER_PREFS, 0)
							.getString(HackerChatApplication.USER_PREFS_USERNAME, null);

					// Get the current epoch time
					Long time = System.currentTimeMillis()/1000;

					// Build a new message object and fill it with data
					HashMap<String, Object> message = new HashMap<String, Object>();
					message.put("text", messageText);
					message.put("sender", username);
					message.put("time", time);

					// Save the message in the global message store
					Firebase globalMessagesRef = new Firebase(HackerChatApplication.FIREBASE_BASE_URL + "/messages");
					Firebase newGlobalMessageRef = globalMessagesRef.push();
					newGlobalMessageRef.setValue(message);
					
					// Push a reference to this message to this chat
					Firebase chatRef = new Firebase(chatPath);
					Firebase chatMessageRef = chatRef.child("messages").push();
					chatMessageRef.setValue(newGlobalMessageRef.getName(), time);
					
					// Save the message in lastMessage
					Firebase lastMessageRef = chatRef.child("lastMessage");
					lastMessageRef.setValue(newGlobalMessageRef.getName());
					
					// Set the priority of this chat to be the time of the most recent message
					chatRef.setPriority(-time);
					
					// Clear the edittext
					mMessageEditText.setText(null);
				}
			}
		});
	}
	
	public static class ChatAdapter extends ArrayAdapter<String> {
		
		Context mContext;
		int mResource;
		
		// These strings are message ids, loaded async
		ArrayList<String> mData;
		
		// Cache the message info
		ArrayList<HackerMessage> mCache;

		public ChatAdapter(Context context, int resource) {
			super(context, resource);
			mContext = context;
			mResource = resource;
			
			mData = new ArrayList<String>();
			mCache = new ArrayList<HackerMessage>();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			if (convertView == null || mCache.get(position) == null) {
				// Create a new view if there's no view to convert or if we don't have cached data
				convertView = LayoutInflater.from(mContext).inflate(mResource, null);
			}

			// Initialize UI components for the views
			final TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
			final TextView messageTextView = (TextView)convertView.findViewById(R.id.messageTextView);
			
			// Fill in the UI with information
			if (mCache.get(position) == null) {
				// Initialize a new model object for caching
				final HackerMessage model = new HackerMessage();

				// We don't have a cached model, so sync to the message
                String messagePath = HackerChatApplication.FIREBASE_BASE_URL + "/messages/" + mData.get(position);
                Firebase messageRef = new Firebase(messagePath);
                messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onCancelled(FirebaseError error) { }

                    @Override
                    public void onDataChange(DataSnapshot messageSnapshot) {
                        // TODO: Make these things fade in together?
                        
                        // Update the message text and the model object
                    	String messageText = (String)messageSnapshot.child("text").getValue();
                        messageTextView.setText(messageText);
                        model.text = messageText;
                        
                        // Update the time in the model
                        model.time = (Long)messageSnapshot.child("time").getValue();
                        
                        // Sync to the user object to get name
                        String username = (String)messageSnapshot.child("sender").getValue();
                        String userPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + username;
                        Firebase userRef = new Firebase(userPath);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onCancelled(FirebaseError error) { }

                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                // Update the name label and the model
                            	String senderName = (String)userSnapshot.child("name").getValue();
                                nameTextView.setText(senderName);
                                model.senderName = senderName;
                                
                                // Save the model object
                                mCache.set(position, model);
                            }
                        });
                    }
                });

			} else {
				// Complete the UI from the model
				HackerMessage model = mCache.get(position);
                messageTextView.setText(model.text);
				nameTextView.setText(model.senderName);
			}
			
			return convertView;
		}
		
		@Override
		public int getCount() {
			return (mData != null) ? mData.size() : 0;
		}
		
		public void addMessageId(String messageId) {
			mData.add(messageId);
			mCache.add(null);
			notifyDataSetChanged();
		}
		
		public void removeFromEnd() {
			// The end is really the front, since the end of the list (i.e. the stuff at the bottom
			// of the chat) is the most recent stuff.
			mData.remove(0);
			mCache.remove(0);
			notifyDataSetChanged();
		}
	}
}
