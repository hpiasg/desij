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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 * Find transitive lock class by representing signals and direct locks as graph
 * and using BFS to find all reachable nodes by starting from a certain nodes
 * --> these reachable nodes form the transitive lock class
 */
public class TransitiveLockDetector {
	
	private STG stg;
	// +1 signals are direct locked; -1 not locked; 0 locking not computed
	private int[][] pairChart; // computing on demand
	private Map<Integer,Integer> signal2PCIndex; // index for pair chart
	
	public TransitiveLockDetector(STG stg) {
		this.stg = stg;
		signal2PCIndex = new HashMap<Integer, Integer>();
		int index = 0; // index for pair chart
		
		for (Integer signal : stg.getSignals())
			if (stg.getSignature(signal) == Signature.INPUT ||
					stg.getSignature(signal) == Signature.OUTPUT ||
					stg.getSignature(signal) == Signature.INTERNAL)
				signal2PCIndex.put(signal, index++);
		
		initializePairChart(signal2PCIndex.keySet().size());
	}
	
	/*
	 * Computes transitive lock class
	 */
	public Collection<Collection<Integer>> getTransitiveLockClasses(
			Collection<Integer> signals) {
		
		List<Integer> copyOfSignals = new LinkedList<Integer>(signals);
		List<Collection<Integer>> result = new LinkedList<Collection<Integer>>();
		
		while ( !copyOfSignals.isEmpty() ) {
			Set<Integer> currentTransitiveLockClass = getTransitiveClosure(copyOfSignals.remove(0), copyOfSignals);
			copyOfSignals.removeAll(currentTransitiveLockClass);
			result.add(currentTransitiveLockClass);
		}
		
		return result;
	}
	
	/*
	 * Find transitive closure by applying a BFS for an undirected graph
	 */
	private Set<Integer> getTransitiveClosure(Integer startSignal, Collection<Integer> otherSignals) {
		Queue<Integer> todo = new LinkedList<Integer>();
		todo.add(startSignal);
		Set<Integer> visited = new HashSet<Integer>();
		
		while ( !todo.isEmpty() ) {
			Integer currentSignal = todo.poll();
			visited.add(currentSignal);
			for (Integer signal : otherSignals) {
				if (visited.contains(signal))
					continue;
				if (getDirectLockRelation(signal, currentSignal) == 1) // there is a direct connection
					todo.offer(signal);
			}
		}
		
		return visited;
	}

	/*
	 * Initializes the pair chart --> make each entry undefinied
	 */
	private void initializePairChart(int signalCount) {
		pairChart = new int[signalCount][signalCount];
		// initialize with zeros
		for (int i = 0; i < signalCount; i++)
			for (int j = 0; j < signalCount; j++)
				pairChart[i][j] = 0;
	}
	
	/*
	 * gives  the direct lock relation between signal1 and signal2 or 
	 * computes it on demand
	 */
	private int getDirectLockRelation(Integer signal1, Integer signal2) {
		int indexSignal1 = signal2PCIndex.get(signal1);
		int indexSignal2 = signal2PCIndex.get(signal2);
		
		if (pairChart[indexSignal1][indexSignal2] != 0 && // lock relation already computed
				pairChart[indexSignal2][indexSignal1] == pairChart[indexSignal1][indexSignal2]) // symmetric representation
			return pairChart[indexSignal1][indexSignal2];
		
		// else: compute lock relation
		if (ConditionFactory.getLockedSignalCondition(stg, signal1).fulfilled(signal2)) {
			pairChart[indexSignal1][indexSignal2] = 1;
			pairChart[indexSignal2][indexSignal1] = 1;
		}
		else {
			pairChart[indexSignal1][indexSignal2] = -1;
			pairChart[indexSignal2][indexSignal1] = -1;
		}
		
		return pairChart[indexSignal1][indexSignal2];						
	}

}
