package net.strongdesign.stg.traversal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.statesystem.simulation.RelationElement;
import net.strongdesign.statesystem.simulation.RelationPropagator;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;

/**
 * A @link net.strongdesign.statesystem.RelationPropagator for STG-Bisimulation
 * @author mark
 *
 * @param <StateA>
 * @param <StateB>
 */
public class STGBisimulationPropagator<StateA, StateB> implements RelationPropagator<StateA, StateB, SignalEdge> {

	/**Specification STG*/
	private StateSystem<StateA, SignalEdge> stgA;
	private Map<Integer, Signature> sigA;
	
	/**Implementation STG statesystem*/
	private  StateSystem<StateB, SignalEdge> stgB;
	private Map<Integer, Signature> sigB;
	
	/**
	 * Initialises the RelationPropagator
	 * @param specification
	 * @param implementation
	 */
	public void setSystems(
			StateSystem<StateA, SignalEdge> specification, 
			StateSystem<StateB, SignalEdge> implementation
			) {
		this.stgA = specification;
		this.stgB = implementation;
	}

	public void setSignatures(
			Map<Integer, Signature> specSignatures, 	Map<Integer, Signature> implSignatures	) {
		sigA = specSignatures;
		sigB = implSignatures;
	}

	/**
	 * Returns the starting relation element
	 */
	public Set<RelationElement<StateA, StateB>> getStartRelation() {
		Set<RelationElement<StateA, StateB>> res = new HashSet<RelationElement<StateA, StateB>>();
		res.add( new RelationElement<StateA, StateB>(stgA.getInitialState(), stgB.getInitialState()) );
		return res;
	}

	/**
	 * Propagates from a given element according to STG-Bisimulation, see @link RelationPropagator#propagateElement(RelationElement) for details.
	 */
	public List<Set<RelationElement<StateA, StateB>>> propagateElement(RelationElement<StateA, StateB> el) throws PropagationException {
		List<Set<RelationElement<StateA, StateB>>> result = new LinkedList<Set<RelationElement<StateA, StateB>>>();
		Set<StateA> nextStatesA = null;
		Set<StateB> nextStatesB = null;
		
		Set<SignalEdge> edgesA = stgA.getEvents(el.a);
		for (SignalEdge edge : edgesA) {
			switch (sigA.get(edge.getSignal())) {
			case DUMMY: 
				throw new PropagationException("STG has dummy transitions."); 
			
			case INPUT: 
				nextStatesA = stgA.getNextStates(el.a, edge);			
				nextStatesB = stgB.getNextStates(el.b, edge);
				break;
			
			case OUTPUT: 
				nextStatesA = stgA.getNextStates(el.a, edge);			
				nextStatesB = getInternallyActivated(stgB, sigB, el.b, edge);
				break;			
			
			case INTERNAL: 
				nextStatesA = stgA.getNextStates(el.a, edge);			
				nextStatesB = getLambda2(stgB, sigB,  el.b);
				break;			
			}
			
			if (nextStatesB == null || nextStatesB.size()==0)
				return null;				
			for (StateA ns : nextStatesA)
				result.add(RelationElement.getCrossProduct(ns, nextStatesB));
		}
		
		Set<SignalEdge> edgesB = stgB.getEvents(el.b);
		f:
		for (SignalEdge edge : edgesB) {
			switch (sigB.get(edge.getSignal())) {
			case DUMMY: 
				throw new PropagationException("STG has dummy transitions."); 
			
			case INPUT: 
				continue f;
				
			case OUTPUT: 
				nextStatesB = stgB.getNextStates(el.b, edge);			
				nextStatesA = getInternallyActivated(stgA, sigA, el.a, edge);
				break;			
			
			case INTERNAL: 
				nextStatesB = stgB.getNextStates(el.b, edge);			
				nextStatesA = getLambda2(stgA, sigA, el.a);
				break;			
			}
			
			if (nextStatesA == null || nextStatesA.size()==0)
				return null;				
			for (StateB ns : nextStatesB)
				result.add(RelationElement.getCrossProduct(nextStatesA, ns));
		}
		
		return result;
	}

	/**
	 * Returns all states which can be reached from state by event preceeded by zero or more internal signaledges.
	 * @param <State>
	 * @param sys The stg statesystem
	 * @param state The current state
	 * @param event The final event
	 * @return
	 */
	protected static <State> Set<State> getInternallyActivated(
			StateSystem<State,SignalEdge> sys, Map<Integer, Signature> sign, State state, SignalEdge event) {
		Set<State> result = new HashSet<State>();
		Set<State> seen = new HashSet<State>();
		
		Queue<State> frontier = new LinkedList<State>();
		frontier.add(state);
		
		while (! frontier.isEmpty()) {
			State curState = frontier.poll();
			if (seen.contains(curState)) continue;
			seen.add(curState);
			
			result.addAll(sys.getNextStates(curState, event));
			
			
			for (SignalEdge edge : sys.getEvents(curState))
				if (sign.get(edge.getSignal())==Signature.INTERNAL)
					frontier.addAll(sys.getNextStates(curState, edge));
		}
		
		return result;
	}
	
	/**
	 * Returns all states which can be reached from state by firing just internal signaledges.
	 * @param <State>
	 * @param sys The stg statesystem
	 * @param state The current state
	 * @return
	 */
	protected static <State> Set<State> getLambda2(StateSystem<State,SignalEdge> sys, Map<Integer, Signature> sign, State state) {
		Set<State> result = new HashSet<State>();
		Set<State> seen = new HashSet<State>();
		
		Queue<State> frontier = new LinkedList<State>();
		frontier.add(state);
		
		result.add(state);
		
		while (! frontier.isEmpty()) {
			State curState = frontier.poll();
			if (seen.contains(curState)) continue;
			seen.add(curState);
			
			for (SignalEdge edge : sys.getEvents(curState))
				if (sign.get(edge.getSignal())==Signature.INTERNAL) {
					frontier.addAll(sys.getNextStates(curState, edge));
					result.addAll(sys.getNextStates(curState, edge));
				}
		}
		
		return result;
	}
	
	
	
	
	
}
