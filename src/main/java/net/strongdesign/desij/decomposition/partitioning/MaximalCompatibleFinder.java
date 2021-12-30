

package net.strongdesign.desij.decomposition.partitioning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Dominic Wist
 * MC finder according to "anlagerndes Verfahren" from (Unger 1969)
 */
class MaximalCompatibleFinder {
	
	private ICompatibilityChecker compatibilityChecker;
	
	private double[][] pairChart;
	// the list of all MCs is the union of compatibleList + incompatibleList
	private List<Collection<Integer>> compatibleList;
	private List<Collection<Integer>> incompatibleList;
	
	private Map<Collection<Integer>, Double> normalizedCompatibleFactors;

	/**
	 * build pair chart, i.e. find compatible and incompatible pairs
	 */
	public MaximalCompatibleFinder(int elementCount, ICompatibilityChecker compChecker) {
		compatibilityChecker = compChecker;
		
		fillPairChart(elementCount); // fills and builds the pair chart	
		findMCs(elementCount-1); // finding MCs -> anlagerndes Verfahren
		computeCompatibleFactors(); // enables assessment of the MCs
	}
	
	// **************** access methods *************************
	
	/*
	 * CAUTION: returns the compatibleList by reference
	 */
	Collection<Collection<Integer>> getCompatibles() {
		return compatibleList;
	}
	
	/*
	 * returns all MCs with compatibleFactor of "compFactor" 
	 * CAUTION return the MCs by reference, but not by value (i.e. not a copy of each MC)
	 */
	Collection<Collection<Integer>> getCompatiblesFromCompFactor(double compFactor) {
		List<Collection<Integer>> result = new ArrayList<Collection<Integer>>();
		
		for (Collection<Integer> mc : compatibleList) 
			if (normalizedCompatibleFactors.get(mc) == compFactor)
				result.add(mc);
		
		return result;
	}
	
	/*
	 * CAUTION: returns the compatibleList by reference
	 */
	Collection<Collection<Integer>> getIncompatibleOneMemberSets() {
		return incompatibleList;
	}
	
	/*
	 * returns the compatibleFactor of maxCompatible - normalized to pairwise compatibility
	 */
	double getNormalizedCompatibleFactor(Collection<Integer> maxCompatible) {
		Double result = normalizedCompatibleFactors.get(maxCompatible);
		if (result == null) return 0;
		else return result.doubleValue();
	}
	
	
	// ******************* private methods ***********************
	
	/*
	 * Initializes the pair chart
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
	
	/*
	 * finding Maximal Compatibles from Compatible Pairs (cf. Unger69)
	 */
	private void findMCs(int columnRowCount) {
		int i = columnRowCount-1; // current column -> set to the rightmost column
		
		// 1. initiate compatible list (c-list)
		this.compatibleList = new ArrayList<Collection<Integer>>();
		boolean isInitialized = false;
		while ( (i >= 0) && !isInitialized ) {
			for (int j = i+1; j <= columnRowCount; j++)
				if (pairChart[i][j] > 0) { // compatible pair
					TreeSet<Integer> compatiblePair = new TreeSet<Integer>();
					compatiblePair.add(i); 
					compatiblePair.add(j);
					if ( !compatibilityChecker.exceedsMaximalElementCount(compatiblePair) ) {
						compatibleList.add(compatiblePair);
						isInitialized = true;
					}
				}
			i--;
		}
				
		if (isInitialized) {
			// 2. proceed pair chart to the left ...
			for ( ; i >= 0; i--) {
				// set consisting of all elements whose entries in column i specify compatible pairs
				Collection<Integer> Si = new TreeSet<Integer>();
				for (int j = i+1; j <= columnRowCount; j++) 
					if (pairChart[i][j] > 0) Si.add(j); // compatible pair
				
				List<Collection<Integer>> newCListMembers = new ArrayList<Collection<Integer>>();
				// intersect Si with each member of the current c-list
				Collection<Integer> intersectionSiAndCmember;
				for (Collection<Integer> cMember: compatibleList) {
					intersectionSiAndCmember = new TreeSet<Integer>(Si);
					intersectionSiAndCmember.retainAll(cMember);
					if (intersectionSiAndCmember.size() > 1) {
						// union of intersected Si and i
						intersectionSiAndCmember.add(i);
						if ( !compatibilityChecker.exceedsMaximalElementCount(intersectionSiAndCmember) )
							newCListMembers.add(intersectionSiAndCmember);
					}
				}
				
				// remove all subsets of the newCListMembers in the current c-list
				for (Collection<Integer> newCMember: newCListMembers) {
					List<Collection<Integer>> forRemoval = new LinkedList<Collection<Integer>>();
					for (Collection<Integer> currentCMember: compatibleList) 
						if (newCMember.containsAll(currentCMember))
							forRemoval.add(currentCMember);
					compatibleList.removeAll(forRemoval);
				}
				// remove all subsets within the list newCListMembers
				List<Collection<Integer>> newCListMemberSubsets = new ArrayList<Collection<Integer>>();
				for (Collection<Integer> newCMember: newCListMembers)
					for (Collection<Integer> newCMemberToDelete: newCListMembers)
						if ( newCMember.containsAll(newCMemberToDelete) && 
								!newCMemberToDelete.containsAll(newCMember) ) // newCMember != newCMemberToDelete
							newCListMemberSubsets.add(newCMemberToDelete);
				newCListMembers.removeAll(newCListMemberSubsets);
				// add the newCMembers to c-list
				compatibleList.addAll(newCListMembers);
				
				// add pairs consisting of i and any member of Si that did not appear in any of the intersections
				for (Collection<Integer> newCMember: newCListMembers)
					Si.removeAll(newCMember);
				
				Collection<Integer> newPair;
				for (int elementOfSi: Si) {
					newPair = new TreeSet<Integer>();
					newPair.add(i);
					newPair.add(elementOfSi);
					if ( !compatibilityChecker.exceedsMaximalElementCount(newPair) )
						compatibleList.add(newPair);
				}				
			}
		} // end if (isInitialized)
		
		// 3. add the incompatible one-member sets
		Collection<Integer> incompatibleMembers = new TreeSet<Integer>();
		for (int j = 0; j <= columnRowCount; j++) 
			incompatibleMembers.add(j);
			
		for (Collection<Integer> cMember: compatibleList)
			incompatibleMembers.removeAll(cMember);
		
		this.incompatibleList = new ArrayList<Collection<Integer>>(incompatibleMembers.size());
		// make a one-element set for each incompatible member
		Collection<Integer> oneElementSet;
		for (int incompMember: incompatibleMembers) {
			oneElementSet = new TreeSet<Integer>();
			oneElementSet.add(incompMember);
			incompatibleList.add(oneElementSet);			
		}
	}
	
	/*
	 * Computes the compatibility degree of all c-list members -- normalized to pairwise compatibility
	 */
	private void computeCompatibleFactors () {
		this.normalizedCompatibleFactors = 
			new HashMap<Collection<Integer>, Double>(compatibleList.size());
		
		for (Collection<Integer> cMember: compatibleList) {
			
			double cumulatedCompatibleFactor = 0.0;
			int pairCount = 0;   
			
			List<Integer> cMemberList = new ArrayList<Integer>(cMember);
			for (int i = 0; i < cMemberList.size()-1; i++) 
				for (int j = i+1; j < cMemberList.size(); j++) {
					cumulatedCompatibleFactor += pairChart[cMemberList.get(i)][cMemberList.get(j)];
					pairCount++;
				}
			
			normalizedCompatibleFactors.put(cMember, cumulatedCompatibleFactor / pairCount);			
		}		
	}

}
