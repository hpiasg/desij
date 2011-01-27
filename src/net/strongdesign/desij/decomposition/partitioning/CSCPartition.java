package net.strongdesign.desij.decomposition.partitioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.Condition;

public class CSCPartition {
	
	private Map<Integer,STG> output2Component;
	private Map<STG,Set<Integer>> component2Outputs;
	
	// for building the resulting partition:
	// Which components will be consolidated?
	private Map<STG,Set<STG>> component2Components;
	
	// abort condition for consolidating components
	private Condition<Collection<STG>> forbidConsolitionCondition;
	
	public CSCPartition(Collection<STG> components, Condition<Collection<STG>> forbidConsolidation) {
		
		component2Outputs = new HashMap<STG, Set<Integer>>(components.size());
		output2Component = new HashMap<Integer, STG>();
		
		component2Components = new HashMap<STG, Set<STG>>(components.size());
		
		Set<Integer> compOutputs;
		Set<STG> stgSet;
		for (STG comp : components) {
			compOutputs = comp.getSignals(Signature.OUTPUT);
			component2Outputs.put(comp, compOutputs);
			
			for (Integer output : compOutputs)
				output2Component.put(output, comp);
			
			stgSet = new HashSet<STG>();
			stgSet.add(comp);
			component2Components.put(comp, stgSet);
		}
		
		forbidConsolitionCondition = forbidConsolidation;
	}
	
	public boolean useSignalForComponent(Integer output, STG component) {
		
		STG auxiliaryComponent = output2Component.get(output);
		
		if (auxiliaryComponent == component) return true; // should be impossible, actually 
		
		Set<STG> componentConsolidationSet = 
			new HashSet<STG>(component2Components.get(component));
		componentConsolidationSet.addAll(component2Components.get(auxiliaryComponent));
		
		// Is this component consolidation allowed?
		if ( forbidConsolitionCondition.fulfilled(componentConsolidationSet) )
			return false; // no component consolidation --> conflict can not be avoided
		
		for (STG comp : componentConsolidationSet) 
			component2Components.put(comp, componentConsolidationSet);
		
		return true;
	}
	
	public boolean signalAvoidsComponentGrowth(Integer output, STG component) {
		
		Set<STG> consolidatedComponents = component2Components.get(component);
		STG auxiliaryComponent = output2Component.get(output);
		
		// output is already in the resulting partition
		if (consolidatedComponents.contains(auxiliaryComponent))
			return true;
		else
			return false;
		
	}
	
	public Partition getModifiedPartitionFor(STG specification) throws STGException {
		
		// the construction of a new set is necessary to avoid repetitions in allConsolidatedComponents
		Set<Set<STG>> allConsolidatedComponents = 
			new HashSet<Set<STG>>(component2Components.values());
		
		Partition newPartition = new Partition();
		
		for (Set<STG> consolidatedComponents: allConsolidatedComponents) {
			newPartition.beginSignalSet();
			for (STG component: consolidatedComponents) 
				for (Integer compOutput : component2Outputs.get(component)) {
					newPartition.addSignal(specification.getSignalName(compOutput));
				}
		}
		
		return newPartition;
	}
		
}


