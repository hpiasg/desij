

package net.strongdesign.desij.decomposition.partitioning;

import java.util.Collection;

/**
 * @author Dominic Wist
 *
 */
public interface IDisjunctiveCoverFinder {
	
	public Collection<Collection<Integer>> getOptimalPartition();

}
