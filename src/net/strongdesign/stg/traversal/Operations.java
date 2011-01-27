/*
 * Created on 26.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg.traversal;

import java.util.*;

import net.strongdesign.stg.STGException;




/**
 * Operation implements generic basic search, collecting and modification functions.
 * 
 * The real work is done by classes implementing the interfaces 
 * {@link stg.traversal.Condition}, {@link stg.traversal.Collector} and 
 * {@link stg.traversal.Operation}.
 * <br><br>
 * They can be combined independently from each other to get the desired operation,
 * missing features should be implemented by implementing one of these interfaces rather than implementing
 * them from scratch.
 * <br>
 * The methods of Operation are wrapped for  convenience in:<br>
 * {@link stg.STGTest#getPlaces}<br>
 * {@link stg.STGTest#getTransitions}<br>
 * {@link stg.STGTest#collectFromPlaces}<br>
 * {@link stg.STGTest#collectFromTransitions}<br>
 * {@link stg.STGTest#modifyPlaces}<br>
 * {@link stg.STGTest#modifyTransitions}  
 *  
 *  
 * 
 * 
 * @author Mark Sch�fer
 *
*/
public class Operations {
	
	public static <T> void modify(Collection<T> collection, Condition<T> condition, Operation<T> modifier) throws STGException {
		List<? extends T> subList = getElements(collection, condition);
		for (T element : subList)
			modifier.operation(element);
	}
		
	public static <T> List<T> getElements(
			Collection<T> collection, 
			Condition<T> condition) {
		List<T> res = new LinkedList<T>();
		for (T actElement : collection) {		
			if (condition.fulfilled( actElement))
				res.add(actElement);
		}		
		return res;		
	}



	public static <T,R> List<R> collect(
			Collection<T> collection, 
			Condition<T> condition, 
			Collector<T,R> collector) {
		
		List<? extends T> subList = getElements(collection, condition);
		
		List<R> result = new LinkedList<R>();
		
		for (T actElement : subList)
			result.add(collector.operation(actElement));
			
		return result;
	}



}
