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

package net.strongdesign.stg.traversal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import net.sf.javailp.Linear;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.MarkingEquationCache;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;


/**
 * <p>
 * Factory class for various Conditions.
 * <p>
 * Parametrized conditions are generated every time, static ones are generated
 * once (at first request) and returned every time.
 * 
 * 
 * <p>
 * <b>History: </b> <br>
 * 03.10.2004: Created <br>
 * 15.02.2005: Added all used Conditions as internal classes
 * 21.02.2005: Reimplemented RedundantPlace Condition
 * 15.03.2005: Fixed an error for SecureContractable Condition
 * 31.03.2005: Fixed an error for RedundantPlace Condition, shortcut places
 * 
 * 
 * @author Mark Schaefer
 */

public abstract class ConditionFactory {
	
	protected static class SelfTriggering<P extends Place> extends AbstractCondition<P> {
		
		public boolean fulfilled(P place) {
			List<Integer> parents = Operations.collect(
					Node.getTransitions(place.getParents()),
					new NotCondition<Transition>(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)),
					CollectorFactory.getSignalCollector());
			
			parents.retainAll(
					Operations.collect(
							Node.getTransitions(place.getChildren()),
							new NotCondition<Transition>(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)),
							CollectorFactory.getSignalCollector()));
			
			
			return !parents.isEmpty();
		}
		
	}
	
	protected static class Activated<T extends Transition> extends AbstractCondition<T> {
		public boolean fulfilled(T transition) {
			return transition.isActivated();
		}
	}
	
	public static class All<T> extends AbstractCondition<T> {
		public boolean fulfilled(T o) {
			return true;
		}
	}
	
	protected static class ArcWeight<N extends Node> extends AbstractCondition<N> {
		int max;
		
		public ArcWeight(int max) {
			this.max = max;
		}
		
		public boolean fulfilled(N node) {
			for (Node parent : node.getParents())
				if (node.getParentValue(parent) > max)
					return true;
			
			for (Node child : node.getChildren())
				if (node.getChildValue(child) > max)
					return true;
			
			return false;
		}
	}
	
	protected static class ChildOf<N extends Node> extends AbstractCondition<N> {
		protected Set<Node> children;
		
		public ChildOf(Node node) {
			children = node.getChildren();
		}
		
		public boolean fulfilled(N node) {
			return children.contains(node);
		}
	}
	
	protected static class DuplicateTransition<T extends Transition> extends AbstractCondition<T> {
		protected Transition transition;
		protected STG stg;
		
		public DuplicateTransition(Transition transition) {
			this.transition = transition;
			this.stg = transition.getSTG();
		}
		
		public boolean fulfilled(T dupTransition) {
			
			if (transition == dupTransition)
				return false;
			
			// dummy transition and non-dummy transition
			if ( (stg.getSignature(transition.getLabel().getSignal()) != Signature.DUMMY) &&
					(stg.getSignature(dupTransition.getLabel().getSignal()) == Signature.DUMMY) )
				return false;
			
			if ( (stg.getSignature(transition.getLabel().getSignal()) == Signature.DUMMY) &&
					(stg.getSignature(dupTransition.getLabel().getSignal()) != Signature.DUMMY) )
				return false;
			
			// non-dummy transition and non-dummy transition
			if ( (stg.getSignature(transition.getLabel().getSignal()) != Signature.DUMMY) &&
					(stg.getSignature(dupTransition.getLabel().getSignal()) != Signature.DUMMY) )
				if (!transition.getLabel().equals(dupTransition.getLabel()))
					return false;
			
			if (transition.getParents().size() != dupTransition.getParents()
					.size())
				return false;
			if (transition.getChildren().size() != dupTransition.getChildren()
					.size())
				return false;
			
			if (!transition.getParents()
					.containsAll(dupTransition.getParents()))
				return false;
			if (!transition.getChildren().containsAll(
					dupTransition.getChildren()))
				return false;
			
			for (Node actTransition : transition.getParents())
				if (transition.getParentValue(actTransition) != dupTransition
						.getParentValue(actTransition))
					return false;
			
			for (Node actTransition : transition.getChildren())
				if (transition.getChildValue(actTransition) != dupTransition
						.getChildValue(actTransition))
					return false;
			
			return true;
		}
		
	}
	
	protected static class SignalEdgeOf<T extends Transition> extends AbstractCondition<T> {
		protected SignalEdge l;
		
		public SignalEdgeOf(SignalEdge l) {
			this.l = l;
		}
		
		public boolean fulfilled(T t) {
			return t.getLabel().equals(l);
		}
		
	}
	
	protected static class TransitionLabelOf<T extends Transition> extends AbstractCondition<T>  {
		String label;
		public TransitionLabelOf(Transition transition) {
			label = transition.getString(Transition.UNIQUE);
		}
		public TransitionLabelOf(String label) {
			this.label = label;
		}
		public boolean fulfilled(T transition) {
			return label.equals(transition.getString(Transition.UNIQUE));
		}
	}
	
	protected static class PlaceLabelOf<P extends Place> extends AbstractCondition<P> {
		protected String l;
		
		public PlaceLabelOf(String l) {
			this.l = l;
		}
		
		public boolean fulfilled(P p) {
			return p.getLabel().equals(l);
		}
		
	}
	
	protected static class LoopNode<N extends Node> extends AbstractCondition<N> {
		public boolean fulfilled(N node) {
			Set<Node> children = node.getChildren();
			for (Node actParent : node.getParents())
				if (children.contains(actParent))
					return true;
			
			Set<Node> parents = node.getParents();
			for (Node actChild : node.getChildren())
				if (parents.contains(actChild))
					return true;
			
			return false;
		}
	}
	
	protected static class LoopOnlyPlace<P extends Place> extends AbstractCondition<P> {
		public boolean fulfilled(P place) {
			if (place.getMarking() == 0)
				return false;
			
			Set<Node> children = place.getChildren();
			int maxChildVlaue = 0;
			for (Node actTransition : children) {
				int childValue = place.getChildValue(actTransition);
				if (childValue > place.getParentValue(actTransition))
					return false;
				maxChildVlaue = maxChildVlaue > childValue ? maxChildVlaue
						: childValue;
			}
			return true;
		}
	}
	
	protected static class LoopOnlyTransition<T extends Transition> extends AbstractCondition<T> {
		public boolean fulfilled(T transition) {
			
			if (transition.getSTG().getSignature(transition.getLabel().getSignal()) != Signature.DUMMY) return false;
			Set<Node> children = transition.getNeighbours();
			for (Node actChild : children) {
				int cv = transition.getChildValue(actChild);
				int pv = transition.getParentValue(actChild);
				if (cv != pv)
					return false;
			}
			return true;
		}
	}
	
	protected static class MarkedPlace<P extends Place> extends AbstractCondition<P> {
		public boolean fulfilled(P place) {
			if (place.getChildren().size() != 1
					|| place.getParents().size() != 1)
				return false;
			if (place.getChildValue(place.getChildren().iterator().next()) > 1)
				return false;
			if (place.getParentValue(place.getParents().iterator().next()) > 1)
				return false;
			return true;
			
		}
		
	}
	
	protected static class MultiNodes<N extends Node> extends AbstractCondition<N> {
		protected Collection<N> nodes;
		
		public MultiNodes(Collection<N> nodes) {
			this.nodes = nodes;
		}
		
		public MultiNodes(N node) {
			this.nodes = new HashSet<N>();
			nodes.add(node);
		}
		
		public boolean fulfilled(N node) {
			return nodes.contains(node);
		}
	}
	
	protected static class NewAutoConflictPair<T extends Transition> extends AbstractCondition<T> {
		//THEO    check for dummy autoconflicts for deep backtracking 
		public boolean fulfilled(T transition) {
			
			List<Set<Node>> neighbours = STGOperations.collectFromCollection(
					transition.getParents(), ALL_NODES, CollectorFactory
					.getChildrenCollector());
			
			List<Node> siblings = new LinkedList<Node>();
			
			for (Set<Node> n1 : neighbours)
				siblings.addAll(n1);
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<Node> n3 = STGOperations.getElements(siblings, new NotCondition(
					getSignatureOfCondition(Signature.DUMMY)));
			
			Set<Integer> siblingSignals = new HashSet<Integer>();
			
			for (Node t : n3)
				siblingSignals.add(((Transition) t).getLabel().getSignal());
			
			siblingSignals.remove(transition.getLabel().getSignal());
			
			
			List<Set<Node>> gChildren = STGOperations.collectFromCollection(
					transition.getChildren(), ConditionFactory.ALL_NODES,
					CollectorFactory.getChildrenCollector());
			
			List<Node> grandchilds = new LinkedList<Node>();
			
			for (Set<Node> n1 : gChildren)
				grandchilds.addAll(n1);
			for (Node t : grandchilds)
				if (siblingSignals.contains(((Transition) t).getLabel().getSignal()))
					return true;
			
			return false;
		}
		
	}
	
	protected static class NumberOfChildren<N extends Node> extends AbstractCondition<N> {
		int min;
		
		public NumberOfChildren(int min) {
			this.min = min;
		}
		
		public boolean fulfilled(N node) {
			return (node.getChildren().size()) > min;
		}
	}
	
	protected static class NumberOfParents<N extends Node> extends AbstractCondition<N> {
		int min;
		
		public NumberOfParents(int min) {
			this.min = min;
		}
		
		public boolean fulfilled(N node) {
			return (node.getParents().size()) > min;
		}
	}
	
	protected static class RedundantPlace<P extends Place> extends AbstractCondition<P> {
		protected STG stg;
		protected Set<Node> reason;
		
		public RedundantPlace(STG stg) {
			this.stg = stg;
			reason = Collections.emptySet();
		}
		
		public Object becauseOf() {
			return reason;
		}
		
		public boolean fulfilled(P place) {
			reason = new HashSet<Node>();
			
			
			// **************** 
			// the easy cases
			// ****************
			
			// no children, does not affect firing of any transition -> definitely redundant
			if (place.getChildren().size() == 0)
				return true;
			
			//is the single parent of a transition -> definitely not redundant
			boolean singleParent = false;
			for (Node children : place.getChildren()) {
				singleParent = singleParent || children.getParents().size() == 1;
			}
			
			if (singleParent) return false;
			
			
			// Is a loop-only place
			if (LOOP_ONLY_PLACE.fulfilled(place))
				return true;
			
			// is a duplicate place
			Collection<Place> sibblings = new HashSet<Place>();
			
			for (Node node : place.getParents()) {
				for (Node sibbling : node.getChildren()) {
					sibblings.add( (Place) sibbling);
				}
			}
			
			
			for (Node node : place.getChildren()) {
				for (Node sibbling : node.getParents()) {
					sibblings.add( (Place) sibbling);
				}
			}
			sibblings.remove(place);
			
			
			l: for (Place actPlace : sibblings) {
				if (place.getMarking() < actPlace.getMarking())
					continue;
				
				if (place.getParents().size() != actPlace.getParents().size())
					continue;
				if (place.getChildren().size() != actPlace.getChildren().size())
					continue;
				
				if (!place.getParents().containsAll(actPlace.getParents()))
					continue;
				if (!place.getChildren().containsAll(actPlace.getChildren()))
					continue;
				
				for (Node act2Place : place.getParents())
					if (place.getParentValue(act2Place) != actPlace
							.getParentValue(act2Place))
						continue l;
				
				for (Node act2Place : place.getChildren())
					if (place.getChildValue(act2Place) != actPlace
							.getChildValue(act2Place))
						continue l;
				
				reason.add(actPlace);
				return true;
				
			}
			
			
			// the primitive case of the shared shortcut place
			if (CLW.instance.SHARED_SHORTCUT_PLACE.isEnabled()) {
				if (place.getMarking()==0) {
					Set<Node> parents = new HashSet<Node>();
					parents.addAll(place.getParents());
					Set<Node> children= new HashSet<Node>();
					children.addAll(place.getChildren());
					
					Set<Node> test = new HashSet<Node>();
					test.addAll(parents);
					test.addAll(children);
					
					// if the place does not have self-loops and its parent and child counts are equal,
					// then it might be a shared shortcut place
					if (parents.size()+children.size()==test.size()&&parents.size()==children.size()) {
						
						for (Node t1: place.getParents()) {
							if (t1.getChildren().size()!=2) break;
							if (t1.getChildValue(place)!=1) break;
							
							Iterator<Node> it = t1.getChildren().iterator();
							Place p = (Place) it.next();
							if (p==place) p = (Place) it.next();
							
							// for now only very primitive cases are considered
							if (p.getMarking()>0) break;
							
							if (!MARKED_GRAPH_PLACE.fulfilled(p)) break;
							
							Transition t2 = (Transition) p.getChildren().iterator().next();
							if (place.getChildValue(t2)!=1) break;
							
							parents.remove(t1);
							children.remove(t2);
						}
					}
					
					// if all parents and children were matched, then it is a redundant place
					if (parents.size()==0&&children.size()==0) return true;
				}
			}
			
			
			// ******************* 
			// the advanced cases
			// *******************
			
			
			if (CLW.instance.SHORTCUTPLACE.isEnabled()) { 
				boolean shortCutPlace = shortCutPlace(place, reason);
				if (shortCutPlace) {
					return true;
				}
			}
			
			
			return false;
		}
	}
	
	
	
	protected static boolean shortCutPlace(Place place, Collection<Node> reason) {
		//shortcutplace?
		//uses a A* algorithm for finding
		//a marked-graph path between the parent and the child of the place not using
		//the place itself, the cost of a path is its marking
		//The class BackNode is used for meorizing the shortcut path
		class BackNode implements Comparable<BackNode> {
			public BackNode(Node node, BackNode parent, int length, int costs) {
				this.parent = parent;
				this.node = node;
				this.length = length;
				this.costs = costs;
				
			}
			public Node node;
			public BackNode parent;
			public int length;
			public int costs;
			
			public int compareTo(BackNode node) {
				return this.costs - node.costs;
			}
			
			
		}
		
		if (MARKED_GRAPH_PLACE.fulfilled(place)) {
			int maxLength = CLW.instance.SHORTCUTPLACE_LENGTH.getIntValue();
			
			Map<Node, BackNode> curBackNode = new HashMap<Node, BackNode>();
			
			//The transitions which will be expanded
			//it was not possible to use a  SortedMap, since it allows no duplicate keys as it is needed here
			PriorityQueue<BackNode> frontier = new PriorityQueue<BackNode>();
			
			//put the start transition, i.e. the parent of place
			Node startTransition = place.getParents().iterator().next();
			BackNode backNode = new BackNode(startTransition, null, 0, 0);
			frontier.add(backNode);
			curBackNode.put(startTransition, backNode);
			
			//if this node is reached we are finished
			Node endNode = place.getChildren().iterator().next();
			int marking = place.getMarking();
			
			//As long there is hope
			while (!frontier.isEmpty()) {
				BackNode head = frontier.poll();
				//If the costs of the cheapest marked-graph way exceeds M(place), place cannot be
				//a shortcut place
				int actCosts = head.costs;
				
				if (actCosts > marking)
					return false;
				
				if (head.length > maxLength)
					continue;
				//If the cheapest transition is the child of place
				//there is a marked-graph path from parent of place to child of place
				//with a marking less or equal (see above) than that of place
				//et voila, place is a shortcut place
				
				Node actNode = head.node;
				if (endNode == actNode) {
					do {				
						head = head.parent;
						reason.add(head.node);
						head = head.parent;
					}
					while (head.parent != null);
					return true;
				}
				
				//look up all childs of actTransition which are marked graph places excluding place itself
				//and add their only child to frontier for further expanding
				
				@SuppressWarnings({ "unchecked", "rawtypes" }) 
				Collection<Place> actChilds = STGOperations.getElements(
						(Collection) actNode.getChildren(), MARKED_GRAPH_PLACE);
				
				actChilds.remove(place);
				for (Place child : actChilds) {
					BackNode placeBack = new BackNode(child, head, -1, -1);
					Node nextTransition = child.getChildren().iterator().next();
					int newCosts = actCosts + child.getMarking();
					
					BackNode exBack = curBackNode.get(nextTransition);
					if ( exBack == null || exBack.costs > newCosts) {
						BackNode newBackNode = new BackNode(nextTransition, placeBack, head.length+1, newCosts);
						frontier.add(newBackNode);
						frontier.remove(exBack);
						curBackNode.put(nextTransition, newBackNode);						
					}
				}
			}
		}
		return false;
		
	}
	
	
	
	protected static class RedundantTransition<T extends Transition> extends AbstractCondition<T> {
		protected STG stg;
		
		public RedundantTransition(STG stg) {
			this.stg = stg;
		}
		
		public boolean fulfilled(T transition) {
			
			//Loop-Only transition
			if (LOOP_ONLY_TRANSITION.fulfilled(transition))
				return true;
			
			//Duplicate transition
			if (stg.getTransitions(
					ConditionFactory
					.getDuplicateTransitionCondition(transition))
					.size() != 0)
				return true;
			
			return false;
		}
	}
	
	protected static class ShortcutPlace<P extends Place> extends AbstractCondition<P> {
		public boolean fulfilled(P place) {
			if (place.getChildren().size() == 0)
				return true;
			if (place.getChildren().size() != 1
					|| place.getParents().size() != 1)
				return false;
			
			return false;
		}
	}
	
	protected static class SignalNameOf<S> extends AbstractCondition<String> {
		protected Set<String> signalNames = new HashSet<String>();
		
		public SignalNameOf(String signalName) {
			this.signalNames.add(signalName);
		}
		
		public SignalNameOf(Collection<String> signalName) {
			this.signalNames.addAll(signalName);
		}
		
		
		public boolean fulfilled(String signal) {
			return signalNames.contains(signal);
		}
	}
	
	protected static class SignalNameOfTransition<T extends Transition> extends AbstractCondition<T> {
		protected Set<String> signalNames = new HashSet<String>();
		
		public SignalNameOfTransition(String signalName) {
			this.signalNames.add(signalName);
		}
		
		public SignalNameOfTransition(Collection<String> signalName) {
			this.signalNames.addAll(signalName);
		}
		
		
		public boolean fulfilled(T transition) {
			if (transition.getSTG() != null)
				return signalNames.contains(transition.getSTG().getSignalName(transition.getLabel().getSignal()));
			else
				return false;
		}
	}
	
	protected static class LocalTransitions<T extends Transition> extends AbstractCondition<T> {
		protected Condition<T> localTransCondition;
		
		public LocalTransitions() {
			List<Signature> localSigns = new LinkedList<Signature>();
			localSigns.add(Signature.INTERNAL);
			localSigns.add(Signature.OUTPUT);
			localTransCondition = new SignatureOf<T>(localSigns);
		}
		
		public boolean fulfilled(T o) {
			return localTransCondition.fulfilled(o);
		}
	}
	
	protected static class SignalOf<T extends Transition> extends AbstractCondition<T> {
		protected Collection<Integer> signals;
		
		public SignalOf(Collection<Integer> signals) {
			this.signals = new LinkedList<Integer>(signals);
		}
		
		public SignalOf(Integer... signals) {
			this.signals = new LinkedList<Integer>();
			for (Integer sig : signals)
				this.signals.add(sig);
		}
		
		public boolean fulfilled(T transition) {
			
			return signals.contains(transition.getLabel().getSignal());
		}
	}
	
	protected static class SignalOfEdge<E extends SignalEdge> extends AbstractCondition<E> {
		protected Collection<String> signals;
		
		public SignalOfEdge(Collection<String> signals) {
			this.signals = new LinkedList<String>(signals);
		}
		
		public SignalOfEdge(String signal) {
			this.signals = new LinkedList<String>();
			this.signals.add(signal);
		}
		
		public boolean fulfilled(E edge) {
			
			return signals.contains(edge.getSignal());
		}
	}
	
	
	
	protected static class SignatureOf<T extends Transition> extends AbstractCondition<T> {
		protected List<Signature> signatures;
		
		public SignatureOf(List<Signature> signatures) {
			this.signatures = signatures;
		}
		
		public SignatureOf(Signature signature) {
			this.signatures = new LinkedList<Signature>();
			signatures.add(signature);
		}
		
		public boolean fulfilled(T transition) {
			return signatures.contains(transition.getSTG().getSignature(transition.getLabel().getSignal()));
		}
	}
	
	
	
	protected static class SplittingAbove<N extends Node> extends AbstractCondition<N> {
		public boolean fulfilled(N node) {
			return !STGOperations.getElements(node.getParents(),
					ConditionFactory.getNumberOfChildrenCondition(1)).isEmpty();
		}
	}
	
	protected static class SplittingBelow<N extends Node> extends AbstractCondition<N> {
		public boolean fulfilled(N node) {
			return !STGOperations.getElements(node.getChildren(),
					ConditionFactory.getNumberOfParentsCondition(1)).isEmpty();
		}
	}
	
	protected static class StructuralConflict<N extends Node> extends AbstractCondition<N> {
		protected Node conflictNode;
		
		public StructuralConflict(Node conflictNode) {
			this.conflictNode = conflictNode;
		}
		
		public boolean fulfilled(N node) {
			if (node == conflictNode)
				return false;
			
			for (Node parent : conflictNode.getParents())
				if (parent.getChildren().contains(node))
					return true;
			
			return false;
		}
	}
	
	
	
	
	
	
	
	/**
	 * A condition which returns true when a transition can be contracted preserving safeness.
	 * Warning! This is a stateful condition: when it was called for a transition t_1, it is
	 * assumed that this condition is actually contracted if the result is true. The check for 
	 * the next transition t_2 is then made under this assumption and so on. Call {@link SafeContraction#reset()}
	 * to start anew. 
	 */
	public static class SafeContraction<T extends Transition> extends AbstractCondition<T> {
		
		protected STG stg;
		protected File unfolding;
		protected Map<Node, Collection<Node>> cEffects;
		
		public SafeContraction(STG stg)  {
			this.stg = stg;
			reset();
			
			try {
				//where the STG is saved
				File tmpSTG = File.createTempFile("desij", ".g");
				
				//where the unfolding is saved
				unfolding = File.createTempFile("desij", ".unf");
				
				//save the STG, generate the unfolding and extract CSC violating traces
				FileSupport.saveToDisk(STGFile.convertToG(stg, false, true), tmpSTG.getCanonicalPath());
				
				HelperApplications.startExternalTool(HelperApplications.PUNF, 
						" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
						HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
						" " + 
						HelperApplications.SECTION_START+tmpSTG.getCanonicalPath()+HelperApplications.SECTION_END ).waitFor();
				
			}
			catch (IOException e) {
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public Object becauseOf() {
			return Collections.emptySet();
		}
		
		public void reset() {	
			cEffects = new HashMap<Node, Collection<Node>>();
		}
		
		public boolean fulfilled(T transition) {
			
			StringBuilder cl = new StringBuilder();
//			try {
//				cl.append(HelperApplications.getApplicationPath("mpsat") + " -F -d (");				
//			} 
//			catch (IOException e) {
//				e.printStackTrace();
//			}
			
			
			Collection<Place> parentCondition = new HashSet<Place>();
			for (Node parent : transition.getParents()) {
				Queue<Node> frontier = new LinkedList<Node>();
				frontier.add(parent);
				
				while (! frontier.isEmpty()) {
					Node curPlace = frontier.poll();
					if (parentCondition.contains(curPlace))
						continue;
					
					Collection<Node> comprisedPlaces = cEffects.get(curPlace);
					if (comprisedPlaces == null) {
						parentCondition.add((Place) curPlace);
					}
					else {
						frontier.addAll(comprisedPlaces);
					}    				
				}    			
			}
			
			Collection<Place> childCondition = new HashSet<Place>();
			for (Node parent : transition.getChildren()) {
				Queue<Node> frontier = new LinkedList<Node>();
				frontier.add((Place) parent);
				
				while (! frontier.isEmpty()) {
					Node curPlace = frontier.poll();
					if (childCondition.contains(curPlace))
						continue;
					
					Collection<Node> comprisedPlaces = cEffects.get(curPlace);
					if (comprisedPlaces == null) {
						childCondition.add((Place)curPlace);
					}
					else {
						frontier.addAll(comprisedPlaces);
					}    				
				}    			
			}
			
			
			for (Place place : parentCondition) {
				cl.append(place.getString(Node.UNIQUE) + "|");
			}
			cl.deleteCharAt(cl.length()-1);
			cl.append(")&(");
			
			for (Place place : childCondition) {
				cl.append(place.getString(Node.UNIQUE) + "|");
			}
			cl.deleteCharAt(cl.length()-1);
			cl.append(") ");
			
			try {
				File tmpOut= File.createTempFile("mpsat", ".out");
				cl.append(HelperApplications.SECTION_START+unfolding.getCanonicalPath()+HelperApplications.SECTION_END + 
						" " + 
						HelperApplications.SECTION_START+tmpOut.getCanonicalPath()+HelperApplications.SECTION_END);
				// Process exec = Runtime.getRuntime().exec(cl.toString());
				Process exec = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
						" -F -d (" + 
						cl.toString());
				
				if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
					StreamGobbler.createGobbler(exec.getInputStream(), "safe mpsat", System.out);
					StreamGobbler.createGobbler(exec.getErrorStream(), "safe mpsat-Error", System.out);
				}
				exec.waitFor();
				exec.getErrorStream().close();
				exec.getInputStream().close();
				exec.getOutputStream().close();
				
				
				String res = FileSupport.loadFileFromDisk(tmpOut.getCanonicalPath());
				if (res.startsWith("NO")) {
					
					for (Node node : transition.getParents()) {
						cEffects.put(node, transition.getChildren());						
					}
					
					for (Node node : transition.getChildren()) {
						cEffects.put(node, transition.getParents());						
					}
					
					return true;
				}
				else 
					return false;
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return false;
		}
		
	}
	
	protected static class PlaceMarking<P extends Place> extends AbstractCondition<P> {
		int marking;
		
		public PlaceMarking(int marking) {
			this.marking = marking;
		}
		
		public boolean fulfilled(P o) {
			return o.getMarking() >= marking;
		}
		
	}
	
	protected static class RelevantTransition<T extends Node> extends AbstractCondition<T> {
		
		protected STG stg; // critical component STG
		
		public RelevantTransition(STG stg) {
			this.stg = stg;
		}
		
		public boolean fulfilled(T transition) {
			// transition should be a transition of the specification STG
			if ( !(transition instanceof Transition) ) return false;
			
			if ( this.stg.getTransition(transition.getIdentifier()) != null ) return true;
			return false;
		}
	}
	
	protected static class TooManyOutputs<T extends Collection<STG>> extends AbstractCondition<T> {
		
		protected int limit = -1; // means no limit
		
		public TooManyOutputs(int limit) {
			this.limit = limit;
		}

		public boolean fulfilled(T o) {
			
			if (this.limit < 0) return false; // never to many output signals
			
			int outputCount = 0;
			for (STG stg: o)
				outputCount += stg.getSignals(Signature.OUTPUT).size();
			
			if (outputCount > this.limit)
				return true;
			else
				return false;
			
		}
		
	}
	
	protected static class TooManySignals<C extends Collection<STG>> extends AbstractCondition<C> {
		
		protected int limit = -1; // means no limit
		
		public TooManySignals(int limit) {
			this.limit = limit;
		}

		public boolean fulfilled(C o) {
			
			if (this.limit < 0) return false; // never to many output signals
			
			int signalCount = 0;
			for (STG stg: o) {
				signalCount += stg.getSignals().size(); // including Dummies
				signalCount -= stg.getSignals(Signature.DUMMY).size();
			}
			
			if (signalCount > this.limit)
				return true;
			else
				return false;
			
		}
		
	}
	
	protected static class MGPlaceBetweenCondition<N extends Node> extends AbstractCondition<N> {
		N sourceTrans;
		N targetTrans;
		
		public MGPlaceBetweenCondition(N source, N target) {
			this.sourceTrans = source;
			this.targetTrans = target;
		}
		
		public boolean fulfilled(N place) {
			Set<Node> children = place.getChildren();
			Set<Node> parents = place.getParents();
			if(children.size() != 1 || parents.size() != 1)
				return false;
			return children.iterator().next() == targetTrans && parents.iterator().next() == sourceTrans;
		}
		
	}
	
	protected static class DummiesAffectedByNewPlaces <T extends Transition> extends AbstractCondition<T> {
		protected Collection<Place> newPlaces;
		protected STG stg;
		
		public DummiesAffectedByNewPlaces(Collection<Place> newPlaces) throws STGException {
			this.newPlaces = newPlaces;
			if (!newPlaces.isEmpty())
				this.stg = newPlaces.iterator().next().getSTG();
			else 
				throw new STGException("No new places were generated after transition contraction!");
		}
		
		public boolean fulfilled(T transition) {
			return false;
		}
		
		
	}
	
	protected static class SafePlaceCondition<P extends Place> extends AbstractCondition<P> {

		protected STG stg;
	
		public SafePlaceCondition(STG stg) {
			this.stg = stg;
		}

		@Override
		public boolean fulfilled(P place) {
			
			SolverFactory factory = new SolverFactoryLpSolve();
			factory.setParameter(Solver.VERBOSE, 0); // we don't need messages from the solver
			factory.setParameter(Solver.TIMEOUT, 0); // no timeout
			
			// M_1 = M_N + C v_1 --> -MN = -M1 + C v_1
			// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem
			Problem problem = MarkingEquationCache.getMarkingEquation(stg);
			Linear linear;
			
			// M_1(place) >= 2
			int id = place.getIdentifier();
			linear = new Linear();
			linear.add(1, "M1" + id);
			problem.add(linear, ">=", 2);
			
			
			Result result = factory.get().solve(problem);
			
			if (result == null) // problem is infeasible
				return true; // i.e. place is definitely safe
			else
				return false; // i.e. place is maybe unsafe
			
		}
		
	}
		
	
	/**
	 * Condition<Transition> which checks if a place is redundant by solving an LP problem.
	 * For this the lp_solv library is used.<br>
	 * 
	 * At the construction the weightmatrix of the stg is calculated for further use when 
	 * checking for redundancy. It is therefore important not to change the STG in any way
	 * when reusing the Condition. When a change occured a new Condition must be generated.
	 * 
	 * @author mark
	 *
	 */
	protected static class RedundantPlaceLP<P extends Place> extends AbstractCondition<P> {
		
		/**
		 * The mapping between the nodes and their numbers in the weightmatrix
		 */
		protected Map<Node, Integer> mapping;
		
		/**
		 * The weightmatrix as described in {@link STG#getWeightMatrix(Map)}
		 */
		protected int[] wm;
		
		protected int nroPlaces;
		protected int nroTransitions;
		
		
		public RedundantPlaceLP(STG stg) {
			mapping = new HashMap<Node, Integer>();
			wm = stg.getWeightMatrix(mapping);
			nroPlaces = wm[wm.length-2];
			nroTransitions = wm[wm.length-1];    	
		}
		
		public boolean fulfilled(P place) {
			int redPlace = mapping.get(place);    		
			
			
			//Some notes to lp_solve:
			//For arrays added as constraints the first entry -- position 0 -- is ignored
			//Nodes of an STG are numbered starting at 0, but lp_solv counts uo from 1
			//Therefore, there are some +1 or even +2 in the code for accessing data in arrays
			
			//The next row to added as a constraint to the LP
			double[] curRow = new double[nroPlaces+2];
			
			
			try {
				//Create a new LP
				LpSolve lp = LpSolve.makeLp(0, nroPlaces + 1);
				
				//we don't want to know what lp_solv is doing, no, no, no, we don't
				lp.setVerbose(0);
				
				//Set the target function, which is constant because only feasibility must be checked
				lp.setObjFn(curRow); 
				
				//The valuation of the redundant place must be strictly greater 0
				//the exact value is not important
				lp.setLowbo(redPlace+1, 1);
				
				//set first constraint, for redundancy condition 1: V(p)M_N(p) - \sum_{q\in Q} V(q)M_N(q) - c = 0
				for (int p=0; p<nroPlaces; ++p)
					curRow[p+1] = -wm[2*nroPlaces*nroTransitions + p]; 
				curRow[nroPlaces+1] = -1;
				
				//entry for place p (from the condition) has the opposite sign
				curRow[redPlace+1] = -curRow[redPlace+1]; 
				lp.addConstraint(curRow, LpSolve.EQ, 0);
				
				//for debugging
				//lp.setRowName(1, "marking");
				
				//set second set of constraints for condition 2: 
				// \forall t\in T : V(p)\Delta_t(p) - \sum_{q\in Q} V(q)\Delta_t(q) \geq 0
				
				//for every constraint 0 -- variable c not iomportant here
				curRow[nroPlaces+1] = 0;			
				for (int t = 0; t<nroTransitions; ++t) {
					for (int p = 0; p<nroPlaces; ++p) {
						curRow[p+1] = -(wm[nroPlaces*nroTransitions+t*nroPlaces+p] - wm[p*nroTransitions+t]);
					}
					//entry for place p (from the condition) has the opposite sign
					curRow[redPlace+1] = -curRow[redPlace+1];
					lp.addConstraint(curRow, LpSolve.GE, 0);					
				}
				
				//third set of constraints for condition 3: 
				// \forall t\in T : V(p)W(p,t) - \sum_{q\in Q} V(q)W(q,t) -c \leq 0
				curRow[nroPlaces+1] = -1;			
				for (int t = 0; t<nroTransitions; ++t) {
					for (int p = 0; p<nroPlaces; ++p) {
						curRow[p+1] = - wm[p*nroTransitions+t];
					}
					//entry for place p (from the condition) has the opposite sign
					curRow[redPlace+1] = -curRow[redPlace+1];
					lp.addConstraint(curRow, LpSolve.LE, 0);
				}
				
				//for debugging
				/*
				 for (Node node : mapping.keySet())
				 if (node instanceof Place)
				 lp.setColName(mapping.get(node)+1, node.toString());
				 else {
				 lp.setRowName(mapping.get(node)+2, node.toString()+"-2");
				 lp.setRowName(mapping.get(node)+2+nroTransitions, node.toString()+"-3");
				 }
				 */
				
				//solve the problem, if it is feasible lp_solv returns immediately with 0 after the first vertex was encoutered
				//because the object function is constant, then the place is redundant
				//if the problem is infeasible the place is not redundant
				if (lp.solve() == 0)
					return true;
				else
					return false;
				
			} catch (LpSolveException e) {
				System.err.println("Internal error while checking redundancy with lp_solv");
				e.printStackTrace();
				return false;
			}
		}
	}
	
	protected static class ImplicitPlaceCondition<P extends Place> extends AbstractCondition<P> {
		
		protected STG stg;

		public ImplicitPlaceCondition(STG stg) {
			this.stg = stg;
		}

		@Override
		public boolean fulfilled(P place) {
			SolverFactory factory = new SolverFactoryLpSolve();
			factory.setParameter(Solver.VERBOSE, 0); // we don't need messages from the solver
			factory.setParameter(Solver.TIMEOUT, 0); // no timeout
			
			for ( Node t : place.getChildren() ) {
				Transition postPlace = (Transition)t;
				
				// M_1 = M_N + C v_1 --> -MN = -M1 + C v_1
				// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem
				Problem problem = MarkingEquationCache.getMarkingEquation(stg);
				Linear linear;
				
				// M_1[postPlace> --> NOT!!! just because of "place"
				for (Node p : postPlace.getParents()) {
					int id = p.getIdentifier();
					linear = new Linear();
					linear.add(1, "M1" + id);
					if (p == place)
						problem.add(linear, "<=", postPlace.getParentValue(p)-1);
					else
						problem.add(linear, ">=", postPlace.getParentValue(p));
				}
				
				Result result = factory.get().solve(problem);
				
				if (result == null) // problem is infeasible
					continue; // i.e. place is definitely implicit w.r.t. transition "postPlace"
				else
					return false; // i.e. place is maybe not implicit
				
			}
			return true;
		}
		
	}
	
	protected static class LockedSignalCondition extends AbstractCondition<Integer> {
		
		protected STG stg;
		protected Integer signal1;
		protected List<Transition> signal1Transitions;

		public LockedSignalCondition(STG stg, Integer signal) {
			this.stg = stg;
			this.signal1 = signal;
			signal1Transitions = stg.getTransitions(getSignalOfCondition(signal1));
		}

		@Override
		public boolean fulfilled(Integer signal2) {
			// introduce new place "lock"
			Place lock = stg.addPlace("lockCheck", 0);
			
			// connect to all transitions of signal1 and signal2 in a proper way
			for ( Transition t : signal1Transitions )
				lock.setParentValue(t, 1);
			
			for ( Transition t : stg.getTransitions(getSignalOfCondition(signal2)) )
				lock.setChildValue(t, 1);
			
			if (getImplicitPlaceCondition(stg).fulfilled(lock) && 
					getSafePlaceCondition(stg).fulfilled(lock)) {
				stg.removePlace(lock);
				return true;
			}
			
			lock.setMarking(1); // check vice versa, ie. signal2 locked with signal1
			
			if (getImplicitPlaceCondition(stg).fulfilled(lock) && 
					getSafePlaceCondition(stg).fulfilled(lock)) {
				stg.removePlace(lock);
				return true;
			}		
			
			stg.removePlace(lock);
			return false;
		}
		
	}
	
	protected static class TransitionConcurrencyCondition<T extends Transition> extends AbstractCondition<T> {
		
		protected STG stg;
		protected T trans1;

		public TransitionConcurrencyCondition(STG stg, T t) {
			this.stg = stg;
			this.trans1 = t;
		}

		@Override
		public boolean fulfilled(T trans2) {
			if (trans1 == trans2) return false; // no concurrency
			
			SolverFactory factory = new SolverFactoryLpSolve();
			factory.setParameter(Solver.VERBOSE, 0); // we don't need messages from the solver
			factory.setParameter(Solver.TIMEOUT, 0); // no timeout
			
			// M_1 = M_N + C v_1 --> -MN = -M1 + C v_1
			// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem
			Problem problem = MarkingEquationCache.getMarkingEquation(stg);
						
			Linear linear;
						
			// M_1[trans1>
			for (Node place : trans1.getParents()) {
				int id = place.getIdentifier();
				linear = new Linear();
				linear.add(1, "M1" + id);
				problem.add(linear, ">=", trans1.getParentValue(place));
			}
			
			// M_1[trans2>
			for (Node place : trans2.getParents()) {
				int id = place.getIdentifier();
				linear = new Linear();
				linear.add(1, "M1" + id);
				problem.add(linear, ">=", trans2.getParentValue(place));
			}
			
			// to avoid a conflict between trans2 and trans1
			
			// M_1 - *trans1 + trans1* [trans2>
			for (Node place : trans2.getParents()) {
				int id = place.getIdentifier();
				linear = new Linear();
				linear.add(1, "M1" + id);
				problem.add(linear, ">=", 
						trans2.getParentValue(place) + trans1.getParentValue(place) - trans1.getChildValue(place));
			}
			
			// M_1 - *trans2 + trans2* [trans1>
			for (Node place : trans1.getParents()) {
				int id = place.getIdentifier();
				linear = new Linear();
				linear.add(1, "M1" + id);
				problem.add(linear, ">=", 
						trans1.getParentValue(place) + trans2.getParentValue(place) - trans2.getChildValue(place));
			}
						
			Result result = factory.get().solve(problem);
			
			if (result == null) // problem is infeasible
				return false; // i.e. trans1 and trans2 are definitely sequential
			else
				return true; // i.e. trans1 and trans2 are MAYBE concurrent
		}
		
	}
	
protected static class SignalConcurrencyCondition extends AbstractCondition<Integer> {
		
		protected STG stg;
		protected Integer signal1;

		public SignalConcurrencyCondition(STG stg, Integer signal) {
			this.stg = stg;
			this.signal1 = signal;
		}

		@Override
		public boolean fulfilled(Integer signal2) {
			if (signal1.equals(signal2)) return false; // no signal concurrency
			
			for (Transition trans1 : stg.getTransitions(getSignalOfCondition(signal1)) ) 
				for (Transition trans2 : stg.getTransitions(getSignalOfCondition(signal2)))
					if (getTransitionConcurrencyCondition(stg, trans1).fulfilled(trans2))
						return true;
			
			return false;
		}
		
	}
	
//	protected static class TriggerOf<T extends Transition> extends AbstractCondition<T> {
//	protected List<Signal> signals;
	
//	public TriggerOf(List<Signal> signals) {
//	this.signals = signals;
//	}
	
//	public boolean fulfilled(Transition transition) {
	
//	return false;
//	}
//	}
	
	protected static class EqualTo<O> extends AbstractCondition<O> {
		private O o;
		public EqualTo(O o) {
			this.o = o;            
		}
		public boolean fulfilled(O t) {
			return o.equals(t);
		}
	}
	
	
	/**
	 * Returns a Condition<Node> which is fulfilled when a node is adjacent to 'node'
	 * 
	 * @param node
	 */
	public static Condition<Node> getAdjacentToCondition(Node node) {
		MultiCondition<Node> result = new MultiCondition<Node>(
				MultiCondition.OR);
		
		result.addCondition(getParentOfCondition(node));
		result.addCondition(getChildOfCondition(node));
		
		return result;
	}
	
	/**
	 * @deprecated 
	 */
	public static Condition<Place> getPlaceLabelOf(String l) {
		return new PlaceLabelOf<Place>(l);
	}
	
	
	/**
	 * Returns a Condition<Node> which is fulfilled if  the node is incident 
	 * with an arc with a weight greater than 'max'
	 */
	public static Condition<Node> getArcWeightCondition(int max) {
		return new ArcWeight<Node>(max);
	}
	
	/**
	 * Returns a Condition<Node> which is fulfilled if a node is a child
	 * of 'node' 
	 */
	public static Condition<Node> getChildOfCondition(Node node) {
		return new ChildOf<Node>(node);
	}
	
	
	public static <O> Condition<O> getEqualTo(O o) {
		return new EqualTo<O>(o);
		
	}
	
	
	/**
	 * Returns a Condition<Transition> which is fulfilled if a transition is labelled equally modulo the signature
	 * as the paramter transition
	 * @param transition
	 * @return
	 */
	public static Condition<Transition> getTransitionLabelOfCondition(Transition transition) {
		return new TransitionLabelOf<Transition>(transition);
	}
	
	
	/**
	 * Returns a Condition<Transition> which is fulfilled if a Transition is 
	 * a duplicate of 'transition'
	 */
	public static Condition<Transition> getDuplicateTransitionCondition(
			Transition transition) {
		return new DuplicateTransition<Transition>(transition);
	}
	
	
	/**
	 * Returns a Condition<Transition> which is fulfilled if
	 * a transition is labelled with 'label'
	 */
	public static Condition<Transition> getSignalEdgeOfCondition(SignalEdge label) {
		return new SignalEdgeOf<Transition>(label);
	}
	
	
	/**
	 * Returns a Condition<Node> which is fulfilled if a node forms a 
	 * loop with 'node'
	 */
	public static Condition<Node> getLoopWithCondition(Node node) {
		MultiCondition<Node> result = new MultiCondition<Node>(
				MultiCondition.AND);
		
		result.addCondition(getParentOfCondition(node));
		result.addCondition(getChildOfCondition(node));
		return result;
	}
	
	
	
	
	
	/**
	 * Returns a Condition<Node> which is fulfilled if a node is 
	 * conatined by the list 'nodes'
	 */
	public static <N extends Node> Condition<N> getMultiNodesCondition(Collection<N> nodes) {
		return new MultiNodes<N>(nodes);
	}
	
	
	/**
	 * Returns a Condition<Node> which is fulfilled if a node is 
	 * equal to 'node'
	 */
	public static <N extends Node> Condition<N> getMultiNodesCondition(N node) {
		return new MultiNodes<N>(node);
	}
	
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for transition which 
	 * fulfils the 'no-backfiring' condition, see voka04 for further details
	 */
	//TODO optimize deep structure, flattening may increase performance
	
	public static class SecureContraction<T> extends AbstractCondition<Transition> {
		public boolean fulfilled(Transition transition) {
			
			// *** check for Secure Type 1 contraction
			List<Node> sibblingsAbove = STGOperations.getElements(
					transition.getParents(),
					ConditionFactory.getNumberOfChildrenCondition(1));
			//no splitting 'above' the transition
			//-> condition 1 fulfilled
			if (sibblingsAbove.isEmpty())
				return true;
			
			// *** check for Secure Type 2 contraction
			List<Node> sibblingsBelow = STGOperations.getElements(transition.getChildren(),
					ConditionFactory.getNumberOfParentsCondition(1));
			
			
			//when splitting 'below' the transition -> not secure
			if (! sibblingsBelow.isEmpty() )
				return false;
			
			//at least one unmarked place 'below' the transition must exists
			boolean unmarked = false;
			for (Node child : transition.getChildren())
				if (((Place) child).getMarking() == 0) {
					unmarked = true;
					break;
				}
			
			if (!unmarked) {
				return false;
			}
			
			if (CLW.instance.OD.isEnabled()) {
				return !isWeakSyntacticConflict(transition);
			}
			
			return true;
		}
		
		private boolean isWeakSyntacticConflict(Transition transition) {
			STG stg = transition.getSTG();
			
			Stack<Transition> stack = new Stack<Transition>();
			Set<Transition> seen = new HashSet<Transition>();
			
			for (Node parent : transition.getParents()) 
				for (Node child : parent.getChildren())
					if (child != transition)
						stack.push((Transition)child);
			
			while (!stack.isEmpty()) {
				Transition element = stack.pop();
				if (seen.add(transition)) {
					switch (stg.getSignature(element.getLabel().getSignal())) {
					case OUTPUT:
						return true;
					case DUMMY:
						for (Node place : element.getChildren()) 
							for (Node trans : place.getChildren())
								stack.push((Transition)trans);
					}
				}
			}
			
			return false;
		}
	}
	
	
	/**
	 * Returns a Cndition<Node> which is fulfilled for nodes with 
	 * at most 'max' children 
	 */
	public static Condition<Node> getNumberOfChildrenCondition(int max) {
		return new NumberOfChildren<Node>(max);
	}
	
	/**
	 * Returns a Condition<Node> which is fulfilled for nodes with 
	 * at most 'max' parents 
	 */
	public static Condition<Node> getNumberOfParentsCondition(int max) {
		return new NumberOfParents<Node>(max);
	}
	
	/**
	 * Returns a Condition<Node> which is fulfilled for nodes which are
	 * parents of 'node'
	 */
	public static Condition<Node> getParentOfCondition(Node node) {
		return getMultiNodesCondition(node.getParents());
	}
	
	public static Condition<Node> getMGPlaceBetweenCondition(Node source, Node target) {
		return new MGPlaceBetweenCondition<Node>(source, target);
	}
	
	public static Condition<Place> getSafePlaceCondition(STG stg) {
		return new SafePlaceCondition<Place>(stg);
	}
	
	/**
	 * Returns a Condition<Place> which is fulfilled if a place is redundant
	 * in 'stg'
	 * version = 0
	 * This Conditions finds drain-only-, loop-only-, extended-duplicate- and shortcut-places
	 * 
	 * version = 1
	 * The condition finds every redundat place by solving an LP problem 
	 * 
	 *  
	 */
	
	public static Condition<Place> getRedundantPlaceCondition(STG stg) {
		return new RedundantPlace<Place>(stg);
	}
	
	
	public static Condition<Place> getPlaceMarkingCondition(int marking) {
		return new PlaceMarking<Place>(marking);
	}
	
	public static Condition<Place> getImplicitPlaceCondition (STG stg) {
		return new ImplicitPlaceCondition<Place>(stg);
	}
	
	
	
	
	
	
	
	public static Condition<Transition> getRedundantTransitionCondition(STG stg) {
		return new RedundantTransition<Transition>(stg);
	}
	
	public static Condition<Transition> getSignalNameOfTransitionCondition(String signalName) {
		return new SignalNameOfTransition<Transition>(signalName);
	}
	
	public static Condition<Transition> getSignalNameOfTransitionCondition(Collection<String> signalNames) {
		return new SignalNameOfTransition<Transition>(signalNames);
	}
	
	
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for transitions
	 * which have a signal of 'signals'
	 */
	public static Condition<Transition> getSignalOfCondition(Collection<Integer> signals) {
		return new SignalOf<Transition>(signals);
	}
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for transitions which are 
	 * labelled with 'signal'
	 */
	public static Condition<Transition> getSignalOfCondition(Integer... signal) {
		return new SignalOf<Transition>(signal);
	}
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for transitions
	 * which have a signature of 'signatures'
	 */
	public static Condition<Transition> getSignatureOfCondition(List<Signature> signatures) {
		return new SignatureOf<Transition>(signatures);
	}
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for transtions which have the
	 * sigtnature 'signature'
	 */
	public static Condition<Transition> getSignatureOfCondition(Signature signature) {
		return new SignatureOf<Transition>(signature);
	}
	
	/**
	 * Returns a Condition<Node> which is fulfilled for nodes which are
	 * in structural conflict with 'node', i.e. they have a common parent
	 */
	public static <N extends Node> Condition<N> getStructuralConflictCondition(N node) {
		return new StructuralConflict<N>(node);
	}
	
	/**
	 * Returns a Condition<Integer> which is not fulfilled for two 
	 * sequential signals --> (I)LP is used implicitly through getTransitionConcurrencyCondition()
	 */
	public static Condition<Integer> getSignalConcurrencyCondition(STG stg, Integer signal) {
		return new SignalConcurrencyCondition(stg, signal);
	}
	
	/**
	 * Returns a Condition<Transition> which is not fulfilled for two sequential 
	 * transitions --> (I)LP is used
	 */
	public static <T extends Transition> Condition<T> getTransitionConcurrencyCondition(STG stg, T t) {
		return new TransitionConcurrencyCondition<T>(stg, t);
	}
	
	
	public static <T extends Node> Condition<T> getRelevantTransitionCondition(STG stgComponent) {
		return new RelevantTransition<T>(stgComponent);
	}
	
	public static <T extends Collection<STG>> Condition<T> getTooManyOutputsCondition(int outputLimit) {
		return new TooManyOutputs<T>(outputLimit);
	}
	
	public static <C extends Collection<STG>> Condition<C> getTooManySignalsCondition(int signalLimit) {
		return new TooManySignals<C>(signalLimit);
	}
	
	public static Condition<Integer> getLockedSignalCondition(STG stg, Integer signal) {
		return new LockedSignalCondition(stg, signal);
	}
	
	/**
	 * Returns a Condition<Transition> which is fulfilled for dummy transitions
	 * which dangle on the new places emerged through a transition contraction
	 */
	public static Condition<Transition> getDummiesAffectedByNewPlacesCondition(Collection<Place> newPlaces) {
		//return new DummiesAffectedByNewPlaces<Transition>(newPlaces);
		
		MultiCondition<Transition> result = new MultiCondition<Transition>(
				MultiCondition.OR);
		for (Place p : newPlaces) {
			result.addCondition(getParentOfCondition(p));
			result.addCondition(getChildOfCondition(p));
		}
		return result;
	}
	
	
	// *******************************************************************
	// The simple singleton conditions
	// *******************************************************************
	
	public static final Condition<Transition> 	ACTIVATED 				= new Activated<Transition>();
	
	public static final Condition<Node> 		ALL_NODES 				= new All<Node>();
	public static final Condition<Place> 		ALL_PLACES 				= new All<Place>();
	public static final Condition<Transition> 	ALL_TRANSITIONS 		= new All<Transition>();
	
	public static final Condition<Transition>	LOCAL_TRANSITIONS		= new LocalTransitions<Transition>();
	
	public static final Condition<Node> 		LOOP_NODE 				= new LoopNode<Node>();
	public static final Condition<Transition> 	LOOP_ONLY_TRANSITION	= new LoopOnlyTransition<Transition>();
	public static final Condition<Place> 		LOOP_ONLY_PLACE 		= new LoopOnlyPlace<Place>();
	
	public static final Condition<Transition> 	ARC_WEIGHT		 		= new ArcWeight<Transition>(1);
	
	public static final Condition<Place> 		MARKED_GRAPH_PLACE 		= new MarkedPlace<Place>();
	public static final Condition<Place>		SELF_TRIGGERING_PLACE	= new SelfTriggering<Place>();
	
	public static final Condition<Transition> 	NEW_AUTOCONFLICT_PAIR 	= new NewAutoConflictPair<Transition>();
	public static final Condition<Transition> 	SECURE_CONTRACTION 		= new SecureContraction<Transition>();
	
	public static final Condition<Node> 		SPLITTING_ABOVE 		= new SplittingAbove<Node>();
	public static final Condition<Node> 		SPLITTING_BELOW 		= new SplittingBelow<Node>();
	
		
	
	
	// *******************************************************************
	// The singleton multi conditions 
	// *******************************************************************
	
	
	/**
	 * {@link Condition}, which is fulfilled if a transition is structurally 
	 * contractable, security is not checked, neither if the transition is lambda-lablled. 
	 */
	public static final MultiCondition<Transition> CONTRACTABLE;
	static {
		CONTRACTABLE = new MultiCondition<Transition>(
				MultiCondition.AND);
		CONTRACTABLE.addCondition(new NotCondition<Transition>(
				new LoopNode<Transition>()));
		CONTRACTABLE.addCondition(new NotCondition<Transition>(
				new ArcWeight<Transition>(1)));
	}
	
	
	/**
	 * {@link Condition}, which is fulfilled if a transition is structurally 
	 * contractable, security is not checked, neither if the transition is lambda-lablled. 
	 */
	public static final Condition<Transition> SAFE_CONTRACTABLE;
	static {
		SAFE_CONTRACTABLE = new Condition<Transition>() {
			
			public boolean fulfilled(Transition o) {
				boolean struc = o.getParents().size()<=1 ||	
				o.getChildren().size()==1 && ((Place)o.getChildren().iterator().next()).getMarking() == 0;
				
				return	struc; 
			}
			
			public Object becauseOf() {
				return null;
			}
		};
	}
	
	
	
	
	protected static final MultiCondition<Transition> SECURE_CONTRACTABLE;
	static {
		SECURE_CONTRACTABLE = new MultiCondition<Transition>(
				MultiCondition.AND);
		
		SECURE_CONTRACTABLE.addCondition(getSignatureOfCondition(Signature.DUMMY));
		SECURE_CONTRACTABLE.addCondition(SECURE_CONTRACTION);
		SECURE_CONTRACTABLE.addCondition(CONTRACTABLE);        
	}
	
	
	
	public static MultiCondition<Transition> getContractableCondition(STG stg) {
		if (CLW.instance.SAFE_CONTRACTIONS.isEnabled()) {
			MultiCondition<Transition> result = new MultiCondition<Transition>(MultiCondition.AND);
			result.addCondition(SECURE_CONTRACTABLE);
			
			if (CLW.instance.SAFE_CONTRACTIONS_UNFOLDING.isEnabled() && (stg.getSize() <= CLW.instance.MAX_STG_SIZE_FOR_UNFOLDING.getIntValue()))
				result.addCondition(new SafeContraction<Transition>(stg));
			else	
				result.addCondition(SAFE_CONTRACTABLE);
			
			return result;
		}
		else {
			return SECURE_CONTRACTABLE;
		}
	}
}
