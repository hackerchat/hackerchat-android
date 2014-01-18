package com.stallion.hackerchat.chat;

import java.util.ArrayList;
import java.util.LinkedList;

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
	
	private EditText mMessageInput;
	private Button mSendButton;
	private ListView mMessagesListView;
	private ChatAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chat);
		
		// Initialize UI components and adapter
		mMessageInput = (EditText)findViewById(R.id.messageInput);
		mSendButton = (Button)findViewById(R.id.sendButton);
		mMessagesListView = (ListView)findViewById(android.R.id.list);
		mAdapter = new ChatAdapter(this, R.layout.message_cell);
		
		// Open the chat from the path given in the intent
		Bundle data = getIntent().getExtras();
		String chatPath = data.getString(CHAT_PATH, null);
		
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
				}

				@Override public void onChildRemoved(DataSnapshot messageSnapshot) {
					// Messages won't ever be removed by us, but this method *will* get called if new messages come in.
					// We can assume that it's removing the last element, so we just remove it.
					mAdapter.removeFromEnd();
				}
			});
		}
		
		// Set up send button
		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: Send the message here
			}
		});
	}
	
	public static class ChatAdapter extends ArrayAdapter<String> {
		
		Context mContext;
		int mResource;
		
		// These strings are message ids, loaded async
		ArrayList<String> mData;

		public ChatAdapter(Context context, int resource) {
			super(context, resource);
			mContext = context;
			mResource = resource;
			
			mData = new ArrayList<String>();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// Create a view if we don't have one
				convertView = LayoutInflater.from(mContext).inflate(mResource, null);
			}
			
			// Initialize UI components for the views
			final TextView nameTextView = (TextView)convertView.findViewById(R.id.nameTextView);
			final TextView messageTextView = (TextView)convertView.findViewById(R.id.messageTextView);
			
			// Sync to the message!
			String messagePath = HackerChatApplication.FIREBASE_BASE_URL + "/messages/" + mData.get(position);
			Firebase messageRef = new Firebase(messagePath);
			messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override public void onCancelled(FirebaseError error) { }

				@Override
				public void onDataChange(DataSnapshot messageSnapshot) {
					// TODO: Make these things fade in together?
					
					// Update the message text
					messageTextView.setText((String)messageSnapshot.child("text").getValue());
					
					// Sync to the user object to get name
					String username = (String)messageSnapshot.child("sender").getValue();
					String userPath = HackerChatApplication.FIREBASE_BASE_URL + "/users/" + username;
					Firebase userRef = new Firebase(userPath);
					userRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override public void onCancelled(FirebaseError error) { }

						@Override
						public void onDataChange(DataSnapshot userSnapshot) {
							// Update the name label
							nameTextView.setText((String)userSnapshot.child("name").getValue());
						}
					});
				}
			});
			
			return convertView;
		}
		
		@Override
		public int getCount() {
			return (mData != null) ? mData.size() : 0;
		}
		
		public void addMessageId(String messageId) {
			mData.add(messageId);
			notifyDataSetChanged();
		}
		
		public void removeFromEnd() {
			// The end is really the front, since the end of the list (i.e. the stuff at the bottom
			// of the chat) is the most recent stuff.
			mData.remove(0);
			notifyDataSetChanged();
		}
	}
}
