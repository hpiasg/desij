

package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 *
 */
public class PartitionerCombineAll implements ICompatibilityChecker,
		IPartitioningStrategy {
	
	private STG specification;
	
	private List<Collection<Integer>> componentOutputs; // initialized through improvePartition()
	private List<Collection<Integer>> relevantSignals; // initialized through improvePartition()
	
	private int maximumPartitionBlockSize = 20; // must be defined by the user; -1 means unrestricted
	
	private Set<IBasicStrategy> basicStrategies;
	private double majorityCountCompatibles; // for efficiency in checkCompatibility()

	/**
	 * Constructor
	 */
	public PartitionerCombineAll(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
		
		basicStrategies = new HashSet<IBasicStrategy>();
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy#improvePartition(net.strongdesign.stg.Partition)
	 */
	@Override
	public Partition improvePartition(Partition oldPartition)
			throws STGException, PartitioningException {
		if (oldPartition.getPartition().size() < 2) return oldPartition;
		
		componentOutputs = new ArrayList<Collection<Integer>>(oldPartition.getPartition().size()); 
		
		for (PartitionComponent signals : oldPartition.getPartition())
			componentOutputs.add(specification.getSignalNumbers(signals.getSignals())); 
				
		relevantSignals = new ArrayList<Collection<Integer>>(componentOutputs.size());
		
		for (int i = 0; i < componentOutputs.size(); i++) 
			relevantSignals.add(i, getInitialTriggers(componentOutputs.get(i))); // componentOutputs[i] corresponds to relevantSignals[i]
		
		initializeBasicStrategies(oldPartition); // important at this position in control flow
		
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

	private void initializeBasicStrategies(Partition oldPartition) throws STGException {
		
		basicStrategies.add(new PartitionerInputConsolidation(specification));
		basicStrategies.add(new PartitionerSequentialMerger(specification));
		basicStrategies.add(new PartitionerIrreducibleCSCAvoidance(specification));
		basicStrategies.add(new PartitionerLockedSignals(specification));
		
		for (IBasicStrategy heuristic : basicStrategies)
			heuristic.initializationForImprovement(componentOutputs, relevantSignals, oldPartition);
		
		// always round up
		majorityCountCompatibles = (double)basicStrategies.size()/2; 
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#checkCompatibility(int, int)
	 * 
	 * Compatibility according to majority voting, i.e. at least 3 of 4
	 */
	@Override
	public double checkCompatibility(int element1, int element2) {
		
		int compatibleCount = 0;
		int incompatibleCount = 0;
				
		for (IBasicStrategy heuristic : basicStrategies)
			if ( heuristic.areCompatible(element1, element2) ) { 
				if (++compatibleCount >= majorityCountCompatibles) // i.e. at least 2 of 4 are compatible
					return 1.0; // compatible
			}
			else {
				if (++incompatibleCount > majorityCountCompatibles) // i.e. at least 3 of 4 are incompatible
					return -1.0;
			}
		
		return -1.0; // should never be reached
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#exceedsMaximalElementCount(java.util.Collection)
	 */
	@Override
	public boolean exceedsMaximalElementCount(Collection<Integer> mc) {
		if (maximumPartitionBlockSize == -1) return false; // unrestricted partition block size
		
		int elementCountOfMC = 0;
		for (int partitionBlock : mc) { 
			elementCountOfMC += componentOutputs.get(partitionBlock).size();
			elementCountOfMC += relevantSignals.get(partitionBlock).size();
		}
		
		if (elementCountOfMC > maximumPartitionBlockSize) 
			return true; // yes
		else return false; // no
	}
	
	
	/*
	 * Helper for improvePartition()
	 */
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

}
