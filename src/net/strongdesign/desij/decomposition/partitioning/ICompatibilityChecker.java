/**
 * 
 */
package net.strongdesign.desij.decomposition.partitioning;

/**
 * @author Dominic Wist
 *
 */
interface ICompatibilityChecker {
	
	/*
	 * checks the measure of compatibility of 2 elements
	 */
	double checkCompatibility(int element1, int element2);
	
	/*
	 *  How many elements a maximal compatible can have?
	 */
	boolean exceedsMaximalElementCount(java.util.Collection<Integer> mc);
}
