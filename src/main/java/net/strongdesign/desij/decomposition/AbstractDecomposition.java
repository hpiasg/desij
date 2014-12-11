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
 * Created on 04.10.2004
 *
 */
package net.strongdesign.desij.decomposition;



import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.strongdesign.desij.DesiJ;
import net.strongdesign.desij.CLW;
import net.strongdesign.desij.Messages;
import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.Pair;

/**
 * An abstract implementation of the decomposition algorithm, which is highly
 * configurable. By an instance of
 * {@link desij.decomposition.DecompositionParameter} all necessary parameters are
 * provided to the algorithm.
 * 
 * <p>
 * <b>History: </b> <br>
 * 11.04.2004: Created <br>
 * 
 * <p>
 * 
 * @author Mark Schaefer
 */
public abstract class AbstractDecomposition   {

	private int number = 0;

	protected String filePrefix;
	
	protected STG specification;
	
	
	public String lastMessage;


	public AbstractDecomposition(String filePrefix) {
		this.filePrefix = filePrefix;	
	}

	protected enum UndoMarker { ENTERED_NODE, BEFORE_DECOMPOSITION, FINAL_REDUCTION, BEFORE_CONTRACTION  }

	public Collection<STG> decompose(STG stg, Partition partition) throws STGException, IOException {
		//  String fileNamePrefix = decoPara.filePrefix;
		this.specification = stg; // specification file --> for determination of initial dummies during OutDet Decomposition

		//valid Partition?
		if (CLW.instance.ALLOW_INCOMPLETE_PARTITION.isEnabled()) { 
			if (! partition.correctSubPartitionOf(stg))
				throw new STGException("Incorrect subpartition");
		}

		else if (! partition.correctPartitionOf(stg)) 
			throw new STGException("Incorrect complete partition (try -" + 
					CLW.instance.ALLOW_INCOMPLETE_PARTITION.getShortName() + " option for incomplete partition)");

		if (! partition.feasiblePartitionOf(stg) ) throw new STGException(Messages.getString("ParametrizedDecomposition.invalid_partition")); //$NON-NLS-1$


		DesiJ.logFile.info(
				Messages.getString("ParametrizedDecomposition.partition") + 
				partition);
		DesiJ.logFile.info(
				Messages.getString("ParametrizedDecomposition.partition_feasible"));

		//Partitionen generieren
		List<STG> components = Partition.splitByPartition(stg, partition);

		//for the results
		List<STG> result= new LinkedList<STG>();



		//und aufrufen

		for (STG component : components) {
			StringBuilder signalNames = new StringBuilder();
			for (String s : component.getSignalNames(component.getSignals(Signature.OUTPUT)))
				signalNames.append(s.toString());

			logging(stg, signalNames.toString(), DecompositionEvent.NEW_COMPONENT, signalNames);

			STGInOutParameter componentParameter = new STGInOutParameter(component);
			reduce(componentParameter);
			result.add(componentParameter.stg);
		}
		return result;
	}


	/**
	 * Tries to remove a transition from an STG
	 * 
	 * @param dP
	 *            The actual parameter set, including the STG
	 * @param transition
	 *            The transition to remove
	 * @return True if it was successful, false if not
	 */
	protected boolean removeIfRedundant(STG stg, Transition transition) {
		if (ConditionFactory.getRedundantTransitionCondition(stg).fulfilled(transition)) {
			stg.removeTransition(transition);
			return true;
		}
		return false;    
	}

	protected void logging(STG stg, DecompositionEvent event, Object affectedComponents) {
		logging(stg, filePrefix + File.separator + "stg_" + digits(number), event, affectedComponents);
		if (event.writeSTG()) ++number;   	
	}
	
	@SuppressWarnings("rawtypes")
	protected void logging(STG stg, String fileName, DecompositionEvent event, Object affectedComponents) {


		DesiJ.stats.logging(stg, event, affectedComponents);


		//TODO move check to events
		// no logging at the console output --> you can see the ouput in the logfile too
//		if (event.getVerboseLevel() <= CLW.instance.VERBOSE.getIntValue()) {
//			if ( ! ((event == DecompositionEvent.RED_PLACE_DEL || event == DecompositionEvent.RED_TRANS_DEL)
//					&& affectedComponents instanceof Collection && ((Collection)affectedComponents).size() == 0) )
//				System.out.println(event.toString()+(affectedComponents!=null?affectedComponents:""));
//
//		}
		try {
			if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
				//tried to delete redundant nodes but nothing found -> prevent empty log entry
				if ( (event == DecompositionEvent.RED_PLACE_DEL || event == DecompositionEvent.RED_TRANS_DEL)
						&& affectedComponents instanceof Collection && ((Collection)affectedComponents).size() == 0)
					return;


				DesiJ.logFile.debug(
						"" + event + (affectedComponents!=null?affectedComponents:"") + 
						(CLW.instance.WRITE_INTERMEDIATE_RESULTS.isEnabled() ? (" - file: " +  fileName) : ""));
			}

			if (CLW.instance.WRITE_INTERMEDIATE_RESULTS.isEnabled() && event.writeSTG()) {

				FileSupport.saveToDisk(STGFile.convertToG(stg), fileName);
			}
		} catch (IOException e) {
			System.out.println("Error during logging");
		}
	}
	

	protected Collection<Node> redDel(STG stg) {
		return STGUtil.redDel(stg);
	}
	
	public abstract List<Transition> reduce(STGInOutParameter stg) throws STGException;



	/**
	 * Contracts a set of transitions; tries to contract a transition several times.
	 * In fact, as long as no transition at all could be contracted.
	 * 
	 * @param decoPara
	 * @param contract
	 * @param number
	 * @return A pair with a: all transittions which could no be contracted, b: not contractable due to syntactic conflict 
	 * @throws Exception
	 * TODO optimise handling of selftriggering inducing contractions, such transitions cannot be contracted
	 *         
	 */
	public Pair<List<Transition>, List<Transition>> contract (STG stg, List<Transition> contract) 
	throws STGException {
		boolean back = false;
		boolean fault;
		List<Transition> tried, triedSyntactic;
		Collection<Node> removed = new HashSet<Node>();

		do {
			triedSyntactic = new LinkedList<Transition>();
			tried = new LinkedList<Transition>();
			fault=false;

			//used to detect an increase in the number of places
			int nroPlaces = stg.getNumberOfPlaces();

			for (Transition actTransition : contract ) {
				if (removed.contains(actTransition))
					continue;
				Reason contractable = isContractable(stg, actTransition);
				if (contractable == Reason.OK) {

					if (CLW.instance.FORBID_SELFTRIGGERING.isEnabled()) {
						stg.addUndoMarker(UndoMarker.BEFORE_CONTRACTION);
					}
					
					
					Collection<Place> newPlaces = stg.contract(actTransition);          
					logging(stg, DecompositionEvent.TRANS_CON, actTransition.getString(Node.UNIQUE));


					if (CLW.instance.FORBID_SELFTRIGGERING.isEnabled()) {
						fp: for (Place place : newPlaces) {
							if (! ConditionFactory.SELF_TRIGGERING_PLACE.fulfilled(place)) continue;
							logging(stg, DecompositionEvent.SELF_TRIGGERING_FOUND, place);

							if (ConditionFactory.getRedundantPlaceCondition(stg).fulfilled(place)) {
								stg.removePlace(place);
								logging(stg, DecompositionEvent.SELF_TRIGGERING_REMOVED, place);
							}
							else {
								logging(stg, DecompositionEvent.SELF_TRIGGERING_NOT_REMOVED, place);
								stg.undoToMarker(UndoMarker.BEFORE_CONTRACTION); // a posteriori it seems syntactically impossible to contract
								tried.add(actTransition); 
								//triedSyntactic.add(actTransition); // However, the signal MUST be backtracked to avoid this conflict --> also when out-det is enabled

								fault = true; 
								break fp;
							}
						}                               
					}

				}
				else {
					if (contractable == Reason.CONFLICT) {
						tried.add(actTransition);
					}
					else if (contractable == Reason.SYNTACTIC) {
						triedSyntactic.add(actTransition);
						tried.add(actTransition);
					}

					fault = true;
				}            	

				if (CLW.instance.CHECK_RED_OFTEN.isEnabled()) {
					removed.addAll(redDel(stg));
				} 
				else {
					int newNroP = stg.getNumberOfPlaces();
					if (newNroP > CLW.instance.PLACE_INCREASE.getDoubleValue() * nroPlaces) {
						removed.addAll(redDel(stg));
						nroPlaces = newNroP;
						logging(stg, DecompositionEvent.PLACE_INCREASE, null);
					}
				}

			}

			if (! CLW.instance.CHECK_RED_OFTEN.isEnabled()) {
				removed.addAll(redDel(stg));
			}

			//tried to contract all transitions, but none was successful
			if (tried.size() == contract.size())
				back = true;

			//could contract some transitions, will try for the remaining again
			//first redundant transitions will be removed
			contract = tried;
			logging(stg, DecompositionEvent.NEW_POSTPONE_TRY, null);



		} while (!back && fault);
//		redDel(stg);

		return Pair.getPair(tried, triedSyntactic);

	}


	public enum Reason {SYNTACTIC, CONFLICT, OK}

	
	public Reason isContractable(Transition transition) {
		return isContractable(transition.getSTG(), transition);
	}
	
	private Reason isContractable(STG stg, Transition transition) {

		if (stg.getSignature(transition.getLabel().getSignal()) != Signature.DUMMY) {
			logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_DUMMY, transition.getString(Node.UNIQUE));
			lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_DUMMY.toString();
			return Reason.SYNTACTIC;
		}

		if ( ! ConditionFactory.SECURE_CONTRACTION.fulfilled(transition)) {
			logging(stg, DecompositionEvent.CONTRACTION_NOT_SECURE, transition.getString(Node.UNIQUE));
			lastMessage = DecompositionEvent.CONTRACTION_NOT_SECURE.toString();
			return Reason.SYNTACTIC;
		}

		//TODO wird das doppelt geprueft ???
		if ( ConditionFactory.LOOP_NODE.fulfilled(transition)) {
			logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_LOOP, transition.getString(Node.UNIQUE));
			lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_LOOP.toString();
			return Reason.SYNTACTIC;
		}

		//TODO wird das doppelt geprueft ???
		if ( ConditionFactory.ARC_WEIGHT.fulfilled(transition)) {
			logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_ARC_WEIGHT, transition.getString(Node.UNIQUE));
			lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_ARC_WEIGHT.toString();
			return Reason.SYNTACTIC;
		}


		if (!CLW.instance.RISKY.isEnabled() && ConditionFactory.NEW_AUTOCONFLICT_PAIR.fulfilled(transition) ) {
			logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_NEW_AUTOCONFLICT, transition.getString(Node.UNIQUE));
			lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_NEW_AUTOCONFLICT.toString();
			return Reason.CONFLICT;
		}


		if (CLW.instance.SAFE_CONTRACTIONS.isEnabled()) {
			if (! ConditionFactory.SAFE_CONTRACTABLE.fulfilled(transition)) {
				if (CLW.instance.SAFE_CONTRACTIONS_UNFOLDING.isEnabled() && stg.getSize() <= CLW.instance.MAX_STG_SIZE_FOR_UNFOLDING.getIntValue()) {
					if (! new ConditionFactory.SafeContraction<Transition>(stg).fulfilled(transition)) {
						logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_DYNAMICALLY_UNSAFE, transition.getString(Node.UNIQUE));
						lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_DYNAMICALLY_UNSAFE.toString();
						return Reason.SYNTACTIC;
					}
				}
				else {
					logging(stg, DecompositionEvent.CONTRACTION_NOT_POSSIBLE_SYNTACTICALLY_UNSAFE, transition.getString(Node.UNIQUE));
					lastMessage = DecompositionEvent.CONTRACTION_NOT_POSSIBLE_SYNTACTICALLY_UNSAFE.toString();
					return Reason.SYNTACTIC;
				}
			}
		}

		return Reason.OK; // a priori it seems OK to contract this transition
	}



	/**
	 * Formats a number as a string with up to three leading zeros
	 * 
	 * @param n
	 *            The number to format
	 * @return The formated string
	 */
	protected String digits(int n) {
		//return String.format("%1$03d", n);
		return "" + (n<=9?"0":"") + (n<=99?"0":"") + (n<=999?"0":"") + n;
	}

}
