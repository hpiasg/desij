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

/*
 * Created on 04.10.2004
 *
 */
package net.strongdesign.desij.decomposition;

import net.strongdesign.stg.*;
import java.util.*;


import net.strongdesign.stg.traversal.*;

/**
 * Chooses a set of transitions with common signals for contraction.
 * The signal is not determined.
 * 
 * @author Mark Schaefer and Dominic Wist
 *
 */
public class AnyCompleteSignal implements ChooseTransitionSet {

	public List<Transition> getTransitions(STG stg) {
		boolean found=false;		
		List<Transition> result=new LinkedList<Transition>();
		Condition<Transition> dummy = ConditionFactory.getSignatureOfCondition(Signature.DUMMY);
		Condition<Transition> signal=null;
		
		for (Transition transition : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
		    if (found) {
		    	if (signal.fulfilled(transition))
		    		result.add(transition);
		    }
		    
		    if (!found && dummy.fulfilled(transition)) {
		        found = true;
		        result.add(transition);
		        signal = ConditionFactory.getSignalOfCondition(transition.getLabel().getSignal());
		    }
		        
		}
		
		return result;		
	}
	
	public List<Transition> getCompleteTransitionSet(STG stg, List<Integer> handledSignals) {
		boolean found=false;		
		List<Transition> result=new LinkedList<Transition>();
		Condition<Transition> dummy = ConditionFactory.getSignatureOfCondition(Signature.DUMMY);
		Condition<Transition> signal=null;
		Integer signalID;
		
		for (Transition transition : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
		    if (found) {
		    	if (signal.fulfilled(transition))
		    		result.add(transition);
		    }
		    
		    if (!found && dummy.fulfilled(transition)) {
		    	signalID = transition.getLabel().getSignal();
		    	if (handledSignals.contains(signalID)) // already tried to contract during Lazy-OutDet Deco
		    		continue;
		    	
		        found = true;
		        result.add(transition);
		        signal = ConditionFactory.getSignalOfCondition(signalID);
		    }
		        
		}
		
		return result;
	}
	
}
