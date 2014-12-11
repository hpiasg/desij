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
