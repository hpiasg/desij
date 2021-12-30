

package net.strongdesign.desij.decomposition.partitioning;

import net.strongdesign.stg.STGException;

/**
 * @author Dominic Wist
 * Interface to an agent who can find good partitions
 */
public interface IPartitioningStrategy {
	// returns an optimized Partition
	public Partition improvePartition(Partition oldPartition) throws STGException, PartitioningException;
}
