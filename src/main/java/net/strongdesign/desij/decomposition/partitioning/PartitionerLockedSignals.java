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
import java.util.TreeSet;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 *
 */
public class PartitionerLockedSignals implements IPartitioningStrategy,
		ICompatibilityChecker, IBasicStrategy {
	
	private STG specification;
	private TransitiveLockDetector lockClassCalculator;
	
	private List<Collection<Integer>> componentOutputs; // initialized through improvePartition()
	private List<Collection<Integer>> relevantSignals; // initialized through improvePartition()
	
	private double lockedDegree = 0.8; // must be defined by the user and should be above 0.5 --> no bigger lock class possible
	private int    maximumPartitionBlockSize = -1; // must be defined by the user; -1 means unrestricted
	
	public PartitionerLockedSignals(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
		
		// necessary for checkCompatibilty
		lockClassCalculator = new TransitiveLockDetector(stg);
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#checkCompatibility(int, int)
	 * returns -1.0 for incompatible pairs and for compatibles the percentage or degree resp. of locked pairs
	 */
	@Override
	public double checkCompatibility(int element1, int element2) {
		
		if (element1 == element2) return 1.0; // compatibility is reflexive
						
		Collection<Integer> signalsBelongingToElement1andElement2 = 
			new TreeSet<Integer>(relevantSignals.get(element1));
		signalsBelongingToElement1andElement2.addAll(componentOutputs.get(element1));
		signalsBelongingToElement1andElement2.addAll(relevantSignals.get(element2));
		signalsBelongingToElement1andElement2.addAll(componentOutputs.get(element2));
		
		Collection<Collection<Integer>> transitiveLockClasses = 
			lockClassCalculator.getTransitiveLockClasses(signalsBelongingToElement1andElement2);
		
		int necessarySignalCount = (int)(Math.round(signalsBelongingToElement1andElement2.size() * lockedDegree)); 
		
		for (Collection<Integer> lockClass : transitiveLockClasses)
			if (lockClass.size() >= necessarySignalCount)
				return (double)(lockClass.size()) / (double)(signalsBelongingToElement1andElement2.size());
		
		return -1.0; // not lock-compatible
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.ICompatibilityChecker#exceedsMaximalElementCount(java.util.Collection)
	 */
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
	 * 
	 * @param oldPartition
	 */
	public void initializationForImprovement(List<Collection<Integer>> componentOutputs, 
			List<Collection<Integer>> relevantSignals, Partition oldPartition) throws STGException {
		this.componentOutputs = componentOutputs;
		this.relevantSignals = relevantSignals;
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

	@Override
	public boolean areCompatible(int element1, int element2) {
		if (checkCompatibility(element1, element2) > 0.0)
			return true;
		else
			return false;
	}

}
