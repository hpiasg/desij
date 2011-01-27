package net.strongdesign.stg;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
		
		result.addAll(removeLoopOnlyTransitions(stg, remover));
		result.addAll(DuplicateTransitionRemover.removeDuplicateTransitions(stg, remover));
		
		return result;
		/*Condition<Transition> redTransition = ConditionFactory.getRedundantTransitionCondition(stg);
		while (true) {
			List<Transition> t = stg.getTransitions(redTransition);
			if (t.size()==0) return result;
			remover.removeTransition(t.get(0));
			result.add(t.get(0));
		}*/
	}

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
		STG result 								= new STG(false);

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


	public static STG parallelComposition() {
		return null;
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
}
