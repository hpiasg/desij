package net.strongdesign.balsa.breezefile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

public class BreezeComponentListElement extends AbstractBreezeElement {

	TreeMap<Integer, BreezeComponentElement> components = new TreeMap<Integer, BreezeComponentElement>(); 
	
	@SuppressWarnings("unchecked")
	public BreezeComponentListElement(LinkedList<Object> list) {
		Iterator<Object> it = list.iterator();
		it.next();
		
		int number = 0; // starting with 0
		
		while (it.hasNext()) {
			LinkedList<Object> cur = (LinkedList<Object>)it.next();
			components.put(number++, new BreezeComponentElement(cur));
			
		}
		
	}
	
	public void output() {
		System.out.print("\n  (components");
		
		for (java.util.Map.Entry<Integer, BreezeComponentElement> be: components.entrySet()) {
			be.getValue().output();
			System.out.print(" ; "+be.getKey());
		}
		System.out.print("\n  )");
		
	}

}
