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

package net.strongdesign.stg;


/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * A Signal represents an electric signal in an STG context. It combines a name and a @link Signature. 
 * @author Mark Sch�fer
  */
public class SignalOld implements Cloneable, Comparable<SignalOld> {

	protected String name;
	
	public SignalOld (String name) {
		this.name = name;
	}

	
	public String toString () {
		return name;
	}

	public String getName() {
		return name;
			}
			
	/**
	 * Checks if two Signals are equal, this considers only the names of the signals since signatures may change
	 * during decomposition.  
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (! (o instanceof SignalOld)) return false;
		
		
		return ((SignalOld)o).name.equals(name);	
	}

	public Object clone() {
		return new SignalOld(new String(name));
	}

	public int hashCode() {
		return name.hashCode();
	}
	
	public int compareTo(SignalOld o) {
		return toString().compareTo(o.toString());
	}
	
	
}
