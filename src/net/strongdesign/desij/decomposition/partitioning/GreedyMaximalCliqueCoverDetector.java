/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Dominic Wist
 * solves a maximal clique cover problem
 */
public class GreedyMaximalCliqueCoverDetector {
	
	private ICompatibilityChecker compatibilityChecker;
	
	private double[][] pairChart;
	private Collection<Integer> nodesToCover;
	
	public GreedyMaximalCliqueCoverDetector(int elementCount, ICompatibilityChecker compChecker) {
		compatibilityChecker = compChecker;
		fillPairChart(elementCount); // fills and builds the pair chart
		
		nodesToCover = new LinkedList<Integer>();
		for (int i = 0; i < elementCount; i++)
			nodesToCover.add(i);
	}
	
	public Collection<Collection<Integer>> getDisjunctiveCover() throws PartitioningException {
		
		Collection<Collection<Integer>> partitionCandidate = new ArrayList<Collection<Integer>>();
		
		while ( !nodesToCover.isEmpty() ) {
			Set<Integer> mc = findMaximalClique();
			if (!mc.isEmpty()) {
				nodesToCover.removeAll(mc);
				partitionCandidate.add(mc);
			}
			else 
				throw new PartitioningException("Partition cannot be improved, since output limit per member is too small.", null);
		}
		
		return partitionCandidate;
	}
	
	


	// ******************* private methods ***********************
	
	/*
	 * Initializes the pair chart, i.e.\ specifies edges of the graph for clique finding
	 */
	private void fillPairChart(int elementCount) {
		pairChart = new double[elementCount][elementCount];
		// initialize with zeros
		for (int i = 0; i < elementCount; i++)
			for (int j = 0; j < elementCount; j++)
				pairChart[i][j] = 0;
		
		// build pair chart
		for (int i = 0; i < elementCount-1; i++)
			for (int j = i+1; j < elementCount; j++)
				pairChart[i][j] = compatibilityChecker.checkCompatibility(i,j);
	}
	
	private Set<Integer> findMaximalClique() {
		Set<Integer> result = new HashSet<Integer>();
		
		for (Integer potentialMCMember : nodesToCover) {
			boolean isCompatibleWithResult = true;
			for (Integer resultElement : result)
				if (potentialMCMember > resultElement)
					if (pairChart[resultElement][potentialMCMember] < 0) { // if incompatible
						isCompatibleWithResult = false;
						break;
					}
				else if (potentialMCMember < resultElement)
					if (pairChart[potentialMCMember][resultElement] < 0) { // if incompatible
						isCompatibleWithResult = false;
						break;
					}
				else continue;
			if (isCompatibleWithResult) {
				result.add(potentialMCMember);
				if (compatibilityChecker.exceedsMaximalElementCount(result))
					result.remove(potentialMCMember);
			}
		}
		
		return result;
	}

}
