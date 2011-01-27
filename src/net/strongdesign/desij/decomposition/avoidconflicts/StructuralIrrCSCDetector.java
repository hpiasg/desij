/**
 * 
 */
package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.SignalState;
import net.strongdesign.stg.SignalValue;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 *	
 * Identifies splittable entry and exit transition pairs in order to avoid 
 * irreducible CSC conflicts.
 * This process is only based on structural techniques, ie. identifying some 
 * graph structures in the critical component; these structures are indications, 
 * but not assurances for irreducible CSC conflicts
 */

public class StructuralIrrCSCDetector {
	
	private Set<List<Transition>> typeIConflicts;
	private Set<Pair<List<Transition>,List<Transition>>> typeIIConflicts;
	
	private STG stg;
	
	public StructuralIrrCSCDetector(STG stg) {
		this.stg = stg;
		
		typeIConflicts = new HashSet<List<Transition>>();
		typeIIConflicts = new HashSet<Pair<List<Transition>,List<Transition>>>();
	}
	
	public static Collection<Set<Pair<Transition,Transition>>> getAllSplittablePairs(
			STG criticalComponent) {
		Collection<Set<Pair<Transition,Transition>>> result = 
			new HashSet<Set<Pair<Transition,Transition>>>(); 
		
		// per potential conflict add a set of transition pairs to result
		StructuralIrrCSCDetector conflictDetector = 
			new StructuralIrrCSCDetector(criticalComponent);
		
		// fill this.typeIConflicts and this.typeIIConflicts
		conflictDetector.findTypeIConflicts();
		conflictDetector.findTypeIIConflicts();
		
		// extract entry and exit pairs per conflict
		Set<Pair<Transition,Transition>> conflictPairs;
		
		for (List<Transition> conflict : conflictDetector.typeIConflicts) {
			conflictPairs = new HashSet<Pair<Transition,Transition>>();
			for (int i = 0; i < conflict.size()-1; i++) {
				Transition entry = conflict.get(i);
				Transition exit = conflict.get(i+1);
				if ( isFirstSyntacticalTriggerOfSecond(entry, exit) )
					conflictPairs.add(new Pair<Transition, Transition>(entry, exit));
			}
			if (!conflictPairs.isEmpty())
				result.add(conflictPairs);
		}
				
		for (Pair<List<Transition>, List<Transition>> conflict : conflictDetector.typeIIConflicts) {
			conflictPairs = new HashSet<Pair<Transition,Transition>>();
			for (int i = 0; i < conflict.a.size()-1; i++) {
				Transition entry = conflict.a.get(i);
				Transition exit = conflict.a.get(i+1);
				if ( isFirstSyntacticalTriggerOfSecond(entry, exit) )
					conflictPairs.add(new Pair<Transition, Transition>(entry, exit));
			}
			
			for (int i = 0; i < conflict.b.size()-1; i++) {
				Transition entry = conflict.b.get(i);
				Transition exit = conflict.b.get(i+1);
				if ( isFirstSyntacticalTriggerOfSecond(entry, exit) )
					conflictPairs.add(new Pair<Transition, Transition>(entry, exit));
			}
			
			if (!conflictPairs.isEmpty())
				result.add(conflictPairs);
		}
		
		return result;
	}
	
	private static boolean isFirstSyntacticalTriggerOfSecond(Transition transitionA, Transition transitionB) {
		for (Node place : transitionA.getChildren()) {
			if (place.getChildren().contains(transitionB)) return true;
		}
		return false;
	}
	
	private void findTypeIIConflicts() {
		Assert.assertNotNull(stg);
		
		List<Place> allPlaces = new LinkedList<Place>(
				stg.getPlaces(ConditionFactory.ALL_PLACES));
						
		while (!allPlaces.isEmpty()) {
			Place p = allPlaces.remove(0); // first Element
			if (p.getChildren().size() > 1) {
				// Set<Node> processedNodes = new HashSet<Node>();
				// processedNodes.add(p);
				
				List<Node> postP = new ArrayList<Node>(p.getChildren());
				leftSide: for (int i = 0; i < postP.size()-1; i++)
					for (int j = i+1; j < postP.size(); j++) {
						Set<Pair<List<Transition>,List<Transition>>> resultingTransPairs = 
							new HashSet<Pair<List<Transition>,List<Transition>>>();
						
						List<SignalState> leftStates = new ArrayList<SignalState>();
						SignalState init = new SignalState(stg.getSignals(), SignalValue.ZERO);
						Transition leftTrans = (Transition)postP.get(i);
						try {
							leftStates.add( applySignalEdge(init, leftTrans) );
						}
						catch (IllegalArgumentException e) {
							// state change not applicable
							continue leftSide;
						}
						
						
						List<Node> visitedPathLeft = new ArrayList<Node>();
						visitedPathLeft.add(leftTrans);
						
						// processedNodes.add(leftTrans);
					
						dfsTypeII(leftTrans, postP.get(j), leftStates, new ArrayList<SignalState>(), 
								visitedPathLeft, new ArrayList<Node>(), resultingTransPairs);
						
						// for processedNodes all Type II sequences are found -- NO!!!
//						for (Node node : processedNodes)
//							if (node instanceof Place)
//								allPlaces.remove(node);
						
						this.typeIIConflicts.addAll(resultingTransPairs);
					}	
			}
		}
	}

	private void dfsTypeII(Node leftNode,
			Node rightNode,
			List<SignalState> leftStates,
			List<SignalState> rightStates,
			List<Node> visitedLeft,
			List<Node> visitedRight,
			Set<Pair<List<Transition>, List<Transition>>> result) {
		
		// leftNode is already in the visited structures
		visitedRight.add(rightNode); 
		// everVisited.add(rightNode);
		
		// leftNode must always be a transition
		if ( !(leftNode instanceof Transition) ) {
			// recursion and leave the right side as it is
			for (Node trans : leftNode.getChildren())
				if (!visitedLeft.contains(trans) && !visitedRight.contains(trans)) {
					visitedLeft.add(trans);
					// everVisited.add(trans);
					
					SignalState state = leftStates.get(leftStates.size()-1); // last element
					try {
						leftStates.add( applySignalEdge(state, (Transition)trans) );
					}
					catch (IllegalArgumentException e) {
						// cannot apply signal edge
						visitedLeft.remove(trans);
						return; // unsuccessful
					}
					
					dfsTypeII(trans, rightNode, leftStates, rightStates, 
							visitedLeft, visitedRight, result);
					visitedLeft.remove(trans); // just simple paths
					leftStates.remove(leftStates.size()-1);
				}
			return; // don't go further here
		}
		else { // abort condition for leftNode as an output
			if (isOutputInternal((Transition)leftNode))
				return; // unsuccessful
		}
		
		if (rightNode instanceof Transition) {
			SignalState state = null;
			if (rightStates.size() > 0)
				state = rightStates.get(rightStates.size()-1); // last element
			else
				state = new SignalState(stg.getSignals(), SignalValue.ZERO);
			
			if (state != null) {
				try {
					rightStates.add( applySignalEdge(state, (Transition)rightNode) );
				}
				catch (IllegalArgumentException e) {
					// signal edge cannot be applied
					visitedRight.remove(rightNode);
					return; // not successful
				}
			}
		}
		
		// abort conditions for rightNode
		if (rightNode instanceof Transition) {
			
			SignalState leftState = leftStates.get(leftStates.size()-1);
			SignalState rightState = rightStates.get(rightStates.size()-1);
			if (leftState.equals(rightState)) {
				List<Transition> leftSequence = new LinkedList<Transition>();
				Iterator<Node> iterator = visitedLeft.iterator();
				while (iterator.hasNext()) {
					Node trans = iterator.next();
					if (trans instanceof Transition)
						leftSequence.add((Transition)trans);
				}
				
				List<Transition> rightSequence = new LinkedList<Transition>();
				iterator = visitedRight.iterator();
				while(iterator.hasNext()) {
					Node trans = iterator.next();
					if (trans instanceof Transition)
						rightSequence.add((Transition)trans);
				}
				
				result.add(new Pair<List<Transition>, List<Transition>>(leftSequence, rightSequence));
				
				visitedRight.remove(rightNode);
				rightStates.remove(rightState);
				return; // successful
			}
			
			if (isOutputInternal((Transition)rightNode)) {
				visitedRight.remove(rightNode);
				rightStates.remove(rightState);
				return; // unsuccessful
			}
		}
		
		// recursion for rightNode by fixed leftNode
		for (Node child : rightNode.getChildren()) 
			if (!visitedLeft.contains(child) && !visitedRight.contains(child))
				dfsTypeII(leftNode, child, leftStates, rightStates, 
						visitedLeft, visitedRight, result);
		
		visitedRight.remove(rightNode); // just simple Paths
		if (rightNode instanceof Transition)
			rightStates.remove(rightStates.size()-1); // remove last element
		
		// recursion for leftNode whenever visitedRight path is empty again
		if (leftNode instanceof Transition && visitedRight.isEmpty()) {
			for (Node place : leftNode.getChildren())
				if (!visitedLeft.contains(place) && !visitedRight.contains(place)) {
					visitedLeft.add(place);
					// everVisited.add(place);
					dfsTypeII(place, rightNode, leftStates, rightStates, 
							visitedLeft, visitedRight, result);
					visitedLeft.remove(place); // just simple paths
				}
		}
	}

	private void findTypeIConflicts() {
		Assert.assertNotNull(stg);
		
		List<Transition> allTransitions = new LinkedList<Transition>(
				stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)); 
		
		// remove internals and outputs
		List<Transition> outsAndInternals = new LinkedList<Transition>();
		for (Transition t : allTransitions) 
			if ( isOutputInternal(t) ) outsAndInternals.add(t); 
		allTransitions.remove(outsAndInternals);
	
		while (!allTransitions.isEmpty()) {
			Transition t = allTransitions.remove(0); // first element
			
			List<SignalState> states = new ArrayList<SignalState>();
			states.add(new SignalState(stg.getSignals(), SignalValue.ZERO));
			Set<List<Transition>> resultingTransLists = new HashSet<List<Transition>>(); 
			List<Node> processedNodes = new LinkedList<Node>();
			dfsTypeI(t, states, new ArrayList<Node>(), processedNodes, resultingTransLists);
			
			preventFromFurtherProcessing(processedNodes, resultingTransLists);
			
			// for processedNodes all Type I sequences are found
			for (Node node : processedNodes) 
				if (node instanceof Transition) 
					allTransitions.remove(node);
			
			this.typeIConflicts.addAll(resultingTransLists);
		}
	}
	
	/**
	 * Conflict sequences can interleave, so for a conflict just the first 
	 * transition must not ever be processed in the future, but the rest.
	 * @param notNecessary - nodes which might not be considered again
	 * @param conflictSequences
	 */
	private void preventFromFurtherProcessing(List<Node> notNecessary,
			Set<List<Transition>> conflictSequences) {
		
		for (List<Transition> conflict : conflictSequences) {
			List<Transition> conflictPostfix = new LinkedList<Transition>(conflict);
			conflictPostfix.remove(0); // first element must not be processed again, but the rest
			notNecessary.removeAll(conflictPostfix);
		}
		
	}

	private boolean isOutputInternal(Transition trans) {
		STG stg = trans.getSTG();
		Integer signal = trans.getLabel().getSignal();
		if (stg.getSignature(signal) == Signature.OUTPUT ||
				stg.getSignature(signal) == Signature.INTERNAL)
			return true;
				
		return false;
	}
	
	private void dfsTypeI(Node node, 
			List<SignalState> states, 
			List<Node> visited, 
			List<Node> everVisited,
			Set<List<Transition>> result) {
		
		visited.add(node);
		everVisited.add(node);
		if (node instanceof Transition) {
			SignalState state = states.get(states.size()-1); // last element
			if (state != null) {
				try {
					states.add( applySignalEdge(state, (Transition)node) );
				}
				catch (IllegalArgumentException e) {
					// state change could not be applied
					visited.remove(node);
					return; // not successful
				}
			}
					
		}
		
		// abort conditions
		if (node instanceof Transition) {
			SignalState currentState = states.get(states.size()-1);
			
			if (isOutputInternal((Transition)node)) {
				visited.remove(node);
				states.remove(currentState);
				return; // not successful
			}
			
			
			int indexOfCurrentState = containsInPrefix(states, currentState);
			if (indexOfCurrentState >= 0) {
				List<Transition> conflictSequence = new LinkedList<Transition>();
				for (int i = indexOfCurrentState*2 ; i < visited.size() ; i++) {
					Node trans = visited.get(i);
					if (trans instanceof Transition)
						conflictSequence.add((Transition)trans);
				}
				result.add(conflictSequence);
				
				visited.remove(node);
				states.remove(currentState);
				return; // successful
			}
		}
		
		// recursion
		for (Node child : node.getChildren())
			if (!visited.contains(child))
				dfsTypeI(child, states, visited, everVisited, result);
		
		visited.remove(node); // just simple paths
		if (node instanceof Transition)
			states.remove(states.size()-1);
	}
	
	/**
	 * @param list - of signalstates
	 * @param lastElement - last element, ie. not in the prefix
	 * @return - the index of an equal signal state in the prefix or -1 if there so no equal state
	 */
	private int containsInPrefix(List<SignalState> list, SignalState lastElement) {
		
		for (int i = 0; i < list.size()-1; i++) 
			if (lastElement.equals(list.get(i)))
				return i;
		
		return -1;
	}
	
	
	/**
	 * Improvement of state.applySignalEdge() to handle dummy firing correctly
	 * @param state - current signal state
	 * @param trans - transition to fire
	 * @return - new signal state
	 */
	private SignalState applySignalEdge(SignalState state, Transition trans) {
		STG stg = trans.getSTG();
		if ( stg.getSignature(trans.getLabel().getSignal()) == Signature.DUMMY )
			return new SignalState(state); // no state change during firing a dummy transition
		else
			return state.applySignalEdge(trans.getLabel());	
	}
}
