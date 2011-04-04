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

/*
 * Created on 29.09.2004
 *
 */
package net.strongdesign.stg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import net.strongdesign.stg.traversal.*;
import net.strongdesign.util.FileSupport;

import net.strongdesign.desij.decomposition.partitioning.IPartitioningStrategy;
import net.strongdesign.desij.decomposition.partitioning.PartitioningException;

/**
 * A Partition represents a partition of the output signals of 
 * a STG. 
 * 
 * <p>Observe the designated use cycle of an instance of Partition.<br>
 * 1. Start with an empty partition with {@link #Partition()}<br>
 * 2. Add Signals to the current signal set of the partition with {@link #addSignal(Signal)}<br>
 * 3. Start a new signal set with {@link #beginSignalSet()}<br>
 * 4. Repeat 2. and 3. as often as needed
 * 
 * <p>Exception will be thrown if a non-output signal is added or a signal is added twice. * 
 * 
 * @author Mark Schaefer
 */
public class Partition {
	protected Set<String> signals;
	protected List<List<String>> partition;
	protected List<String> actNewSignalSet;
	boolean started;

	/**
	 * Constructs an empty partition.	 
	 */
	public Partition() {
		partition = new LinkedList<List<String>>();
		signals = new HashSet<String>();
		started = false;
	}

	public String toString() {
		String result = new String();
		for (List<String> actPartition : partition)
			result = result + actPartition;

		return result;
	}


	public static Partition fromString(STG stg, String param) throws STGException {
		Partition partition = new Partition();

		Collection<String> signalNames = stg.getSignalNames(stg.getSignals());

		for (String actPartition : param.split("/")) {

			if (actPartition.equals("auto")) {
				return getFinestPartition(stg, partition);				
			}
			else {
				partition.beginSignalSet();
				for (String actSignalName : actPartition.split(":")) {
					for (String signalName : signalNames) {
						if (signalName.matches(actSignalName)) {
							partition.addSignal(signalName);	
						}
					}
				}
			}
		}


		return partition;
	}
	
	public static Partition getRoughestPartition(STG stg, Partition partition) throws STGException {
		
		Set<Integer> outputs = stg.getSignals(Signature.OUTPUT);
		
		if (partition == null) {
			partition = new Partition();	
		}
		else {
			for (List<String> block : partition.getPartition()) {
				outputs.removeAll(stg.getSignalNumbers(block));
			}
		}
		
		partition.beginSignalSet();
		
		for (Integer signal : outputs)
			partition.addSignal(stg.getSignalName(signal));
		
		return partition;
	}


	public static Partition getFinestPartition(STG stg, Partition partition) throws STGException {

		Set<Integer> outputs = stg.getSignals(Signature.OUTPUT);

		if (partition == null) {
			partition = new Partition();	
		}
		else {
			for (List<String> p : partition.getPartition()) {
				outputs.removeAll(stg.getSignalNumbers(p));
			}
		}


		l: for (Integer newSignal : outputs) {

			String newName = stg.getSignalName(newSignal);

			for (List<String> set : partition.partition) {
				for (String oldSignal : set) {
					//look for conflict
					List<Set<Node>> oldParents = stg.collectFromTransitions(
							ConditionFactory.getSignalOfCondition(stg.getSignalNumber(oldSignal)), 
							CollectorFactory.getParentsCollector());
					List<Set<Node>> newParents = stg.collectFromTransitions(
							ConditionFactory.getSignalOfCondition(newSignal), 
							CollectorFactory.getParentsCollector());

					Set<Node> oP = new HashSet<Node>();
					for (Set<Node> o : oldParents)
						oP.addAll( o );

					//if yes add to same signal set and proceed with next signal
					for (Set<Node> o : newParents)
						for (Node t :  o)
							if (oP.contains(t))  {
								set.add(newName);
								partition.signals.add(newName);
								continue l;								
							}

				}
			}
			partition.beginSignalSet();
			partition.addSignal(newName);			

		}

		return partition;
	}
	
	public static Partition getMultipleSignalUsagePartition(STG stg) throws STGException {
		IPartitioningStrategy partitioner =
			new net.strongdesign.desij.decomposition.partitioning.PartitionerInputConsolidation(stg);
		
		Partition result = null;
		try {
			result = partitioner.improvePartition(getFinestPartition(stg, null));
		}
		catch (PartitioningException e) {
			// maybe do something against this problem or leave it
			result = e.getPartitionSoFar();
		} 
		
		return result;
	}
	
	public static Partition getCSCAvoidancePartition(STG stg) throws STGException {
		
		IPartitioningStrategy partitioner =
			new net.strongdesign.desij.decomposition.partitioning.PartitionerIrreducibleCSCAvoidance(stg);
		
		Partition result = null;
		try {
			result = partitioner.improvePartition(getFinestPartition(stg, null));
		}
		catch (PartitioningException e) {
			// maybe do something against this problem or leave it
			result = e.getPartitionSoFar();
		} 
		
		return result;
	}
	
	public static Partition getPartitionConcurrencyReduction(STG stg) throws STGException {
		IPartitioningStrategy partitioner =
			new net.strongdesign.desij.decomposition.partitioning.PartitionerSequentialMerger(stg);
		
		Partition result = null;
		try {
			result = partitioner.improvePartition(getFinestPartition(stg, null));
		}
		catch (PartitioningException e) {
			// maybe do something against this problem or leave it
			result = e.getPartitionSoFar();
		} 
		
		return result;
	}
	
	public static Partition getLockedSignalsPartition(STG stg) throws STGException {
		IPartitioningStrategy partitioner =
			new net.strongdesign.desij.decomposition.partitioning.PartitionerLockedSignals(stg);
		
		Partition result = null;
		try {
			result = partitioner.improvePartition(getFinestPartition(stg, null));
		}
		catch (PartitioningException e) {
			// maybe do something against this problem or leave it
			result = e.getPartitionSoFar();
		} 
		
		return result;
	}
	
	public static Partition getBestPartition(STG stg) throws STGException {
				
		IPartitioningStrategy partitioner =
			new net.strongdesign.desij.decomposition.partitioning.PartitionerCombineAll(stg);
		
		Partition result = null;
		try {
			result = partitioner.improvePartition(getFinestPartition(stg, null));
//			result = partitioner.improvePartition(getRoughestPartition(stg, null));
		}
		catch (PartitioningException e) {
			// maybe do something against this problem or leave it
			result = e.getPartitionSoFar();
		} 
		
		return result;
	}

	/**
	 * Returns the current partition.
	 * @return The current partition.
	 */
	public List<List<String>> getPartition() {
		return partition;
	}

	/**Starts a new signal set.*/
	public void beginSignalSet() {
		actNewSignalSet = new LinkedList<String>();
		partition.add(actNewSignalSet);	
		started = true;
	}

	/**Adds a new signal to the current signal set,*/
	public void addSignal(String signal) throws STGException {
		if ( signals.contains(signal) ) throw new STGException("Invalid signal partition. Signal " + signal +" occured twice.");
		if (! started) beginSignalSet();
		signals.add(signal);
		actNewSignalSet.add(signal);		
	}




	/**
	 * Calculates the reverse partition, i.e. all signals which are not contained in a component
	 * or which are not triggers of such signals. In other words the maximal set of signals
	 * which can be contracted to yield the final component.
	 * @param stg The STG for which this should be calculated, needed to determine the trigger signals
	 * @return The set of reverse signal sets, one for each element of the partition 
	 */
	public Collection<Collection<String>> getReversePartition(STG stg)  {
		Collection<Collection<String>> result = new LinkedList<Collection<String>>();

		for (Collection<String> comp : partition)  {
			result.add(getReversePartition(comp, stg));
		}

		return result;
	}

	/**
	 * Calculates the reverse partition for a set of output signals, i.e. all signals which can be removed initially:
	 * the outputs itself and every signal which is not a trigger of an output signal. This is needed for tree decomposition, 
	 * see @link net.strongdesign.desij.decomposition.tree.AbstractTreeDecomposition.
	 * 
	 * @param outputs
	 * @param stg
	 * @return
	 */
	public static Collection<String> getReversePartition(Collection<String> outputs, STG stg)  {

		Collection<String> c = new HashSet<String>();
		c.addAll(stg.getSignalNames(stg.getSignals()));

		Collection<Integer> trigger = STGOperations.collectUniqueFromCollection(					
				stg.getTransitions(ConditionFactory.getSignalOfCondition(stg.getSignalNumbers(outputs))), 
				ConditionFactory.ALL_TRANSITIONS, 
				CollectorFactory.getTriggerSignal());
		c.removeAll(outputs);
		c.removeAll(stg.getSignalNames(trigger));

		return c;
	}



	/**Checks, whether the current partition is a valid one for the given STG.
	 * A partition is valid if every output signal is contained in some signal set
	 * of the partition and every signal occurs only once in the partition; the 
	 * latter condition is checked when adding new signals.
	 * 
	 * @param stg The STG for which this should be checked
	 * @return true, if the instance is a correct partition of stg, false otherwise
	 * 
	 */
	public boolean correctPartitionOf(STG stg) {
		Set<Integer> stgSignals = stg.collectUniqueCollectionFromTransitions(ConditionFactory.getSignatureOfCondition(Signature.OUTPUT), CollectorFactory.getSignalCollector());

		return signals.equals(stg.getSignalNames(stgSignals));      
	}

	/**Similar as {@link #correctPartitionOf(STG)}, but the signalset does not have to complete
	 * 
	 * @param stg The STG for which this should be checked
	 * @return true, if the instance is a correct partition of stg, false otherwise
	 * 
	 */
	public boolean correctSubPartitionOf(STG stg) {
		return stg.getSignalNames(stg.getSignals()).containsAll(signals);      
	}



	/**Checks, whether the current partition is a feasible for the given STG.
	 * The feasibility condition is only checked for output signals
	 * inputs are added automatically to fulfil this conditions by @link stg.STG#splitByPartition(Partition)
	 * @param stg The STG for which this should be checked
	 * @return true, if the instance is a feasible partition of stg, false otherwise
	 * TODO - check for output/lambda conflict for out-det
	 */
	public boolean feasiblePartitionOf(STG stg) {
		for (List<String> partSet : partition)
			for (String currentOutput : partSet) 
				for (Transition transition : stg.getTransitions(ConditionFactory.getSignalOfCondition(
						stg.getSignalNumber(currentOutput)))) {
					MultiCondition<Transition> mc = new MultiCondition<Transition>(MultiCondition.AND);
					mc.addCondition(ConditionFactory.getStructuralConflictCondition(transition));
					mc.addCondition(ConditionFactory.getSignatureOfCondition(Signature.OUTPUT));
					if (! partSet.containsAll(stg.getSignalNames(stg.collectFromTransitions(
							mc, CollectorFactory.getSignalCollector()  ))  ))
						return false;
				}

		return true;
	}


	public static STG getInitialComponent(STG stg, List<String> outputs)  {
		//Start with a copy of the original STG			
		STG newSTG = stg.clone();

		Collection<Integer> outputNumbers = stg.getSignalNumbers(outputs);
		//Find syntactical triggers and return their signals
		Collection<Integer> trigger = newSTG.collectUniqueFromTransitions(
				ConditionFactory.getSignalOfCondition(outputNumbers), 
				CollectorFactory.getTriggerSignal());


		//Make syntactical triggers to inputs, outputs of the partition are preserved
		trigger.removeAll(outputNumbers);
		newSTG.setSignature(trigger, Signature.INPUT);

		//Add conflict signals to trigger list
		//outputs are already in the partition, because it is feasible
		//but inputs are still missing
		for (List<Integer> l :  newSTG.collectFromTransitions(
				ConditionFactory.getSignalOfCondition(outputNumbers), 
				CollectorFactory.getConflictSignalCollector()))
			trigger.addAll(l);	

		//convert all other signals (neither trigger nor conflict signal) to dummies
		for (Integer signal : newSTG.getSignals())
			if (! (trigger.contains(signal) || outputs.contains(signal)))
				newSTG.setSignature(signal, Signature.DUMMY);

		return newSTG;

	}

	public static List<STG> splitByPartition(STG stg, Partition partition) {
		List<STG> result = new LinkedList<STG>();

		//for every partition, generate corresponding STG
		for (List<String> signals : partition.getPartition()) 
			result.add(getInitialComponent(stg, signals));
		return result;
	}

	public static Partition fromFile(String fileName) throws FileNotFoundException, IOException, STGException {
		String  file = FileSupport.loadFileFromDisk(fileName);

		Partition partition = new Partition();

		for (String actPartition : file.split(System.getProperty("line.separator"))) {
			partition.beginSignalSet();
			for (String actSignalName : actPartition.split(" ")) {
				partition.addSignal(actSignalName);	
			}
		}
	
		return partition;
	}

	


}
