package net.strongdesign.balsa.breezefile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

public class BreezeChannelListElement extends AbstractBreezeElement {
	
	private static final long serialVersionUID = 8933781235650293491L;

	TreeMap<Integer, BreezeChannelElement> channels = new TreeMap<Integer, BreezeChannelElement>(); 
	
	@SuppressWarnings("unchecked")
	public BreezeChannelListElement(LinkedList<Object> list) {
		Iterator<Object> it = list.iterator();
		it.next();
		
		int number = 1; // starting with 1
		
		while (it.hasNext()) {
			LinkedList<Object> cur = (LinkedList<Object>)it.next();
			channels.put(number++, new BreezeChannelElement(cur));
			
		}
		
	}
	
	public void output() {
		System.out.print("\n  (channels");
		
		for (java.util.Map.Entry<Integer, BreezeChannelElement> be: channels.entrySet()) {
			System.out.print("\n");
			indent(4);
			be.getValue().output();
			System.out.print(" ; "+be.getKey());
		}
		System.out.print("\n  )");
		
	}
}
