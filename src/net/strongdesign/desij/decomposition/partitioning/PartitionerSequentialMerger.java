/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 *
 * Gets an oldPartition (usually the finest one) and merges 
 * blocks with pairwise sequential signals
 * Replacement for PartitionerConcurrencyReduction
 *
 */
public class PartitionerSequentialMerger implements IPartitioningStrategy, ICompatibilityChecker {
	
	private STG specification;
	
	private List<Collection<Integer>> componentOutputs; // initialized through improvePartition()
	private List<Collection<Integer>> relevantSignals; // initialized through improvePartition()
	
	private double sequentialityDegree = 0.9; // must be defined by the user
	private int    maximumPartitionBlockSize = 20; // must be defined by the user; -1 means unrestricted
	
	// for optimization in concurrency calculation through caching of computation results
	
	// stores whether two signals are concurrent +1, or not -1, or undefined so far +0
	private int[][] pairChart; // computing on demand
	private Map<Integer,Integer> signal2PCIndex; // index for pair chart
	
	/**
	 * Constructor
	 */
	public PartitionerSequentialMerger(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
		
		// initialization for caching optimization
		signal2PCIndex = new HashMap<Integer, Integer>();
		int index = 0; // index for pair chart
		
		for (Integer signal : stg.getSignals())
			if (stg.getSignature(signal) == Signature.INPUT ||
					stg.getSignature(signal) == Signature.OUTPUT ||
					stg.getSignature(signal) == Signature.INTERNAL)
				signal2PCIndex.put(signal, index++);
		
		int signalCount = signal2PCIndex.keySet().size();
		pairChart = new int[signalCount][signalCount];
		// initialize with zeros
		for (int i = 0; i < signalCount; i++)
			for (int j = 0; j < signalCount; j++)
				pairChart[i][j] = 0;
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy#improvePartition(net.strongdesign.stg.Partition)
	 */
	@Override
	public Partition improvePartition(Partition oldPartition)
			throws STGException, PartitioningException {
		if (oldPartition.getPartition().size() < 2) return oldPartition;
		
		componentOutputs = new ArrayList<Collection<Integer>>(oldPartition.getPartition().size());
				
		for (List<String> signals : oldPartition.getPartition())
			componentOutputs.add(specification.getSignalNumbers(signals)); 
		
		
		relevantSignals = new ArrayList<Collection<Integer>>(componentOutputs.size());
		
		for (int i = 0; i < componentOutputs.size(); i++) 
			relevantSignals.add(i, getInitialTriggers(componentOutputs.get(i))); // componentOutputs[i] corresponds to relevantSignals[i]
		
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
		
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#checkCompatibility(int, int)
	 * returns -1.0 for incompatible pairs and for compatibles the percentage or degree resp. of sequential pairs
	 */
	@Override
	public double checkCompatibility(int element1, int element2) {
		
		if (element1 == element2) return 1.0; // compatibility is reflexive
		
		// Initialization
		
		Collection<Integer> exclusiveSignalsBelongingToElement1 = 
			new TreeSet<Integer>(relevantSignals.get(element1));
		exclusiveSignalsBelongingToElement1.addAll(componentOutputs.get(element1));
		
		Collection<Integer> exclusiveSignalsBelongingToElement2 = 
			new TreeSet<Integer>(relevantSignals.get(element2));
		exclusiveSignalsBelongingToElement2.addAll(componentOutputs.get(element2));
		
		Collection<Integer> intersection = 
			new TreeSet<Integer>(exclusiveSignalsBelongingToElement1);
		intersection.retainAll(exclusiveSignalsBelongingToElement2);
		
		exclusiveSignalsBelongingToElement1.removeAll(intersection);
		exclusiveSignalsBelongingToElement2.removeAll(intersection);
		
		int concurrentSigPairLimit = (int)(Math.round(
				( exclusiveSignalsBelongingToElement1.size() * exclusiveSignalsBelongingToElement2.size() + 
						exclusiveSignalsBelongingToElement1.size() * intersection.size() +
						exclusiveSignalsBelongingToElement2.size() * intersection.size() +
						binomialCoefficient(intersection.size(), 2) ) * (1 - sequentialityDegree) ) ); 
		int concSigPairCnt = 0;
		
		// computation for all exclusive signal pairs
		
		for (Integer signal1 : exclusiveSignalsBelongingToElement1)
			for (Integer signal2: exclusiveSignalsBelongingToElement2) 
				if ( areConcurrent(signal1, signal2) ) 
					if (++concSigPairCnt > concurrentSigPairLimit)
						return -1.0; // too many concurrent signal pairs
		
		// computation for all combinations of intersection signals
		
		ArrayList<Integer> intersectionArray = new ArrayList<Integer>(intersection);
		for (int i = 0; i < intersectionArray.size() - 1; i++)
			for (int j = i+1; j < intersectionArray.size(); j++)
				if ( areConcurrent(intersectionArray.get(i),intersectionArray.get(j)) )
					if (++concSigPairCnt > concurrentSigPairLimit)
						return -1.0; // too many concurrent signal pairs
		
		// computation for exclusiveSignalsBelongingToElement1 x intersection
		
		for (Integer signal1 : exclusiveSignalsBelongingToElement1)
			for (Integer signal2: intersection) 
				if ( areConcurrent(signal1, signal2) ) 
					if (++concSigPairCnt > concurrentSigPairLimit)
						return -1.0; // too many concurrent signal pairs
		
		// computation for exclusiveSignalsBelongingToElement2 x intersection
		
		for (Integer signal1 : intersection)
			for (Integer signal2: exclusiveSignalsBelongingToElement2) 
				if ( areConcurrent(signal1, signal2) ) 
					if (++concSigPairCnt > concurrentSigPairLimit)
						return -1.0; // too many concurrent signal pairs
		
		// compute return value, i.e. sequentiality degree of the two corresponding initial components
		
		return 1.0 - ( (double)concSigPairCnt / 
				( exclusiveSignalsBelongingToElement1.size() * exclusiveSignalsBelongingToElement2.size() +
						exclusiveSignalsBelongingToElement1.size() * intersection.size() +
						exclusiveSignalsBelongingToElement2.size() * intersection.size() +
						binomialCoefficient(intersection.size(), 2) ) ); 
	}

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
	
	/*
	 * Helper for checkCompatibility()
	 * Computes the binomial coefficient n choose k
	 */
	private double binomialCoefficient(int n, int k) {
		double result;
		
		if (k > n)
			result = 0; // small choose large is impossible
		else if (k == 0 || k == n)
			result = 1; // n choose 0 = 1 and n choose n = 1
		else {
			result = 1;
			for (int i = 1; i <= k; i++) // computation
				result = result*(n-i+1)/i;
		}
		
		return result;
	}
	
	// ***** private routines for the LP result caching ****** 
	
	/*
	 * Returns whether signal1 and signal2 are concurrent or not; 
	 * Computation on demand
	 */
	private boolean areConcurrent(Integer signal1, Integer signal2) {
		int indexSignal1 = signal2PCIndex.get(signal1);
		int indexSignal2 = signal2PCIndex.get(signal2);
		
		if ( pairChart[indexSignal1][indexSignal2] == 0 ) { // Not cached so far: Compute lock relation!
			if ( ConditionFactory.getSignalConcurrencyCondition(specification, signal1).fulfilled(signal2) ) {
				pairChart[indexSignal1][indexSignal2] = 1;
				pairChart[indexSignal2][indexSignal1] = 1; // symmetric representation
			}
			else {
				pairChart[indexSignal1][indexSignal2] = -1;
				pairChart[indexSignal2][indexSignal1] = -1; // symmetric representation
			}
		}
		
		if ( pairChart[indexSignal1][indexSignal2] == -1 )
			return false; // signal1 and signal2 are sequential
		else // pairChart[indexSignal1][indexSignal2] == +1
			return true; 						
	}

}
