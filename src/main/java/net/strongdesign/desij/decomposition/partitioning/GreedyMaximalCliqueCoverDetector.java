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
