package net.strongdesign.balsa;

import java.util.HashMap;

import net.strongdesign.balsa.components.HSComponent;

public class BreezeNet implements Cloneable {
	public HashMap<Integer, HSComponent> components = new HashMap<Integer, HSComponent>();
	public int nextComponentID=0; // number for the next component, starts with 0 
	
	public HashMap<Integer, HSChannel> channels = new HashMap<Integer, HSChannel>();
	public int nextChannelID=1; // number for the next channel, starts with 1
	public BreezeNet() {
		
	}
}
