/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 12.12.2004
 *
 */
package net.strongdesign.stg.traversal;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.desij.CLW;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

/**
 * A Factory class which provides various Collectors
 * 
 * <p>
 * <b>History: </b> <br>
 * 12.12.2004: Created <br>
 * 19.02.2005: Added all Conditions to this Factory
 * 
 * <p>
 * 
 * @author Mark Schaefer
 */
public abstract  class CollectorFactory {

    
	//this is a stupid comment
    
    /**
     * Returns the children of the node
     * 
     * @author mark
     *
     * */
  
    protected static class Children<T,R> implements Collector<Node,Set<Node>> {
    	public Set<Node> operation(Node node) {
    		return node.getChildren();
    	}

    	
    	
    }
    protected static class ConflictSignalOf<T,R> implements Collector<Transition, List<Integer>> {


    	public List<Integer> operation(Transition transition) {
    		Set<Node> parents = transition.getParents();
    		List<Integer> result = new LinkedList<Integer>();
    		
    		List<Node> conflictTransitions = new LinkedList<Node>();
    		for (Node parent : parents)
    			conflictTransitions.addAll( parent.getChildren() );
    		 	
    		for (Node transition2 :  conflictTransitions) 
    			result.add(((Transition)transition2).getLabel().getSignal());
    		
    		return result;
    			
    	}

    }
    /**
     * 
     * Finds all conflicting signals according to output-determinacy, 
     * i.e. all weak conflicts
     * 
     * @author Dominic Wist
     *
     * @param <T>
     * @param <R>
     */
    protected static class WeakConflictSignalOf<T,R> implements Collector<Transition, List<Integer>> {


    	public List<Integer> operation(Transition transition) {
    		STG stg = transition.getSTG(); // for signature of a signal
    		
    		Set<Transition> visited = new HashSet<Transition>();
    		Queue<Transition> queue = new LinkedList<Transition>();
    		
    		Set<Integer> result = new HashSet<Integer>();
    		
    		for (Node parent : transition.getParents())
    			for ( Node tr : parent.getChildren() )
    				queue.add((Transition)tr); // conflicting transitions
    		
    		while ( !queue.isEmpty() ) {
    			Transition curTransition = queue.poll();
    			if ( visited.add(curTransition) ) {
    				Integer signal = curTransition.getLabel().getSignal();
					if (stg.getSignature(signal) == Signature.DUMMY) {
						List<Set<Node>> curConflicts = STGOperations.collectFromCollection(curTransition.getChildren(), 
								ConditionFactory.ALL_NODES, CollectorFactory.getChildrenCollector() );
			    		
			    		for ( Set<Node> trans : curConflicts )
			    			for (Node tr : trans)
			    				queue.add((Transition)tr);		
    				}
    				else {
    					result.add(signal);
    				}
    			}
    		}
    		
    		return new LinkedList<Integer>(result);	
    	}
    }
    protected static class Constant<T extends Object,R> implements Collector<T, Integer> {
    	protected Integer result;
    	public Constant(int result) {
    		this.result = new Integer(result);
    	}
    	public Constant(Integer result) {
    		this.result = result;
    	}
    	
    	public Integer operation(T o) {
    		return result;
    	}
    }
   
    public static class  Identity<T> implements Collector<T,T> {
    	public T operation(T o) {
    		return o;
    	}
    }

    
    
    public static Collector<STG, Set<Integer>> getStructuralAutoConflictSignals() {
        return new StructuralAutoConflictSignals<STG, Set<Integer>>();
    }
    protected static class StructuralAutoConflictSignals<S,R> implements Collector<STG, Set<Integer>> {
        public Set<Integer> operation(STG stg) {
            Set<Integer> result = new HashSet<Integer>();
            for (Transition transition : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS))
                if ( getStructuralConflictSignals().operation(transition).contains(transition.getLabel().getSignal()) )
                        result.add(transition.getLabel().getSignal());            
            
            return result;
        }
    }
    
    protected static Collector<Transition, Set<Integer>> getStructuralConflictSignals() {
        return new StructuralConflictSignals<Transition, Set<Integer>>();
    }
   
    protected static class StructuralConflictSignals<T,R> implements Collector<Transition, Set<Integer>> {
        public Set<Integer> operation(Transition transition) {
            Set<Integer> result = new HashSet<Integer>();
            
            for (Node parent : transition.getParents())
                for (Node conflict : parent.getChildren())
                    if (conflict != transition)
                        result.add( ((Transition)conflict).getLabel().getSignal() );            
            
            return result;
        }
    }
    

    protected static class Marking<T,R> implements Collector<Place, Integer> {
    	public Integer operation(Place place) {
    		return new Integer(place.getMarking());
    	}
    }
    protected static class NewAutoConflictPair<T,R> implements Collector<Transition, List<Integer>> {
//    TODO handle dummy auto conflicts, deeper backtracking is needed!
    	public List<Integer> operation(Transition transition) {
    		List<Set<Node>> neighbours = STGOperations.collectFromCollection(
    		    transition.getParents(), 
    		    ConditionFactory.ALL_NODES, 
    		    CollectorFactory.getChildrenCollector());
    		
    		List<Node> 	n=new LinkedList<Node>(); 
    		
    		for (Set<Node> n1 : neighbours)
    			n.addAll(n1);
    		
    		
    		@SuppressWarnings("unchecked") //it works but with generic types it will not compile
    		List<Node> n3 = STGOperations.getElements(n, new NotCondition(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)));
    		
    		
    		Set<Integer> s = new HashSet<Integer>();
    		
    		for (Node t : n3)
    			s.add(((Transition)t).getLabel().getSignal());
    		s.remove(transition.getLabel().getSignal());
    		
    		List<Set<Node>> gChildren = STGOperations.collectFromCollection(
    		    transition.getChildren(), 
    		    ConditionFactory.ALL_NODES, 
    		    CollectorFactory.getChildrenCollector());
    		
    		List<Node> 	n2=new LinkedList<Node>(); 
    		
    		List<Integer> result = new LinkedList<Integer>();
    		
    		for (Set<Node> n1 : gChildren)
    			n2.addAll(n1);
    		for (Node t : n2)
    			if ( s.contains( ((Transition)t).getLabel().getSignal()) ) 
    			    result.add(((Transition)t).getLabel().getSignal());
    				
    		return result;
    	}

    }

    
    
    
     protected static class Parents<T,R> implements Collector<Node,Set<Node>> {
    	public Set<Node> operation(Node node) {
    		return node.getParents();
    	}

    	
    	
    }
    
    protected static class SignalCollector<T, R> implements Collector<Transition, Integer> {
    	public Integer operation(Transition transition) {
    		return transition.getLabel().getSignal();
    	}
    }
    protected static class ToString<T,R> implements Collector<T,String>{
    	public String operation(T o) {
    		return o.toString();
    	}
    }
    protected static class SyntacticalTriggerOf<T,R> implements Collector<Transition, Set<Integer>> {
    	public Set<Integer> operation(Transition transition) {
    		
    		Set<Node> parents = transition.getParents();
    		List<Set<Node>> trigger = STGOperations.collectFromCollection(parents, ConditionFactory.ALL_NODES, CollectorFactory.getParentsCollector() );
    		
    		
    		Set<Integer> result = new HashSet<Integer>();
    		//result.add(transition.getLabel().getSignal());
    		
    		for (Set<Node> trans : trigger )
    			for (Node tr : trans)
    			result.add(((Transition)tr).getLabel().getSignal()   );
    		
    		return result;
    		
    	}
    }
    /**
     * finds all signal triggers according to output-determinacy
     * @author mark
     *
     * @param <T>
     * @param <R>
     */
    protected static class SignalTriggerOf<T,R> implements Collector<Transition, Set<Integer>> {
    	public Set<Integer> operation(Transition transition) {
    		
    		STG stg = transition.getSTG();
    		
    		Set<Node> parents = transition.getParents();
    		Set<Transition> seen = new HashSet<Transition>();
    		Queue<Transition> queue = new LinkedList<Transition>();
    
    		List<Set<Node>> trigger = STGOperations.collectFromCollection(
    				parents, ConditionFactory.ALL_NODES, CollectorFactory.getParentsCollector() );
    		
    		
    		Set<Integer> result = new HashSet<Integer>();
    		//result.add(transition.getLabel().getSignal());
    		
    		for (Set<Node> trans : trigger )
    			for (Node tr : trans)
    				queue.add((Transition)tr);
    		
    		
    		while (!queue.isEmpty()) {
    			Transition curTransition = queue.poll();
    			if (seen.add(curTransition)) {
    				Integer signal = curTransition.getLabel().getSignal();
					if (stg.getSignature(signal) == Signature.DUMMY) {
						parents = curTransition.getParents();
						List<Set<Node>> curTrigger = STGOperations.collectFromCollection(
			    				parents, ConditionFactory.ALL_NODES, CollectorFactory.getParentsCollector() );
			    		
			    		for (Set<Node> trans : curTrigger )
			    			for (Node tr : trans)
			    				queue.add((Transition)tr);		
    				}
    				else {
    					result.add(signal);
    				}
    			}
    		}
    		
    		return result;
    		
    	}
    }
    private static Children<Node,List<Node>> childrenCollector = null;
    
    private static Collector<Place,Integer> markingCollector = null;
    private static Collector<Node,Set<Node>> parentsCollector = null;
    private static Collector<Transition, Integer> signalCollector = null;
    private static ToString<Object, String> stringCollector = null;
    private static Collector<Transition,List<Integer>> triggerSignal = null;
    
    public static Children<Node,List<Node>> getChildrenCollector() {
        if (childrenCollector != null) return childrenCollector;
        
        childrenCollector = new Children<Node,List<Node>>();
        
        return childrenCollector;
    }


    
    /**
     * Returns signals which are in conflict with the one provided to the
     * constructor.
     * 
     * @author mark
     *
     */
    public static Collector<Transition, List<Integer>> getConflictSignalCollector () {
    	
    	if (CLW.instance.OD.isEnabled()) {
    		return new WeakConflictSignalOf<Transition, List<Integer>>(); 
        }
        else {
        	return new ConflictSignalOf<Transition, List<Integer>>();
        }
    }
    
    
    
    
    /**
     * Returns for every object an Integer constant provided to the constructor
     * 
     * @author mark
     *
     */
    public static <O> Collector<O, Integer> getConstantCollector(int res) {
        return new Constant<O, Integer>(res);
    }
    

    
  
    
    
    /**
     * Static Collector which returns the element itself.
     * 
     * @author mark
     *
     * */
    public static <T> Collector<T,T> getIdentityCollector() {
        return new Identity<T>();
    }

    public static Collector<Place,Integer> getMarkingCollector() {
        if (markingCollector != null) return markingCollector;
        
        markingCollector = new Marking<Place,Integer>();
        
        return markingCollector;
    }
    
    
    /**
     * Returns the signal name for which an auto-conflict occurs when contracting
     * a transition. See {@link stg.condition.NewAutoConflictPair}
     * 
     * <p><b>History:</b><br>
     * 20.12.2004: Generated<br>
     * 
     * <p>
     * @version 20.12.2004
     * @since 20.12.2004
     * @author Mark Sch�fer
     */
    public static Collector<Transition, List<Integer>> getNewAutoConflictPairCollector() {
        return new NewAutoConflictPair<Transition, List<Integer>>();
    }
    
  
    
    
    public static Collector<Node,Set<Node>> getParentsCollector() {
        if (parentsCollector != null) return parentsCollector;
        
        parentsCollector = new Parents<Node,Set<Node>>();
        
        return parentsCollector;
    }
    
    
    public static Collector<Transition, Integer> getSignalCollector() {
        if (signalCollector != null) return signalCollector;
        
        signalCollector = new SignalCollector<Transition,Integer>();
        return signalCollector;
    }
    
    public static ToString<Object,String> getStringCollector() {
        if (stringCollector != null) return stringCollector;
        
        stringCollector = new ToString<Object,String>();
        
        return stringCollector;
    }
    
    
    /**
     * Returns the string representation of an object
     * 
     * @author mark
     *
      */
    public static Collector<Object, String> getToStringCollector() {
        return new ToString<Object,String>();
        
    }
    
    
    
    @SuppressWarnings("unchecked")
	public static Collector<Transition,List<Integer>> getTriggerSignal() {
        if (triggerSignal != null) return triggerSignal;
        
        if (CLW.instance.OD.isEnabled()) {
        	triggerSignal = (Collector)new SignalTriggerOf<Transition,List<Integer>>(); 
        }
        else {
        	triggerSignal = (Collector)new SyntacticalTriggerOf<Transition,List<Integer>>();
        }
        return triggerSignal;
    }
    
    /**
     * Returns all trigger signals of a transition, i.e. all signals which are associated
     * to the parents of the parents of the transition. If the transition is a loop-transition
     * its own signal will also be returned. //check is that sensible?
     * 
     * @author mark
     *
     */
    public static Collector<Transition, Set<Integer>> getTriggerSignalCollector() {
        return new SyntacticalTriggerOf<Transition, Set<Integer>>();
    }



	public static Collector<Transition, Integer> getSignalNameCollector() {
		return new SignalName<Transition,Integer>();
	}
    
	
	protected static class SignalName<T,R> implements Collector<Transition, Integer> {
    	public Integer operation(Transition transition) {
    		return transition.getLabel().getSignal();
    			
    	}

    }



	public static Collector<Place, Collection<Integer>>  getAutoConflictCollector() {
		return new AutoConflict<Place, Collection<Integer>>();
	}
	
	
	protected static class AutoConflict<T,R> implements Collector<Place, Collection<Integer>> {

		public Collection<Integer> operation(Place o) {
			Collection<Integer> once = new HashSet<Integer>();
			Collection<Integer> twice = new HashSet<Integer>();
			
			for (Node node : o.getChildren()) {
				Integer sig = ((Transition)node).getLabel().getSignal();

				if (! once.add(sig)) 
					twice.add(sig);
			}
			
			return twice;
			
		}
		
	}
	
	
}
