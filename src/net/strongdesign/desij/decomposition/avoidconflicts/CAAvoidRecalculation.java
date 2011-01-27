package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.io.IOException;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJ;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.Partition;
import net.strongdesign.util.Pair;


public class CAAvoidRecalculation extends ComponentAnalyser {
		
	public CAAvoidRecalculation(STG stg, Collection<STG> components, String filePrefix) {
		super(stg, components, filePrefix);
	}
	
			
	@Override
	public boolean avoidIrrCSCConflicts() throws IOException, STGException {
		boolean withoutErrors = true;
		int tTransInsertionCount = 0;
		
		if (CLW.instance.CONFLICT_TYPE.getValue().endsWith("st"))
			for (List<Transition> selfTrigger: selfTriggers.keySet()) {
				for (STG critComp: selfTriggers.get(selfTrigger)) {
					initializeForwardTraversal(critComp, selfTrigger);
					TraversalResult tTransInsertionInfo = doForwardTraversal(selfTrigger); 
					if (tTransInsertionInfo == null) {
						withoutErrors = false;
						continue;
					}
					tTransInsertionInfo.criticalComponent = critComp; 
					if (!doBackwardTraversal(tTransInsertionInfo)) {
						// now placeInsertionInfo is completely filled
						// except markedPlacesDuringBackward and t1
						doPlaceHolderInsertion(tTransInsertionInfo, tTransInsertionCount++);
						// i.e. also the redundant place
						STGUtil.removeRedundantPlaces(stg);
						
						// recalculate the delay component
						boolean recoveryInfo = CLW.instance.ALLOW_INCOMPLETE_PARTITION.isEnabled();
						
						if (!recoveryInfo) {
							CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(true);
						}
						
						Partition incompPart = new Partition();
						incompPart.beginSignalSet();
						for ( String outSignal: tTransInsertionInfo.delayComponent.getSignalNames(
								tTransInsertionInfo.delayComponent.getSignals(Signature.OUTPUT)) )
							incompPart.addSignal(outSignal);
						for ( String intSignal: tTransInsertionInfo.delayComponent.getSignalNames(
								tTransInsertionInfo.delayComponent.getSignals(Signature.INTERNAL)) )
							incompPart.addSignal(intSignal);
						Collection<STG> delayComponent = new BasicDecomposition(filePrefix).decompose(stg, incompPart);
						if (delayComponent.size() == 1) {
							components.remove(tTransInsertionInfo.delayComponent);
							tTransInsertionInfo.delayComponent = (STG)delayComponent.toArray()[0];
							components.addAll(delayComponent);
						}
						
						// recovery of CLW parameter
						if (!recoveryInfo) {
							CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(false);
						}
						continue;					
					} 
					// now placeInsertionInfo is completely filled
					doPlaceHolderInsertion(tTransInsertionInfo, tTransInsertionCount++);
					// i.e. also the redundant place
					STGUtil.removeRedundantPlaces(stg);
				}
			}
		else if (CLW.instance.CONFLICT_TYPE.getValue().equals("general"))
			for (Set<Pair<Transition,Transition>> irrCSCConflict: this.entryExitPairs2Components.keySet()) {
				for (STG critComp: this.entryExitPairs2Components.get(irrCSCConflict)) {
					TraversalResult tTransInsertionInfo = null;
					for (Pair<Transition,Transition> entryExit: irrCSCConflict) {
						List<Transition> ctp = new ArrayList<Transition>(2);
						ctp.add(entryExit.a);
						ctp.add(entryExit.b);
						initializeForwardTraversal(critComp, ctp);
						tTransInsertionInfo = doForwardTraversal(ctp);
						if (tTransInsertionInfo != null) break;
					} 
					
					// from here the same as in the "st" branch
					
					if (tTransInsertionInfo == null) {
						withoutErrors = false;
						continue;
					}
					tTransInsertionInfo.criticalComponent = critComp; 
					if (!doBackwardTraversal(tTransInsertionInfo)) {
						// now placeInsertionInfo is completely filled
						// except markedPlacesDuringBackward and t1
						doPlaceHolderInsertion(tTransInsertionInfo, tTransInsertionCount++);
						// i.e. also the redundant place
						STGUtil.removeRedundantPlaces(stg);
						
						// recalculate the delay component
						boolean recoveryInfo = CLW.instance.ALLOW_INCOMPLETE_PARTITION.isEnabled();
						
						if (!recoveryInfo) {
							CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(true);
						}
						
						Partition incompPart = new Partition();
						incompPart.beginSignalSet();
						for ( String outSignal: tTransInsertionInfo.delayComponent.getSignalNames(
								tTransInsertionInfo.delayComponent.getSignals(Signature.OUTPUT)) )
							incompPart.addSignal(outSignal);
						for ( String intSignal: tTransInsertionInfo.delayComponent.getSignalNames(
								tTransInsertionInfo.delayComponent.getSignals(Signature.INTERNAL)) )
							incompPart.addSignal(intSignal);
						Collection<STG> delayComponent = new BasicDecomposition(filePrefix).decompose(stg, incompPart);
						if (delayComponent.size() == 1) {
							components.remove(tTransInsertionInfo.delayComponent);
							tTransInsertionInfo.delayComponent = (STG)delayComponent.toArray()[0];
							components.addAll(delayComponent);
						}
						
						// recovery of CLW parameter
						if (!recoveryInfo) {
							CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(false);
						}
						continue;					
					} 
					// now placeInsertionInfo is completely filled
					doPlaceHolderInsertion(tTransInsertionInfo, tTransInsertionCount++);
					// i.e. also the redundant place
					STGUtil.removeRedundantPlaces(stg);
				}
			}
		else
			return false; // wrong CONFLICT_TYPE parameter
		
		for (STG comp: this.components) {
			//	i.e. all the MG-self-trigger places
			STGUtil.removeRedundantPlaces(comp);
		}
		
		this.newInsertedSignals += tTransInsertionCount;
		DesiJ.logFile.info("Added signals on account of introducing internal communication: " + this.newInsertedSignals);
				
		return withoutErrors;
	}
	
	private TraversalResult doForwardTraversal(List<Transition> selfTrigger) {
		Transition t_entry = selfTrigger.get(0);
		TraversalResult result = null;
		
		/* the old technique
		for (Node place: t_entry.getChildren()) {
			result = forward_p((Place)place, null, 0, 0, true);
			if (result != null) {
				result.t2 = t_entry;
				break;
			}
		}
		*/
		
		if (t_entry.getChildren().size() > 1)
			return null;
		else
			result = forward_p((Place)t_entry.getChildren().toArray()[0], null, 0, 0, true);
		
		if (result != null)
			result.t2 = t_entry;
		
		return result;
	}
	
	private void initializeForwardTraversal(STG component, List<Transition> ctp) {
		NodeProperty nodeInfo;
		
		for (Node node: additionalNodeInfos.keySet()) {
			nodeInfo = additionalNodeInfos.get(node);
			nodeInfo.isVisited = false;
			if (node instanceof Transition) {
				nodeInfo.isEntryTransition = false;
				nodeInfo.isExitTransition = false;
				if ((Transition)node == ctp.get(0))
					nodeInfo.isEntryTransition = true;
				if ((Transition)node == ctp.get(1))
					nodeInfo.isExitTransition = true;
				if (component.getTransition(node.getIdentifier()) != null)
					nodeInfo.isRelevant = true;
				else
					nodeInfo.isRelevant = false;
			}
		}
	}
	
	private TraversalResult forward_p(Place place, Transition outTrans, 
			int marked1stSec, int marked2ndSec, boolean isIn1stSec) {
		if (additionalNodeInfos.get(place).isVisited)
			return null; // catched in a cycle, should never be happen
		// is not marked graph
		if ((place.getParents().size() != 1) || (place.getChildren().size() != 1))
			return null;
		// STG is not live
		if (place.getChildren().isEmpty())
			return null;
		// there is only one successor transition --> marked graph
		additionalNodeInfos.get(place).isVisited = true;
		if (isIn1stSec && (place.getMarking() > 0))
			return forward_t((Transition)place.getChildren().toArray()[0], outTrans, 
					marked1stSec += place.getMarking(),	marked2ndSec, isIn1stSec);
		if (!isIn1stSec && (place.getMarking() > 0))
			return forward_t((Transition)place.getChildren().toArray()[0], outTrans, marked1stSec, 
					marked2ndSec += place.getMarking(), isIn1stSec);
		return forward_t((Transition)place.getChildren().toArray()[0], outTrans, marked1stSec, 
				marked2ndSec, isIn1stSec);
	}
	
	private TraversalResult forward_t(Transition trans, Transition outTrans, 
			int marked1stSec, int marked2ndSec, boolean isIn1stSec) {
		if (additionalNodeInfos.get(trans).isVisited)
			return null; // catched in a cycle --> Actually, this is impossible 
		if (additionalNodeInfos.get(trans).isEntryTransition)
			return null; // catched in a cycle
		if (additionalNodeInfos.get(trans).isExitTransition && 
				outTrans != null) {
			return formResult(outTrans, trans, marked1stSec, marked2ndSec);			
		}
		// on path w all transitions except the exit transition must have only one outgoing arc
		// of course no outgoing arc is also not useful
		if (trans.getChildren().size() != 1)
			return null;
		
		additionalNodeInfos.get(trans).isVisited = true;
		if (isIn1stSec) {
			// all transitions in this section must have only one ingoing arc
			if (trans.getParents().size() != 1)
				return null;
			// take the first output or internal resp. on the path from entry to exit transition
			if ( stg.getSignature(trans.getLabel().getSignal()) != Signature.INPUT ) {
				if (additionalNodeInfos.get(trans).isExitTransition)
					return formResult(trans, trans, marked1stSec, marked2ndSec);
				// 2nd Section starts
				return forward_p((Place)trans.getChildren().toArray()[0], trans, marked1stSec, 0, false);
				
				/* the old technique
				TraversalResult result = null;
				for (Node place: trans.getChildren()) {
					result = forward_p((Place)place, trans, marked1stSec, 0, false);
					if (result != null) 
						break;
				}
				return result;
				*/
			}
		}
		return forward_p((Place)trans.getChildren().toArray()[0], outTrans, marked1stSec, marked2ndSec, isIn1stSec);
		
		/* the old technique
		TraversalResult result = null;
		for (Node place: trans.getChildren()) {
			result = forward_p((Place)place, outTrans, marked1stSec, marked2ndSec, isIn1stSec);
			if (result != null)
				break;
		}
		return result;
		*/
	}
	
	private TraversalResult formResult(Transition nonInputTransition, Transition exitTransition, 
			int marked1stSec, int marked2ndSec) {
		TraversalResult result = null;
		for (STG comp: this.components) {
			if (comp.getSignals(Signature.OUTPUT).contains(nonInputTransition.getLabel().getSignal())) {
				result = new TraversalResult();
				result.delayComponent = comp;
				result.t3 = nonInputTransition;
				result.t4 = exitTransition;
				result.markedPlacesDuringForwardIn1stSection = marked1stSec;
				result.markedPlacesDuringForwardIn2ndSection = marked2ndSec;
				break;
			}
		}
		return result;
	}
	
	private boolean doBackwardTraversal(TraversalResult traversalInfo) {
		if (traversalInfo == null)
			return false;
		
		Transition t_entry = traversalInfo.t2;
		if (t_entry.getParents().size() != 1) 
			return false; // only one incoming arc is allowed
		
		initializeBackwardTraversal(traversalInfo.delayComponent);
		// if t_entry is already labeled with a relevant signal of the delay component
		if (additionalNodeInfos.get(t_entry).isRelevant) {
			// trans = t1 and marked
			traversalInfo.t1 = t_entry;
			traversalInfo.markedPlacesDuringBackward = 0;
			return true;
		} 
		return backward_p((Place)t_entry.getParents().toArray()[0], 0, traversalInfo);
	}
	
	private void initializeBackwardTraversal(STG component) {
		NodeProperty nodeInfo;
		
		for (Node node: additionalNodeInfos.keySet()) {
			nodeInfo = additionalNodeInfos.get(node);
			//nodeInfo.isVisited = false;
			if (node instanceof Transition) {
				if (component.getTransition(node.getIdentifier()) != null)
					nodeInfo.isRelevant = true;
				else
					nodeInfo.isRelevant = false;
			}
		}
	}
	
	private boolean backward_p(Place place, int marking, TraversalResult traversalInfo) {
		if (additionalNodeInfos.get(place).isVisited)
			return false; // catched in a cycle, should never be happen
		// is not marked graph
		if ((place.getParents().size() != 1) || (place.getChildren().size() != 1))
			return false; 
		// STG is not live
		if (place.getParents().isEmpty())
			return false;
		// there is only one predecessor transition --> marked graph
		additionalNodeInfos.get(place).isVisited = true;
		return backward_t((Transition)place.getParents().toArray()[0], marking += place.getMarking(), traversalInfo);
	}
	
	private boolean backward_t(Transition trans, int marking, TraversalResult traversalInfo) {
		if (additionalNodeInfos.get(trans).isVisited)
			return false; // catched in a cycle
		if (additionalNodeInfos.get(trans).isEntryTransition)
			return false; // catched in a cycle --> should never be happen
		if (additionalNodeInfos.get(trans).isRelevant) {
			// trans = t1 and marked
			traversalInfo.t1 = trans;
			traversalInfo.markedPlacesDuringBackward = marking;
			return true;
		}
		if (trans.getParents().size() != 1 ) 
			return false; // only one synchronizer per transition is allowed
		additionalNodeInfos.get(trans).isVisited = true;
		return backward_p((Place)trans.getParents().toArray()[0], marking, traversalInfo);
	}
	
	/***
	 * a transition as placeholder will be introduced such that after its refinement the 
	 * considered irreducible CSC conflict is avoided
	 * @param traversalInfo
	 * @param tTransInsertionCount
	 */
	private void doPlaceHolderInsertion(TraversalResult traversalInfo, int tTransInsertionCount) {
		STG critComp = traversalInfo.criticalComponent;
		STG delayComp = traversalInfo.delayComponent;
		
		String pULabel = "pU" + tTransInsertionCount;
		String pDLabel = "pD" + tTransInsertionCount;
		
		// generate newLabel
		int idForNewSignal = stg.getHighestSignalNumber() + 1;
		if (critComp.getHighestSignalNumber() >= idForNewSignal)
			idForNewSignal = critComp.getHighestSignalNumber() + 1;
		if (delayComp.getHighestSignalNumber() >= idForNewSignal)
			idForNewSignal = delayComp.getHighestSignalNumber() + 1;
		// an unknown edge direction, since the inserted transition will act only as a placeholder
		SignalEdge newLabel = new SignalEdge(idForNewSignal, EdgeDirection.UNKNOWN);
		
		
		// naive implementation --> priority queue would be better,	but the only important thing is  
		// that the element with the highest maxNodeNumber is at the first position in stgList
		List<STG> stgList = new ArrayList<STG>(3);
		stgList.add(0,stg);
		if (critComp.getMaxNodeNumber() > stgList.get(0).getMaxNodeNumber())
			stgList.add(0, critComp); // addFirst
		else
			stgList.add(critComp); // append
		if (delayComp.getMaxNodeNumber() > stgList.get(0).getMaxNodeNumber())
			stgList.add(0, delayComp); // addFirst
		else
			stgList.add(delayComp); // append
		
		Transition t_ic = null;
		for (int i = 0; i < 3; i++) {
			STG currentElement = stgList.get(i);
			
			if (currentElement == stg) {
				Place pU = stg.addPlace(pULabel, traversalInfo.markedPlacesDuringForwardIn1stSection);
				Place pD = stg.addPlace(pDLabel, 0);
				
				try {
					stg.setSignalName(newLabel.getSignal(), "ic" + tTransInsertionCount);
				}
				catch (STGException e) {e.printStackTrace();}
				stg.setSignature(newLabel.getSignal(), Signature.INTERNAL);
				
				if (i == 0 && t_ic == null)
					t_ic = stg.addTransition(newLabel);
				else
					t_ic = stg.addTransition(newLabel, t_ic.getIdentifier());
				
				pU.setParentValue(traversalInfo.t2, 1);
				pU.setChildValue(t_ic, 1);
				pD.setParentValue(t_ic, 1);
				pD.setChildValue(traversalInfo.t3, 1);
				
				// update additionalNodeInfos
				additionalNodeInfos.put(pU, new NodeProperty());
				additionalNodeInfos.put(pD, new NodeProperty());
				additionalNodeInfos.put(t_ic, new NodeProperty());
			}
			if (currentElement == critComp) {
				Place pU = critComp.addPlace(pULabel, traversalInfo.markedPlacesDuringForwardIn1stSection);
				// mark pD with token count of the path from the entry to the delay transition
				Place pD = critComp.addPlace(pDLabel, traversalInfo.markedPlacesDuringForwardIn2ndSection);
						 
				try {
					critComp.setSignalName(newLabel.getSignal(), "ic" + tTransInsertionCount);
				}
				catch (STGException e) {e.printStackTrace();}
				critComp.setSignature(newLabel.getSignal(), Signature.OUTPUT);
				
				if (i == 0 && t_ic == null)
					t_ic = critComp.addTransition(newLabel);
				else
					t_ic = critComp.addTransition(newLabel, t_ic.getIdentifier());
				
				pU.setParentValue(critComp.getTransition(traversalInfo.t2.getIdentifier()), 1);
				pU.setChildValue(t_ic, 1);
				pD.setParentValue(t_ic, 1);
				pD.setChildValue(critComp.getTransition(traversalInfo.t4.getIdentifier()), 1);
			}
			// only if the backward traversal was successful
			if ((currentElement == delayComp) && (traversalInfo.t1 != null)) {
				Place pU = delayComp.addPlace(pULabel, traversalInfo.markedPlacesDuringBackward + 
						traversalInfo.markedPlacesDuringForwardIn1stSection);
				
				Place pD = delayComp.addPlace(pDLabel, 0);
			
				try {
					delayComp.setSignalName(newLabel.getSignal(), "ic" + tTransInsertionCount);
				}
				catch (STGException e) {e.printStackTrace();}
				delayComp.setSignature(newLabel.getSignal(), Signature.INPUT);
				
				if (i == 0 && t_ic == null)
					t_ic = delayComp.addTransition(newLabel);
				else
					t_ic = delayComp.addTransition(newLabel, t_ic.getIdentifier());
				
				pU.setParentValue(delayComp.getTransition(traversalInfo.t1.getIdentifier()), 1);
				pU.setChildValue(t_ic, 1);
				pD.setParentValue(t_ic, 1);
				pD.setChildValue(delayComp.getTransition(traversalInfo.t3.getIdentifier()), 1);
			}
		}
	}
	
	// use like a struct in C
	private class TraversalResult {
		STG criticalComponent;
		STG delayComponent;
		Transition t1;	// relevant input transition of the delay component
		Transition t2;	// t_entry
		Transition t3;	// delaying output (only one is possible)
		Transition t4;	// t_exit
		int markedPlacesDuringForwardIn1stSection;	// w2
		int markedPlacesDuringForwardIn2ndSection;	// w3
		int markedPlacesDuringBackward;				// w1
		
		TraversalResult() {
			super();
		}
	}
}
