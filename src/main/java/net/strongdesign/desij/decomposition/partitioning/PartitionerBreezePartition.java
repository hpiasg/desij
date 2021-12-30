package net.strongdesign.desij.decomposition.partitioning;



import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.solvers.CSCSolver;
import net.strongdesign.stg.traversal.ConditionFactory;

public class PartitionerBreezePartition implements IPartitioningStrategy {

	private STG stg;
	HashSet<Integer> solvedCSC = new HashSet<Integer>();
	
	public PartitionerBreezePartition(STG stg) throws STGException {
		this.stg = stg;
		if (this.stg == null) 
			throw new STGException("No specification is given!");
	}
	
	
	
	boolean gatherParents(STG stg, Collection<Transition> newTransitions) {
		
		LinkedList<Transition> transitions = new LinkedList<Transition>();
		transitions.addAll(newTransitions);
		
		boolean ret = false;
		
		for (Transition t: transitions) {
			
			for (Node n: t.getParents()) {
				for (Node n2: n.getParents()) {
					
					// all outputs and dummies can also be added here
					Transition presetTran = (Transition)n2;
					
					if (newTransitions.contains(presetTran)) continue;
					
					ret = true;
					// add it to the list of transitions
					newTransitions.add(presetTran);
					
					Signature sig = stg.getSignature(presetTran.getLabel().getSignal());
					
				}
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Experimental "sandwich" component
	 * @param stg
	 * @param signal
	 * @return
	 * @throws STGException 
	 */
	public STG createSandwichComponent(Integer signal) throws STGException {
		
		STG stg = this.stg.clone();
		
		// 1. gather all signal transitions
		HashSet<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(stg.getTransitions(ConditionFactory.getSignalOfCondition(signal)));
		
		// 2. create a list of trigger transitions
		gatherParents(stg, transitions);
		gatherBySignatures(stg, transitions);
		
		// 3. mark unrelated transitions as dummy
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (transitions.contains(t)) {
				if (t.getLabel().getSignal().equals(signal)) {
					stg.setSignature(t.getLabel().getSignal(), Signature.OUTPUT);
				} else 
				{
					stg.setSignature(t.getLabel().getSignal(), Signature.INPUT);
				}
				continue;
			}
			stg.setSignature(t.getLabel().getSignal(), Signature.DUMMY);
		}
		
		
		// 4. split each of trigger transitions
		
		for (Transition tran: transitions) {
			
			if (tran.getLabel().getSignal().equals(signal)) continue;
			
			if (stg.getSignature(tran.getLabel().getSignal())==Signature.DUMMY||
				stg.getSignature(tran.getLabel().getSignal())==Signature.ANY) continue;
			
			// create a transition
			String newName = "t_"+stg.getSignalName(tran.getLabel().getSignal());
			Integer sigNum = stg.getSignalNumber(newName);
			stg.setSignature(sigNum, Signature.OUTPUT);
			
			SignalEdge se = new SignalEdge(sigNum, tran.getLabel().getDirection());
			Transition newTran = stg.addTransition(se);
			
			// copy postset form tran to "t_..."
			for (Node p: tran.getChildren()) {
				newTran.setChildValue(p, tran.getChildValue(p));
			}
			for (Node p: newTran.getChildren()) {
				tran.setChildValue(p, 0);
			}
			
			Place newPlace = stg.addPlace("p", 0);
			
			newPlace.setParentValue(tran, 1);
			newPlace.setChildValue(newTran, 1);
			
		}
		
		// 5. breeze contract
		STGUtil.removeDummiesBreeze(stg, true, false);	
		
		return stg;
	}
	
	
	
	public STG createLevel1Component(Integer signal) throws STGException {
		
		STG stg = this.stg.clone();
		
		// 1. gather all signal transitions
		HashSet<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(stg.getTransitions(ConditionFactory.getSignalOfCondition(signal)));
		
		// 2. get two levels of parents, they all will become inputs
		gatherParents(stg, transitions);
		gatherBySignatures(stg, transitions);
		
		
		// 3. mark unrelated transitions as dummy
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (transitions.contains(t)) {
				if (t.getLabel().getSignal().equals(signal)) {
					stg.setSignature(t.getLabel().getSignal(), Signature.OUTPUT);
				} else 
				{
					stg.setSignature(t.getLabel().getSignal(), Signature.INPUT);
				}
				continue;
			}
			stg.setSignature(t.getLabel().getSignal(), Signature.DUMMY);
		}
		
		// breeze contract
		STGUtil.removeDummiesBreeze(stg, true, false);	
		
		return stg;
	}
	
	/**
	 * Component of two levels of triggers
	 * @param stg
	 * @param signal
	 * @return
	 * @throws STGException 
	 */
	public STG createLevel2Component(Integer signal) throws STGException {
		
		STG stg = this.stg.clone();
		
		// 1. gather all signal transitions
		HashSet<Transition> transitions = new HashSet<Transition>();
		transitions.addAll(stg.getTransitions(ConditionFactory.getSignalOfCondition(signal)));
		
		// 2. get two levels of parents, they all will become inputs
		gatherParents(stg, transitions);
		gatherParents(stg, transitions);
		gatherBySignatures(stg, transitions);
		
		
		// 3. mark unrelated transitions as dummy
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (transitions.contains(t)) {
				if (t.getLabel().getSignal().equals(signal)) {
					stg.setSignature(t.getLabel().getSignal(), Signature.OUTPUT);
				} else 
				{
					stg.setSignature(t.getLabel().getSignal(), Signature.INPUT);
				}
				continue;
			}
			stg.setSignature(t.getLabel().getSignal(), Signature.DUMMY);
		}
		
		// breeze contract
		STGUtil.removeDummiesBreeze(stg, true, false);	
		
		return stg;
	}
	
	
	
	boolean gatherBySignatures(STG stg, Collection<Transition> newTransitions) {
		if (stg==null) stg=this.stg;
		
		LinkedList<Transition> transitions = new LinkedList<Transition>();
		transitions.addAll(newTransitions);
		
		boolean ret = false;
		
		Collection<Integer> signals = new HashSet<Integer>();
		
		// for each transition add all transitions of the same signature 
		for (Transition t: transitions) {
			if (stg.getSignature(t.getLabel().getSignal())==Signature.DUMMY) continue;
			
			signals.add(t.getLabel().getSignal());
		}
		
		
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			int signal = t.getLabel().getSignal();
			
			if (signals.contains(signal)) {
				if (!newTransitions.contains(t)) {
					ret = true;
					newTransitions.add(t);
				}
			}
		}
		
		return ret;
	}
	
	boolean gatherTriggers(Collection<Transition> newTransitions) {
		
		LinkedList<Transition> transitions = new LinkedList<Transition>();
		transitions.addAll(newTransitions);
		
		boolean ret = false;
		
		
		for (Transition t: transitions) {
			
			String sname = stg.getSignalName(t.getLabel().getSignal());
			
			// 1. try to add all direct trigger transitions, do not add presets for transitions of the signals with CSC
			if (solvedCSC.contains(t.getLabel().getSignal())) continue;
			if (level1.contains(sname)) continue;
			if (level2.contains(sname)) continue;
			
			HashSet<String> restrictedEdges = null;
			
			if (stg.getSignature(t.getLabel().getSignal())==Signature.OUTPUT||
				stg.getSignature(t.getLabel().getSignal())==Signature.INTERNAL)
					restrictedEdges = restrictions.get(stg.getSignalName(t.getLabel().getSignal()));
			
			
			for (Node n: t.getParents()) {
				for (Node n2: n.getParents()) {
					// all outputs and dummies can also be added here
					Transition presetTran = (Transition)n2;
					
					if (newTransitions.contains(presetTran)) continue;
					
					// only add the preset transition, if it is among the restricted edges
					if (restrictedEdges!=null&& !restrictedEdges.contains(presetTran.getString(0))) 
						continue;
					
					ret = true;
					// add it to the list of transitions
					newTransitions.add(presetTran);
					
					Signature sig = stg.getSignature(presetTran.getLabel().getSignal());
					
					// for outputs and internals also add by signature
					if ((sig==Signature.OUTPUT||sig==Signature.INTERNAL)) {
						
						HashSet<Transition> sameSignature = new HashSet<Transition>();
						sameSignature.add(presetTran);
						gatherBySignatures(null, sameSignature);
						newTransitions.addAll(sameSignature);
						
					}
					
				}
			}
		}
		
		return ret;
	}
	
//	boolean gatherTriggered(Set<Transition> newTransitions) {
//		
//		LinkedList<Transition> transitions = new LinkedList<Transition>();
//		transitions.addAll(newTransitions);
//		
//		boolean ret = false;
//		
//		
//		for (Transition t: transitions) {
//			
//			for (Node n: t.getChildren()) {
//				for (Node n2: n.getChildren()) {
//					// all outputs and dummies can also be added here
//					
//					Transition postsetTran = (Transition)n2;
//					
//					if (newTransitions.contains(postsetTran)) continue;
//					
//					if (solvedCSC.contains(postsetTran.getLabel().getSignal())) continue;
//					
//					String sname = postsetTran.getSTG().getSignalName(postsetTran.getLabel().getSignal());
//					
//					if (level1.contains(sname)) continue;
//					if (level2.contains(sname)) continue;
//					
//					HashSet<String> restrictedEdges = null;
//					
//					if (stg.getSignature(postsetTran.getLabel().getSignal())==Signature.OUTPUT||
//						stg.getSignature(postsetTran.getLabel().getSignal())==Signature.INTERNAL)
//							restrictedEdges = restrictions.get(stg.getSignalName(postsetTran.getLabel().getSignal()));
//					
//					// only add the postset transition, if it has t as a restricted edge
//					if (restrictedEdges!=null&& !restrictedEdges.contains(t.getString(0))) continue; 
//					
//					// add it to the list of transitions
//					ret = true;
//					newTransitions.add(postsetTran);
//					
//					Signature sig = stg.getSignature(postsetTran.getLabel().getSignal());
//					
//					// 2. if the postset transition is an output transition, add by signature
//					if ((sig==Signature.OUTPUT||sig==Signature.INTERNAL)) {
//						
//						HashSet<Transition> sameSignature = new HashSet<Transition>();
//						sameSignature.add(postsetTran);
//						gatherBySignatures(null, sameSignature);
//						newTransitions.addAll(sameSignature);
//					}
//					
//				}
//			}
//		}
//		
//		return ret;
//	}
//	
	
	/**
	 * Identifies problematic triggers and reports them
	 * @throws InterruptedException 
	 * @throws IOException
	 */
	
	// need to save it as strings because the results are accumulated from saving into .g files, and signal IDs may be changed in the process
	
	HashSet<String> level1 = new HashSet<String>();
	HashSet<String> level2 = new HashSet<String>();
	HashMap<String, HashSet<String>> restrictions = new HashMap<String, HashSet<String>>();
	
	HashSet<String> unsolved = new HashSet<String>();
	
	public void gatherSignalInfo() throws STGException {
		
		
		// first, use common signal partition to find all easy signals
//		Partition partition = Partition.getCommonCausePartition(stg);
		
		// all signals consisting of just one signal have CSC
//		for (List<String> part: partition.getPartition()) {
//			if (part.size()!=1) continue;
//			solvedCSC.add(stg.getSignalNumber(part.get(0)));
//		}
		
		HashSet<Integer> processed = new HashSet<Integer>();
		level1.clear();
		level2.clear();
		restrictions.clear();
		unsolved.clear();
		
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (solvedCSC.contains(t.getLabel().getSignal())) continue;
			
			if (processed.contains(t.getLabel().getSignal())) continue;
			
			if (stg.getSignature(t.getLabel().getSignal())==Signature.OUTPUT||
				stg.getSignature(t.getLabel().getSignal())==Signature.INTERNAL) {
				
				System.out.println("Checking signal " + stg.getSignalName(t.getLabel().getSignal())+":");
				
				STG solvedSTG = null;
				
				try {
					STG sw = createLevel1Component(t.getLabel().getSignal());
					System.out.println("Attempting level 1...");
					solvedSTG = CSCSolver.solveCSCWithMpsat(sw);
				} catch(Exception e) {
					
				}
				
				if (solvedSTG!=null) {
					System.out.println("Success!");
					level1.add(stg.getSignalName(t.getLabel().getSignal()));
					processed.add(t.getLabel().getSignal());
					continue;
				}
				System.out.println("Failed...");
				
				try {
					STG sw = createLevel2Component(t.getLabel().getSignal());
					System.out.println("Attempting level 2...");
					solvedSTG = CSCSolver.solveCSCWithMpsat(sw);
				} catch(Exception e) {
					
				}
				
				if (solvedSTG!=null) {
					System.out.println("Success!");
					level2.add(stg.getSignalName(t.getLabel().getSignal()));
					processed.add(t.getLabel().getSignal());
					continue;
				}
				
				System.out.println("Failed...");
				try {
					STG sw = createSandwichComponent(t.getLabel().getSignal());
					System.out.println("Attempting to solve sw component...");
					solvedSTG = CSCSolver.solveCSCWithMpsat(sw);
				} catch(Exception e) {
					
				}
				
				if (solvedSTG!=null) {
					System.out.println("Success!");
					HashSet<Transition> problematicTriggers = new HashSet<Transition>();
					HashSet<String> edges = new HashSet<String>(); 
					// check which signals were restricted
					for (Transition tran: solvedSTG.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
						
						if (solvedSTG.getSignature(tran.getLabel().getSignal())==Signature.INPUT) {
							
							// is restricted by some concurrent transition?
							Transition postTran = (Transition)tran.getChildren().iterator().next().getChildren().iterator().next();
							boolean isRestricted = postTran.getParents().size()>1; 
							// is restricted by some sequential transition?
							isRestricted|= solvedSTG.getSignature(postTran.getLabel().getSignal())==Signature.INTERNAL;
							
							if (isRestricted) {
								
								// this syntactic trigger is problematic
								problematicTriggers.add(tran);
								edges.add(tran.getString(0));
							}
						}
					}
					
					HashSet<String> newEdges = restrictions.get(t.getString(0));
					if (newEdges==null) newEdges = edges;
					newEdges.addAll(edges);
					restrictions.put(stg.getSignalName(t.getLabel().getSignal()), newEdges);
					
					processed.add(t.getLabel().getSignal());
					continue;
				}
				
				System.out.println("Failed...");
				unsolved.add(stg.getSignalName(t.getLabel().getSignal()));
				processed.add(t.getLabel().getSignal());
				continue;
				
			}
			
		}
		
		// report solved
		// present the total number of synthesis signals
		int outputs = 0;
		for (Integer sig: stg.getSignals()) {
			if (stg.getSignature(sig)==Signature.OUTPUT||
				stg.getSignature(sig)==Signature.INTERNAL)
				outputs++;
		}
		
		System.out.println("Total number of output signals: "+outputs);
		
		System.out.println("\n\nCommon cause found:"+solvedCSC.size());
		for (Integer sig: solvedCSC) {
			String signalName = stg.getSignalName(sig);
			System.out.println("\t"+signalName);
		}
		
		System.out.println("Solved at level 1:"+level1.size());
		for (String name: level1) {
			System.out.println("\t"+name);
		}
		
		System.out.println("Solved at level 2:"+level2.size());
		for (String name: level2) {
			System.out.println("\t"+name);
		}
		
		int swsolved = 0;
		System.out.println("Solved at sw:");
		for (Entry<String, HashSet<String>> en: restrictions.entrySet()) {
			if (!en.getValue().isEmpty()) continue;
			swsolved++;
			String signal = en.getKey();
			System.out.println("\t"+signal);
		}
		
		// report partially solved
		System.out.println("Partially solved at sw:"+ (restrictions.size()-swsolved));
		for (Entry<String, HashSet<String>> en: restrictions.entrySet()) {
			if (en.getValue().isEmpty()) continue;
			
			String signal = en.getKey();
			System.out.print("\t"+signal+":");
			
			for (String edge: en.getValue()) {
				System.out.print(" "+edge);
			}
			System.out.println("");
		}
		
		// report unsolved
		System.out.println("Unsolved:"+unsolved.size());
		for (String name: unsolved) {
			System.out.println("\t"+name);
		}
		
	}
	
	
	private void connect(HashMap<Integer, HashSet<Integer>> graph, Integer a, Integer b) {
		if (a==b) return;
		if (a==null) return;
		if (b==null) return;
		
		HashSet<Integer> setA = graph.get(a);
		HashSet<Integer> setB = graph.get(b);
		
		if (setA==null) setA = new HashSet<Integer>();
		if (setB==null) setB = new HashSet<Integer>();
		
		
		setA.add(b);
		setB.add(a);
		
		graph.put(a, setA);
		graph.put(b, setB);
	}
	
	private void collectConnected(HashMap<Integer, HashSet<Integer>> graph, Integer node, HashSet<Integer> collected) {
		if (collected.contains(node)) return;
		if (graph.get(node)==null) return;
		
		collected.add(node);
		for (Integer sig: graph.get(node)) {
			collectConnected(graph, sig, collected);
		}
	}
	
	
	@Override
	public Partition improvePartition(Partition oldPartition)
			throws STGException, PartitioningException {
		
		// try to accumulate restriction info on the rest of the signals
		// the result is stored in restrictions and unsolved
		gatherSignalInfo();
		
		
		HashMap<Integer, HashSet<Integer>> outputGraph = new HashMap<Integer, HashSet<Integer>>();
		
		HashSet<Transition> processed = new HashSet<Transition>();
		
		// now for each signal accumulate its trigger transitions, until they are met by some transition from the CSC list
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (processed.contains(t)) continue;
			
			// find a non-input transition with unknown CSC
			if (stg.getSignature(t.getLabel().getSignal())!=Signature.OUTPUT&&
				stg.getSignature(t.getLabel().getSignal())!=Signature.INTERNAL) continue;
			
			int snum = t.getLabel().getSignal();
			String sname = stg.getSignalName(snum);
			
			if (solvedCSC.contains(snum)) continue;
			if (level1.contains(sname)) continue;
			if (level2.contains(sname)) continue;
			
			// create a group
			HashSet<Transition> newGroup = new HashSet<Transition>(); 
			newGroup.add(t);
			gatherBySignatures(stg, newGroup);
			
			
			boolean repeat = true;
			// add its parents
			HashSet<Transition> testOld = new HashSet<Transition>();
			HashSet<Transition> testNew = new HashSet<Transition>();
			while (repeat){
				repeat=false;
				
				testNew.clear();
				testOld.clear();
				testOld.addAll(newGroup);
				
				repeat|=gatherTriggers(newGroup);
				testNew.addAll(newGroup);
				testNew.removeAll(testOld);
				
				System.out.println("Adding triggers: "+testNew);

			}
			
			Integer firstSig = null;
			
			// filter out dummies and inputs
			// form bidirectional graph of related signals
			for (Transition tt: newGroup) {
				if (stg.getSignature(tt.getLabel().getSignal())!=Signature.OUTPUT&&
					stg.getSignature(tt.getLabel().getSignal())!=Signature.INTERNAL) continue;
				
				processed.add(tt);
				
				if (firstSig==null) firstSig = tt.getLabel().getSignal();
				
				connect(outputGraph, firstSig, tt.getLabel().getSignal());
			}
		}
		
		// create partitions
		Partition newPartition = new Partition();
		
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			Integer signal = t.getLabel().getSignal();
			String sname = stg.getSignalName(signal);
			
			if (newPartition.getSignals().contains(sname)) continue;
			
			HashSet<Integer> connected = new HashSet<Integer>();
			collectConnected(outputGraph, signal, connected);
			
			if (!connected.isEmpty()) {
				newPartition.beginSignalSet();
				for (Integer sig: connected) {
					newPartition.addSignal(stg.getSignalName(sig));
					
					System.out.print(" "+stg.getSignalName(sig));
				}
				System.out.println();
			}
		}
		
		// add the remaining primitive partitions, which were not used
		for (Integer sig: solvedCSC) {
			String sigName = stg.getSignalName(sig);
			if (!newPartition.getSignals().contains(sigName)) {
				newPartition.beginSignalSet();
				newPartition.addSignal(sigName);
			}
				
		}
		
		// add the remaining level 1 signals
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			String sname = stg.getSignalName(t.getLabel().getSignal());
			if (!level1.contains(sname)) continue;
			
			if (!newPartition.getSignals().contains(sname)) {
				newPartition.beginSignalSet();
				newPartition.addSignal(sname);
			}
		}
		
		return newPartition;
		
	}
}
