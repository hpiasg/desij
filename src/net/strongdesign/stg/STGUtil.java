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

package net.strongdesign.stg;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import lpsolve.LpSolveException;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJ;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.stg.traversal.Condition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.NotCondition;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;



public abstract class STGUtil {
//	IMPLEMENT more efficient, maybe calculate once when neccessary and save the result?
	public static boolean isMarkedGraph(STG stg) {
		return stg.getPlaces(new NotCondition<Place>(ConditionFactory.MARKED_GRAPH_PLACE) ).isEmpty();
	}

	
	/**
	 * Contracts all dummy transitions of an STG.
	 * --> strongly inspired by the method contract() of AbstractDecomposition 
	 * uses Reordering as well
	 * @param stg The stg.
	 * @return The number of contracted dummies
	 * @throws STGException If a dummy cannot be contracted.
	 */
	public static int removeDummies(STG stg) throws STGException {
		if (CLW.instance.ORDER_DUMMY_TRANSITIONS.isEnabled())
			return contractDummies(new TransitionQueueWithTreeMap(stg), stg);
		else
			return contractDummies(new TransitionQueueWithArrayList(stg), stg);
	}
	
	private static int contractDummies(TransitionQueue queue, STG stg) throws STGException
	{
		int result = queue.size();
		int contractions = 0;
		int redDel_frequency = 10;
		while(true)
		{
			int nroPlaces = stg.getNumberOfPlaces();
			int nroTransitions = stg.getNumberOfTransitions();
			Transition actTransition = queue.pop();
			if(actTransition != null)
			{
				Collection<Place> places = stg.contract(actTransition);
				for(Place place : places)
					queue.registerAffectedNodes(place.getNeighbours());
				
				contractions++;
				
				DesiJ.logFile.debug("Contracted transition: " + actTransition.getString(Node.UNIQUE));
			}
			
			if (
				   CLW.instance.CHECK_RED_OFTEN.isEnabled()
				|| actTransition == null
				|| contractions * redDel_frequency >= queue.getContractibleTransitionsCount()
				|| stg.getNumberOfPlaces() > CLW.instance.PLACE_INCREASE.getDoubleValue() * nroPlaces
			) {
				contractions = 0;
				NeighbourTrackingNodeRemover remover = new NeighbourTrackingNodeRemover(stg);
				redDel(stg, remover);
				queue.removeNodes(remover.getRemoved());
				
				Collection<Node> neighbours = remover.getNeighbours();
				queue.registerAffectedNodes(neighbours);
				for(Node neighbour : neighbours)
					queue.registerAffectedNodes(neighbour.getNeighbours());
			}
			
			if(stg.getNumberOfPlaces() == nroPlaces && stg.getNumberOfTransitions() == nroTransitions)
				break;
		}
		return result-queue.size();
	}
	
	
	// the following three (private) methods are dedicated to removeDummies()
	
	enum Reason {SYNTACTIC, OK}

	static Reason isContractable(STG stg, Transition transition) {
		
		if (stg.getSignature(transition.getLabel().getSignal()) != Signature.DUMMY) {
			DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it is not a dummy");
			return Reason.SYNTACTIC;
			
			
		}

		if ( ! ConditionFactory.SECURE_CONTRACTION.fulfilled(transition)) {
			DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it is not secure");
			return Reason.SYNTACTIC;
		}

		//TODO wird das doppelt geprueft ???
		if ( ConditionFactory.LOOP_NODE.fulfilled(transition)) {
			DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it is a loop transition");
			return Reason.SYNTACTIC;
		}

		//TODO wird das doppelt geprueft ???
		if ( ConditionFactory.ARC_WEIGHT.fulfilled(transition)) {
			DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it has no proper arc weights");
			return Reason.SYNTACTIC;
		}


		if (CLW.instance.SAFE_CONTRACTIONS.isEnabled()) {
			if (! ConditionFactory.SAFE_CONTRACTABLE.fulfilled(transition)) {
				if (CLW.instance.SAFE_CONTRACTIONS_UNFOLDING.isEnabled() && stg.getSize() <= CLW.instance.MAX_STG_SIZE_FOR_UNFOLDING.getIntValue()) {
					if (! new ConditionFactory.SafeContraction<Transition>(stg).fulfilled(transition)) {
						DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it is dynamically unsafe");
						return Reason.SYNTACTIC;
					}
				}
				else {
					DesiJ.logFile.debug("Contraction of " + transition.getString(Node.UNIQUE) + " is not possible because it is structurally unsafe");
					return Reason.SYNTACTIC;
				}
			}
		}

		return Reason.OK;
	}
	
	private static Collection<Node> redDel(STG stg, NodeRemover remover) {
		Collection<Node> result = new HashSet<Node>();

		if (CLW.instance.REMOVE_REDUNDANT_TRANSITIONS.isEnabled()) { 
			Collection<Transition> r=STGUtil.removeRedundantTransitions(stg, remover);
			result.addAll(r);
			DesiJ.logFile.debug("Remove all redundant transitions: " + r.toString());
		}

		if (CLW.instance.REMOVE_REDUNDANT_PLACES.isEnabled()) { 
			Collection<Place> r=STGUtil.removeRedundantPlaces(stg, remover);
			result.addAll(r);
			DesiJ.logFile.debug("Remove all redundant places: " + r.toString());
		}   
		return result;
	}

	public static Set<Place> removeRedundantPlaces(STG stg, NodeRemover remover) {
		if (CLW.instance.RED_UNFOLDING.isEnabled() && (stg.getSize() <= CLW.instance.MAX_STG_SIZE_FOR_UNFOLDING.getIntValue()))
			return removeRedundantPlacesWithUnfolding(stg, false, remover);
		else
			return removeRedundantPlaces(stg, false, remover);
	}

	//THEO-XXX move generation of unfolding to constructor, is this correct? can a place become not redundant after del. of some other redundant place?
	public static Set<Place> removeRedundantPlacesWithUnfolding(STG stg, boolean repeat, NodeRemover remover) {
		File unfolding = null;

		try {
			//where the STG is saved
			File tmpSTG = File.createTempFile("desij", ".g");

			//where the unfolding is saved
			unfolding = File.createTempFile("desij", ".unf");

			//save the STG, generate the unfolding 
			FileSupport.saveToDisk(STGFile.convertToG(stg, false, true), tmpSTG.getCanonicalPath());

			Process punf = HelperApplications.startExternalTool(HelperApplications.PUNF,  
					" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
					HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpSTG.getCanonicalPath()+HelperApplications.SECTION_END );

			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(punf.getInputStream(), "punf", System.out);
				StreamGobbler.createGobbler(punf.getErrorStream(), "punf", System.err);
			}

			punf.waitFor();
			punf.getErrorStream().close();
			punf.getInputStream().close();
			punf.getOutputStream().close();


		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}


		Set<Place> removed = new HashSet<Place>();
		boolean found;

		do {			
			found = false;		

			for (Place place : new HashSet<Place>(stg.getPlaces()) ) {
				// boolean red = false;
				
				//no children, does not affect firing of any transition -> definitely redundant
				if (place.getChildren().size() == 0)	{
					found = true;
					remover.removePlace(place);
					removed.add(place);
					continue;
				}

				//is the single parent of a transition -> definitely not redundant
				boolean singleParent = false;
				for (Node children : place.getChildren()) {
					singleParent = singleParent || children.getParents().size() == 1;
				}

				if (singleParent) continue;


				StringBuilder cl = new StringBuilder();
//				try {
//					cl.append(HelperApplications.getApplicationPath("mpsat") + " -F -d ");				
//				} 
//				catch (IOException e) {
//					e.printStackTrace();
//				}

				cl.append("~" + place.getString(Node.UNIQUE) + "&(");

				for (Node child : place.getChildren()) {
					boolean addedEt = false;
					for (Node sibbling : child.getParents()) {
						if (sibbling == place) continue;
						cl.append(sibbling.getString(Node.UNIQUE) + "&");
						addedEt = true;
					}
					if (addedEt)
						cl.deleteCharAt(cl.length()-1);
					cl.append("|");
				}
				cl.deleteCharAt(cl.length()-1);
				cl.append(") ");

				try {
					File tmpOut= File.createTempFile("mpsat", ".out");
					cl.append(HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
							" " + 
							HelperApplications.SECTION_START+tmpOut.getCanonicalPath()+HelperApplications.SECTION_END );
					// Process exec = Runtime.getRuntime().exec(cl.toString());
					Process exec = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
							" -F -d " + 
							cl.toString());

					if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
						StreamGobbler.createGobbler(exec.getInputStream(), "mpsat", System.out);
						StreamGobbler.createGobbler(exec.getErrorStream(), "mpsat", System.err);
					}
					exec.waitFor();
					exec.getErrorStream().close();
					exec.getInputStream().close();
					exec.getOutputStream().close();

					String res = FileSupport.loadFileFromDisk(tmpOut.getCanonicalPath());
					if (res.startsWith("NO")) {
						found = true;
						remover.removePlace(place);
						removed.add(place);
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (repeat && found);

		return removed;
	}



	public static Set<Place> removeRedundantPlaces(STG stg, boolean repeat, NodeRemover remover) {
			Set<Place> result = new HashSet<Place>();
		
		Condition<Place> redPlace = ConditionFactory.getRedundantPlaceCondition(stg);
		boolean found;
		do {			
			found = false;
			int y=stg.getNumberOfPlaces();
			if (y>100000) throw new DesiJException("The STG has more than 100000 places."); //System.exit(1);			
			
			for (Place place : new HashSet<Place>(stg.getPlaces()) ){
				if (redPlace.fulfilled(place)) {
					found = true;
					remover.removePlace(place);
					result.add(place);
				}
			}
		} while (repeat && found);

		return result;
	}


	public static Set<Transition> removeRedundantTransitions(STG stg, NodeRemover remover) {
		Set<Transition> result = new HashSet<Transition>();
		
//		result.addAll(removeLoopOnlyTransitions(stg, remover));
//		result.addAll(DuplicateTransitionRemover.removeDuplicateTransitions(stg, remover));
//		
//		return result;
		Condition<Transition> redTransition = ConditionFactory.getRedundantTransitionCondition(stg);
		while (true) {
			java.util.List<Transition> t = stg.getTransitions(redTransition);
			if (t.size()==0) return result;
			remover.removeTransition(t.get(0));
			result.add(t.get(0));
		}
	}

	@SuppressWarnings("unused")
	private static Collection<? extends Transition> removeLoopOnlyTransitions(STG stg, NodeRemover remover) {
		Set<Transition> result = new HashSet<Transition>();
		for(Transition t : stg.getTransitions(ConditionFactory.LOOP_ONLY_TRANSITION))
		{
			result.add(t);
			remover.removeTransition(t);
		}
		return result;
	}


	/**
	 * Generates from a Collection of signals a set containing the both most popular
	 * signal edges of these signals.	 * 
	 * @param signals
	 * @return
	 */
	public static Set<SignalEdge> getEdges(Collection<Integer> signals) {
		Set<SignalEdge> result = new HashSet<SignalEdge>();

		for (Integer sig : signals) {
			result.add(new SignalEdge(sig, EdgeDirection.UP));
			result.add(new SignalEdge(sig, EdgeDirection.DOWN));
		}

		return result;
	}


	public static STG generateReachabilityGraph(STG stg) throws STGException  {
		STG result 								= new STG();

		for (Integer signal : stg.getSignals()) {
			result.setSignalName(signal, stg.getSignalName(signal));
			result.setSignature(signal, stg.getSignature(signal));
		}

		Map<Marking, Place> knownStates 		= new HashMap<Marking, Place>();
		Queue<Marking> toDoStates  				= new LinkedList<Marking>();
		Queue<Place> toDoPlaces	  				= new LinkedList<Place>();
		StateSystem<Marking, SignalEdge> sys 	= STGAdapterFactory.getStateSystemAdapter(stg);                

		Place startPlace = result.addPlace("p", 1);
		toDoStates.add(sys.getInitialState());
		toDoPlaces.add(startPlace);
		knownStates.put(sys.getInitialState(), startPlace);

		while ( !toDoStates.isEmpty() ) {
			Marking currentState = toDoStates.poll();
			Place currentPlace = toDoPlaces.poll();            

			for (SignalEdge event : sys.getEvents(currentState) ) {
				for (Marking newState : sys.getNextStates(currentState, event)) {
					Transition newTransition = result.addTransition(event);
					currentPlace.setChildValue(newTransition, 1);	

					Place targetPlace = knownStates.get(newState);

					if (targetPlace != null) 
						newTransition.setChildValue(targetPlace, 1);
					else {
						Place newPlace = result.addPlace("p", 0 ); 
						toDoStates.add(newState);
						toDoPlaces.add(newPlace);
						knownStates.put(newState, newPlace);
						newTransition.setChildValue(newPlace, 1);
					}
				}   
			}
		}
		return result;
	}

	public static int sizeOfReachabilityGraph(STG stg)   {
		Set<Marking> knownStates 				= new HashSet<Marking>();
		Queue<Marking> toDoStates				= new LinkedList<Marking>();

		StateSystem<Marking, SignalEdge> sys 	= STGAdapterFactory.getStateSystemAdapter(stg);                

		toDoStates.add(sys.getInitialState());
		knownStates.add(sys.getInitialState());

		int result = 0;

		while ( !toDoStates.isEmpty() ) {
			++result;
			if (result % 1000 ==0 )
				System.err.print(".");
			Marking currentState = toDoStates.poll();			

			for (SignalEdge event : sys.getEvents(currentState) ) {
				for (Marking newState : sys.getNextStates(currentState, event)) {
					if (knownStates.contains(newState))
						continue;

					toDoStates.add(newState);
					knownStates.add(newState);
				}
			}
		}

		return result;
	}
	
	/**
	 * Checks if transitions have the same signals and directions 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static boolean sameTransitions(Transition t1, Transition t2) {
		if (t1.getSTG().getSignature(t1.getLabel().getSignal())==Signature.DUMMY) return false;
		if (t2.getSTG().getSignature(t2.getLabel().getSignal())==Signature.DUMMY) return false;
		
		STG stg1 = t1.getSTG();
		STG stg2 = t2.getSTG();
		
		if (stg1==stg2) {
			return (t1.getLabel().getSignal()==t2.getLabel().getSignal()&&
					t1.getLabel().getDirection()==t2.getLabel().getDirection());
		} else {
			return (stg1.getSignalName(t1.getLabel().getSignal()).equals(
					stg2.getSignalName(t2.getLabel().getSignal()))
					&&
					t1.getLabel().getDirection()==t2.getLabel().getDirection());
		}
		
		
	}
	
	/**
	 * For a given transition it tries to merge all transitions 
	 * with the same signal direction into one instance
	 * It transforms the stg passed as an argument 
	 * @param stg, tran
	 * @return
	 */
	public static void enforceInjectiveLabelling(STG stg, Transition tran) {
		
		LinkedList<Transition> merge = new LinkedList<Transition>();
		
		// collect all transitions that are to be merged, but ignore transitions with self-loops
		Collection<Transition> allt = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS);
		
		for (Transition t: allt) {
			if (sameTransitions(tran, t)) {
				merge.add(t);
			}
		}
		
		if (merge.size()<2) return;
		
		// there should be exactly one pre-place and one post-place for each of the transitions
		// with no tokens, those must be MG places
		for (Transition t: merge) {
			
			if (t.getParents().size()!=1) 
				return;
			
			if (t.getChildren().size()!=1) 
				return;
			
			Place p1 = (Place)t.getParents().iterator().next();
			Place p2 = (Place)t.getChildren().iterator().next();
			
			if (p1.getMarking()!=0) 
				return;
			
			if (p2.getMarking()!=0) 
				return;
			
			if (!ConditionFactory.MARKED_GRAPH_PLACE.fulfilled(p1))
				return;
			if (!ConditionFactory.MARKED_GRAPH_PLACE.fulfilled(p2)) 
				return;
		}
		
		LinkedList<Place> pfrom = new LinkedList<Place>();
		LinkedList<Place> pto   = new LinkedList<Place>();
		
		// create place pairs
		for (Transition t: merge) {
			pfrom.add((Place)t.getParents().iterator().next());
			pto.add((Place)t.getChildren().iterator().next());
		}
		
		// try expand up
		boolean success = true;
		while (success) {
			Transition mt = (Transition)pfrom.get(0).getParents().iterator().next();
			// check if can move up
			for (Place p: pfrom) {
				Transition t = (Transition)p.getParents().iterator().next();
				if (!sameTransitions(mt, t))  success=false;
				if (t.getParents().size()!=1) success=false;
				
				Place pp = (Place)t.getParents().iterator().next();
				if (pto.contains(pp))   success=false;
				if (pp.getMarking()!=0) success=false;
				if (!ConditionFactory.MARKED_GRAPH_PLACE.fulfilled(pp)) success=false;
			}
			// move up
			if (success) {
				LinkedList<Place> np = new LinkedList<Place>();
				for (Place p: pfrom)
					np.add((Place)p.getParents().iterator().next().getParents().iterator().next());
				
				pfrom=np;
			}
		}
		
		// try expand down
		success = true;
		while (success) {
			Transition mt = (Transition)pto.get(0).getChildren().iterator().next();
			// check if can move down
			for (Place p: pto) {
				Transition t = (Transition)p.getChildren().iterator().next();
				if (!sameTransitions(mt, t))   success=false;
				if (t.getChildren().size()!=1) success=false;
				
				Place pp = (Place)t.getChildren().iterator().next();
				if (pfrom.contains(pp)) success=false;
				if (pp.getMarking()!=0) success=false;
				if (!ConditionFactory.MARKED_GRAPH_PLACE.fulfilled(pp)) success=false;
			}
			
			// move down
			if (success) {
				LinkedList<Place> np = new LinkedList<Place>();
				for (Place p: pto)
					np.add((Place)p.getChildren().iterator().next().getChildren().iterator().next());
				
				pto=np;
			}
		}
		
		// create arcs from presets to postsets
		LinkedList<Place> checkRedun  = new LinkedList<Place>();
		for (int i=0;i<pfrom.size();i++) {
			Transition t1 = (Transition)pfrom.get(i).getParents().iterator().next();
			Transition t2 = (Transition)pto.get(i).getChildren().iterator().next();
			Place p = stg.addPlace("p", 0);
			checkRedun.add(p);
			
			t1.setChildValue(p, 1);
			p.setChildValue(t2, 1);
		}
		
		// all connect to the main places (indexed 0)
		for (int i=1;i<pfrom.size();i++) {
			Transition t1 = (Transition)pfrom.get(i).getParents().iterator().next();
			Transition t2 = (Transition)pto.get(i).getChildren().iterator().next();
			
			Place p = pfrom.get(0);
			t1.setChildValue(p, 1);
			
			p = pto.get(0);
			p.setChildValue(t2, 1);
		}
		
		
		// remove all merged nodes, apart from the ones indexed 0
		for (int i=1;i<pfrom.size();i++) {
			Node n = pfrom.get(i);
			Node n2= pto.get(i);
			while (n!=n2) {
				n=n.getChildren().iterator().next();
				stg.removeNode(n.getParents().iterator().next());
			}
			stg.removeNode(n2);
		}
		

		// remove redundant places among the created ones
		Condition<Place> redPlace = ConditionFactory.getRedundantPlaceCondition(stg);
		for (Place p: checkRedun) {
			if (redPlace.fulfilled(p))
				stg.removePlace(p);
		}
	}
	
	/**
	 * Tries to enforce injective labelling to all transitions
	 * @param stg
	 */
	public static void enforceInjectiveLabelling(STG stg) {
		// first, find all transitions that we want to try to enforce
		HashSet< Entry<Integer, EdgeDirection> > entries = new HashSet< Entry<Integer, EdgeDirection> >();
		HashSet<Transition> enforce = new HashSet<Transition>();
		
		Collection<Transition> ct = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS);
		for (Transition t: ct) {
			Entry<Integer, EdgeDirection> en = 
				new AbstractMap.SimpleEntry<Integer, EdgeDirection>(t.getLabel().getSignal(), t.getLabel().getDirection());
			
			if (entries.contains(en) ) {
				enforce.add(t);
			} else {
				entries.add(en);
			}
			
		}
		
		for (Transition t: enforce) {
			enforceInjectiveLabelling(stg, t);
		}
		
	}

	/**
	 * This function merges two STGs (same as the standard parallel composition),
	 * but it only works with signals of the same signature
	 * @return
	 */
	public static STG synchronousProduct(STG stg1, STG stg2, boolean removeRedPlaces) {
		
		if (stg1==null) return stg2;
		if (stg2==null) return stg1;
		if (stg1==stg2) return stg1;
		
		STG stg = new STG();
		
		
		Map<Place, Place> old2new1 = new HashMap<Place,Place>();
		Map<Place, Place> old2new2 = new HashMap<Place,Place>();
		
		Map<Transition, Transition> new2old1 = new HashMap<Transition,Transition>();
		Map<Transition, Transition> new2old2 = new HashMap<Transition,Transition>();
		
		Set<SignalEdge> occurs1 = new HashSet<SignalEdge>();
		Set<SignalEdge> occurs2 = new HashSet<SignalEdge>();
		
		// add transition combinations
		for (Transition t1: stg1.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			for (Transition t2: stg2.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				if (STGUtil.sameTransitions(t1, t2)) {
					
					// create the transition
					String name = stg1.getSignalName(t1.getLabel().getSignal());
					
					Integer newNum = stg.getSignalNumber(name); // get or create the signal
					
					Signature s1 = stg1.getSignature(t1.getLabel().getSignal());
					Signature s2 = stg2.getSignature(t2.getLabel().getSignal());
					
					if (s1!=s2) return null;
					
					stg.setSignature(newNum, s1);
					
					SignalEdge se = new SignalEdge(newNum, t1.getLabel().getDirection());
					
					Transition newTransition = stg.addTransition(se);
					
					
					new2old1.put(newTransition,t1);
					new2old2.put(newTransition,t2);
					
					
					occurs1.add(t1.getLabel());
					occurs2.add(t2.getLabel());
				}
			}
		}
		
		// now add transitions that do not occur in both STGs 
		for (Transition t1: stg1.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (!occurs1.contains(t1.getLabel())) {
				// create the transition
				String name = stg1.getSignalName(t1.getLabel().getSignal());
				Integer newNum = stg.getSignalNumber(name); // get or create the signal
				Signature s1 = stg1.getSignature(t1.getLabel().getSignal());
				stg.setSignature(newNum, s1);
				SignalEdge se = new SignalEdge(newNum, t1.getLabel().getDirection());
				
				Transition newTransition = stg.addTransition(se);
				
				new2old1.put(newTransition, t1);
			}
		}

		for (Transition t2: stg2.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (!occurs2.contains(t2.getLabel())) {
				// create the transition
				String name = stg2.getSignalName(t2.getLabel().getSignal());
				Integer newNum = stg.getSignalNumber(name); // get or create the signal
				Signature s2 = stg2.getSignature(t2.getLabel().getSignal());
				stg.setSignature(newNum, s2);
				SignalEdge se = new SignalEdge(newNum, t2.getLabel().getDirection());
				
				Transition newTransition = stg.addTransition(se);
				
				new2old2.put(newTransition, t2);
			}
		}
		
		// copy all places
		for (Place p: stg1.getPlaces()) {
			Place np = stg.addPlace(p.getLabel(), p.getMarking());
			old2new1.put(p, np);
		}
		
		for (Place p: stg2.getPlaces()) {
			Place np = stg.addPlace(p.getLabel(), p.getMarking());
			old2new2.put(p, np);
		}
		
		// finally, set the weights
		for (Transition tr: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			Transition t1 = new2old1.get(tr);
			if(t1!=null) {
				for (Node p1: t1.getParents()) {
					Place p = old2new1.get(p1);
					tr.setParentValue(p, t1.getParentValue(p1));
				}
				for (Node p1: t1.getChildren()) {
					Place p = old2new1.get(p1);
					tr.setChildValue(p, t1.getChildValue(p1));
				}
			}
			
			Transition t2 = new2old2.get(tr);
			if (t2!=null) {
				for (Node p2: t2.getParents()) {
					Place p = old2new2.get(p2);
					tr.setParentValue(p, t2.getParentValue(p2));
				}
				for (Node p2: t2.getChildren()) {
					Place p = old2new2.get(p2);
					tr.setChildValue(p, t2.getChildValue(p2));
				}
			}
		}
		
		if (removeRedPlaces)
			removeRedundantPlaces(stg);
		
		return stg;
	}
	
	
	public static STG synchronousProduct(LinkedList<STG> stgs, boolean removeRedPlaces) {
		STG ret = null;
		if (stgs.size()==1) return stgs.get(0);
		for (STG stg: stgs) {
			ret = synchronousProduct(ret, stg, removeRedPlaces);
		}
		return ret;
	}
	
	// method overloading because of NodeRemover idea
	
	public static Collection<Place> removeRedundantPlaces(STG stg) {
		return removeRedundantPlaces(stg, new DefaultNodeRemover(stg));
	}

	public static Collection<Place> removeRedundantPlaces(STG stg, boolean repeat) {
		return removeRedundantPlaces(stg, repeat, new DefaultNodeRemover(stg));
	}

	public static Collection<Transition> removeRedundantTransitions(STG stg) {
		return removeRedundantTransitions(stg, new DefaultNodeRemover(stg));
	}


	/**
	 * Removes - if possible - internal signals (encoding signals) 
	 * by preserving the CSC property.
	 * 
	 * @param stg --> must satisfy CSC
	 * @return number of removed internal signals
	 * return -1 --> impossible: either CSC wasn't satisfied initially or LP approximates not good enough
	 * @throws STGException 
	 */
	public static int removeInternalSignals(STG stg) throws STGException {
		// Implement Josep's algorithm for calculation of CSC support:
		
		// First, consider all internal signals to be unnecessary 
		// --> merely the external signals matter
		Set<Integer> unnecessaryInternals = new HashSet<Integer>(stg.getSignals(Signature.INTERNAL));
		Set<Integer> necessarySignals = new HashSet<Integer>(stg.getSignals());
		necessarySignals.removeAll(unnecessaryInternals);
		
		int returnValue = unnecessaryInternals.size();
		
		// initialize CSCChecking --> pick the right strategy here
//		ICSCCheckLPStrategy lpCSCCheck = CSCCheckerLPSolvePreCaching.
//				getCSCCheckerLPSolvePreCaching(stg); // singleton: uses the lpsolve java wrapper directly
		ICSCCheckLPStrategy lpCSCCheck = new CSCCheckerLPSimple(stg); // inefficient, but more transparent implementation
		
		try {
			while (!lpCSCCheck.execute(necessarySignals)) { // while CSC is not satisfied
				Set<Integer> newEncodingSignals = 
						lpCSCCheck.getUnbalancedSignals(necessarySignals);
				if (newEncodingSignals.isEmpty())
					return -1;
				else {
					necessarySignals.add(newEncodingSignals.iterator().next());
					--returnValue; // decrement the count of removable signals
				}
			}
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		unnecessaryInternals.removeAll(necessarySignals);
		
		stg.setSignature(unnecessaryInternals, Signature.DUMMY);
		removeDummies(stg);
		
		// are there un-removable signals (i.e. structurally un-removable)
		Set<Integer> dummiesLeft = stg.getSignals(Signature.DUMMY);
		dummiesLeft.retainAll(unnecessaryInternals);
		if (!dummiesLeft.isEmpty()) {
			returnValue -= dummiesLeft.size();
			stg.setSignature(dummiesLeft, Signature.INTERNAL);
		}
						
		return returnValue;
	}
	
	/*
	 * 1. creates the Cartesian product of the given two sets of places,
	 * 2. creates appropriate transition arcs
	 * 3. sets appropriate token counts
	 * 4. removes old arcs and old places form the STG
	 */
	static public Set<Place> cartesianProductBinding(STG stg, Set<Place> inPlaces, Set<Place> outPlaces) {
		
		Place newPlace;
		
		Set<Place> toDelete = new HashSet<Place>();
		Set<Place> toReturn = new HashSet<Place>();
		
		for (Place p1 : inPlaces) {
			for (Place p2: outPlaces) {
				int m1 = p1.getMarking(); 
				int m2 = p2.getMarking();
				newPlace = stg.addPlace("p", m1+m2);
				toReturn.add(newPlace);

				// now copy arcs
				for (Node n : p1.getParents()) {
					newPlace.setParentValue(n, p1.getParentValue(n));
				}
				for (Node n : p1.getChildren()) {
					newPlace.setChildValue(n, p1.getChildValue(n));
				}
				for (Node n : p2.getParents()) {
					newPlace.setParentValue(n, p2.getParentValue(n));
				}
				for (Node n : p2.getChildren()) {
					newPlace.setChildValue(n, p2.getChildValue(n));
				}
				
				// mark places, which will be removed from the STG
				toDelete.add(p1);
				toDelete.add(p2);
			}
		}
		
		// now remove all the marked places
		for (Place p: toDelete) {
			stg.removePlace(p);
		}
		
		return toReturn;
	}
	
}
