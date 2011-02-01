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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.EdgeDirection;

/**
 * @author dwist
 * An abstract implementation of the place insertion strategy for the avoidance of 
 * irreducible CSC conflicts
 */
abstract class PlaceHolderInsertion implements IPlaceHolderInsertionStrategy {
	//state of PlaceHolderInsertion
	protected boolean initialized = false; 
	protected int tTransInsertionCount = 0;
	
	protected STG stg;
	protected Collection<STG> components;
	protected Map<Node,NodeProperty> additionalNodeInfos;
	protected TraversalResult myTraversalResult;
	protected STG critComp;
	private Map<STG,List<String>> partitionMap;
	
	/**
	 * Constructor
	 */
	public PlaceHolderInsertion (STG stg, Collection<STG> components, 
			Map<Node,NodeProperty> nodeInfos) {
		this.stg = stg;
		this.components = components;
		this.additionalNodeInfos = nodeInfos;
		
		// new partition for recalculating the critical and delay components
		// will be filled during the doPlaceHolderInsertion method
		initialzePartition();
	}
	
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#getInsertedPlaceholderTransitionCount()
	 * should only be called after the entire PlaceHolderInsertion execution
	 */
	public int getInsertedPlaceholderTransitionCount() {
		return this.tTransInsertionCount;
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#execute(net.strongdesign.stg.Transition, net.strongdesign.stg.Transition)
	 * Do not call before initializeTraversal!
	 */
	public abstract boolean execute(Transition t_en, Transition t_ex)
			throws STGException;

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#getNewPartition()
	 * should only be called after the entire PlaceHolderInsertion execution
	 * --> Returns an incomplete partition, generally.
	 */
	public Partition getNewPartition() throws STGException {
		Partition result = new Partition();
		List<String> partitionBlock;
		
		for (STG component: partitionMap.keySet()) {
			partitionBlock = partitionMap.get(component);
			if (!partitionBlock.isEmpty()) {
				result.beginSignalSet();
				for (String outSignal: partitionBlock)
					result.addSignal(outSignal);
			}
		}
		return result;
	}
	
	@Override
	public void initialzePartition() {
		// new partition for recalculating the critical and delay components
		// will be filled during the doPlaceHolderInsertion method
		this.partitionMap = new HashMap<STG, List<String>>();
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#getReplacedComponents()
	 * should only be called after the entire PlaceHolderInsertion execution
	 */
	public Collection<STG> getReplacedComponents() {
		return partitionMap.keySet();
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#initializeTraversal(net.strongdesign.stg.STG, java.util.List)
	 * Call always before a new execute!
	 */
	public void initializeTraversal(STG component, List<Transition> ctp) {
		NodeProperty nodeInfo;
		
		for (Node node: additionalNodeInfos.keySet()) {
			nodeInfo = additionalNodeInfos.get(node);
			nodeInfo.isVisited = false;
			nodeInfo.isVisitedBy2ndSearch = false;
			if (node instanceof Transition) {
				nodeInfo.isEntryTransition = false;
				nodeInfo.isExitTransition = false;
				if ((Transition)node == ctp.get(0))
					nodeInfo.isEntryTransition = true;
				if ((Transition)node == ctp.get(1))
					nodeInfo.isExitTransition = true;
				if (component.getTransition(node.getIdentifier()) != null)
					nodeInfo.isRelevant = true; // should be hold for entry- and exit-transition, too
				else
					nodeInfo.isRelevant = false;
			}
		}
		this.critComp = component;
		myTraversalResult = new TraversalResult();
		initialized = true;
	}
	
	/***
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#doPlaceHolderInsertion(STG critComp)
	 * 
	 * inserts a transition as placeholder such that after its refinement the 
	 * considered irreducible CSC conflict is avoided
	 * @param traversalInfo
	 * @param tTransInsertionCount
	 * @return the inserted internal transition to avoid the irreducible CSC conflict
	 */
	public Transition doPlaceHolderInsertion(STG critComp) {
		
		tTransInsertionCount++;
				
		String pULabel = "pU" + tTransInsertionCount;
		String pDLabel = "pD" + tTransInsertionCount;
		
		// generate newLabel
		int idForNewSignal = stg.getHighestSignalNumber() + 1;
		// an unknown edge direction, since the inserted transition will act only as a placeholder
		SignalEdge newLabel = new SignalEdge(idForNewSignal, EdgeDirection.UNKNOWN);
		
		Transition t_ic = null;
		
		Place pU = stg.addPlace(pULabel, 0);
		Place pD = stg.addPlace(pDLabel, myTraversalResult.marking);
		
		try {
			stg.setSignalName(newLabel.getSignal(), "ic" + tTransInsertionCount);
		}
		catch (STGException e) {e.printStackTrace();}
		stg.setSignature(newLabel.getSignal(), Signature.INTERNAL);
		
		t_ic = stg.addTransition(newLabel);
		
		for (Node trans: myTraversalResult.prePlaceHolder)
			if (trans instanceof Transition) pU.addToParentValue(trans, 1);
		
		pU.setChildValue(t_ic, 1);
		pD.setParentValue(t_ic, 1);
		
		for (Node trans: myTraversalResult.postPlaceHolder)
			if (trans instanceof Transition) pD.addToChildValue(trans, 1);
		
		// update additionalNodeInfos
		additionalNodeInfos.put(pU, new NodeProperty());
		additionalNodeInfos.put(pD, new NodeProperty());
		additionalNodeInfos.put(t_ic, new NodeProperty());
		
		// prepare the new output partition block for the critcal component
		if (stg.getSignalName(newLabel.getSignal()) != null) {
			if (!partitionMap.containsKey(critComp)) {
				ArrayList<String> outputs = new ArrayList<String>();
				for (int sig: critComp.getSignals()) {
					if (critComp.getSignature(sig) == Signature.OUTPUT || critComp.getSignature(sig) == Signature.INTERNAL) {
						outputs.add(critComp.getSignalName(sig));
					}
				}
				partitionMap.put(critComp, outputs);
			}
			partitionMap.get(critComp).add(stg.getSignalName(newLabel.getSignal()));
			
			// assure that all delay components are also in the partitionMap structure
			for (Node trans: myTraversalResult.postPlaceHolder) {
				STG delayComp = null;
				if (trans instanceof Transition)
					delayComp = findDelayComponent(((Transition)trans).getLabel().getSignal());
				if (delayComp != null) 
					if (!partitionMap.containsKey(delayComp)) {
						ArrayList<String> outputs = new ArrayList<String>();
						for ( String outSignal: delayComp.getSignalNames(delayComp.getSignals(Signature.OUTPUT)) )
							outputs.add(outSignal);
						for ( String intSignal: delayComp.getSignalNames(delayComp.getSignals(Signature.INTERNAL)) )
							outputs.add(intSignal);
						partitionMap.put(delayComp, outputs);
					}
			}	
		}
		
		return t_ic;
		
	}
	
	protected STG findDelayComponent(Integer signal) {
		// I don't know whether signals are unique, but signalnames must be unique -> so use them 
		String signalName = stg.getSignalName(signal);
		if (signalName != null) {
			for (STG comp: this.components) {
				for ( String outSignal: comp.getSignalNames(comp.getSignals(Signature.OUTPUT)) )
					if (outSignal.equals(signalName))
						return comp;
				for ( String intSignal: comp.getSignalNames(comp.getSignals(Signature.INTERNAL)) )
					if (intSignal.equals(signalName))
						return comp;
				// if no component found, than the delay signal is one of the new introduced internal
				// communication signals --> i.e. the delay component is a former critical component 
				// and is already in the partitionMap structure
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.IPlaceHolderInsertionStrategy#doInsertionForSameConflict(java.util.Iterator, net.strongdesign.stg.Transition)
	 * 
	 * For optimization: i.e. solve the same irreducible CSC conflict in different components
	 * Call immediatly after doPlaceHolderInsertion() and in particular before execute() 
	 */
	public void doInsertionForSameConflict(Iterator<STG> componentIterator, Transition insertedPlaceHolder) {
		Place post = (Place)insertedPlaceHolder.getChildren().iterator().next();
		Transition previousTransition = insertedPlaceHolder;
				
		while (componentIterator.hasNext()) {
			STG critComp = componentIterator.next();
			tTransInsertionCount++;
			
			String pULabel = "pU" + tTransInsertionCount;
			
			// generate newLabel
			int idForNewSignal = stg.getHighestSignalNumber() + 1;
			// an unknown edge direction, since the inserted transition will act only as a placeholder
			SignalEdge newLabel = new SignalEdge(idForNewSignal, EdgeDirection.UNKNOWN);
			
			Transition t_ic = null;
			
			Place pU = stg.addPlace(pULabel, 0);
			
			try {
				stg.setSignalName(newLabel.getSignal(), "ic" + tTransInsertionCount);
			}
			catch (STGException e) {e.printStackTrace();}
			stg.setSignature(newLabel.getSignal(), Signature.INTERNAL);
			
			t_ic = stg.addTransition(newLabel);
			
			pU.setParentValue(previousTransition, 1);
			pU.setChildValue(t_ic, 1);
			if (!componentIterator.hasNext()) {
				post.setParentValue(t_ic, 1); // only for the last iteration
				post.setParentValue(insertedPlaceHolder, 0); // remove the old connection
			}
			
			// update additionalNodeInfos
			additionalNodeInfos.put(pU, new NodeProperty());
			additionalNodeInfos.put(t_ic, new NodeProperty());
			
			// prepare the new output partition block for the critical component
			if (stg.getSignalName(newLabel.getSignal()) != null) {
				if (!partitionMap.containsKey(critComp)) {
					ArrayList<String> outputs = new ArrayList<String>();
					for (int sig: critComp.getSignals()) {
						if (critComp.getSignature(sig) == Signature.OUTPUT || critComp.getSignature(sig) == Signature.INTERNAL) {
							outputs.add(critComp.getSignalName(sig));
						}
					}
					partitionMap.put(critComp, outputs);
				}
				partitionMap.get(critComp).add(stg.getSignalName(newLabel.getSignal())); // the new internal communication signal
				
				// The delay component must already be in the partitionMap structure since it is a former critical component
				// or it will be inserted in the next iteration
			}
			
			previousTransition = t_ic;
		}
	}
	
//	use like a struct in C
	protected class TraversalResult {
		List<Node> prePlaceHolder;	// PreSet of the PlaceHolder
		List<Node> postPlaceHolder;	// PostSet of the PlaceHolder
		int	marking = 0;			// marking for new p_o
		
		private List<Place> undoMarkers;
				
		TraversalResult() {
			super();
			prePlaceHolder = new LinkedList<Node>();
			postPlaceHolder = new LinkedList<Node>();
			undoMarkers = new LinkedList<Place>();
		}
		
		// ATTENTION: HACK, with an abuse of places
		
		// must always be called before undoToLastMarker is called
		void addUndoMarker() {
			// abuse place as UndoMarker, do not insert it in any STG
			// !!! the identifier specifies the old value of this marking
			Place undoMarker = new Place("UndoMarker",marking,null);
			prePlaceHolder.add(undoMarker); 
			postPlaceHolder.add(undoMarker);
			undoMarkers.add(undoMarker);
		}
		
		void undoToLastMarker() {
			while (true) {
				if (prePlaceHolder.size() > 0) {
					Node el = prePlaceHolder.remove(prePlaceHolder.size() - 1); // remove last element
					if (el instanceof Place && ((Place)el).getLabel().equals("UndoMarker"))
						break;
				}
				else break; // the loop will always terminate
			}
			while (true) {
				if (postPlaceHolder.size() > 0) {
					Node el = postPlaceHolder.remove(postPlaceHolder.size() - 1); // remove last element
					if (el instanceof Place && ((Place)el).getLabel().equals("UndoMarker")) {
						this.marking = ((Place)el).getMarking();
						undoMarkers.remove(undoMarkers.size()-1); // remove last undoMarker
						break;
					}
				}
				else break; // the loop will always terminate
			}
		}
		
		// call after the search algorithm to prepare the insertion of the placeholder
		void removeAllUndoMarkers() {
			prePlaceHolder.removeAll(undoMarkers);
			postPlaceHolder.removeAll(undoMarkers);
		}
	}
}
