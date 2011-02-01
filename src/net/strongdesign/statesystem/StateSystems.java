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

package net.strongdesign.statesystem;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.stg.Marking;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.STGAdapterFactory.StateSystemAdapter;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.Pair;
import net.strongdesign.util.Pointer;

public abstract class StateSystems {
	
	private StateSystems() {}

	/**
	 * 
	 * Used for @link StateSystems#inverseProjection(StateSystem, List, EventCondition)inverseprojection and
	 * @link StateSystems#inverseProjections(StateSystem, List, EventCondition) to define valid additional signals.
	 *
	 * @param <Event>
	 */
	public static interface EventCondition<Event> {
		public boolean fulfilled(Event event);
	}
	
	
	public static class BasicEventCondition<Event> implements EventCondition<Event> {
		private Set<Event> properEvents;
		
		public BasicEventCondition(Collection<Event> properEvents) {
			this.properEvents = new HashSet<Event>(properEvents);
		}
		
		public boolean fulfilled(Event event) {
			return properEvents.contains(event);
		}
	}
	
	
	
	
	/**
	 * Calculates a single inverse projection of the given trace in the given system using only events for which
	 * condition is fulfilled.
	 * 
	 * <p>An inverse projection of
	 * trace = a, d, e, f, e, a could be a, x, d, y, e, x, z, f, e, a<br>
	 * An inverse projection always ends with the last event of trace. 
	 * 
	 * 
	 * @param <State>
	 * @param <Event>
	 * @param system
	 * @param trace
	 * @param condition
	 * @return
	 * @throws StateSystemException
	 */
	public static <State,Event> List<Event> inverseProjection (
			StateSystem<State, Event> system, 
			List<Event> trace,
			EventCondition<Event> condition) 
	throws StateSystemException  {
		//the resulting projection
		List<Event> invProjection = new LinkedList<Event>();
		
		//current state of the system 
		State currentState = system.getInitialState();	
		
		
		//for every event of trace
		for (Event event : trace) {
			
			//if this event is possible add it to the trace update current state, i.e. perform it in system and proceed
			if (system.getEvents(currentState).contains(event)) {
				invProjection.add(event);
				currentState = system.getNextStates(currentState, event).iterator().next();
			}
			
			//otherwise, find shortest sequence of events fulfilling condition which enables event
			else {
				//using bfs for finding shortest path
				//queue elements are states and the events by which they were reached 
				//enhanced with backward pointers for restoring the path
				Queue<Pointer<Pair<State,Event>>> bfsQueue = new LinkedList<Pointer<Pair<State,Event>>>();
				bfsQueue.add(Pointer.getPointer(Pair.getPair(currentState, (Event) null), null));
				Set<State> seen = new HashSet<State>();
				//if we reach a state by event it is stored within finalState
				//finalState equaling null after the bfs loop indicates an error
				Pointer<Pair<State,Event>> finalState = null;
				//IMPLEMENT more efficiently
				bfs: while (! bfsQueue.isEmpty()) {
					
					//get the current state and activated events
					Pointer<Pair<State,Event>> curPointer = bfsQueue.poll();
					
					//if the current state was already encountered, it was on path which
					//is not longer than the current one, we can therefore drop this queue entry
					State curState = curPointer.value.a;
					if (seen.contains(curState)) {
						continue bfs;
					}
					
					seen.add(curState);
					
					Set<Event> events = system.getEvents(curState);
					
					//have we finished?
					
					//jupp, leave bfs
					if (events.contains(event)) {
						finalState = Pointer.getPointer(
										Pair.getPair(
												system.getNextStates(curState, event).iterator().next(), 
												event), 
										curPointer);
						break bfs;						
					}
					
					//nope, procees with bfs
					for (Event e : events) {
						if (condition.fulfilled(e)) {
							bfsQueue.add(	Pointer.getPointer(
												Pair.getPair(
													system.getNextStates(curState, e).iterator().next(), 
													e),
												curPointer));
						}						
					}
				}
				
				//no path found
				if (finalState == null) {
					STG stg = ((StateSystemAdapter<Marking, SignalEdge>)system).getSTG();
					System.out.println(condition);
					
					try {
						FileSupport.saveToDisk(STGFile.convertToG(stg, false), "/home/mark/stg.g");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					throw new StateSystemException(
							"Could not generate inverse projection: could not activate event " + event + 
							" of trace " + trace );
				}
				
				
				//add the events on the path to invProjection from backwards by following
				//the upward pointers
				
				currentState = finalState.value.a;
				ListIterator<Event> itInvProjection = invProjection.listIterator();
				while (itInvProjection.hasNext())
					itInvProjection.next();
				
				while (finalState.value.b != null) {
					itInvProjection.add(finalState.value.b);
					itInvProjection.previous();
					finalState = finalState.pointer;
				}
			}
		}
		
		return invProjection;		
	}

	/**
	 * <b>Not implemendted yet</b>. Calculates all minimal inverse projections of the given trace and returns them as @link StateSystem.
	 * For more details @see #inverseProjection(StateSystem, List, EventCondition). 
	 * @param <State>
	 * @param <Event>
	 * @param system
	 * @param trace
	 * @param condition
	 * @return
	 * @throws StateSystemException
	 * 
	 * @todo Implement this method.
	 * @see #inverseProjection(StateSystem, List)
	 */
	public static <State,Event> StateSystem<State,Event> inverseProjections (			
			StateSystem<State, Event> system, 
			List<Event> trace,
			EventCondition<Event> condition) 
	throws StateSystemException  {
		return emptyStateSystem();
	}


	/**
	 * Returns an empty state system, whose initial state is null and next states and events are always the empty set.
	 * @param <State>
	 * @param <Event>
	 * @return
	 */
	public static <State,Event> StateSystem<State,Event> emptyStateSystem() {
		return new StateSystem<State,Event>() {

			public State getInitialState() {
				return null;
			}

			public Set<Event> getEvents(State state) {
				return Collections.emptySet();
			}

			public Set<State> getNextStates(State state, Event event) {
				return Collections.emptySet();
			}
		};
	}
	
	
}
