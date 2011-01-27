/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 *
 */
public class PartitionerIrreducibleCSCAvoidance implements IPartitioningStrategy {
	
	private STG specification;
	
	private Map<Set<Pair<Transition, Transition>>, Collection<STG>> irreducibleCSCConflicts;
	private CSCPartition partition;
	
	private boolean eachConflictAvoided = true; // optimistic assumption
	
	// How many signals each partition member can have at maximum 
	private int signalLimit = 20; // should be defined by the use; -1 means unlimited	

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
				if (affectedComponents.isEmpty()) break; // runtime optimisation
			}
			
			if (!affectedComponents.isEmpty()) eachConflictAvoided = false;
		}
		
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

}
