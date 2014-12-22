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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 *
 */
public class PartitionerIrreducibleCSCAvoidance implements IPartitioningStrategy, IBasicStrategy {
	
	private STG specification;
	
	private Map<Set<Pair<Transition, Transition>>, Collection<STG>> irreducibleCSCConflicts;
	private CSCPartition partition;
	
	private boolean eachConflictAvoided = true; // optimistic assumption
	
	// How many signals each partition member can have at maximum 
	private int signalLimit = 20; // should be defined by the use; -1 means unlimited	
	
	private List<Collection<Integer>> componentOutputs; // just for combined heuristic needed, not in stand-alone mode

	/**
	 * Constructor
	 */
	public PartitionerIrreducibleCSCAvoidance(STG stg) throws STGException {
		specification = stg;
		if (specification == null) 
			throw new STGException("No specification is given!");
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy#improvePartition(net.strongdesign.stg.Partition)
	 */
	public Partition improvePartition(Partition oldPartition) throws STGException, PartitioningException {
		
		if (oldPartition.getPartition().size() < 2) return oldPartition;
				
		initializationForImprovement(null, null, oldPartition);
		
		// build the new Partition
		Partition result = partition.getModifiedPartitionFor(specification);
		
		if (eachConflictAvoided)
			return result;
		else
			throw new PartitioningException("Not each irreducible CSC conflict could be avoided!", result);
	}

	private boolean avoidConflict(STG criticalComponent, Pair<Transition, Transition> splittablePair) {
		
//		ICSCSolvingSignalFinder signalFinder = new SimpleSignalFinder(
//				specification, criticalComponent, splittablePair, partition);
		ICSCSolvingSignalFinder signalFinder = new LPSignalFinder(
				specification, criticalComponent, splittablePair, partition);
		
		Integer signalForAvoidance = signalFinder.execute();
		
		if (signalForAvoidance != null) 
			if ( partition.useSignalForComponent(signalForAvoidance, criticalComponent) )
				return true;
		
		return false;
	}

	@Override
	public void initializationForImprovement(List<Collection<Integer>> componentOutputs, 
			List<Collection<Integer>> relevantSignals, Partition oldPartition) throws STGException {
		
		IrreducibleCSCDetector cscDetector;
		try {
			cscDetector = new IrreducibleCSCDetector(specification, "DecoForPartitioning", oldPartition);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		irreducibleCSCConflicts = cscDetector.getIrrCSCConf2Components();
		partition = new CSCPartition(cscDetector.getConflictingComponents(), 
				ConditionFactory.getTooManySignalsCondition(signalLimit));
		
		for (Set<Pair<Transition, Transition>> conflict : irreducibleCSCConflicts.keySet()) {
			
			Collection<STG> affectedComponents = new HashSet<STG>(irreducibleCSCConflicts.get(conflict)); 
						
			for (Pair<Transition, Transition> splittablePair : conflict) {
				for (STG criticalComponent : irreducibleCSCConflicts.get(conflict)) {
															
					if (!affectedComponents.contains(criticalComponent)) // conflict already solved
						continue;
					
					if (avoidConflict(criticalComponent, splittablePair))
						affectedComponents.remove(criticalComponent);
					
				}
				if (affectedComponents.isEmpty()) break; // runtime optimization
			}
			
			if (!affectedComponents.isEmpty()) eachConflictAvoided = false;
		}
		
		// just for combined heuristic, otherwise it is "null"
		this.componentOutputs = componentOutputs; 		
	}

	@Override
	public boolean areCompatible(int element1, int element2) {
		
		if (element1 == element2) return true;
		
		Collection<Integer> outputs1 = componentOutputs.get(element1);
		Collection<Integer> outputs2 = componentOutputs.get(element2);
		
		if (outputs1 != null && outputs2 != null) 
			if (outputs1.size() > 0 && outputs2.size() > 0)
				return partition.getCompatibility(outputs1.iterator().next(), 
						outputs2.iterator().next());
		
		return false;
	}

}
