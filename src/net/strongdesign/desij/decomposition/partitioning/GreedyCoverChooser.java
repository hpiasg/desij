/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Dominic Wist
 * Find a disjunctive cover for all old partition blocks by using MCs as far as possible 
 * and if necessary by soften the criteria of maximality
 */
public class GreedyCoverChooser implements IDisjunctiveCoverFinder {
	
	private MaximalCompatibleFinder mcFinder;
	
	private List<Collection<Integer>> mcs;
	private class mccmp implements Comparator<Collection<Integer>>{
		private List<Collection<Integer>> maxCompatibles; 
		
        public mccmp(List<Collection<Integer>> maxCompatibles){
        	this.maxCompatibles = maxCompatibles;
        }

        public int compare(Collection<Integer> mc1,Collection<Integer> mc2){
        	
        	if (maxCompatibles.isEmpty()) return 0; // no comparison possible
        	if (mc1 == mc2) return 0; // equal
        	
        	int mc1intersectedElements = 0;
			Collection<Integer> mc1ElementsNotShared = new LinkedList<Integer>(mc1);
			for (Collection<Integer> compatible : maxCompatibles) {
				if (compatible == mc1) continue;
				for (Integer block : compatible)
					if (mc1.contains(block))
						mc1ElementsNotShared.remove(block);
				if (mc1ElementsNotShared.isEmpty()) break;
			}
			mc1intersectedElements = mc1.size() -  mc1ElementsNotShared.size();
			
			int mc2intersectedElements = 0;
			Collection<Integer> mc2ElementsNotShared = new LinkedList<Integer>(mc2);
			for (Collection<Integer> compatible : maxCompatibles) {
				if (compatible == mc2) continue;
				for (Integer block : compatible)
					if (mc2.contains(block))
						mc2ElementsNotShared.remove(block);
				if (mc2ElementsNotShared.isEmpty()) break;
			}
			mc2intersectedElements = mc2.size() -  mc2ElementsNotShared.size();
			
			if (mc1intersectedElements < mc2intersectedElements)
				return -1;
			else if (mc1intersectedElements > mc2intersectedElements)
				return 1;
			else 
				return 0;
        }
    }
	
	public GreedyCoverChooser(MaximalCompatibleFinder mcFinder) {
		this.mcFinder = mcFinder;
		if (mcFinder != null)
			mcs = new LinkedList<Collection<Integer>>(mcFinder.getCompatibles());
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.partitioning.IDisjunctiveCoverFinder#getOptimalPartition()
	 */
	@Override
	public Collection<Collection<Integer>> getOptimalPartition() {
		
		if (mcFinder == null) return null;
		
		Collection<Collection<Integer>> partitionCandidate = new ArrayList<Collection<Integer>>();
		
		Collections.sort(mcs, new mccmp(mcs));
		
		while ( !mcs.isEmpty() ) {
			Collection<Integer> curMC = mcs.remove(0); // first element
			
			Collection<Integer> intersectedElements = getIntersectedElementsWithMCs(curMC);
			curMC.removeAll(intersectedElements);
			
			if (!curMC.isEmpty())
				partitionCandidate.add(curMC);
			
			if (!intersectedElements.isEmpty()) // curMC was no core MC, so intersections in mcs may have changed
				Collections.sort(mcs, new mccmp(mcs));
		}
		
		// all old partition blocks should be covered, except for the incompatibles
		
		// add the incompatible one-member sets to the result --> now we have a partition candidate
		partitionCandidate.addAll(mcFinder.getIncompatibleOneMemberSets());
		
		return partitionCandidate;
	}

	private Collection<Integer> getIntersectedElementsWithMCs(Collection<Integer> mc) {
		
		Set<Integer> result = new HashSet<Integer>();
		for (Collection<Integer> compatible : mcs) {
			if (compatible == mc) continue; // actually impossible, since already removed
			for (Integer block : compatible)
				if (mc.contains(block))
					result.add(block);
		}
		return result;
	}

}
