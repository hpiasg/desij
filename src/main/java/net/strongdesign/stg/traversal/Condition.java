

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
