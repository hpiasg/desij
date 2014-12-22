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

/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg;


/**
 * @author mark
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SignalEdge implements Cloneable {
	protected Integer signal;
	protected EdgeDirection direction;
	
	
	
	public int hashCode() {
	    return direction.hashCode()*13+signal.hashCode()*41;
	}
	
	public void setDirection(EdgeDirection direction) {
		this.direction = direction;
	}
	
	public SignalEdge(Integer signal, EdgeDirection direction) {
		this.signal = signal;
		this.direction = direction;
	}
	 
	public String toString() {
		return ""+signal+direction;
	}
	
	public Integer getSignal() {
		return signal;
	}
	public EdgeDirection getDirection() {
		return direction;
	}
	
	public Object clone() {
		return new SignalEdge(signal, direction);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (! (o instanceof SignalEdge)) return false;
		return ((SignalEdge)o).signal.equals(signal) && ((SignalEdge)o).direction == direction;
		
	}
}
