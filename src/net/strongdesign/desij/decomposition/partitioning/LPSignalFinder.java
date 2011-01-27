/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.util.HashSet;
import java.util.Set;

import net.sf.javailp.Linear;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.MarkingEquationCache;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.Condition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 *
 */
public class LPSignalFinder implements ICSCSolvingSignalFinder {
	
	private STG specification;
	private STG criticalComponent;
	private Pair<Transition,Transition> splittablePair;
	private CSCPartition partition;
	
	public LPSignalFinder(
			STG specification,
			STG critComponent, 
			Pair<Transition,Transition> splitPair,
			CSCPartition partition) {
		this.specification = specification;
		this.criticalComponent = critComponent;
		this.splittablePair = splitPair;
		this.partition = partition;
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICSCSolvingSignalFinder#execute()
	 */
	@Override
	public Integer execute() {
		
		Integer result = null;		
		
		Set<Transition> possibleDelayTransitions = findReachableOutputs();
		if (possibleDelayTransitions == null) // no possible delay transitions
			return result;
		
		Integer currentSignal;
		currentSignal = splittablePair.a.getLabel().getSignal();
		// initialise result
		if (hasValidSignature(currentSignal))
			result = currentSignal;
		else {
			currentSignal = splittablePair.b.getLabel().getSignal();
			if (hasValidSignature(currentSignal))
				result = currentSignal;
		}
		
		for (Transition t : possibleDelayTransitions) 
			if ( isDelayTransition(t) ) {
				currentSignal = t.getLabel().getSignal();
				if (partition.signalAvoidsComponentGrowth(currentSignal, criticalComponent)) {
					result = currentSignal;
					break;
				}
				if (result == null)
					result = currentSignal;
			}
		
		return result;
	}
	
	private boolean isDelayTransition(Transition delayTrans) {
		
		SolverFactory factory = new SolverFactoryLpSolve();
		factory.setParameter(Solver.VERBOSE, 0); // we don't need messages from the solver
		factory.setParameter(Solver.TIMEOUT, 0); // no timeout
		
		// M_1 = M_N + C v_1 --> -MN = -M1 + C v_1
		// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem
		Problem problem = MarkingEquationCache.getMarkingEquation(specification);
		Linear linear;
		
		// M_1[splittablePair.a>(M_1 - *splittablePair.a + splittablePair.a*)
		for (Node place : splittablePair.a.getParents()) {
			int id = place.getIdentifier();
			linear = new Linear();
			linear.add(1, "M1" + id);
			problem.add(linear, ">=", splittablePair.a.getParentValue(place));
		}
			
			
		// M_2 = (M_1 - *splittablePair.a + splittablePair.a*) + C v_2  
		// --> *splittablePair.a - splittablePair.a* = M_1 - M2 + C v_2
		for (Place p : specification.getPlaces(ConditionFactory.ALL_PLACES)) {
			int id = p.getIdentifier();
			linear = new Linear();
			linear.add(1, "M1" + id);
			linear.add(-1, "M2" + id);
			// avoid building the complete IncidenceMatrix
			for (Node transition : p.getNeighbours())
				linear.add(transition.getChildValue(p) - transition.getParentValue(p), "v2" + transition.getIdentifier());
			problem.add(linear, "=", splittablePair.a.getParentValue(p) - splittablePair.a.getChildValue(p));
		}
		
		// v_2(delayTrans) = 0
		linear = new Linear();
		linear.add(1, "v2" + delayTrans.getIdentifier());
		problem.add(linear, "=", 0);
		
		// v_2(splittablePair.a)=0 --> not necessary!
//		linear = new Linear();
//		linear.add(1, "v2" + splittablePair.a.getIdentifier());
//		problem.add(linear, "=", 0);
		
		// v_2(splittablePair.b)=0 --> not necessary!
//		linear = new Linear();
//		linear.add(1, "v2" + splittablePair.b.getIdentifier());
//		problem.add(linear, "=", 0);
		
		// M_2[splittablePair.b>
		for (Node place : splittablePair.b.getParents()) {
			int id = place.getIdentifier();
			linear = new Linear();
			linear.add(1, "M2" + id);
			problem.add(linear, ">=", splittablePair.b.getParentValue(place));
		}
			
		// M_2 > 0 and v_2 >= 0 and solve as (I)LP problem
		linear = new Linear();
		for (Place p : specification.getPlaces(ConditionFactory.ALL_PLACES)) {
			int id = p.getIdentifier();
			problem.setVarLowerBound("M2" + id, 0);
			linear.add(2, "M2" + id);
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType("M2" + id, Integer.class);
			else
				problem.setVarType("M2" + id, Double.class);
		}
		problem.add(linear, ">=", 1);
		
		// linear = new Linear();
		for (Transition t : specification.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			int id = t.getIdentifier();
			problem.setVarLowerBound("v2" + id, 0);
			// linear.add(1, "v2" + id);
			if (!CLW.instance.NOILP.isEnabled()) 
				problem.setVarType("v2" + id, Integer.class);
			else 
				problem.setVarType("v2" + id, Double.class);
		}
		// problem.add(linear, ">=", 1);
		
		Result result = factory.get().solve(problem);
		
		if (result == null) // problem is unfeasible
			return true; // i.e. delayTrans is a real delay transition
		else
			return false; // delayTrans is MAYBE not a delay transition
	}

	private Set<Transition> findReachableOutputs() {
		
		Set<Node> visitedNodes = new HashSet<Node>();
		
		visitedNodes.add(splittablePair.a);
		if (splittablePair.a == splittablePair.b) {
			return null; // no delay transition in between
		}
		else {
			for (Node childOfStart : splittablePair.a.getChildren())
				if (!visitedNodes.contains(childOfStart))
					dfs(childOfStart, splittablePair.b, 
							ConditionFactory.getRelevantTransitionCondition(criticalComponent), 
							visitedNodes);
		}
		
		visitedNodes.remove(splittablePair.a);
		visitedNodes.remove(splittablePair.b);
		
		// visitedNodes contains every reachable node except for entry and exit transition itself
		Set<Transition> result = new HashSet<Transition>();
		for (Node n : visitedNodes) 
			if (n instanceof Transition) {
				Integer currentSignal = ((Transition)n).getLabel().getSignal();
				if ( hasValidSignature(currentSignal) )
						result.add((Transition) n);
			}
		
		if (result.isEmpty())
			return null;
		else
			return result;
	}

	private void dfs(
			Node node,
			Node endNode,
			Condition<Node> abortPath,
			Set<Node> visitedNodes) {
		
		visitedNodes.add(node);
		
		if (node == endNode) {
			// if it is an output, do not remove
			// visitedNodes.remove(node); // removes last element
			return;
		}
		
		if (abortPath.fulfilled(node)) { // startNode will never be checked!
			// if it is an output, do not remove
			// visitedNodes.remove(node); // remove last element
			return;
		}
				
		for (Node child : node.getChildren()) 
			if (!visitedNodes.contains(child)) 
				dfs(child, endNode, abortPath, visitedNodes);		
	}
	
	private boolean hasValidSignature(Integer signal) {
		if ( (specification.getSignature(signal) == Signature.OUTPUT)  || 
				(specification.getSignature(signal) == Signature.INTERNAL) ) 
			return true;
		else
			return false;
	}

}
