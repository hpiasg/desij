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
