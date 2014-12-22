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

public class BreezeComponentElement extends AbstractBreezeElement implements NamedBreezeElement {

	
	private static final long serialVersionUID = -1344287802409223810L;
	
	boolean isDeclared = true;
	int ID;
	String symbol;
	String name;
	
	public LinkedList<Object> parameters = null;
	public LinkedList<Object> channels = null;
	
	@SuppressWarnings("unchecked")
	public BreezeComponentElement(LinkedList<Object> value, int ID) {
		
		Iterator<Object> it = value.iterator();
		symbol = (String)it.next();
		if (!symbol.equals("component")) isDeclared = false;
		
		name = (String)it.next();
		
		parameters =(LinkedList<Object>)it.next();
		channels   =(LinkedList<Object>)it.next();
		
		while (it.hasNext()) {
			Object cur = it.next(); 
			this.add(cur);
		}
		
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	
	public void output() {
		
		System.out.print("\n    ("+symbol+" "+name+" ");
		output(parameters, 0, false, 0); indent(1);
		output(channels, 0, false, 0);
		
		if (isDeclared) {
			// output parameters and channels
			output(this, 0, true, 0);
			System.out.print(")");
		} else {
			output(this, 4, true, 3);
			System.out.print("\n    )");
			
		}
		
	}
	

}
