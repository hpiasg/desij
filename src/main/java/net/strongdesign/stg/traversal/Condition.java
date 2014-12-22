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


/**
 * <p>
 * <b>History: </b> <br>
 * 26.09.2004: Created <br>
 * 
 * <p>
 * 
 * Interface for checking a condition for an element of a Petri net
 * e.g. if a transition is activated.
 * 
 * In implementing classes, it might be neccessary to implement a constructor to
 * check_ specific conditions, others might be static and will be provided by a 
 * factory class.
 * 
 * It is assumed that {@link #becauseOf()} is called after a successful call of {@link #fulfilled(T)}
 * 
 * @see stg.traversal.STGOperations
 * @see stg.condition.ConditionFactory 
 * 
 * @author Mark Sch�fer
 */
public interface Condition<T> {
	public boolean fulfilled(T o);
	public Object becauseOf();
}
