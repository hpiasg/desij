/*
 * Created on 04.10.2004
 *
 */
package net.strongdesign.desij.decomposition;

import java.util.*;


import net.strongdesign.desij.CLW;
import net.strongdesign.stg.*;
import net.strongdesign.stg.traversal.*;
import net.strongdesign.util.Pair;

/**
 * An implementation of the decomposition algorithm, which is highly
 * configurebale. By an instance of
 * {@link desij.decomposition.DecompositionParameter} all neccessary parameters are
 * provided to the algorithm.
 * <p>
 * This version implements the correct fast backtracking, i.e. if backtracking is
 * performed the decomposition is not restarted from the most recent copy in any case;
 * if the dummy transitions of a signal s could not be removed completely but in a previous stage
 * of the decomposition there was an auto-conflict for this signal s due to the contraction of a
 * transition with signal s', the contraction of s' must also be undone. If there was an auto-conflict
 * for s' this must be repeated, and so on. 
 * 
 * <p>
 * <b>History: </b> <br>
 * 04.10.2004: Created <br>
 * 25.12.2004: Externalized Strings, split main algorithm in several methods,transition reordering
 * 
 * 23.02.2005: Created
 * 12.04.2005: Changes class structure, refacored as subclass of {@link AbstractDecomposition}
 * 
 * 
 * <p>
 * 
 * @author Mark Schaefer
 */
public class LazyDecompositionSingleSignal extends AbstractDecomposition {
	
	public LazyDecompositionSingleSignal(String filePrefix) {
		super(filePrefix);
	}

	/**
	 * The implementation of the decomposition algorithm. When each transition
	 * of a signal is contracted or removed as a redundant one, it calls itself
	 * tail-recursively. By this procedure the time for backtracking is reduced.
	 * 
	 * @param decoPara
	 *            The actual component and other related information
	 * @param number
	 *            For logging purposes, the number of the intermediate component
	 * @return The final component
	 * @throws Exception
	 */
	
	public List<Transition> reduce(STGInOutParameter stgParam) throws STGException {
		
				
		// *************************************************************
		// Initialisation
		// *************************************************************
		
		//Contains the intermediate results
		LinkedList<STG> 	resultsSTG 		= new LinkedList<STG>();
		LinkedList<Integer> 	resultsSignal 	= new LinkedList<Integer>();
		
		//Start with the initial component
		resultsSTG.add(stgParam.stg);
		
		
		
		// *************************************************************
		// The main loop, pick the latest result and try a contraction 
		// *************************************************************
		
		while (true) {
			
			// *************************************************************
			// Try the next contraction, if finished -> return 
			// *************************************************************
			
			//get latest result and make a copy
			stgParam.stg = resultsSTG.getLast().clone();
			
			//choose transition set for contraction
			List<Transition> contract; 
			if (CLW.instance.OD.isEnabled()) 
				contract = new AnyCompleteSignal().getCompleteTransitionSet(stgParam.stg, resultsSignal);
			else 
				contract = new AnyCompleteSignal().getTransitions(stgParam.stg); // old method
			
			//no transitions left, we are finished
			if (contract.size() == 0) {
				if (CLW.instance.OD.isEnabled() && 
						!stgParam.stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)).isEmpty()) {
					// maybe there are some Dummy Transtitions left which are now contractible
					new BasicDecomposition(filePrefix, specification).reduce(stgParam);
				}
				else 
					redDel(stgParam.stg);
				return null; // regular finishing point of Lazy Decomposition
			}
			
			//memorize signal to be contracted
			//there is no null pointer exception since contract.size()!=0, see above
			Integer actSignal = contract.iterator().next().getLabel().getSignal();
			resultsSignal.addLast(actSignal);
			
			
			logging(stgParam.stg, DecompositionEvent.LAZY_NEWSIGNAL, actSignal);
			
			//Contract signal
			Pair<List<Transition>, List<Transition>> remainingTransitions = contract(stgParam.stg, contract);
			boolean success;
			
			if (CLW.instance.OD.isEnabled()) {
				remainingTransitions.a.removeAll(remainingTransitions.b);
				if (remainingTransitions.a.isEmpty()) // no new auto-conflict pair
					success = true; // no backtracking needed
				else {
					// which signal was tried to contract?
					Integer correspondingSpecSignal = 
						this.specification.getTransition(remainingTransitions.a.iterator().next().getIdentifier()).getLabel().getSignal(); // no null pointer exception, see above
					if (this.specification.getSignature(correspondingSpecSignal) == Signature.DUMMY) // spec-dummy?
						success = true; // no backtracking for spec-dummies!!!
					else
						success = false; 
				}
			}
			else
				success = remainingTransitions.a.isEmpty(); // the old way
			
			//boolean success = contract(stg, contract).a.isEmpty();
			
			
			// *************************************************************
			// Deal with the results of the previous contraction
			// *************************************************************
			
			if (!success) {
				
				// *************************************************************
				// No success -> check for struc. autoconflicts
				// and add neccessary signals to resolve them				
				// *************************************************************
								
				logging(stgParam.stg, DecompositionEvent.LAZY_BACKTRACKING, null);
				
				if (CLW.instance.STOP_WHEN_BACKTRACKING.isEnabled())
					return null;
				
				//if backtracking of two or more stages is needed this List
				//contains all dummy signals which must be changed to input
				//in the correspondent component
				//initial this is only actSignal, resultsSignal.last() resp.
				Integer revertSignal = resultsSignal.getLast();
				
				//memorize last contracted signal
				Integer lastSignal = null;
				
				//need to check whether it was sufficient to add the last signal
				boolean firstTry = false;
				
				
				// *************************************************************
				// Go back in the result list and add signals as long there
				// are no more struc. autoconflicts
				// *************************************************************
				
				backtracking: while (true) {
					//update last intermediate result
					stgParam.stg = resultsSTG.getLast();
					stgParam.stg.setSignature(revertSignal, Signature.INPUT);
					
					//if backtracking was performed until the original component was restored
					//(with all dummies converted to inputs) we must stop, too
					if (resultsSTG.size() == 1) {
						logging(stgParam.stg, DecompositionEvent.LAZY_REACHED_N, revertSignal);
						break backtracking;
					}
					
					//if auto-conflicts do not matter, only one backtracking step is needed
					if (CLW.instance.RISKY.isEnabled()) {
						break backtracking;
					}
					
					//otherwise, check for autoconflict for revertSignal
					boolean newConflict=false;
					
					List<Set<Node>> parents = stgParam.stg.collectFromTransitions(
							ConditionFactory.getSignalOfCondition(revertSignal),
							CollectorFactory.getParentsCollector());
					
					Set<Node> sN = new HashSet<Node>();
					
					conf: for (Collection<Node> aP : parents)
						for (Node node : aP)
							if (!sN.add(node)) {
								newConflict = true;
								break conf;
							}
					
					//no conflict detected for actRevertSignal
					//remove the last result and proceed with the second last in the next iteration of loop k
					//add the second-last contracted signal for change to input and autoconflict detection
					if (newConflict) {					
						logging(stgParam.stg, DecompositionEvent.LAZY_CAUSALNOTFOUND, lastSignal);
						
						//memorize for next iteration, no conflict then -> lastSignal was important
						lastSignal = resultsSignal.getLast();
						
						resultsSignal.removeLast();                           
						resultsSTG.removeLast();
						
						firstTry = false;

						continue backtracking;
						
					}
					
					if (firstTry) break backtracking;
					
					logging(stgParam.stg, DecompositionEvent.LAZY_CAUSALFOUND, lastSignal);
					
					//lastSignal was the signal whose contraction resulted in a conflict, therefore it will
					//be delambdarised in the next iteration of backtracking
					//if again there is no conflict (firstTry = true), we are finished
					revertSignal = lastSignal;
					
					firstTry = true;
					continue backtracking;
					
				}
				
			}
			
			//success -> save new intermediate result and continue
			else {
				redDel(stgParam.stg);
				
				resultsSTG.addLast(stgParam.stg);
			}
		}
	}
	

	
}
