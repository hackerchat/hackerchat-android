package com.stallion.hackerchat.model;

import com.firebase.client.DataSnapshot;

public class HackerMessage {
	public String text;
	public double time;
	public String sender;
	
	public HackerMessage(String text, double time, String sender) {
		this.text = text;
		this.time = time;
		this.sender = sender;
	}
	
	public HackerMessage(DataSnapshot messageSnapshot) {
		this.text = (String)messageSnapshot.child("text").getValue();
		this.time = (Double)messageSnapshot.child("text").getValue();
		this.sender = (String)messageSnapshot.child("sender").getValue();
	}
}
