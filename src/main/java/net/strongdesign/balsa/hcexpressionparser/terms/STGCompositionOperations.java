package net.strongdesign.balsa.hcexpressionparser.terms;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

public abstract class STGCompositionOperations {
	
	
	/**
	 * 
	 * @param stg - where to create a new transition copy
	 * @param t1  - the transition to be copied
	 * @return
	 */
	static Transition copyTransitionInSTG(STG stg, Transition t1) {
		STG stgFrom = t1.getSTG();
		String name = stgFrom.getSignalName(t1.getLabel().getSignal());
		Integer newNum = stg.getSignalNumber(name); // get or create the signal
		Signature s1 = stgFrom.getSignature(t1.getLabel().getSignal());
		stg.setSignature(newNum, s1);
		SignalEdge se = new SignalEdge(newNum, t1.getLabel().getDirection());
		return stg.addTransition(se);
	}
	
	/**
	 * Copies all places and transitions from another STG and sets all weights,
	 * existing STG elements remain untouched
	 */
	public static void addSTG(STG stg, STG stgFrom,
			Map<Place, Place> old2newp,
			Map<Transition, Transition> old2newt) {
		
		// add all transitions from first STG
		for (Transition t1: stgFrom.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			// create the transition
			Transition t = copyTransitionInSTG(stg, t1);
			old2newt.put(t1, t);
		}
		
		// copy all places
		for (Place p: stgFrom.getPlaces()) {
			Place np = stg.addPlace(p.getLabel(), p.getMarking());
			old2newp.put(p, np);
		}
		
		// set weights
		for (Place sp: stgFrom.getPlaces()) {
			for (Node t: sp.getChildren()) {
				int val = sp.getChildValue(t);
				
				old2newp.get(sp).setChildValue(
						old2newt.get(t), val);
			}
			
			for (Node t: sp.getParents()) {
				int val = sp.getParentValue(t);
				
				old2newp.get(sp).setParentValue(
						old2newt.get(t), val);
			}
			
		}
	}
	
	
	/**
	 * Sequential composition of two STGs into one
	 */
	public static STG sequentialComposition(
			STG stg1, Set<Place> inPlaces1, Set<Place> outPlaces1, 
			STG stg2, Set<Place> inPlaces2, Set<Place> outPlaces2,
			Set<Place> newInPlaces, Set<Place> newOutPlaces) {
		
		
		Map<Place, Place> old2newp = new HashMap<Place,Place>();
		Map<Transition, Transition> old2newt = new HashMap<Transition, Transition>();
		
		addSTG(stg1, stg2, old2newp, old2newt);
		
		boolean isLoop1 = inPlaces1.size()>0&&inPlaces1.equals(outPlaces1);
		boolean isLoop2 = inPlaces2.size()>0&&inPlaces2.equals(outPlaces2);
		
		// TODO: if there are two loops, we have to introduce a dummy transition
		if (isLoop1 && isLoop2) return null;
		
		newInPlaces.addAll(inPlaces1);
		for (Place p: outPlaces2) {
			newOutPlaces.add(old2newp.get(p));
		}
		
		// do the Cartesian product of places
		Set<Place> ip2 = new HashSet<Place>();
		for (Place p: inPlaces2) {
			ip2.add(old2newp.get(p));
		}
		
		
		if (isLoop1) {
			newInPlaces.clear();
			newInPlaces.addAll(STGUtil.cartesianProductBinding(stg1, outPlaces1, ip2));
		} else if (isLoop2) {
			newOutPlaces.clear();
			newOutPlaces.addAll(STGUtil.cartesianProductBinding(stg1, outPlaces1, ip2));
		} else {
			STGUtil.cartesianProductBinding(stg1, outPlaces1, ip2);
		}
		
		return stg1;
	}
	
	
	/**
	 * Choice composition of two STGs into one 
	 */
	public static STG choiceComposition(
			STG stg1, Set<Place> inPlaces1, Set<Place> outPlaces1, 
			STG stg2, Set<Place> inPlaces2, Set<Place> outPlaces2,
			boolean removeRedPlaces, Set<Place> newInPlaces, Set<Place> newOutPlaces) {
		
		
		Map<Place, Place> old2newp = new HashMap<Place,Place>();
		Map<Transition, Transition> old2newt = new HashMap<Transition, Transition>();
		
		addSTG(stg1, stg2, old2newp, old2newt);
		
		 
		Set<Place> ip2 = new HashSet<Place>();
		Set<Place> op2 = new HashSet<Place>();
		
		// select in/out places in the new STG 
		for (Place p: inPlaces2)  ip2.add(old2newp.get(p));
		for (Place p: outPlaces2) op2.add(old2newp.get(p));
		
		// do the Cartesian product of places
		newInPlaces.addAll(STGUtil.cartesianProductBinding(stg1, inPlaces1, ip2));
		newOutPlaces.addAll(STGUtil.cartesianProductBinding(stg1, outPlaces1, op2));
		
		return stg1;
		
	}
	
	/**
	 * Parallel composition of two STGs
	 * but it only works with signals of the same signature
	 */
	public static STG synchronousProduct(
			STG stg1, Set<Place> inPlaces1, Set<Place> outPlaces1, 
			STG stg2, Set<Place> inPlaces2, Set<Place> outPlaces2,
			boolean removeRedPlaces, Set<Place> newInPlaces, Set<Place> newOutPlaces) {
		
		
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
				if (STGUtil.sameTransitions(t1, t2, false)) {
					
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
				Transition newTransition = copyTransitionInSTG(stg,t1);
				new2old1.put(newTransition, t1);
			}
		}

		for (Transition t2: stg2.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (!occurs2.contains(t2.getLabel())) {
				// create the transition
				Transition newTransition = copyTransitionInSTG(stg, t2);
				new2old2.put(newTransition, t2);
			}
		}
		
		// copy all places
		for (Place p: stg1.getPlaces()) {
			Place np = stg.addPlace(p.getLabel(), p.getMarking());
			
			if (inPlaces1.contains(p)) newInPlaces.add(np);
			if (outPlaces1.contains(p)) newOutPlaces.add(np);
			
			old2new1.put(p, np);
		}
		
		for (Place p: stg2.getPlaces()) {
			Place np = stg.addPlace(p.getLabel(), p.getMarking());

			if (inPlaces2.contains(p)) newInPlaces.add(np);
			if (outPlaces2.contains(p)) newOutPlaces.add(np);
			
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
			STGUtil.removeRedundantPlaces(stg);
		
		// clean up input and output places
		newInPlaces.retainAll(stg.getPlaces());
		newOutPlaces.retainAll(stg.getPlaces());
		return stg;
	}

}
