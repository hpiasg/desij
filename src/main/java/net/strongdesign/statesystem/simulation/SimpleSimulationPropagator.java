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

package net.strongdesign.statesystem.simulation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;

/**
 * A simple simulation propagator for ordinary (bi)-simulation with lambda.
 * @param <StateA> States of the first system
 * @param <StateB> States of the second system
 * @param <Event> Common event type of the two systems.
 */
public class SimpleSimulationPropagator<StateA, StateB, Event> implements RelationPropagator<StateA, StateB, Event> {
	
	/**First statesysten */
	private StateSystem<StateA, Event> sysA;
	
	/**Second statesysten */
	private StateSystem<StateB, Event> sysB;
	
	/**The event considered as lambda, i.e. invisible */
	private Event lambda;
	
	/**True, if a bisimulation should be found, false for a simulation from sysA to sysB.*/
	private boolean isBisimulation;
	
	
	/**
	 * Constructs a new @link SimpleSimulationPropagator
	 * @param lambda The desired lambda labelling
	 * @param isBisimulation True, if a bisimulation is desired, false for a simulation from sysA to sysB.
	 */
	public SimpleSimulationPropagator(Event lambda, boolean isBisimulation) {
		this.lambda = lambda;
		this.isBisimulation = isBisimulation;
	}
	
	/**
	 * Sets the systems between which a relation should be found.
	 */
	public void setSystems(StateSystem<StateA, Event> sysA, StateSystem<StateB, Event> sysB) {
		this.sysA = sysA;
		this.sysB = sysB;
	}
	
	/**
	 * Returns the initial element of the desired relation, in this case the two starting states of the systems.
	 */
	public Set<RelationElement<StateA, StateB>> getStartRelation() {
		Set<RelationElement<StateA, StateB>> res = new HashSet<RelationElement<StateA, StateB>>();
		res.add( new RelationElement<StateA, StateB>(sysA.getInitialState(), sysB.getInitialState()) );
		return res;
	}
	
	
	/**
	 * Helper method for finding events which are activated after some internal signals.
	 * @param <State>
	 * @param <Event>
	 * @param sys The corresponding system
	 * @param state The starting state
	 * @param event The event which has tp perfomed eventually
	 * @param lambda The lambda labelling
	 * @return
	 */
	protected static <State,Event> Set<State> getLambda(StateSystem<State,Event> sys, State state, Event event, Event lambda) {
		Set<State> result = new HashSet<State>();
		Set<State> seen = new HashSet<State>();
		
		Queue<State> frontier = new LinkedList<State>();
		frontier.add(state);
		
		while (! frontier.isEmpty()) {
			State curState = frontier.poll();
			if (seen.contains(curState)) continue;
			seen.add(curState);
			
			result.addAll(sys.getNextStates(curState, event));
			frontier.addAll(sys.getNextStates(curState, lambda));
		}
		
		return result;
	}
	
	/**
	 * Propgates an element according to the semantic of (bi)simulation, see @link RelationPropagator.
	 */
	public List<Set<RelationElement<StateA, StateB>>> propagateElement(RelationElement<StateA, StateB> el)  {
		List<Set<RelationElement<StateA, StateB>>> result = new LinkedList<Set<RelationElement<StateA, StateB>>>();
		
		Set<Event> eventsA = sysA.getEvents(el.a);
		for (Event curEvent : eventsA) {			
			if (curEvent.equals(lambda)) {
				Set<StateA> nextStatesA = sysA.getNextStates(el.a, lambda);
				for (StateA ns : nextStatesA)
					result.add(RelationElement.getCrossProduct(ns, el.b));				
			}
			else {
				Set<StateA> nextStatesA = sysA.getNextStates(el.a, curEvent);			
				Set<StateB> nextStatesB = getLambda(sysB, el.b, curEvent, lambda);
				
				if (nextStatesB == null || nextStatesB.size()==0)
					return null;
				
				for (StateA ns : nextStatesA)
					result.add(RelationElement.getCrossProduct(ns, nextStatesB));
			}
		}
		
		if (! isBisimulation) return result;
		
		Set<Event> eventsB = sysB.getEvents(el.b);
		for (Event curEvent : eventsB) {
			if (curEvent.equals(lambda)) {
				Set<StateB> nextStatesB = sysB.getNextStates(el.b, lambda);
				for (StateB ns : nextStatesB)
					result.add(RelationElement.getCrossProduct(el.a, ns));				
			} 
			else {
				Set<StateB> nextStatesB = sysB.getNextStates(el.b, curEvent);			
				Set<StateA> nextStatesA = getLambda(sysA, el.a, curEvent, lambda);
				if (nextStatesA == null || nextStatesA.size()==0)
					return null;
				
				for (StateB ns : nextStatesB)
					result.add(RelationElement.getCrossProduct(nextStatesA, ns));
			}
		}
		
		
		
		
		return result;
	}
	
}
