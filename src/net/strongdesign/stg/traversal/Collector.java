/*
 * Created on 26.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg.traversal;


/**
 * The Collector interface is used for collecting results from a specific
 * operation implemented by the implementing classes from various objects.
 * 
 * @see stg.traversal.STGOperations
 * 
 *
 * @author mark
 */
public interface  Collector<T,R> {
	public R operation(T o);
}
