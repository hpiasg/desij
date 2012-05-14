package net.strongdesign.balsa;


public class HSChannel {
	public enum ChannelSense {SYNC, PUSH, PULL}; 
	
	public ChannelSense sense=ChannelSense.SYNC;
	public int width=0;
	public HSPort Source=null;
	public HSPort Destination=null;
	public HSChannel() {
	
	}
	
	public HSChannel(ChannelSense sense, int width) {
		this.sense = sense;
		this.width = width;
	}
}
