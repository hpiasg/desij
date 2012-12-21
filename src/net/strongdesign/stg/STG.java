/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

import java.awt.Point;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.naming.OperationNotSupportedException;

import net.strongdesign.desij.CLW;
import net.strongdesign.stg.traversal.Collector;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.Condition;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.Operation;
import net.strongdesign.stg.traversal.Operations;
import net.strongdesign.stg.traversal.STGOperations;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;



/**
 * The STG class collects places and transitions of an STG and provided methods
 * for their modification. It is one of the central classes of DesiJ.
 * 
 * <p>Additionally an undo mechanism for all decomposition operations is provided which
 * covers adding and removing of nodes but not the modification of single edges. 
 * 
 * 
 * @author Mark Schaefer
 * @since 25.09.2004
 */
public final class STG implements Cloneable {

	/**All places of the STG.*/
	private Set<Place> places;

	/**All transitions of the STG.*/
	private Set<Transition> transitions;

	/**The signatures of signals.*/
	private Map<Integer, Signature> signatures;

	/**Number occurrences of a given signal.*/
	private Map<Integer, Integer> signalOccurences;

	/**The real names of the signals.*/
	private Map<Integer,String> signalNames;

	/**The numbers of the signals.*/
	private Map<String,Integer> signalNumbers;	

	/**The maximal unique id assigned to some node. This is an internal management information.*/
	private int maxNodeNumber;

	/**The undo stack.*/
	private Stack<UndoOperation> undoStack;

	/**The coordinates of the nodes for a graphical representation.*/
	private STGCoordinates coordinates;

	/**True if node coordinates are actually saved.*/
	//private boolean withCoordinates;
	public boolean isWithCoordinates() {
		return !coordinates.isEmpty();
	}

	// *******************************************************************
	// Construction and Generation
	// *******************************************************************

	/**
	 * Constructs an empty STG.
	 */
	public STG() {
		places = new HashSet<Place>();
		transitions = new HashSet<Transition>();
		maxNodeNumber = 0;		
		signatures = new HashMap<Integer, Signature>();
		signalOccurences = new HashMap<Integer, Integer>();
		undoStack = new Stack<UndoOperation>();
		signalNames = new HashMap<Integer, String>();
		signalNumbers = new HashMap<String, Integer>();
		
		//this.withCoordinates = withCoordinates;
		//if (withCoordinates)
		coordinates = new STGCoordinates();

	}

	/**
	 * Generates a deep copy of the STG, but without the undo stack.
	 * @see #completeClone()
	 * @Override
	 */
	public STG clone() {
		//The resulting copy 
		STG result = new STG();

		result.signalNames = new HashMap<Integer, String>(signalNames);
		result.signalNumbers = new HashMap<String, Integer>(signalNumbers);

		//Contains the copy of each place, transition resp.  
		Map<Place,Place> newPlaces = new HashMap<Place,Place>();
		Map<Transition,Transition> newTransitions = new HashMap<Transition,Transition>();


		try {
			//For each place: make a copy 	
			for (Place place : places) {
				Place newPlace = place.clone();
				newPlace.setSTG(result);
				result.places.add(newPlace);
				newPlaces.put(place, newPlace);		        
				if (isWithCoordinates())
					result.coordinates.put(newPlace, coordinates.get(place));
			}

			//For each transition: make a copy 	
			for (Transition transition : transitions) {
				Transition newTransition = transition.clone();
				newTransition.setSTG(result);
				result.transitions.add(newTransition);
				newTransitions.put(transition, newTransition);
				if (isWithCoordinates())
					result.coordinates.put(newTransition, coordinates.get(transition));
			}

			//Copy incidence matrix to new nodes
			for (Place place : places) {
				Place newPlace = newPlaces.get(place);
				for (Node child : place.getChildren()) 
					newPlace.setChildValue(
							newTransitions.get(child),
							place.getChildValue(child) );
			}

			for (Transition transition : transitions) {
				Transition newTransition = newTransitions.get(transition); 
				for (Node child : transition.getChildren()) 
					newTransition.setChildValue(
							newPlaces.get(child),
							transition.getChildValue(child) );
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		result.maxNodeNumber = maxNodeNumber;		

		for (Integer signal : signatures.keySet())
			result.signatures.put(signal, signatures.get(signal));

		for (Integer signal : signalOccurences.keySet())
			result.signalOccurences.put(signal, signalOccurences.get(signal));

		
		return result;
	}
	
	/**
	 * Turns the current STG into a copy of the given STG. The undo stack is not copied.
	 * Essentially, this method is a copy of {@link #clone()} with reversed roles.
	 * @param copy
	 */
	public void restore(STG stg) {
		signalNames = new HashMap<Integer, String>(stg.signalNames);
		signalNumbers = new HashMap<String, Integer>(stg.signalNumbers);

		places = new HashSet<Place>();
		transitions = new HashSet<Transition>();
		
		coordinates = new STGCoordinates();
		
		signalOccurences = new HashMap<Integer, Integer>();
		signatures = new HashMap<Integer, Signature>();
		
		
		//Contains the copy of each place, transition resp.  
		Map<Place,Place> newPlaces = new HashMap<Place,Place>();
		Map<Transition,Transition> newTransitions = new HashMap<Transition,Transition>();


		try {
			//For each place: make a copy 	
			for (Place place : stg.places) {
				Place newPlace = place.clone();
				newPlace.setSTG(this);
				places.add(newPlace);
				newPlaces.put(place, newPlace);		        
				if (isWithCoordinates())
					coordinates.put(newPlace, stg.coordinates.get(place));
			}

			//For each transition: make a copy 	
			for (Transition transition : stg.transitions) {
				Transition newTransition = transition.clone();
				newTransition.setSTG(this);
				transitions.add(newTransition);
				newTransitions.put(transition, newTransition);
				if (isWithCoordinates())
					coordinates.put(newTransition, stg.coordinates.get(transition));
			}

			//Copy incidence matrix to new nodes
			for (Place place : stg.places) {
				Place newPlace = newPlaces.get(place);
				for (Node child : place.getChildren()) 
					newPlace.setChildValue(
							newTransitions.get(child),
							place.getChildValue(child) );
			}

			for (Transition transition : stg.transitions) {
				Transition newTransition = newTransitions.get(transition); 
				for (Node child : transition.getChildren()) 
					newTransition.setChildValue(
							newPlaces.get(child),
							transition.getChildValue(child) );
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		maxNodeNumber = stg.maxNodeNumber;		

		for (Integer signal : stg.signatures.keySet())
			signatures.put(signal, stg.signatures.get(signal));

		for (Integer signal : stg.signalOccurences.keySet())
			signalOccurences.put(signal, stg.signalOccurences.get(signal));

	}




	/**
	 * Generates a deep copy of the STG together with the undo stack.
	 * @throws OperationNotSupportedException 
	 * @see #clone()
	 * 
	 * Not implemented yet.
	 * 
	 * @Override
	 */
	public STG completeClone() throws OperationNotSupportedException {

		throw new OperationNotSupportedException("Cloning with undo stack is not supported.");
		/*
	    //The resulting copy 
		STG result = new STG(withCoordinates);

		//Contains the copy of each place, transition resp.  
		Map<Place,Place> newPlaces = new HashMap<Place,Place>();
		Map<Transition,Transition> newTransitions = new HashMap<Transition,Transition>();


		try {
		    //For each place: make a copy 	
		    for (Place place : places) {
		    	Place newPlace = place.clone();
		    	newPlace.setSTG(result);
		    	result.places.add(newPlace);
		        newPlaces.put(place, newPlace);		        
		        if (withCoordinates)
		        	result.coordinates.put(newPlace, coordinates.get(place));
		    }

		    //For each transition: make a copy 	
		    for (Transition transition : transitions) {
		    	Transition newTransition = transition.clone();
		    	newTransition.setSTG(result);
		    	result.transitions.add(newTransition);
		        newTransitions.put(transition, newTransition);
		        if (withCoordinates)
		        	result.coordinates.put(newTransition, coordinates.get(transition));
		    }

		    //Copy incidence matrix to new nodes
		    for (Place place : places) {
		        Place newPlace = newPlaces.get(place);
		        for (Node child : place.getChildren()) 
		            newPlace.setChildValue(
		                    newTransitions.get(child),
		                    place.getChildValue(child) );
		    }

		    for (Transition transition : transitions) {
		        Transition newTransition = newTransitions.get(transition); 
		        for (Node child : transition.getChildren()) 
		            newTransition.setChildValue(
		                    newPlaces.get(child),
		                    transition.getChildValue(child) );
		    }		
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		result.maxNodeNumber = maxNodeNumber;		

		for (String signal : signatures.keySet())
			result.signatures.put(signal, signatures.get(signal));

		for (String signal : signalOccurences.keySet())
			result.signalOccurences.put(signal, signalOccurences.get(signal));

		return result;
		 */
	}



	// *******************************************************************
	// Collection from Nodes
	// *******************************************************************

	public <R> List<R> collectFromPlaces(
			Condition<Place> condition, 
			Collector<? super Place, R> collector) {
		return STGOperations.collectFromCollection(places, condition, collector);
	}

	public <R> List<R> collectFromTransitions(
			Condition<? super Transition> condition, 
			Collector<? super Transition, R> collector) {
		return STGOperations.collectFromCollection(transitions, condition, collector);
	}


	public <R> Set<R> collectUniqueCollectionFromPlaces(Condition<Place> condition, Collector<Place, R> collector) {
		return STGOperations.collectUniqueCollectionFromCollection(places, condition, collector);
	}

	//TODO refactor collect (unique)	
	public <R> Set<R> collectUniqueCollectionFromTransitions(
			Condition<Transition> condition, 
			Collector<Transition, R> collector) {
		return STGOperations.collectUniqueCollectionFromCollection(transitions, condition, collector);
	}


	public <R> Set<R> collectUniqueFromPlaces(Condition<? super Place> condition, Collector<? super Place, Collection<R>> collector) {
		return STGOperations.collectUniqueFromCollection(places, condition, collector);
	}


	public <R> Set<R> collectUniqueFromTransitions(
			Condition<Transition> condition, 
			Collector<Transition, ? extends Collection<R>> collector) {
		return STGOperations.collectUniqueFromCollection(transitions, condition, collector);
	}


	// *******************************************************************
	// Information retrieval
	// *******************************************************************


	/**
	 * Returns the weightmatrix W and the current marking M_N of the STG as lowlevel data structure.
	 * The array returned, contains in this order:<br>
	 * W(place_0, transition_0), W(place_0, transition_1), ... W(place_0, transition_m)<br>
	 * W(place_1, transition_0), W(place_1, transition_1), ... W(place_1, transition_m)<br>
	 * ...<br>
	 * W(place_n, transition_0), W(place_n, transition_1), ... W(place_n, transition_m)<br>
	 * W(transition_0, place_0), W(transition_0, place_1), ... W(transition_0, place_n),<br>
	 * W(transition_1, place_0), W(transition_1, place_1), ... W(transition_1, place_n),<br>
	 * ...
	 * W(transition_m, place_0), W(transition_m, place_1), ... W(transition_m, place_n),<br>
	 * M_N(place_0), M_N(place_1), ... M_N(place_n), 
	 * Number of Places, Number of Transitions
	 *   
	 * The number of places, transitions resp. is saved at the end of the array in order to save
	 * time when accessing the weight entries, because one saves the addition of 2 to every calculated position.<br>
	 * <b>Caution!</b>The array contains two subarrays which different dimensions.
	 *   
	 * @param mapping Mapping should be an empty map. It is filled with the mapping from Places/Transitions of the 
	 * original STG to their numbers in the weightmatrix.
	 * 
	 * @return The weightmatrix as array.
	 */
	public int[] getWeightMatrix(Map<Node, Integer> mapping) {
		int numPlaces = getNumberOfPlaces();
		int numTrans = getNumberOfTransitions();		

		int[] weightMatrix = new int[2*numPlaces*numTrans +2*numPlaces + 2];

		weightMatrix[2*numPlaces*numTrans + 2*numPlaces + 0] = numPlaces;
		weightMatrix[2*numPlaces*numTrans + 2*numPlaces + 1] = numTrans;


		int number=0;
		for (Node node : getPlaces(ConditionFactory.ALL_PLACES)) {
			mapping.put(node, number);
			weightMatrix[2*numPlaces*numTrans + number] = ((Place)node).getMarking();
			++number;
		}

		number=0;
		for (Node node : getTransitions(ConditionFactory.ALL_TRANSITIONS)) 
			mapping.put(node, number++);



		for (Node node : getNodes()) 
			for (Node child : node.getChildren()) {
				int pos;
				if (node instanceof Place)
					pos = mapping.get(node) * numTrans + mapping.get(child);

				else
					pos = numPlaces*numTrans + mapping.get(node) * numPlaces + mapping.get(child);

				weightMatrix[pos] = node.getChildValue(child);				
			}


		return weightMatrix;
	}

	/**
	 * Returns all nodes.
	 * @return
	 */
	public Set<Node> getNodes() {
		Set<Node> result = new HashSet<Node>();
		for (Node n : places)
			result.add(n);
		for (Node n : transitions)
			result.add(n);

		return result;
	}


	/**
	 * Returns all transitions fulfilling the given condition.
	 * @param condition
	 * @return
	 */
	public List<Place> getPlaces(Condition<? super Place> condition) {
		return STGOperations.getElements(places, condition);
	}

	/**
	 * Returns the number of places.
	 * @return
	 */
	public int getNumberOfPlaces() {
		return places.size();
	}

	/**
	 * Returns all transitions fulfilling the given condition. 
	 * @param condition
	 * @return
	 */
	public List<Transition> getTransitions(Condition<? super Transition> condition) {
		return STGOperations.getElements(transitions, condition);
	}

	/**
	 * Returns the number of transitions.
	 * @return
	 */
	public int getNumberOfTransitions() {
		return transitions.size();
	}

	/**
	 * Returns the number of dummies
	 * @return
	 */
	public int getNumberOfDummies() {
		return getTransitions(ConditionFactory.IS_DUMMY).size();
	}

	
	public String getSTGInfo() {
		// get the number of transitions, places, dummies, arcs, and loops 
		int arcs=0;
		int loops=0;
		
		int aw=0; // the number of weightet arcs
		// now for each place find the number of associated arcs and loops (read-arcs)
		for (Node p: getPlaces()) {
			arcs+=p.getParents().size();
			arcs+=p.getChildren().size();
			
			HashSet<Node> par = new HashSet<Node>();
			HashSet<Node> chi = new HashSet<Node>();
			
			par.addAll(p.getParents());
			chi.addAll(p.getChildren());
			
			for (Node t: par) {
				if (p.getChildValue(t)>1) aw++;
				if (t.getChildValue(p)>1) aw++;
			}
			
			for (Node t: chi) {
				if (p.getChildValue(t)>1) aw++;
				if (t.getChildValue(p)>1) aw++;
			}
			
			par.retainAll(chi);
			loops+=par.size();
		}
		
		String aws="";
		if (aw>0) aws+="("+aw+")";
		return " A:"+arcs+aws+" L:"+loops+" T:"+getNumberOfTransitions()+" P:"+getNumberOfPlaces()+" D:"+getNumberOfDummies();
	}
	
	// *******************************************************************
	// Marking
	// *******************************************************************


	/**
	 * Returns the current marking of the STG, the underlying Petri net resp.
	 * @return
	 */
	public Marking getMarking() {
		Marking m = new Marking();

		for (Place place : places )
			m.setMarking(place, new Integer(place.getMarking()));

		return m;
	}


	public void setMarking(Marking marking) {
		for (Place p : places)
			p.setMarking(marking.getMarking(p).intValue() );
	}

	// *******************************************************************
	// Signals and Signatures
	// *******************************************************************
	//TODO comment new ones

	public String getSignalName(Integer signal) {
		return signalNames.get(signal);
	}

	public Collection<String> getSignalNames(Collection<Integer> signals) {
		Collection<String> result = new HashSet<String>();
		for (Integer signal : signals) {
			result.add(signalNames.get(signal));
		}

		return result;
	}

	public void setSignalName(Integer signal, String name) throws STGException {
		if (signalNames.put(signal, name) != null) {
			throw new STGException(	"Signal number " + signal + " already assigned to " 
					+ signalNames.get(signal) + ". Reassignment to " + name + " is not possible.");
		}

		if (signalNumbers.put(name, signal) != null) {
			throw new STGException(	"Signal name " + name + " already assigned to " 
					+ signalNumbers.get(name) + ". Reassignment to " + signal + " is not possible.");

		}
	}

	public Integer getSignalNumber(String signalName) {
		Integer number = signalNumbers.get(signalName);
		if (number == null) {
			int nn = signalNames.size();
			while (signalNames.keySet().contains(nn)) ++nn;
			try {
				setSignalName(nn, signalName);
			} catch (STGException e) {
				e.printStackTrace();
			}
			number = nn;
		}
		return number;
	}

	public Collection<Integer> getSignalNumbers(Collection<String> signalNames) {
		Collection<Integer> result = new HashSet<Integer>();
		for (String signal : signalNames) {
			result.add(signalNumbers.get(signal));
		}

		return result;
	}



	/**
	 * Returns all signals. 
	 */
	public Set<Integer> getSignals() {
		return Collections.unmodifiableSet((signalOccurences.keySet()));	
	}

	/**
	 * Renames the signals of an STG with an injective renaming.
	 * @param renaming
	 * @throws STGException If the renaminig is not injective.
	 */
	public void renameSignals(Map<String, String> renaming) throws STGException {
		Set<String> newSignalNames = new HashSet<String>();

		for (String signalName : signalNumbers.keySet()) {
			String newName = renaming.get(signalName);
			if (newName == null) {
				newSignalNames.add(signalName);
			}
			else {
				newSignalNames.add(newName);
			}
		}

		if (newSignalNames.size() != signalNumbers.size())
			throw new STGException("Renaming is not injective for given STG " + renaming);

		// works only because renaming is injective as checked above
		for (Integer signal : getSignals()) {
			String oldName = signalNames.get(signal);
			String newName = renaming.get(oldName);
			if (newName == null) continue;
			
			signalNames.put(signal, newName);
			signalNumbers.put(newName, signal);
		}
	}


	/**
	 * Retuns all signals of a given signature.
	 * @param sign
	 * @return
	 */
	public Set<Integer> getSignals(Signature sign) {
		Set<Integer> result = new HashSet<Integer>();
		for (Integer signal : getSignals())
			if (getSignature(signal) == sign)
				result.add(signal);

		return result;
	}

	/**
	 * Returns the signature of the given signal.
	 * @param signal
	 * @return
	 */
	public Signature getSignature(Integer signal) {
		return signatures.get(signal);		
	}


	/**
	 * Sets the new signature of the given signals.
	 * @param signals
	 * @param sign
	 */
	public void setSignature(Collection<Integer> signals, Signature sign) {
		for (Integer signal : signals)
			setSignature(signal, sign);
	}

	/**
	 * Sets the signature of the given signal.
	 * @param signal
	 * @param sign
	 */
	public void setSignature(Integer signal, Signature sign) {
		addUndo(new UndoSetSignature(signal, getSignature(signal)));
		signatures.put(signal, sign);
	}



	/**
	 * Changes the signature of signals, such that all signals with signature oldSig
	 * have the new signature newSig. After this, there is no signal with signature oldSig.
	 */
	public void changeSignature(Signature oldSig, Signature newSig) {
		for (Integer signal : getSignals())
			if (getSignature(signal) == oldSig)
				setSignature(signal, newSig);
	}





	// *******************************************************************
	// Adding and Removal of Nodes
	// *******************************************************************

	/**
	 * Adds a new place to the net, instead of giving a ready made place the relevant external information
	 * must be provided, id's are generated by the STG itself.
	 */
	public Place addPlace(String label, int marking) {
		Place newPlace = new Place(label, ++maxNodeNumber, marking, this);
		places.add(newPlace);
		addUndo(new UndoAddNode(newPlace));

		return newPlace;
	}

	/**
	 * Assigns new node ids to the nodes. TODO update documentation
	 * @param ids
	 * @param reassignOtherNodes
	 * @throws STGException
	 */
	public void setIds(Map<Node,Integer> ids, boolean reassignOtherNodes) throws STGException {
		if (ids.size() != ids.values().size())
			throw new STGException("New node ids are not unique");
		
		if (!reassignOtherNodes && (places.size() + transitions.size()) != ids.size())
			throw new STGException("New node ids are not complete");
		
		List<Node> unassigned = new LinkedList<Node>(); 
		maxNodeNumber = 0;
		for (Node node : getNodes()) {
			Integer newId = ids.get(node);
			if (newId == null) {
				unassigned.add(node);				
			}
			else {
				node.setIdentifier(newId);
				maxNodeNumber = Math.max(maxNodeNumber,newId);
			}				
		}
			
		
		for (Node node : unassigned) {
			node.setIdentifier(++maxNodeNumber);
		}

		// rebuild places and nodes sets
		
	}
	
	/**
	 * Creates a dummy transition
	 * @param name
	 * @return
	 */
	public Transition addDummy(String name) {
		int num=getSignalNumber(name);
		setSignature(num, Signature.DUMMY);
		
		Transition ret = addTransition(
				new SignalEdge(
						num, 
						EdgeDirection.UNKNOWN
						)
				);
		return ret;
	}
	
	/**
	 * Adds a new transition to the net, instead of giving a ready made transition the relevant external information
	 * must be provided, id's are generated by the STG itself.
	 */
	public Transition addTransition(SignalEdge label) {
		//Generate new Transition ...
		Transition newTransition = new Transition(label, ++maxNodeNumber, this);

		//.. and add it to the net
		transitions.add(newTransition);

		//update signal count and signatures
		Integer signal = label.getSignal();
		Integer signalCount = signalOccurences.get(signal);
		Signature signature = signatures.get(signal);
		addUndo(new UndoSignatureModification(signal, signalCount, signature));

		if (signalCount == null) {
			//signal is unkown, now it occurs exactly once
			signalOccurences.put(signal, 1);
			//if there is no signature yet, add one
			if (signature==null)
				signatures.put(signal, Signature.ANY);
		}
		else
			//signal is known, update number
			signalOccurences.put(signal, signalCount+1);

		//add the appropriate undo operation
		addUndo(new UndoAddNode(newTransition));

		return newTransition;
	}
	
	/**
	 * Adds a new transition to the net with a specific id
	 */
	public Transition addTransition(SignalEdge label, Integer identifier) {
		//Generate new Transition ...
		Transition newTransition = new Transition(label, identifier, this);
		// we must check whether the identifier exists or not
		if (identifier > maxNodeNumber)
			maxNodeNumber = identifier; 

		//.. and add it to the net
		transitions.add(newTransition);

		//update signal count and signatures
		Integer signal = label.getSignal();
		Integer signalCount = signalOccurences.get(signal);
		Signature signature = signatures.get(signal);
		addUndo(new UndoSignatureModification(signal, signalCount, signature));

		if (signalCount == null) {
			//signal is unkown, now it occurs exactly once
			signalOccurences.put(signal, 1);
			//if there is no signature yet, add one
			if (signature==null)
				signatures.put(signal, Signature.ANY);
		}
		else
			//signal is known, update number
			signalOccurences.put(signal, signalCount+1);

		//add the appropriate undo operation
		addUndo(new UndoAddNode(newTransition));

		return newTransition;
	}


	/**
	 * Removes the given node from the STG.
	 * @param node
	 */
	public void removeNode(Node node) {
		if (node instanceof Transition) 
			removeTransition((Transition)node);
		if (node instanceof Place) 
			removePlace((Place)node);
	}


	/**
	 * Removes the given place from the STG.
	 * @param place
	 */
	public void removePlace(Place place) {
		place.disconnect();
		places.remove(place);	

		addUndo(new UndoRemoveNode(place));
	}


	/**
	 * Removes the given transition from the net.
	 * @param transition
	 */
	public void removeTransition(Transition transition) {
		transition.disconnect();		
		transitions.remove(transition);

		//update signal count and signatures
		Integer signal = transition.getLabel().getSignal();
		Integer signalCount = signalOccurences.get(signal);

		addUndo(new UndoSignatureModification(signal, signalCount, getSignature(signal)));
		//signal must be known
		if (signalCount == 1) {
			//last occurence, remove it
			signalOccurences.remove(signal);
			signatures.remove(signal);
		}
		else
			signalOccurences.put(signal, signalCount-1);

		addUndo(new UndoRemoveNode(transition));
	}




	// *******************************************************************
	// Undo of operations
	// *******************************************************************

	/**
	 * Adds an undo operation to the undo stack.
	 */
	private void addUndo(UndoOperation operation) {
		if (CLW.instance.UNDO_STACK.isEnabled()) {
			undoStack.push(operation);
		} 
	}

	/**
	 * Undos an operation.
	 * @throws STGException
	 */
	public void undo() throws STGException {
		if (! CLW.instance.UNDO_STACK.isEnabled())
			throw new STGException("Undo stack is not enabled.");
		
		while (undoStack.size() != 0) {
			UndoOperation op = undoStack.pop();
			if (! (op instanceof UndoMarker)) {
				op.apply();
				return;
			}
		}
		throw new STGException("Undo stack is empty.");
	}

	public void clearUndoStack() {
		undoStack.clear();
	}

	/**
	 * Adds an undo marker to the stack, see @link #undoToLastMarker().
	 */
	public void addUndoMarker(Object m) {
		undoStack.push(new UndoMarker(m));
		
		if (! CLW.instance.UNDO_STACK.isEnabled()) {
			undoStack.push(new CloneUndo(this));
		}
	}


	/**
	 * Undos all operations until an marker is reached or the undo stack is empty.
	 * @throws STGException
	 */
	public void undoToMarker(Object m) throws STGException {

		while (!undoStack.isEmpty()) {
			UndoOperation lastOperation = undoStack.pop();
			if (! (lastOperation instanceof UndoMarker) ) {
				lastOperation.apply();				
			}

			else if (((UndoMarker)lastOperation).getMarker()==m)
				break;
		}
	}


	/**
	 * Common interface for undo operations.
	 */
	private interface UndoOperation {
		public void apply() throws STGException;
	}



	private class UndoMarker implements UndoOperation {
		private Object marker;

		public UndoMarker(Object marker) {
			this.marker = marker;
		}

		public void apply(){}

		public String toString() {
			return "\nUndo marker "+marker ;
		}

		public Object getMarker() {
			return marker;
		}

	}


	private class CloneUndo implements UndoOperation {
		private STG target;
		private STG copy;
		

		public CloneUndo(STG stg) {
			target = stg;
			copy = stg.clone();
		}
		
		public void apply() throws STGException {
			target.restore(copy);			
		}
		
		public String toString() {
			return "Clone undo ";
		}
		
	}
	
	/**
	 * Collects several other undo operations, which are undone together. 
	 */
	private class CombinedUndo implements UndoOperation {

		private Stack<UndoOperation> undoStack = new Stack<UndoOperation>();
		private String message;

		public CombinedUndo(String message) {
			this.message = message;
		}

		@SuppressWarnings("unused")
		public CombinedUndo(List<UndoOperation> operations) {
			undoStack.addAll(operations);
		}

		public void addUndo(UndoOperation operation) {
			undoStack.add(operation);
		}
		public String toString() {
			return message;
		}

		public void apply() throws STGException {
			while (!undoStack.isEmpty()) {
				undoStack.pop().apply();				
			}
		}
	}

	private class UndoSignatureModification implements UndoOperation {
		private Integer signal;
		private Integer signalCount;
		private Signature signature;

		public UndoSignatureModification(Integer signal, Integer signalCount, Signature signature) {
			this.signal = signal;
			this.signalCount = signalCount;
			this.signature = signature;
		}

		public void apply() {
			if (signalCount == null || signalCount==0) {
				signatures.remove(signal);
				signalOccurences.remove(signal);
			}
			else {
				signalOccurences.put(signal, signalCount);
				if (signature==null)
					signatures.remove(signal);
				else
					signatures.put(signal, signature);
			}
		}

		public String toString() {
			return "Former signature of " + signal + " was " + signature +", former count was " + signalCount;
		}
	}

	/**
	 * Undos the addition of a node, i.e. removes some node. 
	 */
	private class UndoAddNode implements UndoOperation {

		private Node node;

		public UndoAddNode(Node node) {
			this.node = node;
		}

		public void apply() {
			--maxNodeNumber;


			node.disconnect();
			if (node instanceof Place)
				places.remove(node);
			else 
				transitions.remove(node);
		}

		public String toString() {
			return "Added node "+node.toString();
		}

	}

	/**
	 *	Undos the deletion of a node, i.e. adds it to the STG. 
	 */
	private class UndoRemoveNode implements UndoOperation {
		private Node node;

		public UndoRemoveNode(Node node) {
			this.node = node;
		}

		public void apply() {
			if (node instanceof Place)
				places.add((Place) node);

			else if (node instanceof Transition)
				transitions.add((Transition) node);

			node.reconnect();
		}

		public String toString() {
			return "Removed node "+node.toString();
		}
	}

	private class UndoSetSignature implements UndoOperation {
		private Collection<Integer> signals;
		private Signature oldSignature;

		@SuppressWarnings("unused")
		public UndoSetSignature(Collection<Integer> signals, Signature oldSignature) {
			this.signals = signals;
			this.oldSignature = oldSignature;
		}

		public UndoSetSignature(Integer signal, Signature oldSignature) {
			this.signals = new LinkedList<Integer>();
			signals.add(signal);
			this.oldSignature = oldSignature;
		}

		public void apply() {
			for (Integer signal : signals)
				signatures.put(signal, oldSignature);

		}

		public String toString() {
			return "Changed signature of " + signals +" from " + oldSignature;
		}

	}

	// *******************************************************************
	// Coordinate handling
	// *******************************************************************


	/**
	 * Returns an immutable representation of the saved coordinates.
	 * @throws @link UnsupportedOperationException if @link #withCoordinates
	 * is false.
	 */
	public STGCoordinates getCoordinates() {

		return coordinates;
	}


	/**
	 * creates a clone for coordinates from another STG
	 */
	public void copyCoordinates(STGCoordinates coordinates) {
		this.coordinates.clear();
		this.coordinates = (STGCoordinates)coordinates.clone();
	}

	
	/**
	 * Returns the coordinates of the given node.
	 * @throws @link UnsupportedOperationException if @link #withCoordinates
	 * is false.
	 */
	public Point getCoordinates(Node node) {
		if (!coordinates.containsKey(node)) return null;
//			throw new UnsupportedOperationException("Coordinate for node "+node+" not found");

		return coordinates.get(node);
	}


	
	/**
	 * Sets the coordinates of the given node.
	 * @throws @link STGException if the node is unknown
	 * and ignores the call if @link #withCoordinates
	 * is false.
	 */
	public Point setCoordinates(Node node, Point point) throws STGException {

		if (!places.contains(node) && !transitions.contains(node))
			throw new STGException("Unknown node '"+node+"'");
		
		return coordinates.put(node, point);
	}



	// *******************************************************************
	// The rest
	// *******************************************************************

	/**
	 * Converts the STG to a PostScript file and shows it on the screen.
	 * TODO move this eleswhere
	 */
	public void showPS()  {
		Set<Node> emptySet = Collections.emptySet();
		showPS(emptySet);		
	}
		 
	
	
	/**
	 * Converts the STG to a PostScript file and shows it on the screen. TODO
	 * move this eleswhere
	 */
	public void showPS(Collection<Node> nodes) {
		try {
			String gFile = File.createTempFile("desij-stg", ".g")
					.getCanonicalPath();
			String psFile = gFile + ".ps";

			FileSupport.saveToDisk(STGFile.convertToDot(this, nodes, ""), gFile);

			HelperApplications.startExternalTool(HelperApplications.DOT, 
					" -Tps " + 
					HelperApplications.SECTION_START+gFile+HelperApplications.SECTION_END + 
					" -o " + 
					HelperApplications.SECTION_START+psFile+HelperApplications.SECTION_END).waitFor();
			HelperApplications.startExternalTool(HelperApplications.GHOSTVIEW, 
					HelperApplications.SECTION_START+psFile+HelperApplications.SECTION_END);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retuns a compressed String representation of the STG, essentially a List
	 * of all nodes.
	 */
	@Override
	public String toString() {
		List<String> places = collectFromPlaces(
				ConditionFactory.ALL_PLACES, 
				CollectorFactory.getStringCollector());
		List<String> transitions = collectFromTransitions(
				ConditionFactory.ALL_TRANSITIONS, 
				CollectorFactory.getStringCollector());

		return places.toString()+transitions.toString();
	}


	/**
	 * Optimises the STG, i.e. place and transitions are just numbered, place labels are removed.
	 */
	public void simplifyLabels() {
		for (Place place : places) {		
			place.setLabel("p");
		}		
	}


	/**
	 * Contracts the given transition and return the newly generated places.
	 * @param transition
	 * @return
	 * @throws STGException
	 */
	public Collection<Place> contract(Transition transition) throws STGException {
		if (!ConditionFactory.CONTRACTABLE.fulfilled(
				transition))
			throw new STGException("Contraction not possible: "+transition);

		CombinedUndo contractionUndo = new CombinedUndo("Contraction of "+transition);

		Set<Node> parents = transition.getParents();
		Set<Node> children = transition.getChildren();

		//checking for self-triggering
		List<Place> newPlaces = new LinkedList<Place>();

		for (Node actParent : parents) {
			for (Node actChild : children) {

				//add brace symbol to all regular expressions
				//IMPORTANT give SIMPLE names to the places
				Place newPlace;
				int marking = ((Place) actParent).getMarking() + ((Place) actChild).getMarking();    

				if (CLW.instance.PRODUCTIVE.isEnabled()  )
				{
					newPlace = new Place("p", ++maxNodeNumber, marking, this);
					places.add(newPlace);
					contractionUndo.addUndo(new UndoAddNode(newPlace));
				}
				else {                	
					newPlace = new Place(
							"_"+actParent.getString(Node.SIMPLE) + "." + actChild.getString(Node.SIMPLE)+"_", 
							++maxNodeNumber, marking, this);
					places.add(newPlace);
					contractionUndo.addUndo(new UndoAddNode(newPlace));
				}



				/*
				 * !! Important !! For the following loops, from the first
				 * two ones transition has to be removed from the current
				 * list, otherwise a ConcurrentModificationException is
				 * raised for the outer loop over children, parents resp.
				 * This does not change the result since, transition is
				 * removed afterwards.
				 * 
				 * This is not neccessary for the last two loops, because
				 * transition is contractable and therefore has no adjacent
				 * loop places
				 */

				Set<Node> pc = actParent.getChildren();
				pc.remove(transition);
				for (Node actChildParent : pc)
					newPlace.addToChildValue(actChildParent, actParent
							.getChildValue(actChildParent));

				Set<Node> cp = actChild.getParents();
				cp.remove(transition);
				for (Node actParentChild : cp)
					newPlace.addToParentValue(actParentChild, actChild
							.getParentValue(actParentChild));

				for (Node actParentParent : actParent.getParents())
					newPlace.addToParentValue(actParentParent, actParent
							.getParentValue(actParentParent));

				for (Node actChildChild : actChild.getChildren())
					newPlace.addToChildValue(actChildChild, actChild
							.getChildValue(actChildChild));

				newPlaces.add(newPlace);


			}
		}

		
		Set<Node> toRemove = new HashSet<Node>(); // avoid concurrent container modification
		
		for (Node node : parents){
			contractionUndo.addUndo(new UndoRemoveNode(node));
			toRemove.add(node);
		}

		for (Node node : children){
			contractionUndo.addUndo(new UndoRemoveNode(node));
			toRemove.add(node);
		}
		
		for (Node node: toRemove) {
			node.disconnect();
			places.remove(node);
		}

		Integer signal = transition.getLabel().getSignal();
		int sc = signalOccurences .get(signal);
		contractionUndo.addUndo(new UndoSignatureModification(signal, sc, getSignature(signal)));
		if (sc>1)
			signalOccurences.put(signal, sc-1);
		else {
			signalOccurences.remove(signal);
			signatures.remove(signal);
		}

		transition.disconnect();
		contractionUndo.addUndo(new UndoRemoveNode(transition));
		transitions.remove(transition);

		addUndo(contractionUndo);

		return newPlaces;
	}

	public Map<Integer, Signature> getSignature() {
		return signatures;
	}

	public Map<String, Integer> getSignalNumbers() {
		return Collections.unmodifiableMap(signalNumbers);
	}

	/**
	 * Associates a new signal number with a signal name. If the new number is already in use
	 * for some signal, the numbers are exchanged. If the signal is already associated to
	 * the signal name, the method returns immediately without an error. 
	 * @param newName
	 * @param newSignal
	 * @throws STGException
	 */
	public void reassignSignal(String newName, Integer newSignal) throws STGException {
		Integer oldSignal = signalNumbers.get(newName);

		// Reassignment not necessary
		if (oldSignal.equals(newSignal))
			return;

		String oldName = signalNames.get(newSignal);
		if (oldName == null) {
			// new signal not in use
			final Integer nSignal = newSignal;
			Operations.modify(
					transitions, 
					ConditionFactory.getSignalOfCondition(oldSignal), 
					new Operation<Transition>() {
						public void operation(Transition o) throws STGException {
							o.label = new SignalEdge(nSignal, o.label.getDirection());					
						}		
					}
			);

			signalNumbers.put(newName, newSignal);
			signalNames.put(newSignal, newName);
			signalOccurences.put(newSignal, signalOccurences.remove(oldSignal));
			signatures.put(newSignal, signatures.remove(oldSignal));

			signalNames.remove(oldSignal);
		}
		else {
			// new signal already in use -> exchange them
			final Integer nSignal = newSignal;
			final Integer oSignal = oldSignal;

			Operations.modify(
					transitions, 
					ConditionFactory.getSignalOfCondition(oldSignal, newSignal), 
					new Operation<Transition>() {
						public void operation(Transition o) throws STGException {
							if (o.label.getSignal().equals(oSignal)) {
								o.label = new SignalEdge(nSignal, o.label.getDirection());	
							}
							else {
								o.label = new SignalEdge(oSignal, o.label.getDirection());
							}												
						}		
					}
			);		


			Integer oldOccurcences = signalOccurences.get(newSignal);
			Integer newOccurcences = signalOccurences.get(oldSignal);
			signalOccurences.put(newSignal, newOccurcences);
			signalOccurences.put(oldSignal, oldOccurcences);

			Signature oldSignature = signatures.get(newSignal);
			Signature newSignature = signatures.get(oldSignal);
			signatures.put(newSignal, newSignature);
			signatures.put(oldSignal, oldSignature);

			signalNames.put(oldSignal, oldName);
			signalNumbers.put(oldName, oldSignal);
			signalNames.put(newSignal, newName);
			signalNumbers.put(newName, newSignal);
		}

	}

	public int getSize() {
		return places.size() + transitions.size();
	}

	/**
	 * Computes the parallel composition of n STGs according to the STG-decompositon papers of Vogler et al.
	 * @param args - the STGs which should be composed
	 * @return - the parallel composition result
	 * @throws STGException 
	 */
	public static STG parallelComposition(LinkedList<STG> stgs) throws STGException {

		int size = stgs.size() - 1;
		int cur = 1;
		
		
		STG result = new STG();
		
		
		while (stgs.size() > 1) {
			System.out.println("Building composition " + cur++ + " of " + size);
			result = STG.parallelCompositionOf2STGs(stgs.poll(), stgs.poll());
//			STGUtil.removeRedundantPlaces(result);
//			stgs.addLast(result);
			stgs.addFirst(result);
		}

		return stgs.peek();
	}
	
	
	/**
	 * Generates the parallel composition of 2 STGs according to the STG-decomposition papers of Vogler et al.
	 * Acts as a helper for the parallel compositon of n STGs, in particular with n > 2
	 * @param n1 - the first STG
	 * @param n2 - the second STG
	 * @return - the parallelComposition STG
	 * @throws STGException
	 */
	private static STG parallelCompositionOf2STGs(STG n1, STG n2) throws STGException {

		if (n1 == null)
			return n2;
		else if (n2 == null)
			return n1;

		// **** Validation check ****
		// intersection(n1.out, n2.out) is empty ?
		// and by the way generate the set of common signals A
		Signature	signN1, signN2;
		String		signalNameN1, signalNameN2;
		Set<String>	setA = new HashSet<String>(); // A: set of common signals
		for ( Integer signalOfN1 : n1.getSignals() )
			for ( Integer signalOfN2 : n2.getSignals() ) {
				signN1 = n1.signatures.get(signalOfN1);
				signN2 = n2.signatures.get(signalOfN2);
				signalNameN1 = n1.signalNames.get(signalOfN1);
				signalNameN2 = n2.signalNames.get(signalOfN2);
				if (signalNameN1.equals(signalNameN2)) {
					if ( (signN1 == Signature.OUTPUT || signN1 == Signature.INTERNAL) && 
							(signN2 == Signature.OUTPUT || signN2 == Signature.INTERNAL) ) {
						throw new STGException("|| Composition is impossible, due to output - or internal signal " 
								+ signalNameN1 + " is in at least 2 STGs.");
					}
					else if ( (signN1 == Signature.INTERNAL && signN2 == Signature.INPUT) ||
							(signN1 == Signature.INPUT && signN2 == Signature.INTERNAL) ) {
						throw new STGException("|| Composition is impossible, due to signal " + signalNameN1 + 
								" is an once internal - and furthermore an input signal.");
					}
					else if ( (signN1 == Signature.INPUT || signN1 == Signature.OUTPUT) && 
							(signN2 == Signature.INPUT || signN2 == Signature.OUTPUT ) ) {
						// fill the set A
						setA.add(signalNameN1);
					}
				}
			}



		// ****** build the parallel composition *******
		// make a copy of N1 without transitions with the signalnames of setA 
		// and traverse N2 and add copies of all its elements to the clone of N1
		// according to the definition of parallel composition

		Map<Place,Place> newN1Places = new HashMap<Place,Place>();
		STG result = n1.specialCloneWithPlaceMapping(newN1Places);

		// the next two for-loops are dedicated to remove transitions labeled with signalNames from setA 
		// but don't remove transitions which do not have counterpart-transitions in N2 (with the same label,
		// in particular the same direction)
		Set<String> setASignalsOfResultNotToDelete 	= new HashSet<String>();
		for (Transition transOfN1 : result.getTransitions(ConditionFactory.getSignalNameOfTransitionCondition(setA))) {
			boolean flag = false;
			for (Transition transOfN2 : 
				n2.getTransitions(ConditionFactory.getSignalNameOfTransitionCondition(result.getSignalName(transOfN1.label.signal))))
				if (transOfN1.label.direction.toString().equals(transOfN2.label.direction.toString()))
					flag = true;
			if (flag)
				result.removeTransition(transOfN1);
			else 
				setASignalsOfResultNotToDelete.add(result.getSignalName(transOfN1.label.signal));
		}

		// consistency of signalNames und signalNumbers
		for (String signalName : setA)
			if (!setASignalsOfResultNotToDelete.contains(signalName))
				result.signalNames.remove(result.signalNumbers.remove(signalName));


		// temporary mappings between the elements of N2 and new created places, transitions and labels
		// the keys of newTransitions and newSynchronizers are disjoint sets
		Map<Transition,Transition> 		newTransitions 	= new HashMap<Transition,Transition>();
		Map<Place,Place>				newN2Places		= new HashMap<Place,Place>();
		Map<SignalEdge,SignalEdge> 		newLabels		= new HashMap<SignalEdge,SignalEdge>();
		Map<Transition,Set<Transition>>	newSynchronizers= new HashMap<Transition,Set<Transition>>();

		// for each place of N2 make a copy in result and adopt its marking
		for (Place placeOfN2 : n2.getPlaces(ConditionFactory.ALL_PLACES)) {
			Place newPlace = result.addPlace("p" + (result.maxNodeNumber+1), placeOfN2.getMarking());
			newN2Places.put(placeOfN2, newPlace);
		}

		// for each transition of N2, whereas its signal is not an element of set A make a copy in result;
		// if the transition-signal is an element of setA compute the Cartesian product of transN2 and all 
		// the corresponding transitions in N1 with the same label

		for (Transition transOfN2 : n2.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			Transition newTransition;
			SignalEdge newLabel;
			signalNameN2 = n2.signalNames.get(transOfN2.label.signal);
			if (setA.contains(signalNameN2)) {
				// compute the cartesian product of transOfN2 and all transOfN1 with the same signalName and direction
				newSynchronizers.put(transOfN2, new HashSet<Transition>());
				for (Transition transOfN1 : 
					n1.getTransitions(ConditionFactory.getSignalNameOfTransitionCondition(signalNameN2))) {
					if (transOfN2.label.direction.toString().equals(transOfN1.label.direction.toString())) {
						if (newLabels.containsKey(transOfN2.label)) {
							newLabel = newLabels.get(transOfN2.label);
						}
						else if (result.signalNumbers.containsKey(signalNameN2) && 
								result.signalNames.containsValue(signalNameN2)) {
							newLabel = new SignalEdge(result.signalNumbers.get(signalNameN2), 
									transOfN2.label.direction);
							newLabels.put(transOfN2.label, newLabel);
						}
						else {
							newLabel = new SignalEdge(new Integer(result.maxNodeNumber+1),transOfN2.label.direction);
							newLabels.put(transOfN2.label, newLabel);
							result.signalNames.put(newLabel.signal, signalNameN2);
							result.signalNumbers.put(signalNameN2,newLabel.signal);
							if (n2.signatures.get(transOfN2.label.signal).compareTo(Signature.OUTPUT) == 0 ||
									n1.signatures.get(transOfN1.label.signal).compareTo(Signature.OUTPUT) == 0 )
								result.signatures.put(newLabel.signal, Signature.OUTPUT);
							else
								result.signatures.put(newLabel.signal, Signature.INPUT);
						}
						newTransition = result.addTransition(newLabel);
						newSynchronizers.get(transOfN2).add(newTransition);
						// Connection to n1-mapped pre and post result-places
						for (Node parent : transOfN1.getParents())
							newTransition.setParentValue(newN1Places.get(parent), transOfN1.getParentValue(parent));
						for (Node child : transOfN1.getChildren())
							newTransition.setChildValue(newN1Places.get(child), transOfN1.getChildValue(child));
					}
				}
				if (newSynchronizers.get(transOfN2).isEmpty()) {
					// in N1 there are transistions with the same signalName with transOfN2 but not with the same direction
					newSynchronizers.remove(transOfN2);
					if (newLabels.containsKey(transOfN2.label)) {
						newLabel = newLabels.get(transOfN2.label);
					}
					else if (result.signalNumbers.containsKey(signalNameN2) &&
							result.signalNames.containsValue(signalNameN2)) {
						newLabel = new SignalEdge(result.signalNumbers.get(signalNameN2), transOfN2.label.direction);
						newLabels.put(transOfN2.label, newLabel);
					}
					else {
						newLabel = new SignalEdge(new Integer(result.maxNodeNumber+1),transOfN2.label.direction);
						newLabels.put(transOfN2.label, newLabel);
						result.signalNames.put(newLabel.signal, signalNameN2);
						result.signalNumbers.put(signalNameN2, newLabel.signal);
						result.signatures.put(newLabel.signal, n2.signatures.get(transOfN2.label.signal));
					}
					newTransition = result.addTransition(newLabel);
					newTransitions.put(transOfN2, newTransition); // because it is not a real synchronizer
				}
			}
			else { // if the Signalname of transOfN2 is not an element of setA
				if (newLabels.containsKey(transOfN2.label)) {
					newLabel = newLabels.get(transOfN2.label);
				}
				else if (result.signalNumbers.containsKey(signalNameN2) &&
						result.signalNames.containsValue(signalNameN2)) {
					newLabel = new SignalEdge(result.signalNumbers.get(signalNameN2), transOfN2.label.direction);
					newLabels.put(transOfN2.label, newLabel);
				}
				else {
					newLabel = new SignalEdge(new Integer(result.maxNodeNumber+1),transOfN2.label.direction);
					newLabels.put(transOfN2.label, newLabel);
					result.signalNames.put(newLabel.signal, signalNameN2);
					result.signalNumbers.put(signalNameN2, newLabel.signal);
					result.signatures.put(newLabel.signal, n2.signatures.get(transOfN2.label.signal));
				}
				newTransition = result.addTransition(newLabel);
				newTransitions.put(transOfN2, newTransition);
			}
		}

		// Completion of the incidence matrix of result with the new transitions corresponding to transitions of N2
		for (Place placeOfN2 : n2.getPlaces(ConditionFactory.ALL_PLACES)) {
			Place newPlace = newN2Places.get(placeOfN2);
			for (Node child : placeOfN2.getChildren()) {
				if (newTransitions.containsKey(child)) // Signalname is not an element of setA (resp. newSynchronizers)
					newPlace.setChildValue(newTransitions.get(child), placeOfN2.getChildValue(child));
				else if (newSynchronizers.containsKey(child)) // Signalname is an element of setA
					for (Transition trans : newSynchronizers.get(child))
						newPlace.setChildValue(trans, placeOfN2.getChildValue(child));
				else
					throw new STGException("Composition not possible, because of forgotten transition:" + child.toString());
			}
		}

		for (Transition transOfN2 : n2.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (newTransitions.containsKey(transOfN2)) {
				Transition newTransition = newTransitions.get(transOfN2);
				for (Node child : transOfN2.getChildren())
					newTransition.setChildValue(newN2Places.get(child), transOfN2.getChildValue(child));
			}
			else if (newSynchronizers.containsKey(transOfN2))
				for (Transition newTransition : newSynchronizers.get(transOfN2))
					for (Node child : transOfN2.getChildren())
						newTransition.setChildValue(newN2Places.get(child), transOfN2.getChildValue(child));
			else
				throw new STGException("Composition not possible, because of forgotten transition: " + transOfN2.toString());
		}

		return result;
	}

	/**
	 * Acts as a helper function only for the parallel composition of two STGs;
	 * It works like the stg.clone() function (without regarding of 'withCoordinates') 
	 * but it returns the newPlaces-HashMap as an additional return value
	 * @see #clone()
	 */
	private STG specialCloneWithPlaceMapping(Map <Place,Place> newPlaces) {
		//The resulting copy
		STG result = new STG();


		result.signalNames = new HashMap<Integer, String>(signalNames);
		result.signalNumbers = new HashMap<String, Integer>(signalNumbers);

		//Contains the copy of each transition  
		Map<Transition,Transition> newTransitions = new HashMap<Transition,Transition>();


		try {
			//For each place: make a copy 	
			for (Place place : places) {
				Place newPlace = place.clone();
				newPlace.setSTG(result);
				result.places.add(newPlace);
				newPlaces.put(place, newPlace);		        
			}

			//For each transition: make a copy 	
			for (Transition transition : transitions) {
				Transition newTransition = transition.clone();
				newTransition.setSTG(result);
				result.transitions.add(newTransition);
				newTransitions.put(transition, newTransition);
			}

			//Copy incidence matrix to new nodes
			for (Place place : places) {
				Place newPlace = newPlaces.get(place);
				for (Node child : place.getChildren()) 
					newPlace.setChildValue(
							newTransitions.get(child),
							place.getChildValue(child) );
			}

			for (Transition transition : transitions) {
				Transition newTransition = newTransitions.get(transition); 
				for (Node child : transition.getChildren()) 
					newTransition.setChildValue(
							newPlaces.get(child),
							transition.getChildValue(child) );
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		result.maxNodeNumber = maxNodeNumber;		

		for (Integer signal : signatures.keySet())
			result.signatures.put(signal, signatures.get(signal));

		for (Integer signal : signalOccurences.keySet())
			result.signalOccurences.put(signal, signalOccurences.get(signal));

		return result;
	}

	public Collection<Place> getPlaces() {
		return Collections.unmodifiableCollection(places);
	}
	
	/**
	 * Returns the node with the given identifier
	 * @param identifier
	 * @return Node or null if no node was found
	 */
	public Node getNode(int identifier) {
		for (Transition trans: transitions)
			if (trans.getIdentifier() == identifier) return trans;
		
		for (Place place: places)
			if (place.getIdentifier() == identifier) return place;
		
		return null;
	}
	
	
	/**
	 * Returns the transition with the given identifier
	 * @param identifier
	 * @return trans or null if no transition was found
	 */
	public Transition getTransition(int identifier) {
		for (Transition trans: transitions)
			if (trans.getIdentifier() == identifier) return trans;
		return null;
	}
	
	
	/**
	 * Returns the maxNodeNumber in order to generate unique Names for Nodes
	 * @return
	 */
	public int getMaxNodeNumber() {
		return this.maxNodeNumber;
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int PRIME = 31;
//		int result = 1;
//		result = PRIME * result + ((coordinates == null) ? 0 : coordinates.hashCode());
//		result = PRIME * result + maxNodeNumber;
//		result = PRIME * result + ((places == null) ? 0 : places.hashCode());
//		result = PRIME * result + ((signalNames == null) ? 0 : signalNames.hashCode());
//		result = PRIME * result + ((signalNumbers == null) ? 0 : signalNumbers.hashCode());
//		result = PRIME * result + ((signalOccurences == null) ? 0 : signalOccurences.hashCode());
//		result = PRIME * result + ((signatures == null) ? 0 : signatures.hashCode());
//		result = PRIME * result + ((transitions == null) ? 0 : transitions.hashCode());
//		result = PRIME * result + (withCoordinates ? 1231 : 1237);
//		return result;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final STG other = (STG) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (maxNodeNumber != other.maxNodeNumber)
			return false;
		if (places == null) {
			if (other.places != null)
				return false;
		} else if (!places.equals(other.places))
			return false;
		if (signalNames == null) {
			if (other.signalNames != null)
				return false;
		} else if (!signalNames.equals(other.signalNames))
			return false;
		if (signalNumbers == null) {
			if (other.signalNumbers != null)
				return false;
		} else if (!signalNumbers.equals(other.signalNumbers))
			return false;
		if (signalOccurences == null) {
			if (other.signalOccurences != null)
				return false;
		} else if (!signalOccurences.equals(other.signalOccurences))
			return false;
		if (signatures == null) {
			if (other.signatures != null)
				return false;
		} else if (!signatures.equals(other.signatures))
			return false;
		if (transitions == null) {
			if (other.transitions != null)
				return false;
		} else if (!transitions.equals(other.transitions))
			return false;
		
		if (isWithCoordinates() != other.isWithCoordinates())
			return false;
		return true;
	}
	
	/*
	 * returns the highest signal number of this STG in order to insert new Signals with 
	 * a different signal number
	 */
	public int getHighestSignalNumber() {
		int highestSignalNumber = 0;
		
		for (int i: this.getSignalNumbers().values())
			if (i > highestSignalNumber)
				highestSignalNumber = i;
		
		return highestSignalNumber;
	}
	
	
	
	/*
	 * returns true, if a transition has enough tokens to fire
	 */
	public boolean canFire(Transition t) {
		
		for (Node n : t.getParents()) {
			Place p = (Place)n;  
			if (p.getMarking()<t.getParentValue(p)) return false; 
		}
		return true;
	}
	
	/*
	 * fires a given transition (moves tokens)
	 */
	public void fireTransition(Transition t) {
		if (canFire(t)) {
			
			for (Node n : t.getParents()) {
				Place p = (Place)n;
				int pv = t.getParentValue(p);
				p.setMarking(p.getMarking()-pv); 
			}
			
			for (Node n : t.getChildren()) {
				Place p = (Place)n;
				int cv = t.getChildValue(p);
				p.setMarking(p.getMarking()+cv); 
			}
		}
	}

	/*
	 * returns true, if a transition has enough tokens to unfire
	 */
	public boolean canUnFire(Transition t) {
		
		for (Node n : t.getChildren()) {
			Place p = (Place)n;  
			if (p.getMarking()<t.getChildValue(p)) return false; 
		}
		return true;
	}
	
	/*
	 * unFires a given transition (moves tokens)
	 */
	public void unFireTransition(Transition t) {
		if (canUnFire(t)) {
			
			for (Node n : t.getChildren()) {
				Place p = (Place)n;
				int pv = t.getChildValue(p);
				p.setMarking(p.getMarking()-pv); 
			}
			
			for (Node n : t.getParents()) {
				Place p = (Place)n;
				int cv = t.getParentValue(p);
				p.setMarking(p.getMarking()+cv); 
			}
		}
	}
	
}


