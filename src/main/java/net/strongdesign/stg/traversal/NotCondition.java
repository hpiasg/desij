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

package net.strongdesign.stg.traversal;

import net.strongdesign.stg.traversal.Condition;

/**
 * 
 * 
 * <p>
 * <b>History: </b> <br>
 * 03.10.2004: Created <br>
 * 
 * <p>
 * 
 * @author Mark Schaefer
 */
public  class NotCondition<T> implements Condition<T> {
	protected Condition<? super T> condition;
	public NotCondition(Condition<? super T> condition) {
		this.condition = condition;			
	}		
	public boolean fulfilled (T o) {
		return ! condition.fulfilled(o);
	}
	
	public Object becauseOf() {
		return "Not ("+condition.becauseOf()+")";
	}
}