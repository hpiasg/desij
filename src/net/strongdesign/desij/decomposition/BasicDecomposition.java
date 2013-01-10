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

package net.strongdesign.desij.decomposition;

import java.util.*;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.decomposition.tree.AbstractTreeDecomposition;
import net.strongdesign.stg.*;
import net.strongdesign.stg.traversal.*;
import net.strongdesign.util.Pair;



/**
 * This is the simplest form of decomposition. No special backtracking, no fancy algorithms: simply,  all lambda transitions are
 * contracted in a row. If this is not possible for some of them, they are postponed to the end. Only if a set of uncrontacable
 * transitions remains, decomposition is started anew. There is one optional improvement, transitions are contracted in the order
 * the only few new places are produced, @see net.strongdesign.desij.DesiJCommandLineWrapper#ORDER_DUMMY_TRANSITIONS.
 * 
 * @author Mark Schaefer
 *
 */
public class BasicDecomposition extends AbstractDecomposition {


	public BasicDecomposition(String filePrefix) {
		super(filePrefix);
	}
	
	public BasicDecomposition(String filePrefix, STG specification) {
		super(filePrefix);
		this.specification = specification;
	}
	
	@Override
	public List<Transition> reduce(STGInOutParameter stgParam) throws STGException {

		redDel(stgParam.stg);
		while (true) {
	
			//for backtracking
			stgParam.stg.addUndoMarker(UndoMarker.BEFORE_DECOMPOSITION);

			//collect all dummies
			List<Transition> toContract = stgParam.stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY));
			//no dummies left?, then we have finished (could be deleted, but more efficient for STGs which initially have no dummies)
			if (toContract.isEmpty())
				return new LinkedList<Transition>();

			//sort dummies by number of newly generated places |pre t|*|post t| //TODO use priority queue
			if (CLW.instance.ORDER_DUMMY_TRANSITIONS.isEnabled()) {
				// Date s = new Date();
				Collections.sort(toContract, new Comparator<Transition>() {
					public int compare(Transition o1, Transition o2) {
						return o1.getChildren().size()*o1.getParents().size() - o2.getChildren().size()*o2.getParents().size();
					}});

			}

			//Contract transitions
	
			Pair<List<Transition>, List<Transition>> remainingTransitions = contract(stgParam.stg, toContract);
	
			
			// the new implementation which can ignore some dummy transitions
			if (CLW.instance.LEAVE_DUMMIES.isEnabled() || CLW.instance.OD.isEnabled()) {
				remainingTransitions.a.removeAll(remainingTransitions.b);
				int bSize = remainingTransitions.b.size();
				
				//finished if no transition producing an structural auto-conflict remains
				if (remainingTransitions.a.isEmpty()) { // no new auto-conflict pair
					redDel(stgParam.stg);
					logging(stgParam.stg, DecompositionEvent.FINISHED_STATIC, bSize);
					return remainingTransitions.b;
				}
				else { // auto conflicts have to be avoided
					//some signal needs to be delambdarised
					//check if backtracking is not desired
					if (CLW.instance.STOP_WHEN_BACKTRACKING.isEnabled()) {
						logging(stgParam.stg, DecompositionEvent.STOPPED_FOR_BACKTRACKING, null);
						redDel(stgParam.stg);
						return new LinkedList<Transition>();
					}
					
					// delambdarise arbitrary transition, it is one available for a transitione with a new conflict pair
					//Integer newInput = remainingTransitions.a.iterator().next().getLabel().getSignal();
					
					Integer newInput = null;
					STG overallSpec = AbstractTreeDecomposition.RootSpecification; // when BasicDecomposition is used in the context of TreeDecomposition
					if (overallSpec == null) // we're not in context of an enclosing TreeDecomposition
						overallSpec = this.specification;
					
					for (Transition subjectToDelambdarisation : remainingTransitions.a) {
						Transition specTransition = overallSpec.getTransition(subjectToDelambdarisation.getIdentifier()); 
						Integer correspondingSpecTransitionSignal = specTransition.getLabel().getSignal(); 
						
						// test whether subjectToDelambdarisation is a SpecDummy or not
						if (overallSpec.getSignature(correspondingSpecTransitionSignal) == Signature.DUMMY) 
							continue;
						else {
							newInput = subjectToDelambdarisation.getLabel().getSignal();
							break;
						}
					}
					
					if (newInput == null) { // remainingTransitions.a consists of SpecDummies only --> not delambdarisable!!! 
						// same situation as "remainingTransitions.a.isEmpty() == true"
						redDel(stgParam.stg);
						logging(stgParam.stg, DecompositionEvent.FINISHED_STATIC, bSize);
						return remainingTransitions.b;
					}
					
					
					stgParam.stg.undoToMarker(UndoMarker.BEFORE_DECOMPOSITION);			
					stgParam.stg.setSignature(newInput, Signature.INPUT);

					
					logging(stgParam.stg, DecompositionEvent.REMAINING_TRANSITIONS, "" + (remainingTransitions.a.size()+bSize) + " / " + bSize);
					logging(stgParam.stg, DecompositionEvent.BACKTRACKING, stgParam.stg.getSignalName(newInput));
				}

			}

			// the original implementation
			else {
				//finished if no transition remains
				if (remainingTransitions.a.isEmpty()) {
					redDel(stgParam.stg);					
					logging(stgParam.stg, DecompositionEvent.FINISHED, null);
					return new LinkedList<Transition>();
				}
				else {
					//some signal needs to be delambdarised
					//check if backtracking is not desired
					if (CLW.instance.STOP_WHEN_BACKTRACKING.isEnabled()) {
						logging(stgParam.stg, DecompositionEvent.STOPPED_FOR_BACKTRACKING, null);
						redDel(stgParam.stg);
						return new LinkedList<Transition>();
					}

					//delambdarise arbitrary transition, when available for a transitione with a new conflict pair
					Integer newInput;
					if (! remainingTransitions.b.isEmpty()) {
						newInput = remainingTransitions.b.iterator().next().getLabel().getSignal();
					}
					else {
						newInput = remainingTransitions.a.iterator().next().getLabel().getSignal();
					}

					stgParam.stg.undoToMarker(UndoMarker.BEFORE_DECOMPOSITION);			
					stgParam.stg.setSignature(newInput, Signature.INPUT);

					logging(stgParam.stg, DecompositionEvent.REMAINING_TRANSITIONS, "" + remainingTransitions.a.size() + " / " + remainingTransitions.b.size());
					logging(stgParam.stg, DecompositionEvent.BACKTRACKING, stgParam.stg.getSignalName(newInput));
				}
			}
		}        
	}

}
