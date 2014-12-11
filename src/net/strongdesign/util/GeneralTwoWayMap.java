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

import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Wist
 * Implemention of a bijective function
 */
public class GeneralTwoWayMap<T1, T2> {
	
	final Map<T1, T2> firstToSecond;
	final Map<T2, T1> secondToFirst;
	
	/**
	 * Can only be initialised with two empty maps to avoid ambiguities
	 * @param first: empty map
	 * @param second: empty map
	 */
	public GeneralTwoWayMap(Map<T1, T2> map1, Map<T2, T1> map2) {
		if ( !map1.isEmpty() || !map2.isEmpty() )
			throw new ArgumentException("Maps should be empty, initially");
		this.firstToSecond = map1;
		this.secondToFirst = map2;
	}
	
	public void put (T1 first, T2 second) {
		removeKey(first);
		removeValue(second);
		if (first == null || second == null)
			throw new NullPointerException();
		firstToSecond.put(first, second);
		secondToFirst.put(second, first);
	}
	
	public Set<T1> keys() {
		return firstToSecond.keySet();
	}
	
	public Set<T2> values() {
		return secondToFirst.keySet();
	}
	
	public T2 getValue(T1 first) {
		return firstToSecond.get(first);
	}
	
	public T1 getKey(T2 second) {
		return secondToFirst.get(second);
	}
	
	public void remove (T1 first, T2 second) {
		firstToSecond.remove(first);
		secondToFirst.remove(second);
	}
	
	public void removeKey(T1 first) {
		T2 second = getValue(first);
		if (second != null)
			remove(first, second);
	}
	
	public void removeValue(T2 second) {
		T1 first = getKey(second);
		if (first != null)
			remove(first, second);
	}
	
	public boolean isEmpty() {
		return firstToSecond.isEmpty();
	}
	
	public boolean containsKey(T1 key) {
		return firstToSecond.containsKey(key);
	}
	
	public boolean containsValue(T2 value) {
		return secondToFirst.containsKey(value);
	}
}
