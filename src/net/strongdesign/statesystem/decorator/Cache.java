package net.strongdesign.statesystem.decorator;

import java.util.*;

import net.strongdesign.statesystem.StateSystem;

/**
 * A Cache is a decorator for a {@link statesystem.StateSystem} which memorizes all
 * states, possible events and resulting states it has seen so far.<br>
 * If the results for some method call are already known they will be returned immediately and
 * must not be calculated again, if not they are calculated, returned and saved for later use.
 * 
 * <p>
 * <b>History: </b> <br>
 * 24.01.2005: Created <br>
 * 08.02.2005: Fixed an error in {@link #getEvents(State)}
 * <p>
 * 
 * @author Mark Sch�fer
 */
public class Cache<State,Event> implements StateSystem<State,Event>{
    protected Map<State, Map<Event, Set<State>>> nextStateCache;
    protected Map<State, Set<Event>> eventCache;
    protected StateSystem<State,Event> system;
    protected State currentState;
    
    
    public Cache (StateSystem<State,Event> system) {
        this.system = system;
        currentState = system.getInitialState();
        eventCache 		= new HashMap<State, Set<Event>>();
        nextStateCache 	= new HashMap<State, Map<Event, Set<State>>>();
    }
    

public State getInitialState() {
	return currentState;
}



public Set<Event> getEvents(State state) {
	Set<Event> posEvent = eventCache.get(state);
	
	if (posEvent == null) { 
	    posEvent = system.getEvents(state);
	    eventCache.put(state, posEvent);
	}
	
    return posEvent;	
}


public Set<State> getNextStates(State state, Event event) {
    Map<Event, Set<State>> nextStates = nextStateCache.get(state);
    
    if (nextStates==null) {
        nextStates = new HashMap<Event, Set<State>>();
        nextStateCache.put(state, nextStates);
    }
            
    Set<State> nextState = nextStates.get(event);
    
    if (nextState == null) {
        nextState = system.getNextStates(state, event);
        nextStates.put(event, nextState);
    }
    
    return nextState;
}

}
