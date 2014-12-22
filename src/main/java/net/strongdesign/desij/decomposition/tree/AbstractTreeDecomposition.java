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

package net.strongdesign.desij.decomposition.tree;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.decomposition.AbstractDecomposition;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.DecompositionEvent;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.partitioning.PartitionComponent;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.util.BitsetTree;
import net.strongdesign.util.Pair;
import net.strongdesign.util.PresetTree;

public abstract class AbstractTreeDecomposition extends AbstractDecomposition {
	
	public static STG RootSpecification = null;
    
	
	public AbstractTreeDecomposition(String filePrefix) {
		super(filePrefix);
	}



	protected void updateSignature(Collection<Integer> outputs, STG component) {
		//update input/output signals according to outputs signals in the leafs
		for (Integer signal : component.getSignals(Signature.OUTPUT)) {						
			if (! outputs.contains(signal))
				component.setSignature(signal, Signature.INPUT);
		}
		for (Integer signal : component.getSignals(Signature.INPUT)) {						
			if (outputs.contains(signal))
				component.setSignature(signal, Signature.OUTPUT);
		}
		
	}

	public Collection<STG> decompose(STG stg, Partition partition) throws STGException, IOException {
		
		logging(stg, DecompositionEvent.TREE_START, null);		
	
		this.specification = stg; // will be changed during the decomposition process --> former versions can be recovered using the Undo-Stack
		RootSpecification = stg.clone(); // important to know the overall specifications, esp. for the BasicDecompositions used during the Tree-Traversal
		
		//The final components
		Collection<STG> components = new LinkedList<STG>();
			
		
		Collection<Pair<Collection<Integer>, Collection<Integer>>> leafs = 
			new LinkedList<Pair<Collection<Integer>, Collection<Integer>>>();
		
		//Determine the signals which should be contracted in every component
		//and add the outputs of the components to the leafs		
		for (PartitionComponent actSignals : partition.getPartition()) {
			leafs.add( new Pair<Collection<Integer>, Collection<Integer>>( 
						stg.getSignalNumbers(Partition.getReversePartition(actSignals.getSignals(), stg)),
						stg.getSignalNumbers(actSignals.getSignals())));
		}
		
		//the initial decomposition tree
		PresetTree<Integer, Collection<Integer>> tree = null;

		
		int method = 0;
		
		if (CLW.instance.DECOMPOSITION_TREE.getValue().equals("combined")) method = 1;
		else if (CLW.instance.DECOMPOSITION_TREE.getValue().equals("random")) method = 2;
		
//		tree = optimiseDecompositionTree(PresetTree.buildTreeLeafInfo(leafs, method), null);	
		tree = BitsetTree.buildTreeLeafInfo(leafs, method);
		
		if (tree != null) {
			logging(stg, DecompositionEvent.TREE_CREATED, tree.getSize());
			logging(stg, DecompositionEvent.TREE_CREATED_VALUE, tree);
		
			decomposeTree(stg, tree, components);
			
			logging(stg, DecompositionEvent.FINISHED, null);
			
			RootSpecification = null; // tidy up, such that it doesn't influence other decompositions
						
			return components;
		}
		else { // tree == null
			RootSpecification = null; // tidy up, that it doesn't influence other decompositions
			return new BasicDecomposition(filePrefix).decompose(stg, partition);
		}
		
	}
	
	

	
    protected abstract void decomposeTree(STG stg, PresetTree<Integer, Collection<Integer>> tree, Collection<STG> components) throws STGException, IOException ;
	
    
   
	@Override
	/**
	 * @deprecated
	 */
	public final List<Transition> reduce(STGInOutParameter stg) {
		return null;
	}

	
	/**
     * Optimises a preset tree, i.e. removes empty inner nodes by adding its children to its parent.
     * Doing this, the order of the tree may be changed.
     * @param tree
     * @param parent must be null for direct calls
     * @return tree               
     */
    protected  PresetTree<Integer, Collection<Integer>> optimiseDecompositionTree(
    		PresetTree<Integer, Collection<Integer>> tree, PresetTree<Integer, Collection<Integer>> parent ) {
    	
    	//only for the root node which must not be optimised
    	if (parent == null) {    
    		for (PresetTree<Integer, Collection<Integer>> child : new LinkedList<PresetTree<Integer, Collection<Integer>>>(tree.getSubtrees()))
    			optimiseDecompositionTree(child, tree);
        	return tree;
    	}
    	
    	//empty leafs are allowed
    	if (tree.getSubtrees().isEmpty())
    		return null;
    	
    	//now the real work
    	if (tree.getValue().isEmpty()) {
    		parent.getSubtrees().remove(tree);
    		parent.getSubtrees().addAll(tree.getSubtrees());
    	}

   		for (PresetTree<Integer, Collection<Integer>> child : new LinkedList<PresetTree<Integer, Collection<Integer>>>(tree.getSubtrees()))
   			optimiseDecompositionTree(child, tree);
    	
    	//the resulting tree is only returned for convenience from the first call with null as second parameter
    	//subsequent calls will not work with the return value
    	return null;
    }
    
    
	
}
