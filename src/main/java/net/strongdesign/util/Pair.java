

package net.strongdesign.util;


/**
 * Support class for combining two variables.
 * One have direct access to the two variables by the
 * public memeber variables {@link #a a} and {@link #b b}.  
 * 
 * @author Mark Sch�fer
 */
public class Pair<A,B>  implements Comparable<Pair<A,B>>{
	/**First stored element.*/
	public A a;
	/**Second stored element.*/
	public B b;
	/**Constructs an instance from two variables.*/
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;		
	}
	
	/**
	 * Returns equal if the first elements of the two pairs are
	 * equal.
	 * @param o Must be an instance of pair
	 * @return True if the first elements are equal
	 * @throws ClassCastException
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (this==o) return true;
		if (! (o instanceof Pair)) return false;

		return a.equals(((Pair<A, B>)o).a) && b.equals(((Pair<A, B>)o).b);
	}
	
	public int hashCode() {
	    return a.hashCode()*13+b.hashCode()*41;
	}
	
	public String toString() {
		return "("+a.toString()+","+b.toString()+")";
	}

	public int compareTo(Pair<A,B> o) {
		int res = a.hashCode()-o.a.hashCode();
		if (res==0)
			res = b.hashCode()-o.b.hashCode();
		
		return res;
	}
	
	public static <A,B> Pair<A,B> getPair(A a, B b) {
		return new Pair<A,B>(a,b);
	}
	
	
}
