/**
 * 
 */
package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.Map;
import java.util.Collection;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

/**
 * @author Dominic Wist
 * Concrete Implementation of sequential placeholder insertion
 */
class SequentialPlaceHolderInsertion extends PlaceHolderInsertion {
	
	// ******************* public area *******************************************
	
	/**
	 * @param stg
	 * @param components
	 * @param nodeInfos
	 */
	public SequentialPlaceHolderInsertion(STG stg, Collection<STG> components, 
			Map<Node,NodeProperty> nodeInfos) {
		super(stg, components, nodeInfos);
	}
	
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderInsertion#execute(net.strongdesign.stg.Transition, net.strongdesign.stg.Transition)
	 * Do not call before initializeTraversal!
	 */
	@Override
	public boolean execute(Transition t_en, Transition t_ex) throws STGException {
		boolean status = false;
		if (!initialized) {
			return status;
		}
		for (Node place: t_en.getChildren()) {
			myTraversalResult.addUndoMarker();
			if (forward_p((Place)place, false, t_en, null, false)) {
				status = true;
				break;
			}
			else myTraversalResult.undoToLastMarker();
		}
		initialized = false;
		if (!status)
			return false;
		// clean up myTraversalResult from undoMarkings
		myTraversalResult.removeAllUndoMarkers();
		// insert placeholder according to myTraversalResult
		// doPlaceHolderInsertion(myTraversalResult, tTransInsertionCount++);
		//this.stg.undoToMarker(BEFORE_INSERTIONS);
		return status;
	}
	
	
	// ********************* private area ***************************************
	
	private boolean forward_p(Place place, boolean outFound, 
			Transition t_pre, Place p_pre, boolean insertionStarted) {
		boolean starterFlag = false;
		
		if ((!outFound && additionalNodeInfos.get(place).isVisited) ||
				(outFound && additionalNodeInfos.get(place).isVisitedBy2ndSearch))
			return true; // to avoid cycles
		if (outFound) additionalNodeInfos.get(place).isVisitedBy2ndSearch = true;
		else additionalNodeInfos.get(place).isVisited = true;
		
		if (insertionStarted && !outFound && (place.getMarking() > 0))
			myTraversalResult.marking += place.getMarking(); // if a marked place is visited then mark pD
		
		if (!insertionStarted && !outFound) {
			// look ahead for an output which delays the firing of the exit transition!
			for (Node t: place.getChildren())
				if (lookAhead((Transition)t)) {
					myTraversalResult.prePlaceHolder.add(t_pre);
					if (place.getMarking() > 0)
						myTraversalResult.marking += place.getMarking();
					starterFlag = true;
					break;
				}
		}
		
		for (Node t: place.getChildren()) 
			if (!forward_t((Transition)t, outFound, t_pre, place, (insertionStarted || starterFlag))) {
				additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
				additionalNodeInfos.get(place).isVisitedBy2ndSearch = false;
				return false;
			}
		additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
		additionalNodeInfos.get(place).isVisitedBy2ndSearch = false;
		return true;
	}
	
	private boolean forward_t(Transition t, boolean outFound, 
			Transition t_pre, Place p_pre, boolean insertionStarted) {
		if (additionalNodeInfos.get(t).isEntryTransition)
			return false; // visiting the entry transition a second time
		if (outFound && additionalNodeInfos.get(t).isExitTransition)
			return true; // the path leads to the exit transition
		if (!outFound && additionalNodeInfos.get(t).isExitTransition &&
				( stg.getSignature(t.getLabel().getSignal()) == Signature.INPUT )
			)
			return false; // there is no output between entry and exit transition
			
		if ((!outFound && additionalNodeInfos.get(t).isVisited) || 
			(outFound && additionalNodeInfos.get(t).isVisitedBy2ndSearch)) {
			return true; // to avoid cycles
		}
		
		if (outFound) additionalNodeInfos.get(t).isVisitedBy2ndSearch = true;
		else additionalNodeInfos.get(t).isVisited = true;
		
		if (!outFound && insertionStarted &&
				( stg.getSignature(t.getLabel().getSignal()) != Signature.INPUT ) 
			) {
			// insertion is already started
			if (myTraversalResult.postPlaceHolder.contains(t)) {
				additionalNodeInfos.get(t).isVisited = false; // tidy up for next resursive search
				return true; // t and placeHolder are already connected in the right way
			}
			
			
			if (p_pre != null) { // actually, p_pre must not be null!!!
				for (Node trans: p_pre.getParents()) // no real backward traversal is needed
					if ((trans != t_pre) && (!myTraversalResult.prePlaceHolder.contains(trans)))
						myTraversalResult.prePlaceHolder.add((Transition)trans); // connect trans and placeholder
			}
			myTraversalResult.postPlaceHolder.add(t); // connect placeholder and t
			additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
			return true;
		}
		
		if (insertionStarted)
		{
			for (Node place: t.getChildren()) {
				myTraversalResult.addUndoMarker();
				if (forward_p((Place)place, outFound, t, null, true)) {
					additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
					additionalNodeInfos.get(t).isVisitedBy2ndSearch = false;
					return true;
				}
				else myTraversalResult.undoToLastMarker();
			}
		}
		else {
			for (Node place: t.getChildren())
				if (forward_p((Place)place, outFound, t, null, false)) {
					additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
					additionalNodeInfos.get(t).isVisitedBy2ndSearch = false;
					return true;
				}
		}
		additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
		additionalNodeInfos.get(t).isVisitedBy2ndSearch = false;
		return false;
	}
	
	private boolean lookAhead(Transition t) {
		if (additionalNodeInfos.get(t).isEntryTransition)
			return false; // visiting the entry transition a second time
		if (stg.getSignature(t.getLabel().getSignal()) != Signature.INPUT ) {
			if (additionalNodeInfos.get(t).isExitTransition) return true; // the path to the exit transition is the transition itself
			additionalNodeInfos.get(t).isVisitedBy2ndSearch = true; // to avoid loops
			boolean result = false;
			for (Node place: t.getChildren())
					if (forward_p((Place)place, true, t, null, false)) {
						result = true;
						break; // it is not necessary to loop again
					}
			additionalNodeInfos.get(t).isVisitedBy2ndSearch = false; // tidy up after recursion
			if (!result) // if all forward_p(...) calls returned false, i.e. the path do not lead to the exit transition
				return false;
			else 
				return true;
		}
		else return false;
	}

}
