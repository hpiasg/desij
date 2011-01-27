
package net.strongdesign.stg;

import net.strongdesign.statesystem.*;
import net.strongdesign.stg.traversal.*;


import java.util.*;


/**
 * A Factory which constructs Objects which implment adapters between STGs and
 * other Interfaces
 * 
 * <p>
 * <b>History: </b> <br>
 * 13.02.2005: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�fer
 */
public abstract class STGAdapterFactory {
    private STGAdapterFactory() {}
    
    public static class StateSystemAdapter<State,Event> implements StateSystem<Marking, SignalEdge> {
        protected STG stg;
        
        
        public STG getSTG() {
        	return stg;
        }
        
        public StateSystemAdapter(STG stg) {
            this.stg = stg;
        }
        
        public Marking getInitialState () {
            return stg.getMarking();
        }
        
        public Set<SignalEdge> getEvents(Marking marking) {
            Marking oldMarking = stg.getMarking();
            
            stg.setMarking(marking);
            
            Set<SignalEdge> res = new HashSet<SignalEdge>();
            
            List<Transition> actTrans =  stg.getTransitions(ConditionFactory.ACTIVATED);
            
            for (Transition t : actTrans )
                res.add(t.getLabel() );
            
            
            stg.setMarking(oldMarking);
            
            return res;
        }
        
        public Set<Marking> getNextStates(Marking marking, SignalEdge event) {
            Set<Marking> res = new HashSet<Marking>();
            
            
            Marking oldMarking = stg.getMarking();
            stg.setMarking(marking);
            
            MultiCondition<Transition> cond = new MultiCondition<Transition>(MultiCondition.AND);
            cond.addCondition(ConditionFactory.getSignalEdgeOfCondition(event  ));
            cond.addCondition(ConditionFactory.ACTIVATED);
            
            java.util.List<Transition> rightTransitions = stg.getTransitions(cond);
            
            for (Transition t : rightTransitions) {
                t.fire();
                res.add(stg.getMarking());
                stg.setMarking(marking);
            }
            
            stg.setMarking(oldMarking);
            
            return res;
        }
    }
    
    
    
    public static StateSystem<Marking, SignalEdge> getStateSystemAdapter(STG stg) {
        return new StateSystemAdapter<Marking, SignalEdge>(stg);
    }
    
    
    
    
}
