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

package net.strongdesign.statesystem.decorator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;

/**
 * A statesystem adapter/decorator transforming any given @link net.strongdesign.statesystem.StateSystem
 * into an isomorphic one using integers as state type.
 * 
 * @param <State> IMPORTANT! The state type of the decorated statesystem
 * @param <Event> The events used by the statesystems
 */
public class StateEnumerator<State, Event> implements StateSystem<Integer, Event> {
	/** The decorated system. */
	private final StateSystem<State, Event> system;
	
	/**Mapping from new states to the ones of @link #system*/
	protected Map<Integer, State> newToOld;
	
	/**Mapping from the states of @link #system to the new ones*/
	protected Map<State, Integer> oldToNew;
	
	/**The next number assigned to a formerly unknown state of @link #system.*/
	protected int newStateNumber = 1;
	
	/**The current state of the system.*/
	protected Integer currentState;	
	
	/**
	 * Constructs a new instance from another statesystem.
	 */
	public StateEnumerator(StateSystem<State, Event> system) {
		this.system = system;
		
		currentState = 0;
		
		newToOld = new HashMap<Integer, State>();
		newToOld.put(currentState, system.getInitialState());
		
		oldToNew = new HashMap<State, Integer>();
		oldToNew.put(system.getInitialState(), currentState);
	}
	
	
	public State getOriginalState(Integer i) {
		if (! newToOld.containsKey(i))
			return null;
		
		return newToOld.get(i);
	}
	
	/**
	 * Returns the current state 
	 */
	public Integer getInitialState() {
		return currentState;
	}
	
	/**
	 * Sets the current state.
	 * @param state
	 */
	public boolean setCurrentState(Integer state) {
		if (! newToOld.containsKey(state))
			return false;
		
		currentState = state;
		return true;
	}

	/**
	 * Gets the events activated in the given state.
	 * @param state
	 * @return
	 */
	public Set<Event> getEvents(Integer state) {
		return system.getEvents(newToOld.get(state));
	}
	
	/**
	 * Returns the states reached by activating an event in a given state, see @link StateSystem#getNextStates(State, Event)
	 * @param state
	 * @param event
	 * @return
	 */
	public Set<Integer> getNextStates(Integer state, Event event) {
		Set<State> newStates = system.getNextStates(newToOld.get(state), event);
		
		Set<Integer> result = new HashSet<Integer>();
		
		for (State ns : newStates) {
			Integer ins = oldToNew.get(ns);
			if (ins == null) {
				Integer nm = newStateNumber++;
				oldToNew.put(ns, nm);
				newToOld.put(nm, ns);
				result.add(nm);				
			}
			else
				result.add(ins);
		}
		
		return result;
		
	}
}
