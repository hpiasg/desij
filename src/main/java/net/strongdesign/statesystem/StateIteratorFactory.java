

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
