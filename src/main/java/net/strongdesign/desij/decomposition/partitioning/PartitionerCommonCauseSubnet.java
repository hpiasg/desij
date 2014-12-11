package net.strongdesign.desij.decomposition.partitioning;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.SignalValue;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

public class PartitionerCommonCauseSubnet implements IPartitioningStrategy {
	
	private STG stg;
	
	public PartitionerCommonCauseSubnet(STG stg) throws STGException {
		this.stg = stg;
		if (this.stg == null) 
			throw new STGException("No specification is given!");
	}
	
	
	boolean tryFire(Transition transition, HashMap<Place, Integer> tokens) {
		
		// first check if there are enough tokens
		for (Node n: transition.getParents()) {
			Place p = (Place)n;
			
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			
			int parentValue = transition.getParentValue(n);
			if (tok<parentValue) return false;
		}
		
		// remove old tokens
		for (Node n: transition.getParents()) {
			Place p = (Place)n;
			int parentValue = transition.getParentValue(n);
			
			int tok = tokens.get(p);
			
			if (tok-parentValue!=0)
				tokens.put(p, tok-parentValue);
			else
				tokens.remove(p);
		}
		
		// add new tokens
		for (Node n: transition.getChildren()) {
			Place p = (Place)n;
			int childValue = transition.getChildValue(n);
			
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			
			tokens.put(p, tok+childValue);
		}
		
		return true;
	}
	
	
	
	boolean tryUnfire(Transition transition, HashMap<Place, Integer> tokens) {
		// first check if there are enough tokens
		for (Node n: transition.getChildren()) {
			Place p = (Place)n;
			
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			
			int childValue = transition.getChildValue(n);
			if (tok<childValue) return false;
		}
		
		// remove old tokens
		for (Node n: transition.getChildren()) {
			Place p = (Place)n;
			int childValue = transition.getChildValue(n);
			
			int tok = tokens.get(p);
			
			if (tok-childValue!=0)
				tokens.put(p, tok-childValue);
			else
				tokens.remove(p);
		}
		
		// add new tokens
		for (Node n: transition.getParents()) {
			Place p = (Place)n;
			int parentValue = transition.getParentValue(n);
			
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			tokens.put(p, tok+parentValue);
		}
		
		return true;
	}
	
	
	/**
	 * Return the next sequence transition
	 */
	public Transition nextSequenceTransition(Transition initTransition, Integer signal, SignalValue value, HashSet<Place> visited) {
		
		boolean hasFired = false;
		
		HashSet<Transition> fired = new HashSet<Transition>();
		
		HashSet<Place> processedPlaces = new HashSet<Place>();
		
		// init tokens from a given initTransition
		HashMap<Place, Integer> tokens = new HashMap<Place, Integer>();
		
		// initiate tokens
		for (Node n: initTransition.getChildren()) {
			Place p = (Place)n;
			int childValue = initTransition.getChildValue(n);
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			
			tokens.put(p, tok+childValue);
		}
		
		fired.add(initTransition);
		
		HashMap<Place, Integer> ltok = new HashMap<Place, Integer>();
		
		do {
			hasFired = false;
			ltok.clear();
			ltok.putAll(tokens);
			processedPlaces.addAll(tokens.keySet());
			
			// try firing connected transitions
			for (Entry<Place, Integer> en: ltok.entrySet()) {
				
				Place p = en.getKey();
				
				if (p.getChildren().size()!=1) 
					return null; // places must be the marked graph places
				if (p.getParents().size()!=1) 
					return null;  // places must be the marked graph places
				
				Transition toFire = (Transition)p.getChildren().iterator().next();
				
				if (toFire.getChildren().isEmpty()) 
					return null; // do not allow transitions with an empty postset
				
				
				if (!tryFire(toFire, tokens)) continue;
				hasFired = true;
				
				
				if (fired.contains(toFire)) 
					return null; // if a transition has fired a second time, no sequence transition is to be returned
				
				
				fired.add(toFire);
				
				// now after firing we have a new set of places
				
				// 1. is it a sequence transition?
				int cnt = 0;
				for (Entry<Place, Integer> en2: tokens.entrySet()) cnt+=en2.getValue();
				for (Node n: toFire.getChildren()) cnt-=toFire.getChildValue(n);
				
				// if the fired transition was the only enabled transition, then it is a sequence transition
				boolean isSequence = cnt==0;
				
				if (isSequence) {
					
					if (value!=null&&visited!=null) {
						
						// assign known signal value to all processed places
						for (Place pp: processedPlaces) {
							
							HashMap<Integer, SignalValue> placeVal = signalValues.get(pp);
							
							if (placeVal==null) {
								placeVal = new HashMap<Integer, SignalValue>();
								signalValues.put(pp, placeVal);
							}
							
							SignalValue signalVal = placeVal.get(signal);
							
							placeVal.put(signal, value);
						}
						
						visited.addAll(processedPlaces);
					}
					
					return toFire;
				}
				
				// 2. if it is not a sequence transition, and it is related to signal, return null
				if (signal!=null && toFire.getLabel().getSignal().equals(signal))
					return null;
			}
		} while (hasFired);
		
		return null;
	}
	
	
	/**
	 * Return the previous sequence transition
	 */
	public Transition prevSequenceTransition(Transition initTransition, Integer signal, SignalValue value, HashSet<Place> visited) {
		
		boolean hasUnfired = true;
		
		HashSet<Transition> unfired = new HashSet<Transition>();
		
		HashSet<Place> processedPlaces = new HashSet<Place>();
		
		// init tokens from a given initTransition
		HashMap<Place, Integer> tokens = new HashMap<Place, Integer>();
		
		// initiate tokens
		for (Node n: initTransition.getParents()) {
			Place p = (Place)n;
			int parentValue = initTransition.getParentValue(n);
			Integer tok = tokens.get(p);
			if (tok==null) tok = 0;
			
			tokens.put(p, tok+parentValue);
		}
		
		unfired.add(initTransition);
		
		HashMap<Place, Integer> ltok = new HashMap<Place, Integer>();
		
		while (hasUnfired) {
			hasUnfired = false;
			
			ltok.clear();
			ltok.putAll(tokens);
			processedPlaces.addAll(tokens.keySet());
			
			// try firing connected transitions
			for (Entry<Place, Integer> en: ltok.entrySet()) {
				
				Place p = en.getKey();
				
				if (p.getParents().size()!=1) return null;  // places must be the marked graph places
				if (p.getChildren().size()!=1) return null; // places must be the marked graph places
				
				Transition toUnfire = (Transition)p.getParents().iterator().next();
				
				if (toUnfire.getParents().isEmpty()) return null; // do not allow transitions with an empty postset
				
				if (!tryUnfire(toUnfire, tokens)) continue;
				hasUnfired = true;
				
				if (unfired.contains(toUnfire)) return null; // do not continue on second transition firing
				
				unfired.add(toUnfire);
				
				// now after firing we have a new set of places
				
				// 1. is it a sequence transition?
				int cnt = 0;
				for (Entry<Place, Integer> en2: tokens.entrySet()) cnt+=en2.getValue();
				for (Node n: toUnfire.getParents()) cnt-=toUnfire.getParentValue(n);
				
				// if the fired transition was the only enabled transition, then it is a sequence transition
				boolean isSequence = cnt==0;
				
				if (isSequence) {
					
					if (value!=null&&visited!=null) {
						
						// assign known signal value to all processed places
						for (Place pp: processedPlaces) {
							
							HashMap<Integer, SignalValue> placeVal = signalValues.get(pp);
							
							if (placeVal==null) {
								placeVal = new HashMap<Integer, SignalValue>();
								signalValues.put(pp, placeVal);
							}
							
							SignalValue signalVal = placeVal.get(signal);
							
							placeVal.put(signal, value);
						}
						
						visited.addAll(processedPlaces);
					}
					
					return toUnfire;
				}
				
				// 2. if it is not a sequence transition, and it is related to signal, return null
				if (signal!=null && toUnfire.getLabel().getSignal().equals(signal))
					return null;
			}
		}
		return null;
	}
	
	
	boolean comparePostsets(Transition t1, Transition t2) {
		
		HashSet<SignalEdge> ppost1 = new HashSet<SignalEdge>();
		HashSet<SignalEdge> ppost2 = new HashSet<SignalEdge>();
		
		for (Node n: t1.getChildren()) {
			for (Node n2: n.getChildren()) {
				ppost1.add(((Transition)n2).getLabel());
			}
		}
		
		for (Node n: t2.getChildren()) {
			for (Node n2: n.getChildren()) {
				ppost2.add(((Transition)n2).getLabel());
			}
		}
		
		if (ppost1.size() != ppost2.size()) return false;
		
		return ppost1.equals(ppost2); 
	}
	
	/*
	 * Return true, if post sets are equivalent (signal edge wise)
	 */
	boolean comparePostsets(Set<Transition> edges) {
		for (Transition t1: edges) {
			for (Transition t2: edges) {
				if (t1==t2) continue;
				
				if (!comparePostsets(t1,  t2)) return false;
			}
		}
		
		return true;
	}
	
	
	boolean isChoiceTransition(Transition t) {
		
		if (stg.getSignature(t.getLabel().getSignal())==Signature.DUMMY) return false;
		
		for (Node n: t.getParents()) {
			if (n.getChildren().size()>1) return true;
		}
		
		return false;
	}
	
	void reportSolution(Set<Transition> edges) throws Exception {
		
//		solution that finds closest choice signal
//		seems to be not reliable, but might just work...
		
		// for each transition, accumulate preset transitions until the choice transition is found
		
		for (Transition tran: edges) {
			HashSet<Transition> processed = new HashSet<Transition>();
			HashSet<Transition> oldPreset = new HashSet<Transition>();
			
			
			for (Node n: tran.getParents()) {
				for (Node n2: n.getParents()) {
					oldPreset.add((Transition)n2);
				}
			}
			
			boolean found = false;
			
			HashSet<Transition> newPreset = new HashSet<Transition>();
			
			do {
				for (Transition tt: oldPreset) {
					for (Node n: tt.getParents()) {
						for (Node n2: n.getParents()) {
							
							Transition t2 = (Transition)n2;
							
							if (processed.contains(t2)) continue;
							
							if (isChoiceTransition(t2)) {
								found=true;
								System.out.print("Solution for "+tran.getString(Transition.UNIQUE)+": "+stg.getSignalName(t2.getLabel().getSignal()));
								break;
							}
							
							newPreset.add((Transition)n2);
						}
						
						if (found) break;
					}
					if (found) break;
				}
				
				if (found) break;
				
				processed.addAll(oldPreset);
				
				oldPreset=newPreset;
				newPreset = new HashSet<Transition>();
				
			} while (!oldPreset.isEmpty());
			
			if (!found) {
				System.out.println(" solution not found for "+tran.getString(Transition.UNIQUE));
			} else 
				System.out.println("");
		}
// 		solution using gathered signal info (so far doesn't work very well)
//		for (Transition t1: edges) {
//			for (Transition t2: edges) {
//				if (t1==t2) continue;
//				
//				if (!comparePostsets(t1,  t2)) {
//					HashMap<Integer, SignalValue> t1val  = getTransitionSignalInfo(t1);
//					HashMap<Integer, SignalValue> t2val  = getTransitionSignalInfo(t2);
//					HashSet<Integer> common = new HashSet<Integer>();
//					
//					common.addAll(t1val.keySet());
//					common.retainAll(t2val.keySet());
//					//common.retainAll(stg.getSignals(Signature.INPUT));
//					
//					boolean found = false;
//					System.out.print("Conflict between "+ t1.getString(Node.UNIQUE)+" and "+t2.getString(Node.UNIQUE)+":");
//					// now find signals with different values
//					for (Integer dsig: common) {
//						if (!t1val.get(dsig).equals(t2val.get(dsig))) {
//							System.out.print(" "+stg.getSignalName(dsig));;
//							found=true;
//						}
//					}
//					
//					if (!found) System.out.print("unsolved!");
//					System.out.println();
//				}
//			}
//		}
		
	}
	
	Set<Transition> gatherUniqueEdgeTransitions() {
		Set<Transition> collector = new HashSet<Transition>();
		
		Map<SignalEdge, Integer> map = new HashMap<SignalEdge, Integer>();
		
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (stg.getSignature(t.getLabel().getSignal())==Signature.DUMMY) continue;
			
			Integer cur = map.get(t.getLabel());
			if (cur==null) cur=0;
			
			map.put(t.getLabel(), cur+1);
		}
		
		for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (stg.getSignature(t.getLabel().getSignal())==Signature.DUMMY) continue;
			
			if (map.get(t.getLabel()).equals(1)) {
				collector.add(t);
			}
		}
		
		return collector;
	}
	
	void findSolution(Set<Transition> edges, Set<Transition> unique, Map<Transition, Integer> collector) {
		
//		solution that finds closest choice signal
//		also check all the unique transitions on the way
		
		
		 
		
		// for each transition, accumulate preset transitions until the choice transition is found
		
		for (Transition tran: edges) {
			HashSet<Transition> processed = new HashSet<Transition>();
			HashSet<Transition> oldPreset = new HashSet<Transition>();
			
			// gather data about unique occurrences of a signal
			HashMap<Integer, Integer> processedSignals = new HashMap<Integer, Integer>();
			
			for (Node n: tran.getParents()) {
				for (Node n2: n.getParents()) {
					oldPreset.add((Transition)n2);
				}
			}
			
			boolean found = false;
			
			HashSet<Transition> newPreset = new HashSet<Transition>();
			
			do {
				for (Transition tt: oldPreset) {
					for (Node n: tt.getParents()) {
						for (Node n2: n.getParents()) {
							
							Transition t2 = (Transition)n2;
							
							if (processed.contains(t2)) continue;
							
							// gather unique signal occurrences
							if (unique.contains(t2)) {
								Integer cur = processedSignals.get(t2.getLabel().getSignal());
								if (cur==null) cur=0;
								processedSignals.put(t2.getLabel().getSignal(), cur+1);
							}
							
							// same signal is met
							if (t2.getLabel().getSignal().equals(tran.getLabel().getSignal())) {
								// same signal as tran is met, check the list of unique transition edges
								// can we find some unique signal that only occurs once?
								for (Entry<Integer, Integer> en: processedSignals.entrySet()) {
									if (en.getValue().equals(1)) {
										found=true;
										collector.put(tran, en.getKey());
										break;
									}
								}
								
								// if yes, this should be a solution
								if (found) break;
							}
								
							if (isChoiceTransition(t2)) {
								found=true;
								
								for (Entry<Integer, Integer> en: processedSignals.entrySet()) {
									if (en.getValue().equals(1)) {
										found=true;
										collector.put(tran, en.getKey());
										break;
									}
								}
								
								// if yes, this should be a solution
								if (found) break;
								
								// final attempt, if hasn't found among unique
								collector.put(tran, t2.getLabel().getSignal());
								break;
							}
							
							newPreset.add(t2);
						}
						
						if (found) break;
					}
					if (found) break;
				}
				
				if (found) break;
				
				processed.addAll(oldPreset);
				
				oldPreset=newPreset;
				newPreset = new HashSet<Transition>();
				
			} while (!oldPreset.isEmpty());
			
			if (!found) {
				collector.put(tran, null);
			}
		}
	}
	
	
	public Map<Transition, Integer> solveProblematicTriggers() {
		
		HashMap<Transition, Integer> ret = new HashMap<Transition, Integer>();
		Set<Transition> unique = gatherUniqueEdgeTransitions();
		
		for (Integer sig: stg.getSignals()) {
			// find an input signal
			if (stg.getSignature(sig)==Signature.DUMMY) continue;
			
			// collect sets of transitions for two edges
			HashSet<Transition> upEdges = new HashSet<Transition>();
			HashSet<Transition> downEdges = new HashSet<Transition>();
			// collect all edges
			for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				if (!t.getLabel().getSignal().equals(sig)) continue;
				if (t.getLabel().getDirection() == EdgeDirection.UP) upEdges.add(t);
				if (t.getLabel().getDirection() == EdgeDirection.DOWN) downEdges.add(t);
			}
			
			// compare postsets
			boolean ups = comparePostsets(upEdges);
			boolean downs = comparePostsets(downEdges);
			
			String sname = stg.getSignalName(sig);
			// report possible solution
			if (!ups) findSolution(upEdges, unique, ret);
			if (!downs) findSolution(downEdges, unique, ret);
		}
		
		return ret;
	}

	public void reportProblematicTriggers() throws Exception {
		
		Map<Transition, Integer> solution = solveProblematicTriggers();
		
		for (Entry<Transition, Integer> en: solution.entrySet()) {
			Transition tran = en.getKey();
			Integer sig = en.getValue();
			
			if (sig!=null) {
				System.out.println("Solution for "+tran.getString(Transition.UNIQUE)+": "+stg.getSignalName(sig));
			} else {
				System.out.println("Solution for "+tran.getString(Transition.UNIQUE)+": not found");
			}
		}
		
//		for (Integer sig: stg.getSignals()) {
//			// find an input signal
//			if (stg.getSignature(sig)==Signature.DUMMY) continue;
//			
//			// collect sets of transitions for two edges
//			HashSet<Transition> upEdges = new HashSet<Transition>();
//			HashSet<Transition> downEdges = new HashSet<Transition>();
//			// collect all edges
//			for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
//				if (!t.getLabel().getSignal().equals(sig)) continue;
//				if (t.getLabel().getDirection() == EdgeDirection.UP) upEdges.add(t);
//				if (t.getLabel().getDirection() == EdgeDirection.DOWN) downEdges.add(t);
//			}
//			
//			// compare postsets
//			boolean ups = comparePostsets(upEdges);
//			boolean downs = comparePostsets(downEdges);
//			String sname = stg.getSignalName(sig);
//			if (!ups)
//				System.out.println("Edge "+sname+"+ is problematic");
//			
//			if (!downs)
//				System.out.println("Edge "+sname+"- is problematic");
//			
//			if (!ups||!downs) {
//				
//				// report possible solution
//				if (!ups) reportSolution(upEdges);
//				if (!downs) reportSolution(downEdges);
//			}
//		}
	}
	
	/**
	 * 
	 */
	private HashMap<Place, HashMap<Integer, SignalValue>> signalValues = new HashMap<Place, HashMap<Integer, SignalValue>>(); 
	
	
	
	void propagateSignalInfoUp(Node fromNode, int signal, SignalValue value, HashSet<Place> visited, boolean isFirst) throws Exception {
		
		if (fromNode instanceof Transition) {
			
			Transition fromTransition = (Transition)fromNode;
			
			if (!isFirst&&fromTransition.getLabel().getSignal().equals(signal)) return;
			
			
			if (fromTransition.getParents().size()!=1) {
				// find next sequence transition
				Transition prev = prevSequenceTransition(fromTransition, signal, value, visited);
				if (prev==null) return;
				
				// if it didn't fail, continue from next
				propagateSignalInfoUp(prev, signal, value, visited, false);
			} else {
				
				for (Node n: fromTransition.getParents())
					propagateSignalInfoUp(n, signal, value, visited, false);
				
			}
		} else if (fromNode instanceof Place) {
			
			Place fromPlace = (Place)fromNode;
			
			if (fromPlace.getChildren().size()>1) {
				
				boolean failed = false;
				// if each of place preset transitions can guarantee the value, only then propagate
				for (Node preN: fromPlace.getChildren()) {
					Transition preTrans = (Transition)preN;
					
					if (preTrans.getParents().size()!=1) return;
					
//					if (preTrans.getLabel().getSignal().equals(signal)) {
//						if (preTrans.getLabel().getDirection() == EdgeDirection.UP && value == SignalValue.HIGH) continue;
//						if (preTrans.getLabel().getDirection() == EdgeDirection.DOWN && value == SignalValue.LOW) continue;
//						return;
//					}
					
					HashMap<Integer, SignalValue> signals = getTransitionSignalInfo(preTrans);
					SignalValue sigval = signals.get(signal);
					
					if (sigval==null || sigval != value) return; 
				}
				
			}
			
			HashMap<Integer, SignalValue> placeVal = signalValues.get(fromPlace);
			
			if (placeVal==null) {
				placeVal = new HashMap<Integer, SignalValue>();
				signalValues.put(fromPlace, placeVal);
			}
			
			SignalValue signalVal = placeVal.get(signal);
			
			if (signalVal!=null) {
				
				if (signalVal!=value) 
					value = SignalValue.UNKNOWN;
				else
					if (visited.contains(fromPlace)) return;
				
				visited.add(fromPlace);

			}
			
			placeVal.put(signal, value);
			
			for (Node n: fromPlace.getParents()) {
				propagateSignalInfoUp(n, signal, value, visited, false);
			}
			
		}
	}
	
	void propagateSignalInfoDown(Node fromNode, int signal, SignalValue value, HashSet<Place> visited, boolean isFirst) throws Exception {
		
		if (fromNode instanceof Transition) {
			
			Transition fromTransition = (Transition)fromNode;
			
			if (!isFirst&&fromTransition.getLabel().getSignal().equals(signal)) return;
			
			
			if (fromTransition.getChildren().size()!=1) {
				// find next sequence transition
				Transition next = nextSequenceTransition(fromTransition, signal, value, visited);
				if (next==null) return;
				
				// if it didn't fail, continue from next
				propagateSignalInfoDown(next, signal, value, visited, false);
			} else {
				
				for (Node n: fromTransition.getChildren())
					propagateSignalInfoDown(n, signal, value, visited, false);
				
			}

			
			
		} else if (fromNode instanceof Place) {
			
			Place fromPlace = (Place)fromNode;
			
			if (fromPlace.getParents().size()>1) {
				
				boolean failed = false;
				// if each of place preset transitions can guarantee the value, only then propagate
				for (Node preN: fromPlace.getParents()) {
					Transition preTrans = (Transition)preN;
					
					if (preTrans.getChildren().size()!=1) return;
					
					if (preTrans.getLabel().getSignal().equals(signal)) {
						if (preTrans.getLabel().getDirection() == EdgeDirection.UP && value == SignalValue.HIGH) continue;
						if (preTrans.getLabel().getDirection() == EdgeDirection.DOWN && value == SignalValue.LOW) continue;
						return;
					}
					
					HashMap<Integer, SignalValue> signals = getTransitionSignalInfo(preTrans);
					SignalValue sigval = signals.get(signal);
					
					if (sigval==null || sigval != value) return; 
				}
				
			}
			
			HashMap<Integer, SignalValue> placeVal = signalValues.get(fromPlace);
			
			if (placeVal==null) {
				placeVal = new HashMap<Integer, SignalValue>();
				signalValues.put(fromPlace, placeVal);
			}
			
			SignalValue signalVal = placeVal.get(signal);
			
			if (signalVal!=null) {
				
				if (signalVal!=value) 
					value = SignalValue.UNKNOWN;
				else
					if (visited.contains(fromPlace)) return;
				
				visited.add(fromPlace);
			}
			
			placeVal.put(signal, value);
			
			for (Node n: fromPlace.getChildren()) {
				propagateSignalInfoDown(n, signal, value, visited, false);
			}
		}
	}
	
	public HashMap<Place, HashMap<Integer, SignalValue>> gatherSignalInfo(STG stg) {
		
		signalValues = new HashMap<Place, HashMap<Integer, SignalValue>>();
		
		try {
			
			for (Transition t: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				
				if (stg.getSignature(t.getLabel().getSignal())!=Signature.INPUT&&
					stg.getSignature(t.getLabel().getSignal())!=Signature.OUTPUT&&
					stg.getSignature(t.getLabel().getSignal())!=Signature.INTERNAL) continue;
				
				SignalValue upVal = SignalValue.LOW;
				SignalValue downVal = SignalValue.HIGH;
				
				if (t.getLabel().getDirection() == EdgeDirection.DOWN) {
					upVal = SignalValue.HIGH;
					downVal = SignalValue.LOW;
				}
				
				String s = stg.getSignalName(t.getLabel().getSignal());
				
				propagateSignalInfoDown(t, t.getLabel().getSignal(), downVal, new HashSet<Place>(), true);
				propagateSignalInfoUp(t, t.getLabel().getSignal(), upVal, new HashSet<Place>(), true);
				
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<Place, HashMap<Integer, SignalValue>> ret = new HashMap<Place, HashMap<Integer, SignalValue>>();
		
		// Filter out the unknown values
		for (Entry<Place, HashMap<Integer, SignalValue>> en: signalValues.entrySet()) {
			
			HashMap<Integer, SignalValue> placeValues = new HashMap<Integer, SignalValue>();
			ret.put(en.getKey(), placeValues);
			
			for (Entry<Integer, SignalValue> en2: en.getValue().entrySet()) {
				if (en2.getValue()!=SignalValue.UNKNOWN) {
					placeValues.put(en2.getKey(), en2.getValue());
				}
			}
		}
//		signalValues = ret;
		
		return signalValues;
	}
	
	
	// returns known info about a given transition
	public HashMap<Integer, SignalValue> getTransitionSignalInfo(Transition transition) throws Exception {
		
		if (signalValues==null) gatherSignalInfo(transition.getSTG());
		
		HashMap<Integer, SignalValue> values = new HashMap<Integer, SignalValue>();
		
		
		Signature sig = stg.getSignature(transition.getLabel().getSignal()); 
		
		if (sig==Signature.INPUT||sig==Signature.OUTPUT||sig==Signature.INTERNAL) {
			// add the signal itself
			if (transition.getLabel().getDirection()==EdgeDirection.UP) {
				values.put(transition.getLabel().getSignal(), SignalValue.LOW);
			} else if (transition.getLabel().getDirection()==EdgeDirection.DOWN) {
				values.put(transition.getLabel().getSignal(), SignalValue.HIGH);
			}
		}
				
		
		for (Node p: transition.getParents()) {
			HashMap<Integer, SignalValue> tval = signalValues.get((Place)p); 
			
			if (tval!=null) {
				for (Entry<Integer, SignalValue> en: tval.entrySet()) {
					
					if (values.containsKey(en.getKey())) {
						
						// if several places report different signal values, this is inconsistency
						if (!values.get(en.getKey()).equals(en.getValue())) {
							String signal = stg.getSignalName(en.getKey());
							throw new Exception("Inconsistent signal "+signal+" values for transition "+transition+"");
						}
						
					} else {
						values.put(en.getKey(), en.getValue());
					}
				}
			}
		}
		
		
		return values;
	}
	
	
	public HashMap<Integer, SignalValue> getPlaceSignalInfo(Place place) throws Exception {
		if (signalValues==null) gatherSignalInfo(place.getSTG());
		
		HashMap<Integer, SignalValue> ret = new HashMap<Integer, SignalValue>();
		
		if (signalValues.get((Place)place)!=null)
			ret.putAll(signalValues.get((Place)place));
		
		return ret;
	}
	
	
	
	HashMap<Point, Integer> lockInfo = null;
	
	// +1   -- a+ -> b+ and a- -> b-
	// -1   -- a+ -> b- and a- -> b+
	// null -- not yet met, any value is acceptable
	// 0    -- failed to find
	
	void gatherLockInfo() throws Exception {
		
		Set<Integer> signals = stg.getSignals();
		
		lockInfo = new HashMap<Point, Integer>();
		
		
		List<Transition> tran = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS);
		
		for (Transition t: tran) {
			int sig2 = t.getLabel().getSignal();
			EdgeDirection edge = t.getLabel().getDirection();
			
			HashMap<Integer, SignalValue> info = getTransitionSignalInfo(t);
			
			for (Entry<Integer, SignalValue> en: info.entrySet()) {
				int sig1 = en.getKey();
				
				if (sig1==sig2) continue;
				
				Point p = new Point(sig1, sig2);
				if (lockInfo.get(p)!=null&&
					lockInfo.get(p)==0) continue;
				
				int lockType = 0;
				
				if (edge==EdgeDirection.UP) {
					if (en.getValue()==SignalValue.LOW) {
						lockType = -1; // a- -> b+
					} else if (en.getValue()==SignalValue.HIGH) {
						lockType = 1;  // a+ -> b+
					} else continue;
					
				} else if (edge==EdgeDirection.DOWN) {
					if (en.getValue()==SignalValue.LOW) {
						lockType = 1;  // a- -> b-
					} else if (en.getValue()==SignalValue.HIGH) {
						lockType = -1; // a+ -> b-
					} else continue;
					
				} else continue;
				
				
				if (lockInfo.get(p)==null) {
					lockInfo.put(p, lockType);
				} else {
					if (lockInfo.get(p)!=lockType) {
						lockInfo.put(p, 0);
					}
				}
				
			}
		}
	}
	
	int commonSignals = 0;
	boolean areDifferent(Transition transition1, Transition transition2) {
		
		HashMap<Integer, SignalValue> info1;
		
		boolean ret = false;
		
		try {
			
			info1 = getTransitionSignalInfo(transition1);
			HashMap<Integer, SignalValue> info2 = getTransitionSignalInfo(transition2);
			commonSignals = 0;
			for (Entry<Integer, SignalValue> en: info1.entrySet()) {
				if (info2.containsKey(en.getKey())) {
					commonSignals++;
					if (en.getValue() != info2.get(en.getKey())) ret = true; 
				}
			}
			
			return ret;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	
	boolean areInConflict(Transition transition1, Transition transition2) {
		
		// inputs are not in CSC conflict
		if (stg.getSignature(transition1.getLabel().getSignal())==Signature.OUTPUT ||
			stg.getSignature(transition1.getLabel().getSignal())==Signature.INTERNAL ||
			stg.getSignature(transition2.getLabel().getSignal())==Signature.OUTPUT ||
			stg.getSignature(transition2.getLabel().getSignal())==Signature.INTERNAL) {
			
			// a signal edge is not in conflict with itself
			if (transition1.getLabel().getSignal()    == transition2.getLabel().getSignal()&&
				transition1.getLabel().getDirection() == transition2.getLabel().getDirection()) return false;
			
			
			if (areDifferent(transition1, transition2)) return false;
			
			// one more check, to see if they are comparable (too many false positives otherwise)
			// the output, if it is in conflict, needs to be in both transition values
			try {
				HashMap<Integer, SignalValue> info;
				
				if (stg.getSignature(transition1.getLabel().getSignal())==Signature.OUTPUT ||
					stg.getSignature(transition1.getLabel().getSignal())==Signature.INTERNAL) {
					info = getTransitionSignalInfo(transition1);
					if (info.containsKey(transition2.getLabel().getSignal())) return true;
				}
				
				if (stg.getSignature(transition2.getLabel().getSignal())==Signature.OUTPUT ||
					stg.getSignature(transition2.getLabel().getSignal())==Signature.INTERNAL) {
					info = getTransitionSignalInfo(transition2);
					if (info.containsKey(transition1.getLabel().getSignal())) return true;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}
		
		return false;
	}
	
	/**
	 * Returns true, if signals are locked, 
	 * returns false if not locked or not known 
	 * 
	 * @param Signal1
	 * @param Signal2
	 * @return
	 */
	boolean areLocked(int signal1, int signal2) {
		try {
			if (lockInfo==null)
				gatherLockInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Point p = new Point(signal1, signal2);
		Point p2 = new Point(signal2, signal1);
		
		if (lockInfo.get(p)!=null&&lockInfo.get(p2)!=null) {
			
			if (lockInfo.get(p)!=0&&
					lockInfo.get(p)==-lockInfo.get(p2)) 
				return true;
			
		}
		
		return false;
	}
	
	
	public void outputPossibleCSCConflicts() {
		boolean found = false;
		
		HashSet<Transition> processed = new HashSet<Transition>();
		
		for(Transition t1 : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			processed.add(t1);
			for (Transition t2 : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				
				// each pair is only reported once
				if (processed.contains(t2)) continue;
				
				if (areInConflict(t1, t2)) {
					
					
					found = true;
					System.out.println("Transitions "+t1+" and "+t2+" are possibly in conflict!"+" (based on "+commonSignals+" common signals)");
				}
			}
			
		}
		
		if (!found)  {
			System.out.println("There are no CSC conflicts");
		}
		
	}
	
	
	public void outputSignalsWithCSC() {
		
		Set<Integer> cleanSignals = new HashSet<Integer>();
		
		cleanSignals.addAll(stg.getSignals(Signature.OUTPUT));
		cleanSignals.addAll(stg.getSignals(Signature.INTERNAL));
		
		int total = cleanSignals.size();
		
		
		for (Transition t1 : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			for (Transition t2 : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
				if (areInConflict(t1, t2)) {
					cleanSignals.remove(t1.getLabel().getSignal());
					cleanSignals.remove(t2.getLabel().getSignal());
				}
			}
		}
		
		
		if (!cleanSignals.isEmpty()) {
			System.out.println("Outputs with CSC: ");
			for (Integer sig: cleanSignals) {
				System.out.print(stg.getSignalName(sig)+" ");
			}
		}
		
		System.out.println("\n" + cleanSignals.size()+" of "+total+" outputs have CSC");
		
	}
	
	
//	/**
//	 * Returns true, if current transition is a divisor transition
//	 * @param transition
//	 * @return
//	 */
//	boolean isDivisor(Transition transition) {
//		
//	}
	
	public void outputLocked() {
		for( Integer sig1 : stg.getSignals()) {
			for (Integer sig2: stg.getSignals()) {
				
				if (areLocked(sig1, sig2)) {
					String s1 = stg.getSignalName(sig1);
					String s2 = stg.getSignalName(sig2);
					System.out.println("Signals "+s1+" and "+s2+" are locked!");
				}
			}
		}
	}
	
	boolean gatherTriggers(Set<Transition> triggers, Set<Transition> triggered) {
		
		boolean ret = false;
		
		// for each non-input transition add its preset to the new transition list 
		for (Transition t: triggered) {

			if (t.getSTG().getSignature(t.getLabel().getSignal()) != Signature.INPUT) {
				
				for (Node n: t.getParents()) {
					for (Node n2: n.getParents()) {
						
						if (!triggers.contains(n2)) {
							ret = true;
							//if (!areLocked(t.getLabel().getSignal(), ((Transition)n2).getLabel().getSignal()))
							triggers.add((Transition)n2);
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	
	boolean gatherTriggered(Set<Transition> triggers, Set<Transition> triggered) {
		
		boolean ret = false;
		
		// for each transition add all non-input children 
		for (Transition t: triggers) {
			
			for (Node n: t.getChildren()) {
				for (Node n2: n.getChildren()) {
					Transition t2 = (Transition)n2;
					
//					if (t2.getSTG().getSignature(t2.getLabel().getSignal()) == Signature.OUTPUT ||
//						t2.getSTG().getSignature(t2.getLabel().getSignal()) == Signature.INTERNAL) {
						
						if (!triggered.contains(t2)) {
							ret = true;
							triggered.add(t2);
						}
						
//					}
				}
			}
			
		}
		
		return ret;
	}
	
	
	boolean gatherBySignatures(Set<Transition> newTransitions) {
		LinkedList<Transition> transitions = new LinkedList<Transition>();
		transitions.addAll(newTransitions);
		
		boolean ret = false;
		
		Collection<Integer> signals = new HashSet<Integer>();
		
		// for each non-dummy transition add all transitions of the same signature 
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
	
	
	public void gatherPartition(HashSet<Transition> triggers, HashSet<Transition> triggered) {
		boolean work = true;
		while(work) {
			work = gatherBySignatures(triggers);
			work |= gatherBySignatures(triggered);
			
			work |= gatherTriggers(triggers, triggered);
			work |= gatherTriggered(triggers, triggered);
		}
	}
	
	
	@Override
	public Partition improvePartition(Partition oldPartition)
			throws STGException, PartitioningException {
		
		// nothing to improve, just create a partition
		
		HashSet<Integer> processed = new HashSet<Integer>();
//		LinkedList<HashSet<Transition>> clusters = new LinkedList<HashSet<Transition>>();
		
		Partition newPartition = new Partition();
		
		
		Map<Transition, Integer> additionalSolved = solveProblematicTriggers();
		
		
		// go through each output transition, add its cluster
		for (Transition transition: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			
			if (stg.getSignature(transition.getLabel().getSignal())!=Signature.OUTPUT&&
				stg.getSignature(transition.getLabel().getSignal())!=Signature.INTERNAL) continue;
			
			if (processed.contains(transition.getLabel().getSignal())) continue;
			
			// create a new cluster from a given transition
			HashSet<Transition> triggers = new HashSet<Transition>();
			HashSet<Transition> triggered = new HashSet<Transition>();
			
			triggered.add(transition);
			
			gatherPartition(triggers, triggered);
			
			// the cluster is complete
			for (Transition trigg: triggered) {
				processed.add(trigg.getLabel().getSignal());
			}
			
			// for each output/internal transition, add it to the partition
			HashSet<Integer> signals = new HashSet<Integer>();
			for (Transition tr: triggered) {
				if (stg.getSignature(tr.getLabel().getSignal())==Signature.OUTPUT||
					stg.getSignature(tr.getLabel().getSignal())==Signature.INTERNAL)
					signals.add(tr.getLabel().getSignal());
			}
			
			if (!signals.isEmpty()) {
				newPartition.beginSignalSet();
				for (Integer signal: signals) {
					newPartition.addSignal(stg.getSignalName(signal));
				}
				
				// add additional signals for solving potential conflicts in syntactical triggers
				for (Transition tr: triggers) {
					if (triggered.contains(tr)&&stg.getSignature(tr.getLabel().getSignal())!=Signature.INPUT) continue;
					
					// also add all additional input signals
					for (Entry<Transition, Integer> en: additionalSolved.entrySet()) {
						Transition tran = en.getKey();
						Integer asig = en.getValue();
						
						if (tran.getLabel().getSignal().equals(tr.getLabel().getSignal())&&asig!=null) {
							newPartition.addInput(stg.getSignalName(asig));
						}
					}
				}
			}
		}

		return newPartition;
		
	}

}
