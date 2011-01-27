/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.util.List;
import java.util.SortedSet;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.GraphOperations;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 *
 */
public class SimpleSignalFinder implements ICSCSolvingSignalFinder {
	
	private STG specification;
	private STG criticalComponent;
	private Pair<Transition,Transition> splittablePair;
	private CSCPartition partition;
	
	public SimpleSignalFinder(
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
		SortedSet<List<Node>> simplePaths = // between t_entry and t_exit
			GraphOperations.findSimplePaths(splittablePair.a, splittablePair.b,
					ConditionFactory.getRelevantTransitionCondition(criticalComponent));
				
		Integer signalForAvoidance = null;
		
		Integer currentSignal;
		// initialise signalForAvoidance
		currentSignal = splittablePair.a.getLabel().getSignal();
		if (hasValidSignature(currentSignal))
			signalForAvoidance = currentSignal;
		else {
			currentSignal = splittablePair.b.getLabel().getSignal();
			if (hasValidSignature(currentSignal))
				signalForAvoidance = currentSignal;
		}
		
			 
		pathAnalysis: while ( !simplePaths.isEmpty() ) {
			List<Node> simplePath = simplePaths.first(); // shortest path first
			simplePaths.remove(simplePath);
			
			simplePath.remove(0); // remove start node, ie. splittablePair.a
			simplePath.remove(simplePath.size()-1); // remove end node, ie. splittablePair.b
			
			// path traversal
			for (Node trans: simplePath) {
				if (trans instanceof Transition) {
					currentSignal = ((Transition)trans).getLabel().getSignal();
					if ( hasValidSignature(currentSignal) ) {
						
						if (partition.signalAvoidsComponentGrowth(currentSignal, criticalComponent)) {
							signalForAvoidance = currentSignal;
							break pathAnalysis; // leave the other paths
						}
						
						if (signalForAvoidance == null) 
							signalForAvoidance = currentSignal; // store the first found output as a possible avoidanceSignal 
					
					}
						
				}
			}
		}
		
		return signalForAvoidance;
	}
	
	private boolean hasValidSignature(Integer signal) {
		if ( (specification.getSignature(signal) == Signature.OUTPUT)  || 
				(specification.getSignature(signal) == Signature.INTERNAL) ) 
			return true;
		else
			return false;
	}

}
