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

public class BreezeChannelElement extends AbstractBreezeElement {
	
	private static final long serialVersionUID = 1180548319980606751L;


	public enum ChannelType {
		SYNC("sync"), PUSH("push"), PULL("pull"), ERROR("error!");
		String ctype;
		
		ChannelType(String type) {
			ctype = type;
		}
		
		public String toString() {
			return ctype;
		}
	};
	
	ChannelType type;
	int width=0;
	
	BreezeChannelElement(LinkedList<Object> value) {
		Iterator<Object> it = value.iterator();
		
		String ctype = (String)it.next();
		// read type and width
		type = ChannelType.ERROR;
		if (ctype.equals("sync")) {
			type = ChannelType.SYNC;
		} else if (ctype.equals("push")) {
			type = ChannelType.PUSH;
			width = (Integer)it.next();
		} else if (ctype.equals("pull")) {
			type = ChannelType.PULL;
			width = (Integer)it.next();
		}
		
		//read the tail part
		while(it.hasNext()) {
			this.add(it.next());
		}
	}
	
	public void output() {
		System.out.print("("+type.toString());
		
		if (type!=ChannelType.SYNC) {
			System.out.print(" "+width);
		}
		
		output(this, 0, true, 0);
		
		System.out.print(")");
	}
	
}
