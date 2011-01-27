/**
 * 
 */
package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.List;
import java.util.Collection;
import java.util.Iterator;

import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;

/**
 * @author Dominic Wist
 * Strategy interface for the place insertion in order to 
 * support the avoidance of irreducible CSC conflicts
 */
interface IPlaceHolderInsertionStrategy {
	
	public void initializeTraversal(STG component, List<Transition> ctp);
	public boolean execute(Transition t_en, Transition t_ex) throws STGException;
	public Partition getNewPartition() throws STGException;
	public Collection<STG> getReplacedComponents();
	public int getInsertedPlaceholderTransitionCount();
	public Transition doPlaceHolderInsertion(STG critComp);
	public void doInsertionForSameConflict(Iterator<STG> componentIterator, Transition insertedPlaceHolder);
	public void initialzePartition();
	
}
