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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedSet;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

import net.strongdesign.stg.traversal.GraphOperations;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.Condition;

/**
 * @author Dominic Wist
 * Concrete implementation of PlaceHolderInsertion such that irreducible
 * CSC conflict will be avoided in STGs with general structure 
 *
 */
public class GeneralPlaceHolderInsertion extends PlaceHolderInsertion {
	
	private MultiSet<Node> in = null;
	private MultiSet<Node> out = null;
	private Set<Place> Q = null;
	
	private Queue<Node> delayingSimplePath = null;
	private Transition t_entry = null;
	private Transition t_exit = null;
	
	// for checkTransition loop-preservation
	private Set<Transition> formerIn;
	private Set<Transition> formerOut;
	private Set<Transition> loopTransitions;

	/**
	 * @param stg
	 * @param components
	 * @param nodeInfos
	 */
	public GeneralPlaceHolderInsertion(STG stg, Collection<STG> components,
			Map<Node, NodeProperty> nodeInfos) {
		super(stg, components, nodeInfos);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderInsertion#execute(net.strongdesign.stg.Transition, net.strongdesign.stg.Transition)
	 */
	@Override
	public boolean execute(Transition t_en, Transition t_ex)
			throws STGException {
		boolean status = false;
		if (!initialized) {
			return status;
		}
		
		// *********** body of execute **************
		
		this.t_entry = t_en;
		this.t_exit = t_ex;
		
		Condition<Node> abortCondition; 
		abortCondition = ConditionFactory.getRelevantTransitionCondition(this.critComp);
		
		SortedSet<List<Node>> simplePaths; // between t_entry and t_exit
		simplePaths = GraphOperations.findSimplePaths(t_en, t_ex, abortCondition);
		
		while ( !(simplePaths.isEmpty() || status) ) {
			List<Node> simplePath = simplePaths.first(); // shortest path first
			simplePaths.remove(simplePath);
			
			status = determineInsertionPoint(simplePath);
		}
		
		// *********** end body **************
		
		initialized = false; // tidy up after execution
		if (!status)
			return false;
		// insert placeholder according to myTraversalResult
		// doPlaceHolderInsertion(myTraversalResult, tTransInsertionCount++);
		return status;
	}
	
//	 ********************* private area ***************************************
	
	private boolean determineInsertionPoint(List<Node> simplePath) {
		
		Transition firstDelayer = null;
		
		// Traverse the simplePath in reverse direction
		for (int i = simplePath.size()-1; i >= 2; i--) {
			Node node = simplePath.get(i);
			if (node instanceof Transition) {
				if ( (this.stg.getSignature( ((Transition)node).getLabel().getSignal() ) == Signature.OUTPUT) ||
						(this.stg.getSignature( ((Transition)node).getLabel().getSignal() ) == Signature.INTERNAL) )
					firstDelayer = (Transition)node;
			}
			else if (node instanceof Place) {
				Node postNode = simplePath.get(i+1);
				if (!(postNode instanceof Transition)) break;
				if ( checkPlaceForMergeAndMarking((Place)node, (Transition)postNode) ) break;
			}
		}
		
		// No insertion point can be determined
		if (firstDelayer == null) return false;
		
		// extract all places on the simplePath between entry and delay transition
		delayingSimplePath = new java.util.LinkedList<Node>(); // empty FIFO
		for (int i = 0; i < simplePath.indexOf(firstDelayer); i++) 
			delayingSimplePath.add(simplePath.get(i));
		
		// insertImplicitPlace procedure according to the Paper
		// but with determination according to the found path objects
		
		in = new MultiSet<Node>(); // empty set
		out = new MultiSet<Node>(); // empty set
		Q = new HashSet<Place>(); // empty set
		
		formerIn = new HashSet<Transition>();
		formerOut = new HashSet<Transition>();
		loopTransitions = new HashSet<Transition>();
		
		Node t_entry = delayingSimplePath.remove(); // t_entry
		Node startPlace = delayingSimplePath.remove(); // first place after t_entry
		if ( (startPlace instanceof Place) && (t_entry instanceof Transition) )
			if ( !this.place((Place)startPlace, (Transition)t_entry, true) ) return false;
		
		if (!in.isEmpty()) 
			for (Node element : in)
				myTraversalResult.prePlaceHolder.add(element); 
		else return false;
		
		if (!out.isEmpty())
			for (Node element : out)
				myTraversalResult.postPlaceHolder.add(element);
		else return false;
		
		if (!Q.isEmpty())
			for (Place p : Q)
				myTraversalResult.marking += p.getMarking();
		else return false;
		
		// **** Actually, this is really important to avoid the considered 
		// **** self-trigger, but it introduces new conflicts very quickly.
		// add the loopTransitions
//		for (Transition t : loopTransitions) {
//			myTraversalResult.prePlaceHolder.add(t);
//			myTraversalResult.postPlaceHolder.add(t);
//		}
		
		return true; // only when true is returned all results are valid
	}
	
	private boolean place(Place p, Transition t, boolean firstCall) {
		
		// save old values:
		MultiSet<Node> inOld = in.shallowCopy();
		MultiSet<Node> outOld = out.shallowCopy();
		
		Q.add(p); // p cannot be an element of Q
		in.addAll(p.getParents());
		out.addAll(p.getChildren());
		MultiSet<Node> inTmp = in.shallowCopy();
		in.minus(out);
		out.minus(inTmp); // but with in value before the last operation
		
		boolean result = true;
		
		if (firstCall) {
			while (!this.delayingSimplePath.isEmpty()) {
				Node nextTrans = delayingSimplePath.remove();
				if (!(nextTrans instanceof Transition)) { 
					result = false;
					break;
				}
				
				Set<Node> post_p_without_t = new HashSet<Node>(p.getChildren());
				post_p_without_t.remove(t); // no loop transitions
				post_p_without_t.remove(nextTrans); // we treat nextTrans by hand
				for (Node tPrime : post_p_without_t)
					if ( !(tPrime instanceof Transition) || !this.transitionF((Transition)tPrime) ) {
						result = false;
						break;
					}
				
				if (result) {
					Set<Node> pre_p_without_t = new HashSet<Node>(p.getParents());
					pre_p_without_t.remove(t); // no loop transitions
					for (Node tPrime : pre_p_without_t)
						if ( !(tPrime instanceof Transition) || !this.transitionB((Transition)tPrime) ) {
							result = false;
							break;
						}
				}
				
				if (!result) break;
				
				// treat all nextTrans on delayingSimplePath by hand --> i.e. make transitionF() by hand
				Node nextPlace = this.delayingSimplePath.remove();
				while (Q.contains(nextPlace) && !delayingSimplePath.isEmpty()) {
					nextTrans = this.delayingSimplePath.remove();
					if (!(nextTrans instanceof Transition)) {
						result = false; 
						break;
					}
					nextPlace = this.delayingSimplePath.remove();
				}
				if (!Q.contains(nextPlace))
					if ( !(nextPlace instanceof Place && 
							this.place((Place)nextPlace, (Transition)nextTrans, !delayingSimplePath.isEmpty())) ) {
						result = false;
						break;
					}
			}
		}
		else {
			Set<Node> post_p_without_t = new HashSet<Node>(p.getChildren());
			post_p_without_t.remove(t); // no loop transitions
			for (Node tPrime : post_p_without_t)
				if ( !(tPrime instanceof Transition) || !this.transitionF((Transition)tPrime) ) {
					result = false;
					break;
				}
			
			if (result) {
				Set<Node> pre_p_without_t = new HashSet<Node>(p.getParents());
				pre_p_without_t.remove(t); // no loop transitions
				for (Node tPrime : pre_p_without_t)
					if ( !(tPrime instanceof Transition) || !this.transitionB((Transition)tPrime) ) {
						result = false;
						break;
					}
			}
		}
		
		// restore old values of in, out and Q
		if (!result) {
			this.in = inOld;
			this.out = outOld;
			this.Q.remove(p);
		}
					
		return result;
	}
	
	private boolean transitionF(Transition t) {
		BooleanPair abortCondition = checkTransitionF(t);
		if (abortCondition.stop) return abortCondition.value;
		
		Set<Node> post_t_without_Q = new HashSet<Node>(t.getChildren());
		post_t_without_Q.removeAll(Q);
		// determinatePostT(post_t_without_Q);
		for (Node p : post_t_without_Q)
			if (p instanceof Place) 
				if ( this.place((Place)p, t, false) ) return true;
		
		return false;
	}
	
	private boolean transitionB(Transition t) {
		BooleanPair abortCondition = checkTransitionB(t);
		if (abortCondition.stop) return abortCondition.value;
		
		Set<Node> pre_t_without_Q = new HashSet<Node>(t.getParents());
		pre_t_without_Q.removeAll(Q);
		// no determinism-maker necessary
		for (Node p : pre_t_without_Q)
			if (p instanceof Place)
				if ( this.place((Place)p, t, false) ) return true;
		
		return false;
	}
	
	private BooleanPair checkTransitionF(Transition t) {
		if (t == this.t_entry) return new BooleanPair(true, false);
		
		Condition<Transition> outputInternalCondition = 
			ConditionFactory.getSignatureOfCondition(
					new ArrayList<Signature>(java.util.Arrays.asList(Signature.OUTPUT, Signature.INTERNAL)));
		if (outputInternalCondition.fulfilled(t)) {
			formerOut.add(t);
			if (formerIn.contains(t)) loopTransitions.add(t);
			return  new BooleanPair(true, true);
		}
		
		if (formerIn.contains(t)) return new BooleanPair(true, false); // passing a former In-Transition
		if (t == this.t_exit) return new BooleanPair(true, false); // ... is an input transition
		return new BooleanPair(false, false);
	}
	
	private BooleanPair checkTransitionB(Transition t) {
		if (t == this.t_exit) return new BooleanPair(true, false);
		if (formerOut.contains(t)) {
			loopTransitions.add(t);
			return new BooleanPair(true, true);
		}
		if (ConditionFactory.getRelevantTransitionCondition(this.critComp).fulfilled(t)) {
			formerIn.add(t);
			return new BooleanPair(true, true);
		}
		return new BooleanPair(false, false);
	}
	
	/****
	 * Checks whether a place has a merge structure or reverse conflict resp.
	 * or has no children or has a marking that can potentially activate the post transition before t_delay fires 
	 * --> should even work for STGs with arcweights > 1
	 * --> omit the check of marking because of SeqParTrees with [seq_0_0_in-;seq_0_0_in+]
	 * @param place
	 * @return true if the place has no reverse conflict and false otherwise.
	 */
	private boolean checkPlaceForMergeAndMarking(Place place, Transition postPlace) {
		if (place.getParents().size() > 1 || place.getChildren().size() == 0)// || place.getMarking() >= place.getChildValue(postPlace))
			return true;
		else
			return false;
	}
	
	/****
	 * 
	 * A Multiset for 'in' and 'out' in implicit-place-insertion-algorithm
	 *
	 * @param <T>
	 */
	private class MultiSet<T> implements Iterable<T> {
		
		private List<T> elements;
		
		MultiSet() {
			this.elements = new ArrayList<T>();
		}
		
		private MultiSet(List<T> items) {
			this.elements = new ArrayList<T>(items.size());
			for (T item : items)
				this.elements.add(item);
		}
		
		void add(T item) {
			if (item != null) this.elements.add(item);			
		}
		
		void addAll(Collection<? extends T> items) {
			if (items != null) 
				for (T item : items) this.add(item);
		}
		
		void minus(T item) {
			if (item != null) this.elements.remove(item);
		}
		
		void minus(MultiSet<? extends T> items) {
			if (items != null)
				for (T item : items) this.minus(item);
		}
		
		boolean isEmpty() {
			return this.elements.isEmpty();
		}
		
		MultiSet<T> shallowCopy() {
			return new MultiSet<T>(this.elements);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Iterable<T>#iterator()
		 */
		@Override
		public java.util.Iterator<T> iterator() {
			return this.elements.iterator();
		}
		
	}
	
	/****
	 * 
	 * Acts as result structure for the checkTransition functions
	 *
	 */
	private class BooleanPair {
		/**First stored element.*/
		boolean stop;
		/**Second stored element.*/
		boolean value;
		
		/**Constructs an instance from two variables.*/
		BooleanPair(boolean stop, boolean value) {
			this.stop = stop;
			this.value = value;		
		}
	}

}
