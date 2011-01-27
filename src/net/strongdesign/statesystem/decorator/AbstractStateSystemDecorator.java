
package net.strongdesign.statesystem.decorator;
import java.util.*;

import net.strongdesign.statesystem.StateSystem;

/**
 * An abstratct implementation of {@link StateSystem} which
 *  hands over each call to a method.
 * <p>
 * This is an abstract Decorator and a base for concrete realizations.
 * 
 * 
 * <p>
 * <b>History: </b> <br>
 * 25.01.2005: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�fer
 */
public abstract class AbstractStateSystemDecorator<State,Event> implements StateSystem<State, Event> {

    protected StateSystem<State, Event> system;
    
public AbstractStateSystemDecorator(StateSystem<State,Event> system) {
    this.system = system;
}
    
public State getInitialState() {
    return system.getInitialState();
}



public Set<Event> getEvents(State state) {
    return system.getEvents(state);
}

public Set<State> getNextStates(State state, Event event) {
    return system.getNextStates(state, event);
}


}
