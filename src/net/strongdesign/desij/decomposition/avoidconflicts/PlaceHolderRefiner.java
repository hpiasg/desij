/**
 * 
 */
package net.strongdesign.desij.decomposition.avoidconflicts;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
/**
 * @author dwist
 * interface or strategy resp. for the placeholder refinement
 */
abstract class PlaceHolderRefiner {
	
	/*
	 * not in use
	 */
	public PlaceHolderRefiner() {
		super();
	}
	
	public abstract void execute(STG stg) throws STGException;
}
