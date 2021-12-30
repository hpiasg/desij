

package net.strongdesign.statesystem;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class represents a finite (non-deterministic) automaton. The automaton does not care about 
 * lambda-labelled transitions. If a special treatment is desired it has to be done externally.
 * If <Event> is of type {@link java.lang.String}, it is suggested to use the empty String "" a 
 * representation for lambda.
 * 
 * <p>The user of this class must take care that the methods {@link java.lang.Object#hashCode()} and
 * {@link java.lang.Object#equals(java.lang.Object)} are overwritten properly.
 *  
 * @param <State> Type parameter for the states of the automaton.
 * @param <Event> Type parameter for the transition labeles of the automaton.
 */
public class Automaton<State, Event> implements StateSystem<State, Event> {

	/**
	 * The internal represenation of the autmaton.
	 */
	private Map<State, Map<Event, Set<State>>> automaton;
		
	/**
	 * The current state of the automaton.
	 */
	private State initialState;
	
	/**
	 * Constructs an empty automaton.
	 *
	 */
	public Automaton() {
		automaton = new LinkedHashMap<State, Map<Event, Set<State>>>();
		initialState = null;
	}
	
	
	/**
	 * Adds a new state without incident acrs to the automaton, provided this state was not added before; in this case
	 * nothing happens.
	 * @param state The new state.
	 */
	public void addState(State state) {
		automaton.put(state, new LinkedHashMap<Event, Set<State>>());
	}
	
	
	/**
	 * Adds an arc, transition, connection whatever between two states which have to be added before; if this
	 * is not the case this action is silently ignored. 
	 * 
	 * TODO change the 'silently'
	 * @param source Source state of the arc.
	 * @param event Labelling of the arc.
	 * @param target Traget state of the arc.
	 */
	public void addArc(State source, Event event, State target) {
		if (! (automaton.keySet().contains(source) && automaton.keySet().contains(target)) ) return;
		
		Map<Event, Set<State>> os = automaton.get(source);		
		
		Set<State> targets = os.get(event);
		if (targets==null) {
			targets = new LinkedHashSet<State>();
			os.put(event, targets);
		}
		
		targets.add(target);
	}
	
	/** Returns a string representation of the automaton.*/
	public String toString() {
		return automaton.toString();
	}
	
	/**
	 * Returns the initial state.
	 */
	public State getInitialState() {
		return initialState;
	}



	/**
	 * Get the activated events of a specific state.
	 */
	public Set<Event> getEvents(State state) {
		Set<Event> result = automaton.get(state).keySet();
		if (result == null)
			result = new HashSet<Event>();
		return result;
	}

	/**
	 * Returns all next states if in a given state an event is fired. Since the automaton may be
	 * non-deterministic, the returned set may contain more than one element.
	 */
	public Set<State> getNextStates(State state, Event event) {
		Set<State> result = automaton.get(state).get(event);
		if (result == null)
			result = new HashSet<State>();
		return result;
		
	}


	public void setInitialState(State state) {
		initialState = state;
	}

}
