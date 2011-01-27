package net.strongdesign.desij.decomposition.partitioning;

import net.strongdesign.stg.Partition;

public class PartitioningException extends Exception {
	private static final long serialVersionUID = -1548694067654620156L;
	
	private Partition inOptimalPartition;

	public PartitioningException(String message, Partition inOptimalPartition) {
		super(message);
		this.inOptimalPartition = inOptimalPartition;
	}
	
	public Partition getPartitionSoFar() {
		return inOptimalPartition;
	}

}
