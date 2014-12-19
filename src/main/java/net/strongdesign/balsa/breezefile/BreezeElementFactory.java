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

public class BreezeElementFactory {
	
	public static String symbolOf(Object value) {
		if (value instanceof LinkedList<?>) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> ll = (LinkedList<Object>)value;
			Iterator<Object> it = ll.iterator();
			Object temp = it.next();
			
			if (!(temp instanceof String)) return "";
			
			if (((String)temp).charAt(0)=='"') return "";
			return (String)temp;
		}
		return "";
	}
	
	
	// for now we only work with the breeze-part element and its contents, the rest elements are not changed 
	
	public static AbstractBreezeElement baseElement(Object value) {
		if (value instanceof LinkedList<?>) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> ll = (LinkedList<Object>)value;
			Iterator<Object> it = ll.iterator();
			String first = (String)it.next();
			
			if (first.equals("import")) {
				return new BreezeImport((String)it.next());
			}
			
			if (first.equals("breeze-part")) {
				return new BreezePartElement(ll);
			}
		}
		return new AbstractBreezeElement(value);
	}
	

}
