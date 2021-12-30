

package net.strongdesign.desij.decomposition;

import net.strongdesign.stg.STG;

/**
 * @author Dominic Wist
 * To Implement an InOut Parameter for reduce() method of Decomposition
 * especially important for LazyDecomposition
 */
public final class STGInOutParameter {
	public STG stg;
	
	public STGInOutParameter(STG stg) {
		this.stg = stg;
	}
}
