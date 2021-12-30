

package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.Collection;
import java.util.Map;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

/**
 * @author dwist
 * Concrete Implementation of concurrent placeholder insertion
 */
class ConcurrentPlaceHolderInsertion extends PlaceHolderInsertion {

	//	 ******************* public area *******************************************
	
	/**
	 * @param stg
	 * @param components
	 * @param nodeInfos
	 */
	public ConcurrentPlaceHolderInsertion(STG stg, Collection<STG> components,
			Map<Node, NodeProperty> nodeInfos) {
		super(stg, components, nodeInfos);
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderInsertion#execute(net.strongdesign.stg.Transition, net.strongdesign.stg.Transition)
	 * Do not call before initializeTraversal!
	 */
	@Override
	public boolean execute(Transition t_en, Transition t_ex)
			throws STGException {
		boolean status = false;
		if (!initialized) {
			return status;
		}
		// ATTENTION!!!! with undo_markings - ask for good handling!
		//this.stg.addUndoMarker(BEFORE_INSERTIONS);
		myTraversalResult.prePlaceHolder.add(t_en); // connect t_en and placeholder
		additionalNodeInfos.get(t_en).isVisited = true;
		for (Node place: t_en.getChildren()) {
			myTraversalResult.addUndoMarker();
			if (forward_p((Place)place)) {
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
	
	private boolean forward_p(Place place) {
		if (additionalNodeInfos.get(place).isVisited)
			return true; // to avoid cycles
		
		additionalNodeInfos.get(place).isVisited = true;
		
		if (place.getMarking() > 0)
			myTraversalResult.marking += place.getMarking(); // if a marked place is visited then mark pD
		
		if (place.getParents().size() > 1) { // reverse conflict
			for (Node t: place.getParents()) {
				if (!backward_t((Transition)t)) {
					additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
					return false;
				}
			}
		}
		
		for (Node t: place.getChildren()) {
			if (!forward_t((Transition)t)) {
				additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
				return false;
			}
		}
		additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
		return true;
			
	}
	
	private boolean backward_p(Place place) {
		if (additionalNodeInfos.get(place).isVisited)
			return true; // to avoid cycles
		
		additionalNodeInfos.get(place).isVisited = true;
		
		if (place.getMarking() > 0)
			myTraversalResult.marking += place.getMarking(); // if a marked place is visited then mark pD
		
		
		if (place.getChildren().size() > 1) { // conflict
			for (Node t: place.getChildren()) {
				if (!forward_t((Transition)t)) {
					additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
					return false;
				}
			}
		}
		
		for (Node t: place.getParents()) { 
			if (!backward_t((Transition)t)) {
				additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
				return false;
			}
		}
		
		additionalNodeInfos.get(place).isVisited = false; // tidy up after recursion
		return true;
	}
	
	private boolean forward_t(Transition t) {
		if (additionalNodeInfos.get(t).isEntryTransition)
			return false; // visiting the entry transition a second time
		if (additionalNodeInfos.get(t).isExitTransition &&
			( stg.getSignature(t.getLabel().getSignal()) == Signature.INPUT ) 
			)
			return false; // there is no output between entry and exit transition
		
		// due to the backward traversals needed, e.g. when the entry transition moves upwards
		if (myTraversalResult.prePlaceHolder.contains(t)) { // an output is later on the path
			myTraversalResult.prePlaceHolder.remove(t);
			return true; // because a correct connected output will follow
		}
		
		if (additionalNodeInfos.get(t).isVisited) 
			return true; // to avoid cycles
				
		additionalNodeInfos.get(t).isVisited = true;
		
		if ( stg.getSignature(t.getLabel().getSignal()) != Signature.INPUT ) {
			if (myTraversalResult.postPlaceHolder.contains(t)) { 
				additionalNodeInfos.get(t).isVisited = false; // reset Visited-flag
				return true; // t and placeHolder are already connected in the right way
			}
			
			// no proof for a path directing to the exit transition or sth. else is needed
			
			myTraversalResult.postPlaceHolder.add(t); // connect placeholder and t
			additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
			return true;
		}
		
		// no backward traversal is needed!
		
		for (Node place: t.getChildren()) {
			myTraversalResult.addUndoMarker();
			if (forward_p((Place)place)) {
				additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
				return true;
			}
			else myTraversalResult.undoToLastMarker();
		}
			
		additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
		return false;
	}
	
	private boolean backward_t(Transition t) {
		if (additionalNodeInfos.get(t).isEntryTransition)
			return true; // Ok, relevant signal
		if (additionalNodeInfos.get(t).isExitTransition) {
			return false; // go not into the core of the CSC conflict
		}
				
		// due to the forward traversals needed, e.g. when the output transition moves downwards
		if (myTraversalResult.postPlaceHolder.contains(t)) { // an input is later on the path
			myTraversalResult.postPlaceHolder.remove(t); // observe that t must not be an input
			return true; // because a correct connected input will follow
		}
		
		if (additionalNodeInfos.get(t).isVisited) {
			return true; // to avoid cycles
		}
				
		additionalNodeInfos.get(t).isVisited = true;
		
		if (additionalNodeInfos.get(t).isRelevant) {
			if (myTraversalResult.prePlaceHolder.contains(t)) { 
				additionalNodeInfos.get(t).isVisited = false; // reset Visited-flag
				return true; // t and placeHolder are already connected in the right way
			}
			
			// no proof for a path directing to the entry transition or sth. else is needed
									
			myTraversalResult.prePlaceHolder.add(t); // connect t and placeholder
			additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
			return true;
		}
		
		// no forward traversal needed!
		
		for (Node place: t.getParents()) {
			myTraversalResult.addUndoMarker();
			if (backward_p((Place)place)) {
				additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
				return true;
			}
			else myTraversalResult.undoToLastMarker();
		}
		
		additionalNodeInfos.get(t).isVisited = false; // tidy up after recursion
		return false;
	}

}
