

package net.strongdesign.desij.decomposition.partitioning;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;

import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 *
 */
public class PartitionerInputConsolidation implements IPartitioningStrategy, ICompatibilityChecker, IBasicStrategy {
	
	private STG specification;
	
	private List<Collection<Integer>> componentOutputs; // initialized through improvePartition()
	private List<Collection<Integer>> relevantSignals; // initialized through improvePartition()
	
	private double consolidationFactor = 0.66; // must be defined by the user
	private int    maximumPartitionBlockSize = -1; // must be defined by the user; -1 means unrestricted

	/**
	 * Constructor
	 */
	public PartitionerInputConsolidation(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy#improvePartition(net.strongdesign.stg.Partition)
	 */
	public Partition improvePartition(Partition oldPartition) throws STGException, PartitioningException {
		
		if (oldPartition.getPartition().size() < 2) return oldPartition;
		
		componentOutputs = new ArrayList<Collection<Integer>>(oldPartition.getPartition().size());
		
		for (PartitionComponent signals : oldPartition.getPartition())
			componentOutputs.add(specification.getSignalNumbers(signals.getSignals())); 
		
		
		relevantSignals = new ArrayList<Collection<Integer>>(componentOutputs.size());
		
		for (int i = 0; i < componentOutputs.size(); i++) 
			relevantSignals.add(i, getInitialTriggers(componentOutputs.get(i))); // componentOutputs[i] corresponds to relevantSignals[i]
		
//		MaximalCompatibleFinder mcFinder = new MaximalCompatibleFinder(componentOutputs.size(), this);
//		Collection<Collection<Integer>> partitionCandidate = 
//			new GreedyCoverChooser(mcFinder).getOptimalPartition(); 
//			// new SimpleMaximalCoverChooser(mcFinder).getOptimalPartition();
		Collection<Collection<Integer>> partitionCandidate = 
			new GreedyMaximalCliqueCoverDetector(componentOutputs.size(), this).getDisjunctiveCover(); 
		
		// build the new partition ...
		
		Partition newPartition = new Partition();
		
		for (Collection<Integer> partitionBlock : partitionCandidate) {
			newPartition.beginSignalSet();
			for (int outputBlock : partitionBlock) {
				Collection<String> outputNames = 
					specification.getSignalNames(componentOutputs.get(outputBlock));
				for (String outputName : outputNames)
					newPartition.addSignal(outputName);
			}
		}
		
		return newPartition;
	}
	

	/**
	 * ... also used by a combined heuristic
	 * @param oldPartition
	 */
	public void initializationForImprovement(List<Collection<Integer>> componentOutputs, 
			List<Collection<Integer>> relevantSignals, Partition oldPartition) throws STGException {
		this.componentOutputs = componentOutputs;
		this.relevantSignals = relevantSignals;
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#exceedsMaximalElementCount(java.util.Collection<Integer> mc)
	 */
	public boolean exceedsMaximalElementCount(Collection<Integer> mc) {
		if (maximumPartitionBlockSize == -1) return false; // unrestricted partition block size
		
		int elementCountOfMC = 0;
		for (int partitionBlock : mc) 
			elementCountOfMC += componentOutputs.get(partitionBlock).size();
		
		if (elementCountOfMC > maximumPartitionBlockSize) 
			return true; // yes
		else return false; // no
	}
	
				
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#checkCompatibility(int,int)
	 * checks compatibility pairwise
	 * return: the consolidation degree of compatible pairs or -1.0 for incompatible pairs
	 */
//	public double checkCompatibility(int element1, int element2) {
//		
//		// check whether element1-relevantsignals consolidate with element2-signals
//		Collection<Integer> signalsBelongingToElement2 = 
//			new TreeSet<Integer>(relevantSignals.get(element2));
//		signalsBelongingToElement2.addAll(componentOutputs.get(element2));
//		
//		Collection<Integer> relevantSignalsBelongingToElement1 = 
//			new TreeSet<Integer>(relevantSignals.get(element1));
//		
//		relevantSignalsBelongingToElement1.retainAll(signalsBelongingToElement2);
//		
//		double consolidationForElement1 = 
//			relevantSignalsBelongingToElement1.size() / relevantSignals.get(element1).size();
//		
//		// check whether element2-relevantsignals consolidate with element1-signals
//		Collection<Integer> signalsBelongingToElement1 = 
//			new TreeSet<Integer>(relevantSignals.get(element1));
//		signalsBelongingToElement1.addAll(componentOutputs.get(element1));
//		
//		Collection<Integer> relevantSignalsBelongingToElement2 = 
//			new TreeSet<Integer>(relevantSignals.get(element2));
//		
//		relevantSignalsBelongingToElement2.retainAll(signalsBelongingToElement1);
//		
//		double consolidationForElement2 = 
//			relevantSignalsBelongingToElement2.size() / relevantSignals.get(element2).size();
//		
//		// compute and return the consolidation degree
//		
//		if ( (consolidationForElement1 < consolidationFactor) && 
//				(consolidationForElement2 < consolidationFactor) )
//			return -1.0; // incompatible pair
//		else
//			return consolidationForElement1 + consolidationForElement2; // compatible pair
//	}
	
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#checkCompatibility(int,int)
	 * checks compatibility pairwise
	 * return: the consolidation degree of compatible pairs or -1.0 for incompatible pairs
	 */
	public double checkCompatibility(int element1, int element2) {
		
		if (element1 == element2) return 1.0; // compatibility is reflexive, ie. maximum consolidation factor
		
		Collection<Integer> signalsBelongingToElement1 = 
			new TreeSet<Integer>(relevantSignals.get(element1));
		signalsBelongingToElement1.addAll(componentOutputs.get(element1));
		
		Collection<Integer> signalsBelongingToElement2 = 
			new TreeSet<Integer>(relevantSignals.get(element2));
		signalsBelongingToElement2.addAll(componentOutputs.get(element2));
		
		Collection<Integer> intersection = 
			new TreeSet<Integer>(signalsBelongingToElement1);
		intersection.retainAll(signalsBelongingToElement2);
		
		// check whether element1-signals consolidate with element2-signals
		
		double consolidationForElement1 = 
			(double)(intersection.size()) / signalsBelongingToElement1.size();
		
		// check whether element2-signals consolidate with element1-signals
				
		double consolidationForElement2 = 
			(double)(intersection.size()) / signalsBelongingToElement2.size();
		
		// compute and return the consolidation degree as the maximum of the consolidation factor
		
		if ( (consolidationForElement1 < consolidationFactor) && 
				(consolidationForElement2 < consolidationFactor) )
			return -1.0; // incompatible pair
		else if (consolidationForElement1 < consolidationForElement2)
			return consolidationForElement2; // compatible pair
		else
			return consolidationForElement1; // compatible pair
	}
	
	private Collection<Integer> getInitialTriggers(
			Collection<Integer> outputs) {
		
		// Find syntactical triggers and return their signals
		Collection<Integer> triggers = specification.collectUniqueFromTransitions(
				ConditionFactory.getSignalOfCondition(outputs), 
				CollectorFactory.getTriggerSignal());
		triggers.removeAll(outputs);
					
		//Add conflict signals to trigger list
		//outputs are already in the partition, because it is feasible
		//but inputs are still missing
		for (List<Integer> l :  specification.collectFromTransitions(
				ConditionFactory.getSignalOfCondition(outputs), 
				CollectorFactory.getConflictSignalCollector()))
			triggers.addAll(l);
		
		return triggers;
	}

	@Override
	public boolean areCompatible(int element1, int element2) {
		if (checkCompatibility(element1, element2) > 0.0)
			return true;
		else
			return false;
	}
}
