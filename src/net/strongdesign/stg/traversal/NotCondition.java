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