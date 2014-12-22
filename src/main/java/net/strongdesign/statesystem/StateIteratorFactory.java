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

import java.util.*;

import net.strongdesign.statesystem.decorator.AbstractStateSystemDecorator;



/**
 * This class is a decorator which is iterable over all states reachable from the current state
 * of the {@link StateSystem}
 * 

 * @author Mark Sch�fer
 */
public class StateIteratorFactory<State,Event> extends AbstractStateSystemDecorator<State,Event> implements Iterable<State> {
    public StateIteratorFactory(StateSystem<State,Event> system) {
        super(system);
    }
    
    public Iterator<State> iterator() {
        return new StateIterator<State>(system);
    }
    
    
    protected class StateIterator<S> implements Iterator<State> {
        protected StateSystem<State,Event> system;
        protected Set<State> visitedStates;
        protected Queue<State> actualStates;
        
        public StateIterator(StateSystem<State,Event> system) {
            this.system = system;
            visitedStates = new HashSet<State>();
            actualStates = new LinkedList<State>();
            actualStates.offer(system.getInitialState());
            
        }
        
        public boolean hasNext() {
            return !actualStates.isEmpty();
        }
        
        public State next() {
            State nextState = actualStates.peek();
            
            for (Event event : system.getEvents(nextState)) 
                for (State state : system.getNextStates(nextState, event))
                    if (!visitedStates.contains(state) && !actualStates.contains(state))
                        actualStates.offer(state);
                
            actualStates.poll();
            visitedStates.add(nextState);
            return nextState;
        }
        
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
    }    
}
