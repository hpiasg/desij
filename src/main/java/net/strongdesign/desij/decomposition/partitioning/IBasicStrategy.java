

package net.strongdesign.desij.decomposition.partitioning;

import java.util.Collection;
import java.util.List;

import net.strongdesign.stg.STGException;

/**
 * @author Dominic Wist
 *
 */
public interface IBasicStrategy {
	public void initializationForImprovement(List<Collection<Integer>> componentOutputs, 
			List<Collection<Integer>> relevantSignals, Partition oldPartition) throws STGException;
	public boolean areCompatible(int element1, int element2);
}
