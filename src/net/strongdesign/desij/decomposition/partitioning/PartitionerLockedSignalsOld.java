/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 * @deprecated
 */
public class PartitionerLockedSignalsOld implements IPartitioningStrategy, ICompatibilityChecker {
	
	private STG specification;
	private List<Collection<Integer>> componentOutputs; // initialised through improvePartition()
	
	private int maximumPartitionBlockSize = -1; // must be defined by the user; -1 means unrestricted
	
	public PartitionerLockedSignalsOld(STG stg) throws STGException {
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
		if (oldPartition.getPartition().size() < 2) return oldPartition;
		
		componentOutputs = new ArrayList<Collection<Integer>>(oldPartition.getPartition().size());
		for (List<String> signals : oldPartition.getPartition())
			componentOutputs.add(specification.getSignalNumbers(signals));
		
		MaximalCompatibleFinder mcFinder = new MaximalCompatibleFinder(componentOutputs.size(), this);
		Collection<Collection<Integer>> partitionCandidate = getOptimalPartition(mcFinder);
		
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
	
	private Collection<Collection<Integer>> getOptimalPartition(MaximalCompatibleFinder mcFinder) {
		if (mcFinder == null) return null;
		
		Collection<Collection<Integer>> partitionCandidate = new ArrayList<Collection<Integer>>();
		Set<Integer> partitionBlocksToCover = new TreeSet<Integer>();
		Collection<Collection<Integer>> copyOfMCs = 
			new ArrayList<Collection<Integer>>(mcFinder.getCompatibles()); 
		
		// find PartitionBlocksToCover
		for (Collection<Integer> mc : copyOfMCs) {
			partitionBlocksToCover.addAll(mc);
		}
		
		// find a good cover from list of compatibles with high compatible factors
		double highestCompatibleFactor = 0.0;
		while ( !copyOfMCs.isEmpty() ) {
			
			// get the best MC having the highest compatible factor for the result
			highestCompatibleFactor = getHighestCompatibleFactor(copyOfMCs, mcFinder);
			
			Collection<Collection<Integer>> bestMCs = 
				getCompatiblesFromCompFactor(copyOfMCs, mcFinder, highestCompatibleFactor);
			Collection<Integer> bestMC = bestMCs.iterator().next();
			bestMCs.remove(bestMC);
			while ( !bestMCs.isEmpty() ) {
				Collection<Integer> bestMCCandidate = bestMCs.iterator().next();
				bestMCs.remove(bestMCCandidate);
				if (bestMCCandidate.size() > bestMC.size()) // MC covering the most elements is best
					bestMC = bestMCCandidate;
			}
			
			partitionCandidate.add(bestMC);
			
			// remove all MCs having a non-empty intersection with bestMC in copyOfMCs
			reviseAwithRespectToB(copyOfMCs, bestMC);
			
			// remove all members of bestMC from partitionBlocksToCover
			partitionBlocksToCover.removeAll(bestMC);
		}
		
		// add all the non-covered partition blocks as one-member sets to partitionCandidate
		for (int partitionBlock : partitionBlocksToCover) {
			Collection<Integer> oneElementSet = new TreeSet<Integer>();
			oneElementSet.add(partitionBlock);
			partitionCandidate.add(oneElementSet);
		}
		
		// add the incompatible one-member sets to the result --> now we have a partition candidate
		partitionCandidate.addAll(mcFinder.getIncompatibleOneMemberSets());
		
		return partitionCandidate;
	}
	
	/*
	 * Helper routine for getOptimalPartition(...)
	 * remove all sets in A having a non-empty intersection with B
	 */
	private void reviseAwithRespectToB(Collection<Collection<Integer>> A, Collection<Integer> B) {
		
		Collection<Integer> copyOfB;
		Collection<Collection<Integer>> membersToDeleteInA = 
			new ArrayList<Collection<Integer>>();
		for (Collection<Integer> memberOfA : A) {
			copyOfB = new TreeSet<Integer>(B);
			copyOfB.retainAll(memberOfA); // set intersection
			if (copyOfB.size() > 0) // non-empty intersection
				membersToDeleteInA.add(memberOfA);
		}
		
		A.removeAll(membersToDeleteInA);			
	}
	
	/*
	 * Helper routine for getOptimalPartition(...)
	 */
	private double getHighestCompatibleFactor(
			Collection<Collection<Integer>> maxCompatibles, 
			MaximalCompatibleFinder compatibilityEvaluator) {
		
		double result = 0.0;
		
		for (Collection<Integer> mc : maxCompatibles) {
			if (compatibilityEvaluator.getNormalizedCompatibleFactor(mc) > result)
				result = compatibilityEvaluator.getNormalizedCompatibleFactor(mc);
		}
		
		return result;
	}
	
	/*
	 * Helper routine for getOptimalPartition(...)
	 */
	private Collection<Collection<Integer>> getCompatiblesFromCompFactor(
			Collection<Collection<Integer>> maxCompatibles,
			MaximalCompatibleFinder compatibilityEvaluator,
			double compFactor) {
		List<Collection<Integer>> result = new ArrayList<Collection<Integer>>();
		
		for (Collection<Integer> mc : maxCompatibles) 
			if (compatibilityEvaluator.getNormalizedCompatibleFactor(mc) == compFactor)
				result.add(mc);
		
		return result;
	}

//	private Collection<Collection<Integer>> getCoreMCs(
//			Collection<Collection<Integer>> maxCompatibles) {
//		
//		Collection<Collection<Integer>> result = new LinkedList<Collection<Integer>>();
//		
//		for (Collection<Integer> signalSet1 : maxCompatibles) {
//			boolean isCoreMC = true;
//			CoreTest : for (Integer signal : signalSet1) 
//				for (Collection<Integer> signalSet2 : maxCompatibles) {
//					if (signalSet1 == signalSet2) continue;
//					if (signalSet2.contains(signal)) {
//						isCoreMC = false;
//						break CoreTest;
//					}
//				}
//			if (isCoreMC) result.add(signalSet1);
//		}
//		
//		return result;
//	}

	@Override
	public double checkCompatibility(int element1, int element2) {
		
		for (int signal1 : componentOutputs.get(element1))
			for (int signal2 : componentOutputs.get(element2))
				if (!ConditionFactory.getLockedSignalCondition(
						specification, signal1).fulfilled(signal2))
					return -1.0; // incompatible partition blocks
		
		return 1.0; // all signal pairs of these partition blocks are locked
	}

	@Override
	public boolean exceedsMaximalElementCount(Collection<Integer> mc) {
		if (maximumPartitionBlockSize == -1) return false; // unrestricted partition block size
		
		int elementCountOfMC = 0;
		for (int partitionBlock : mc) 
			elementCountOfMC += componentOutputs.get(partitionBlock).size();
		
		if (elementCountOfMC > maximumPartitionBlockSize) 
			return true; // yes
		else return false; // no
	}

}
