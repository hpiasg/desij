package net.strongdesign.statesystem;

import java.util.Set;

/**
 * A StateSystem is something like an indeterministic automaton. There are states and transitions, events resp.  between them.
 * The nature of theese things is not specified, it may be any class which implements the coresponding
 * interfaces: {@link State}, {@link Event}
 * 
*/
public interface StateSystem<State,Event> {
    /** 
     * @return The current state of the system
     */
    public State getInitialState();
    

    /**
     * Retrieves the possible events for some state of the system
     * @param state The state in which the events occur
     * @return The possible events in state.  Implemenations must not return null. Instead an empty set should be returned.
     */
    public Set<Event> getEvents(State state);
    
    /**
     * Get the states which can be reached from a given state and event
     * @param state The original state
     * @param event The event which schall occurr
     * @return The set of reachable states. Implemenations must not return null. Instead an empty set should be returned.
     */
    public Set<State> getNextStates(State state, Event event);
    

}
