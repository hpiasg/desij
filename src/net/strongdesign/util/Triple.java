/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
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

package net.strongdesign.util;


/**
 * Support class for combining three variables.
 * One have direct access to the two variables by the
 * public memeber variables {@link #a a} and {@link #b b}.  
 * 
 * @author Mark Sch�fer
 */
public class Triple<A,B,C>  implements Comparable<Triple<A,B,C>>{
	/**First stored element.*/
	public A a;
	/**Second stored element.*/
	public B b;
	/**Third stored element.*/
	public C c;
	
	/**Constructs an instance from two variables.*/
	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;		
		this.c = c;		
	}
	
	/**
	 * Returns equal if the first elements of the two pairs are
	 * equal.
	 * @param o Must be an instance of pair
	 * @return True if the first elements are equal
	 * @throws ClassCastException
	 */
	public boolean equals(Object o) {
		return a.equals(((Triple)o).a);
	}
	
	public String toString() {
		return a.toString()+","+b.toString()+","+c.toString();
	}

	public int compareTo(Triple<A,B,C> o) {
		return a.hashCode()-o.a.hashCode();
	}
}
