

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
