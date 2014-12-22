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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.util.Pair;

/**
 * Contains an 'order for a node' in a decomposition tree
 * (@see net.strongdesign.util.PresetTree).
 * This order includes the known set of CSC violation traces, the output 
 * signals for which these traces are relevant and the level of this order, i.e.
 * the distance to the original leaf.
 * 
 * @author mark
 *
 */
public class SolveCSC {
	/**
	 * The CSC violation traces.
	 */
	public  List<Pair<List<SignalEdge>,List<SignalEdge>>> violationTraces;
	
	/**
	 * The outputs of the corresponding component.
	 */
	public Collection<Integer> outputs;
	
	/**
	 * The signals which must not be used for the inverse projection.
	 */
	public Collection<Integer> componentSignals;
	
	/**
	 * Distance to the original leaf.
	 */
	public int level;

	/**
	 * The signals which were 'delambdarised' 
	 * (i.e. whose contractions were undone while going up the deco tree)
	 */
	public Collection<Integer> delambdarisedSignals;
	
	
	/**
	 * The signals which have destroyed at least one core of the conflict
	 */
	public Collection<Integer> coreDestroyingSignals;
	
	
	
	public SolveCSC(
			List<Pair<List<SignalEdge>, List<SignalEdge>>> violationTraces, 
			Collection<Integer> componentSignals,
			Collection<Integer> outputs, 
			int level,
			Collection<Integer> delambdarisedSignals) {
		this.violationTraces = violationTraces;
		this.componentSignals = componentSignals;
		this.outputs = outputs;
		this.level = level;
		this.delambdarisedSignals = delambdarisedSignals;
		coreDestroyingSignals = new HashSet<Integer>();
	}
	
	public SolveCSC(
			List<Pair<List<SignalEdge>, List<SignalEdge>>> violationTraces, 
			Collection<Integer> componentSignals,
			Collection<Integer> outputs, 
			int level,
			Collection<Integer> delambdarisedSignals,
			Collection<Integer> coreDestroyingSignals	) {
		this.violationTraces = violationTraces;
		this.componentSignals = componentSignals;
		this.outputs = outputs;
		this.level = level;
		this.delambdarisedSignals = delambdarisedSignals;
		this.coreDestroyingSignals = coreDestroyingSignals;
	}
	
	
	
	public String toString()  {
		StringBuilder result = new StringBuilder();
		result.append("Traces: "+violationTraces+"\n");
		result.append("Componet: "+componentSignals+"\n");
		result.append("Outputs: "+outputs+"\n");
		result.append("Delambda: "+delambdarisedSignals+"\n");
		result.append("Destroy: "+coreDestroyingSignals+"\n");
		result.append("Level: "+level+"\n");
		
		return result.toString();
		
	}
	

	
}
