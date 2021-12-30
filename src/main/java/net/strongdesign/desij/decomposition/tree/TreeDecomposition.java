

package net.strongdesign.desij.decomposition.tree;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.DecompositionEvent;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.PresetTree;

public class TreeDecomposition extends AbstractTreeDecomposition {
	
    public TreeDecomposition(String filePrefix) {
		super(filePrefix);
	}


	protected void decomposeTree(
    		STG stg, 
    		PresetTree<Integer, Collection<Integer>> tree, 
    		Collection<STG> components) 
    throws STGException, IOException {
	
    	logging(stg, DecompositionEvent.TREE_NEW_NODE, tree.getValue());
    	
		
		stg.addUndoMarker(UndoMarker.ENTERED_NODE);
		
    	//Lambdarise signals
    	stg.setSignature(tree.getValue(), Signature.DUMMY);
    	
    	STGInOutParameter stgParam = new STGInOutParameter(stg);
    	new BasicDecomposition(filePrefix, specification).reduce(stgParam);
    	// stg = stgParam.stg; // not necessary for BasicDeco
    	
    	STG currentComponent = stg;
    	
    	//check for non-contractable signals //XXX easier now
    	Collection<Integer> nonContractable = currentComponent.collectUniqueCollectionFromTransitions(
    			ConditionFactory.getSignalOfCondition(tree.getValue()), 
    			CollectorFactory.getSignalCollector());
    	
    	//if there are any add them to descandent nodes
    	if (nonContractable.size() != 0) {
        	logging(stg, DecompositionEvent.TREE_SIGNAL_POSTPONED, nonContractable);
    		for (PresetTree<Integer, Collection<Integer>> child : tree.getSubtrees()) {
    			child.getValue().addAll(nonContractable);
    		}
    	}
    	
    	//component aggregation
    	if (CLW.instance.AGGREGATION.isEnabled())
    		if (! tree.getSubtrees().isEmpty() && stg.getSignals().size() <= CLW.instance.MAX_COMPONENT_SIZE.getDoubleValue()) {
    			aggregateSubtree(tree);
    			logging(stg, DecompositionEvent.AGGR_AGGRD_TREE, null);
    		}
    	
    	//no childs? yes -> add to components
    	if (tree.getSubtrees().isEmpty()) {
    		logging(stg, DecompositionEvent.TREE_FINISHED_LEAF, stg.getSignals());
    		
    		//update input/output signals according to outputs signals in the leafs
    		STG component = currentComponent.clone();
    		updateSignature(tree.getAdditionalValue(), component);
    		components.add(component);
    		stg.undoToMarker(UndoMarker.ENTERED_NODE);
    		return;
    	}
    	
    	//otherwise -> go into the subtrees
    	
    	for (PresetTree<Integer, Collection<Integer>> child : tree.getSubtrees()) {
    		decomposeTree(stg, child, components);
    	}
    	
    	stg.undoToMarker(UndoMarker.ENTERED_NODE);
	}

	
    /**
	 * Aggregates the given tree, i.e. collect all additional signals (the outputs of the components) and merge them
	 * in this tree.
	 * @param tree
	 */
	protected void aggregateSubtree(PresetTree<Integer, Collection<Integer>> tree) {
		//recursion finished, nothing to do
		if (tree.getSubtrees().size() == 0)
			return;
		
		if (tree.getAdditionalValue() == null)
			tree.setAdditionalValue(new HashSet<Integer>());
		
		//first, aggregate subtrees    	
		for (PresetTree<Integer, Collection<Integer>> subtree : tree.getSubtrees()) 
			aggregateSubtree(subtree);
		
		//second, now every subtree is a leaf, merge them in tree
		for (PresetTree<Integer, Collection<Integer>> subtree : tree.getSubtrees()) {    		
			tree.getAdditionalValue().addAll(subtree.getAdditionalValue());
		}    	
		
		//remove children
		tree.getSubtrees().clear();
	}
    
}
