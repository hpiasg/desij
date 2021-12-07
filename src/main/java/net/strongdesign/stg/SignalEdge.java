

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
