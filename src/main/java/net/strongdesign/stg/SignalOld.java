

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
