/*
 * Created on 26.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg.traversal;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;



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
 * <p>
 * <b>History: </b> <br>
 * 26.09.2004: Created <br>
 * 21.05.2005: Removed shortest path algorithm
 * 
 *  
 * 
 * 
 * @author Mark Sch�fer
 *
*/
public abstract class STGOperations {
    public static void checkForWeights2(STG stg) throws STGException {
     //   StringBuilder rep = new StringBuilder();
        
        for (Place place : stg.getPlaces(ConditionFactory.ALL_PLACES))
            for (Node child :place.getChildren())
                if (place.getChildValue(child) != child.getParentValue(place)   )
                    throw new STGException("MÖÖP!");

        for (Transition place : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS))
            for (Node child :place.getChildren())
                if (place.getChildValue(child) != child.getParentValue(place)   )
                    throw new STGException("M��P!");
            
                
        for (Place place : stg.getPlaces(ConditionFactory.ALL_PLACES))
            for (Node child :place.getParents())
                if (place.getParentValue(child) != child.getChildValue(place)   )
                    throw new STGException("M��P!");

        for (Transition place : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS))
            for (Node child :place.getParents())
                if (place.getParentValue(child) != child.getChildValue(place)   )
                    throw new STGException("M��P!");
            
            
    }
    
   
	public static <T> void modifyElements(Collection<T> collection, Condition<? super T> condition, Operation<? super T> modifier) throws STGException{
		List<? extends T> subList = getElements(collection, condition);
		for (T element : subList)
			modifier.operation(element);
	}
		
	public static <T> List<T> getElements(Collection<T> collection, Condition<? super T> condition) {
		List<T> res = new LinkedList<T>();
		for (T actElement : collection) {
			if (condition.fulfilled(actElement))
				res.add(actElement);
		}		
		return res;		
	}



	public static <T,R> List<R> collectFromCollection(Collection<T> collection, Condition<? super T> condition, Collector<? super T,R> collector) {
		List<? extends T> subList = getElements(collection, condition);
		
		List<R> result = new LinkedList<R>();
		
		for (T actElement : subList)
			result.add(collector.operation(actElement));
			
		return result;
	}


	public static <T, R> Set<R> collectUniqueFromCollection(
			Collection<T> collection, 
			Condition<? super T> condition, 
			Collector<? super T, ? extends Collection<R>> collector) {
		List<? extends T> subList = getElements(collection, condition);
		
		Set<R> result = new HashSet<R>();
		
		
		for (T actElement : subList) {
		    Collection<R> o = collector.operation(actElement);
	        result.addAll(o);
		}
		return result;
	}
		

	public static <T, R> Set<R> collectUniqueCollectionFromCollection(
			Collection<T> collection, 
			Condition<? super T> condition, 
			Collector<? super T,R> collector) {
		List<? extends T> subList = getElements(collection, condition);
		
		Set<R> result = new HashSet<R>();
		
		for (T actElement : subList) {
		    R o = collector.operation(actElement);
		    if (o instanceof Collection ) 
		        result.addAll((Collection<? extends R>)o);
		    else
		        result.add(o);
		}
		return result;
	}
		

	

}
