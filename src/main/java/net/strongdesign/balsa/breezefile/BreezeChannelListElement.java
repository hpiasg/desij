package net.strongdesign.balsa.breezefile;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
 * 
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

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
