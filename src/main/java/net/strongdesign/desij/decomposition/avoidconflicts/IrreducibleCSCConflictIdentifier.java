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

package net.strongdesign.desij.decomposition.avoidconflicts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;
import net.strongdesign.util.StreamGobbler;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Marking;
import net.strongdesign.stg.synthesis.SynthesisException;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.MultiCondition;
import net.strongdesign.stg.Signature;

/**
 * @author Dominic Wist
 *
 * To identify all irreducible CSC conflicts in a component STG with the help of punf 
 * and mpsat or with a help of the state graph --> in terms of 2 conflicting transition sequences
 * 
 */
public class IrreducibleCSCConflictIdentifier {
	
	private STG criticalComponent;
	private Set<Pair<List<SignalEdge>, List<SignalEdge>>> cscTraces;
	private List<Pair<List<Transition>, List<Transition>>> cscTransitionSequences;
	
	public IrreducibleCSCConflictIdentifier(STG comp) {
		this.criticalComponent = comp;
	}
	
	public static boolean isDynamicSelfTrigger(Transition tEntry, Transition tExit) {
		
		Map<Place, Integer> reachabilityCondition = new HashMap<Place, Integer>();
		
		// built the reachability condition for a dynamic self-trigger 
		// in terms of minimal token counts for the respective places
		
		for (Node place : tEntry.getParents())
			if (place instanceof Place) {
				int minimalMarking = tEntry.getParentValue(place);
				if (tExit.getParents().contains(place)) {
					minimalMarking += tExit.getParentValue(place);
					if (tEntry.getChildren().contains(place))
						minimalMarking -= tEntry.getChildValue(place); // assure that minimalMarking is always >=0
					if (minimalMarking < 0) minimalMarking = 0;
				}
				reachabilityCondition.put((Place)place, new Integer(minimalMarking));
			}
		
		for (Node place : tExit.getParents())
			if (place instanceof Place)
				if (!tEntry.getParents().contains(place)) { // because these places are already processed
					int minimalMarking = tExit.getParentValue(place);
					if (tEntry.getChildren().contains(place))
						minimalMarking -= tEntry.getChildValue(place); // assure that minimalMarking is always >=0
					if (minimalMarking < 0) minimalMarking = 0;
					reachabilityCondition.put((Place)place, new Integer(minimalMarking));
				}
		
		// Is there any unsafe condition?
		for (Integer i : reachabilityCondition.values())
			if (i.intValue() > 1) return true; // We cannot handle unsafe nets with mpsat
		
		// check the reachability condition for the considered dynamic self-trigger with mpsat
		if ( !reachabilityCheckWithMpsat(reachabilityCondition, tEntry.getSTG()) ) return false;		
		
		return true; // optimistic approach
	}
	
	/**
	 * @param component - with irreducible CSC conflicts
	 * @return - A collection of sets of splittable pairs per conflict
	 * @throws IOException
	 * @throws SynthesisException 
	 * @throws STGException 
	 */
	public static Collection<Set<Pair<Transition,Transition>>> getAllIrreducibleCSCConflicts (
			STG component, boolean dynamic) throws IOException {
		Collection<Set<Pair<Transition,Transition>>> result;
		
		if (dynamic) { // based on unfolding or on state graph methods
			result = new HashSet<Set<Pair<Transition,Transition>>>();
			IrreducibleCSCConflictIdentifier transSequenceFinder = new IrreducibleCSCConflictIdentifier(component);
			
			// ****** Unfolding based
			transSequenceFinder.cscTraces = net.strongdesign.stg.synthesis.Unfolding.getCSCViolationTraces(
					transSequenceFinder.criticalComponent);
			
			// ****** State graph based
//			try {
//				transSequenceFinder.cscTraces = net.strongdesign.stg.synthesis.StateGraph.getSG(
//						transSequenceFinder.criticalComponent).getCSCViolationTraces();
//			} catch (STGException e) {
//				throw new RuntimeException(e.getMessage());
//			} catch (SynthesisException e) {
//				throw new RuntimeException(e.getMessage());
//			}
			
			transSequenceFinder.cscTransitionSequences = new ArrayList<Pair<List<Transition>,List<Transition>>>();
			for (Pair<List<SignalEdge>, List<SignalEdge>> conflictingTraces : transSequenceFinder.cscTraces) {
				List<Transition> sequenceA = transSequenceFinder.projectTraceToTransitionSequence(conflictingTraces.a);
				List<Transition> sequenceB = transSequenceFinder.projectTraceToTransitionSequence(conflictingTraces.b);
				if (sequenceA == null || sequenceB == null) return null; // something went wrong
				transSequenceFinder.cscTransitionSequences.add(new Pair<List<Transition>, List<Transition>>(sequenceA,sequenceB));
			}
			
			for (Pair<List<Transition>, List<Transition>> conflictingSequences : transSequenceFinder.cscTransitionSequences) {
				Set<Pair<Transition,Transition>> entryExitPairs = transSequenceFinder.extractEntryExitPairs(conflictingSequences);
				if (entryExitPairs != null) result.add(entryExitPairs); // Only if it is an irreducible CSC conflict
			}
		}
		else { // structural, i.e. find critical component structures indicating irreducible conflicts 
			result = StructuralIrrCSCDetector.getAllSplittablePairs(component); 
		}
		  
		return result;
	}
	
	
	/**
	 * @param conflictingSequences: a pair of firing sequences leading to the two conflicting markings 
	 * @return a set of entry/exit transition pairs or null as a failure result
	 */
	private Set<Pair<Transition,Transition>> extractEntryExitPairs(Pair<List<Transition>,List<Transition>> conflictingSequences) {
		Set<Pair<Transition,Transition>> result = new HashSet<Pair<Transition,Transition>>();
		
		
		List<Transition> sequenceLong = conflictingSequences.a;
		List<Transition> sequenceShort = conflictingSequences.b;
		
		// Claim: sequenceLong >= sequenceShort
		if (sequenceShort.size() > sequenceLong.size()) { 
			sequenceLong = conflictingSequences.b;
			sequenceShort = conflictingSequences.a;
		}
		
		// safe the differences of the conflictingSequences only
		List<Transition> diffSequenceLong = new ArrayList<Transition>();
		List<Transition> diffSequenceShort = new ArrayList<Transition>();
		
		for (int i = 0; i < sequenceLong.size(); i++) {
			try {
				Transition elementLong = sequenceLong.get(i); // no Exception could be thrown
				Transition elementShort = sequenceShort.get(i);
				if (elementShort != elementLong) {
					diffSequenceShort.add(elementShort);
					diffSequenceLong.add(elementLong);
				}
			} catch (IndexOutOfBoundsException ex) {
				// ex could only be thrown due to sequenceShort.get(i)
				diffSequenceLong.add(sequenceLong.get(i)); // no element in elShort
			}
		}
		
		// continue for an irreducible conflict only
		if (!isIrreducibleConflict(diffSequenceLong, diffSequenceShort)) return null; 
		
		for (int i = 0; i < diffSequenceLong.size()-1; i++) 
			if (isASyntacticalTriggerOfB(diffSequenceLong.get(i), diffSequenceLong.get(i+1)))
				result.add(new Pair<Transition, Transition>(diffSequenceLong.get(i), diffSequenceLong.get(i+1)));
		
		// the body of the for loop will probably never reached, because of an empty diffSequenceShort list
		for (int i = 0; i < diffSequenceShort.size()-1; i++)
			if (isASyntacticalTriggerOfB(diffSequenceShort.get(i), diffSequenceLong.get(i+1)))
				result.add(new Pair<Transition, Transition>(diffSequenceShort.get(i), diffSequenceShort.get(i+1)));
		
		if (result.isEmpty()) return null; // Assert: result is not empty
		
		return result;
	}
	
	private boolean isIrreducibleConflict(List<Transition> diffSequenceLong, List<Transition> diffSequenceShort) {
		if (!diffSequenceLong.isEmpty())
			if (!this.criticalComponent.getTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.INPUT)).containsAll(diffSequenceLong))
				return false;
		if (!diffSequenceShort.isEmpty()) 
			if (!this.criticalComponent.getTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.INPUT)).containsAll(diffSequenceShort))
				return false;
		return true;
	}
	
	private boolean isASyntacticalTriggerOfB(Transition transitionA, Transition transitionB) {
		for (Node place : transitionA.getChildren()) {
			if (place.getChildren().contains(transitionB)) return true;
		}
		return false;
	}
	
	private List<Transition> projectTraceToTransitionSequence(List<SignalEdge> trace) {
		
		List<Transition> result = new ArrayList<Transition>(trace.size());
		
		// simulate the trace in the deterministic critical component
		Marking initialState = this.criticalComponent.getMarking(); // save for later
		
		// assure to traverse the list from start to end
		for (int i = 0; i < trace.size(); i++) {
			SignalEdge currentEvent = trace.get(i);
			
			MultiCondition<Transition> cond = new MultiCondition<Transition>(MultiCondition.AND);
            cond.addCondition(ConditionFactory.getSignalEdgeOfCondition(currentEvent));
            cond.addCondition(ConditionFactory.ACTIVATED);
			
            List<Transition> rightTransitions = this.criticalComponent.getTransitions(cond);
            if (rightTransitions.size() > 1) return null; // criticalComponent is not deterministic
            
			Transition currentTransition = rightTransitions.iterator().next();
			result.add(currentTransition);
			
			currentTransition.fire(); // new currentState
		}
		
		criticalComponent.setMarking(initialState); // set back to initial state for later trace analysis
		
		if (result.size() != trace.size()) return null; // return null if the result is wrong
		
		return result;
	}
	
	



	/**
	 * Checks the reachabilityCondition with the help of unfoldings
	 * @param reachabilityCondition: a minimal reachable marking -> i.e. gives a lower bound to token count for each place
	 * @return whether this Marking is reachable (true) or not (false)
	 */
	private static boolean reachabilityCheckWithMpsat(Map<Place,Integer> reachabilityCondition, STG stg) {
		
		if (reachabilityCondition.isEmpty()) return true; // no reachabilityCondition is always reachable
		
		StringBuilder reachabilityExpression = new StringBuilder();
		
		for (Place place : reachabilityCondition.keySet())
			if (reachabilityCondition.get(place).intValue() == 0)
				reachabilityExpression.append("~$P" + "\"" + place.getString(Node.UNIQUE) + "\"" + "&");
			else if (reachabilityCondition.get(place).intValue() == 1)
				reachabilityExpression.append("$P" + "\"" + place.getString(Node.UNIQUE) + "\"" + "&");
		
		reachabilityExpression.deleteCharAt(reachabilityExpression.lastIndexOf("&"));
		reachabilityExpression.trimToSize();
		
		try {
			//	where the STG is saved
			File tmpSTG = File.createTempFile("desij", ".g");
			
			//where the unfolding is saved
			File unfolding = File.createTempFile("desij", ".unf");
			
			//save the STG, generate the unfolding and extract CSC violating traces
			FileSupport.saveToDisk(STGFile.convertToG(stg, false, true), tmpSTG.getCanonicalPath());
			
			HelperApplications.startExternalTool(HelperApplications.PUNF, 
					" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
					HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpSTG.getCanonicalPath()+HelperApplications.SECTION_END ).waitFor();
			
			
			File tmpOut= File.createTempFile("mpsat", ".out");
			File propertyFile = File.createTempFile("property", ".re");
		
			java.io.FileWriter propertyFileStream = new java.io.FileWriter(propertyFile.getCanonicalFile());
			java.io.BufferedWriter outToPropertyFile = new java.io.BufferedWriter(propertyFileStream);
			outToPropertyFile.write("(" + reachabilityExpression.toString() + ")");
			outToPropertyFile.close();
			
			StringBuilder mpsatCommandLinePart = new StringBuilder();
			mpsatCommandLinePart.append("@" + 
					HelperApplications.SECTION_START+propertyFile.getCanonicalPath()+HelperApplications.SECTION_END + 
					" ");
			
			mpsatCommandLinePart.append(HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpOut.getCanonicalPath()+HelperApplications.SECTION_END);
			// Process exec = Runtime.getRuntime().exec(cl.toString());
			Process exec = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -F -d " + mpsatCommandLinePart.toString());
			
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(exec.getInputStream(), "safe mpsat", System.out);
				StreamGobbler.createGobbler(exec.getErrorStream(), "safe mpsat-Error", System.out);
			}
			exec.waitFor();
			exec.getErrorStream().close();
			exec.getInputStream().close();
			exec.getOutputStream().close();
			
			
			String res = FileSupport.loadFileFromDisk(tmpOut.getCanonicalPath());
			if (res.startsWith("NO"))
				return false; // Marking is not reachable
			else // res.startsWith("YES")
				return true;
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true; // Marking COULD be reachable
	}

}
