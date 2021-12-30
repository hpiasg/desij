

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
