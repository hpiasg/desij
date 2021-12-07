

package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.MultiCondition;

/**
 * @author Dominic Wist
 * 
 * Gets an oldPartition and splits each Partition block into smaller 
 * blocks containing sequential outputs only.
 * @deprecated --> use PartitionerSequentialMerger instead
 */
public class PartitionerConcurrencyReduction implements IPartitioningStrategy {
	
	private STG specification;
	
	private List<Collection<Integer>> componentOutputs; // initialised through improvePartition()
	private Map<Collection<Integer>, Collection<Collection<Integer>>> improvedBlocks;
	
	// false means that also trigger signals are taken into account
	private boolean reductionForOutputsOnly = false; // must be defined by the user
	
	public PartitionerConcurrencyReduction(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy#improvePartition(net.strongdesign.stg.Partition)
	 */
	@Override
	public Partition improvePartition(Partition oldPartition)
			throws STGException, PartitioningException {

		componentOutputs = new ArrayList<Collection<Integer>>(oldPartition.getPartition().size());
		improvedBlocks = new HashMap<Collection<Integer>, Collection<Collection<Integer>>>();		
		
		for (PartitionComponent signals : oldPartition.getPartition())
			componentOutputs.add(specification.getSignalNumbers(signals.getSignals())); 
		
		for (Collection<Integer> block : componentOutputs) {
			if (block.size() < 2) continue; // not splittable
			
			List<Integer> sequentialOutputs;
			List<Integer> remainingOutputs = new ArrayList<Integer>(block);
			Collection<Collection<Integer>> newBlocks = new HashSet<Collection<Integer>>();
			do {
				sequentialOutputs = remainingOutputs;
				remainingOutputs = buildSequentialBlock(
						sequentialOutputs, !reductionForOutputsOnly);
				newBlocks.add(sequentialOutputs);
			} while(!remainingOutputs.isEmpty());
			
			if (newBlocks.size() > 1) 
				improvedBlocks.put(block, newBlocks);
		}
		
		// build new partition from improvedBlocks
		Partition newPartition = new Partition();
		
		for (Collection<Integer> oldBlock : componentOutputs) {
			Collection<Collection<Integer>> newBlocks = improvedBlocks.get(oldBlock);
			if (newBlocks == null) { // all outputs in oldBlocks are sequential
				newBlocks = new HashSet<Collection<Integer>>(1);
				newBlocks.add(oldBlock);
			}
			
			for (Collection<Integer> newBlock : newBlocks) {
				newPartition.beginSignalSet();
				for (String outputName : specification.getSignalNames(newBlock)) {
					newPartition.addSignal(outputName);
				}
			}
		}
		return newPartition;
	}

	private List<Integer> buildSequentialBlock(List<Integer> sequentialOutputs, boolean mostSequential) throws PartitioningException {
		
		List<Integer> unsequentialOutputs = new ArrayList<Integer>();
		
		// sequentialOutputs has just one element
		if (sequentialOutputs.size() < 2) return unsequentialOutputs; 

		int i = 0;
		int j;
		while (i < sequentialOutputs.size()-1) {
			j = i+1;
			while (j < sequentialOutputs.size()) {
				if ( signalsAreConcurrent(sequentialOutputs.get(i), sequentialOutputs.get(j)) )
					unsequentialOutputs.add(sequentialOutputs.remove(j)); // j stays the same --> i.e. we don't miss an element
				else 
					j++;
			}
			i++;
		}
		
		// reduce concurrency to trigger signals as well
		if (sequentialOutputs.size() > 1 && mostSequential) {
			i = 0;
			while (i < sequentialOutputs.size()-1) {
				j = i+1;
				// Find syntactical triggers and return their signals
				Collection<Integer> signals1 = specification.collectUniqueFromTransitions(
						ConditionFactory.getSignalOfCondition(sequentialOutputs.get(i)), 
						CollectorFactory.getTriggerSignal());
				signals1.add(sequentialOutputs.get(i));
				for (Integer signal1 : signals1)
					nextOutputJ : while (j < sequentialOutputs.size()) {
						Collection<Integer> triggers2 = specification.collectUniqueFromTransitions(
								ConditionFactory.getSignalOfCondition(sequentialOutputs.get(j)), 
								CollectorFactory.getTriggerSignal());
						for (Integer signal2 : triggers2)
							if (signalsAreConcurrent(signal1, signal2)) {
								unsequentialOutputs.add(sequentialOutputs.remove(j));
								continue nextOutputJ; // right, without incrementing j
							}
						if (signal1 != sequentialOutputs.get(i) && // check already done, see above
								signalsAreConcurrent(signal1, sequentialOutputs.get(j)))
							unsequentialOutputs.add(sequentialOutputs.remove(j)); // don't increment j here
						else 
							j++;
					}
				i++;
			}
		}
		
		// feasability check for the sequentialOutputs block
		List<Integer> lackingSignals = missingSignalsForFeasability(sequentialOutputs);
		
		if (!lackingSignals.isEmpty()) {
			for (Integer signal : lackingSignals) 
				if (unsequentialOutputs.remove(signal)) 
					sequentialOutputs.add(signal);
				else
					throw new PartitioningException("Partition was not feasible before!", null);
		}
		
		
		return unsequentialOutputs;
	}
	
	private boolean signalsAreConcurrent(Integer signal1, Integer signal2) {
		return ConditionFactory.getSignalConcurrencyCondition(specification, signal1).fulfilled(signal2);
	}

	private List<Integer> missingSignalsForFeasability(List<Integer> partitionBlock) {
		
		List<Integer> result = new LinkedList<Integer>();
		
		for (Transition t : specification.getTransitions(
				ConditionFactory.getSignalOfCondition(partitionBlock))) {
			MultiCondition<Transition> mc = new MultiCondition<Transition>(MultiCondition.AND);
			mc.addCondition(ConditionFactory.getStructuralConflictCondition(t));
			mc.addCondition(ConditionFactory.getSignatureOfCondition(Signature.OUTPUT));
			result = new LinkedList<Integer>( 
					specification.collectFromTransitions(mc, CollectorFactory.getSignalCollector()) );
			result.removeAll(partitionBlock);
		}
		
		return result;
	}

}
