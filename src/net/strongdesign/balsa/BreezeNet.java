package net.strongdesign.balsa;

import java.util.HashMap;

public class BreezeNet implements Cloneable {
	public HashMap<Integer, Object> components = new HashMap<Integer, Object>();
	public int nextComponentID=0; // number for the next component, starts with 0 
	
	public HashMap<Integer, HSChannel> channels = new HashMap<Integer, HSChannel>();
	public int nextChannelID=1; // number for the next channel, starts with 1
	public BreezeNet() {
		
	}
}
