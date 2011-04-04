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

/**
 * @author Dominic Wist
 *
 */
public class CSCPartition {
	
	private Map<Integer,STG> output2Component;
	private Map<STG,Set<Integer>> component2Outputs;
	
	// for building the resulting partition:
	// Which components will be consolidated?
	private Map<STG,Set<STG>> component2Components;
	
	// abort condition for consolidating components
	private Condition<Collection<STG>> forbidConsolitionCondition;
	
	// information used by a combined partitioning heuristic
	// a true entry means compatible and no entry (i.e. null) means not compatible
	// for each compatible pair: there will always be both symmetric entries 
	private Map<STG,Map<STG,Boolean>> compatibilityChart;
	
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
		
		compatibilityChart = new HashMap<STG, Map<STG,Boolean>>();
	}
	
	public boolean useSignalForComponent(Integer output, STG component) {
		
		STG auxiliaryComponent = output2Component.get(output);
		
		setCompatibilty(component, auxiliaryComponent); // information used by a combined heuristic
		
		if (auxiliaryComponent == component) return true; // should be impossible, actually 
		
		Set<STG> componentConsolidationSet = 
			new HashSet<STG>(component2Components.get(component));
		componentConsolidationSet.addAll(component2Components.get(auxiliaryComponent));
		
		// Is this component consolidation allowed?
		if ( forbidConsolitionCondition.fulfilled(componentConsolidationSet) )
			return false; // no component consolidation --> conflict can not be avoided
		
		// update all necessary entries in component2Components
		for (STG comp : componentConsolidationSet) 
			component2Components.put(comp, componentConsolidationSet);
		
		return true;
	}
	
	
	/**
	 * Ensures the consistency of compatibilityChart
	 * that means for one compatible pair there are two symmetric entries
	 * 
	 * @param component
	 * @param auxiliaryComponent
	 */
	private void setCompatibilty(STG component, STG auxiliaryComponent) {
		
		// first entry
		Map<STG, Boolean> entry = compatibilityChart.get(component);
		if (entry == null) {
			entry = new HashMap<STG, Boolean>();
			compatibilityChart.put(component, entry);
		}
 		entry.put(auxiliaryComponent, true);
			
		// symmetric entry
 		entry = compatibilityChart.get(auxiliaryComponent);
 		if (entry == null) { // must correlate to "if (entry == null)" from above 
			entry = new HashMap<STG, Boolean>();
			compatibilityChart.put(auxiliaryComponent, entry);
			
 		}
 		entry.put(component, true);
	
	}
	
	
	/**
	 * Information whether component1 and component2 are in a "critical <--> delay component"-relation
	 * it is for PartitionerIrreducibleCSCAvoidance
	 * 
	 * @param output1
	 * @param output2
	 * @return
	 */
	public boolean getCompatibility(int output1, int output2) {
		
		STG component1 = output2Component.get(output1); // according to the oldPartition
		STG component2 = output2Component.get(output2); // according to the oldPartition
		
		if (component1 == component2) return true; // actually impossible --> just to assure
		
		Map<STG,Boolean> firstLevelAccess = compatibilityChart.get(component1);
		if (firstLevelAccess != null) 
			if (firstLevelAccess.get(component2) != null)
				return true;
		
		return false;
	}

	/**
	 * Helper routine for delay transition detector
	 * @param output
	 * @param component
	 * @return
	 */
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


