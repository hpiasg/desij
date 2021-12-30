

package net.strongdesign.desij.decomposition.avoidconflicts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.strongdesign.desij.CLW;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

import net.strongdesign.util.Pair;

/**
 * @author dwist
 *
 */
public abstract class ComponentAnalyser {
//	private PlaceHolderRefiner pHrefiner = new ToggleTransitionRefiner(); // works with petrify but not punf&mpsat
	private PlaceHolderRefiner pHrefiner = new ToggleNetRefiner();
	// 4 phase HS refinement leads to bad performance and large implementations!
//	private PlaceHolderRefiner pHrefiner = new FourPhaseHSRefiner(); // only for CAAvoidRecalculation
	
	
	protected STG stg;
	protected Collection<STG> components;
	protected String filePrefix; // for a second decomposition pass
	protected int newInsertedSignals = 0;
	
	// mapping: self-trigger -> Set_of_components where they arise
	protected Map<List<Transition>,Collection<STG>> selfTriggers = 
		new HashMap<List<Transition>,Collection<STG>>();
	// mapping: Set of (entry,exit)-pairs -> Set of components where these transition pair sequences arise
	protected Map<Set<Pair<Transition,Transition>>,Collection<STG>> entryExitPairs2Components = 
		new HashMap<Set<Pair<Transition,Transition>>, Collection<STG>>();
	
	// necessary for the traversal 
	protected Map<Node,NodeProperty> additionalNodeInfos;
	
	
	protected ComponentAnalyser() {
		super();
		// do nothing! 
		// invoked by derived classes from other packages  
		// that specify their own constructor
	}
	
	public ComponentAnalyser(STG stg, Collection<STG> components, String filePrefix) {
		this.stg = stg;
		this.components = components;
		this.filePrefix = filePrefix;
		
		// some information for the traversal, e.g. that we do not 
		// catch in graph cycles during the traversal
		additionalNodeInfos = new HashMap<Node,NodeProperty>();
		for (Node node: stg.getNodes()) {
			additionalNodeInfos.put(node, new NodeProperty());
		}
	}
	
	public int getNewInternals() {
		if (pHrefiner instanceof ToggleTransitionRefiner || pHrefiner instanceof ToggleNetRefiner)
			return this.newInsertedSignals;
		else if (pHrefiner instanceof FourPhaseHSRefiner)
			return 2 * this.newInsertedSignals;
		else 
			return -99999999; // wrong value --> so many signals will never be inserted
	}
	
	
	/**
	 * identifies all syntactical input self-triggers in the components
	 * and build the data structure selfTriggers!
	 * @param selfTriggersOnly - general CSC conflicts or selfTriggers only
	 * @return - successful or not?
	 */
	public boolean identifyIrrCSCConflicts(boolean selfTriggersOnly) {
		if (selfTriggersOnly) {
			List<Transition> selfTrigger;
			for (STG component: components) { 
				for (Transition t1: component.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
					for (Node place: t1.getChildren()) {
						for (Node t2: place.getChildren()) {
							if (t2 instanceof Transition) {
								if (t1 != t2 && 
										((Transition)t2).getLabel().getSignal() == t1.getLabel().getSignal() &&
										(component.getSignature(((Transition)t1).getLabel().getSignal()) == Signature.INPUT) &&
										(component.getSignature(((Transition)t2).getLabel().getSignal()) == Signature.INPUT)
								) {
									if (CLW.instance.CONFLICT_TYPE.getValue().equals("st")) {
										// Don't put any pure structural self-trigger in the seflTriggers data structure
										if (!IrreducibleCSCConflictIdentifier.isDynamicSelfTrigger(t1,(Transition)t2))
											continue;
									}
									selfTrigger = new ArrayList<Transition>(2);
									selfTrigger.add(stg.getTransition(t1.getIdentifier()));
									selfTrigger.add(stg.getTransition(t2.getIdentifier()));
									if (selfTriggers.get(selfTrigger) == null) {
										selfTriggers.put(selfTrigger, new ArrayList<STG>());
									}
									selfTriggers.get(selfTrigger).add(component);
								}
							}
						}
					}
				}
			}
		}
		else { // general, (dynamic) irreducible CSC conflicts
			for (STG component: this.components) {
				Collection<Set<Pair<Transition,Transition>>> irrCSCConflicts;
				try { // so far, look just for structural general conflicts 
					irrCSCConflicts = IrreducibleCSCConflictIdentifier.getAllIrreducibleCSCConflicts(component, false);
				} catch (IOException e) {
					return false;
				}

				for (Set<Pair<Transition,Transition>> irrCSCConflict : irrCSCConflicts) {
					Set<Pair<Transition,Transition>> projectedConflictPairs = 
						new HashSet<Pair<Transition,Transition>>(irrCSCConflicts.size());
					for (Pair<Transition,Transition> entryExit : irrCSCConflict) {
						Pair<Transition,Transition> newPair = new Pair<Transition, Transition>(
								this.stg.getTransition(entryExit.a.getIdentifier()),	// projection to specification 
								this.stg.getTransition(entryExit.b.getIdentifier()) );	// projection to specification
						if ( (newPair.a == null) || (newPair.b == null) ) return false;
						projectedConflictPairs.add(newPair);
					}
					if (this.entryExitPairs2Components.get(projectedConflictPairs) == null)
						this.entryExitPairs2Components.put(projectedConflictPairs, new ArrayList<STG>());
					this.entryExitPairs2Components.get(projectedConflictPairs).add(component);
				}
			}
		}
		return true;
	}
	
	/**
	 * Shows the STG (with viewer) such that the interesting conflicts are highlighted.
	 */
	public void showWithConflicts() {
		if (selfTriggers.isEmpty() && entryExitPairs2Components.isEmpty()) {
			identifyIrrCSCConflicts(true);
		}
		
		Map<STG,Collection<Node>> stg2CritTrans = new HashMap<STG,Collection<Node>>();
		
		Set<Node> critTransitions = new HashSet<Node>();
		
		if (!entryExitPairs2Components.isEmpty()) {
			for (Set<Pair<Transition,Transition>> irrCSCConflict : entryExitPairs2Components.keySet())
				for (Pair<Transition,Transition> entryExit : irrCSCConflict) {
					critTransitions.add(entryExit.a);
					critTransitions.add(entryExit.b);
				}
			stg2CritTrans.put(this.stg, critTransitions);
			
			
			for (STG comp: this.components) {
				critTransitions = new HashSet<Node>();
				for (Set<Pair<Transition,Transition>> irrCSCConflict : entryExitPairs2Components.keySet()) 
					for (STG oldComp: entryExitPairs2Components.get(irrCSCConflict)) {
						if ( comp.getSignals(Signature.OUTPUT).containsAll(
								oldComp.getSignals(Signature.OUTPUT)) ) {
							for (Pair<Transition,Transition> entryExit: irrCSCConflict) {
								critTransitions.add(entryExit.a);
								critTransitions.add(entryExit.b);
							}
							break;
						}
					}
				stg2CritTrans.put(comp, critTransitions);
			}
					
		}
		else {
			for (List<Transition> selfTrigger: selfTriggers.keySet()) {
				critTransitions.addAll(selfTrigger);
			}
			stg2CritTrans.put(this.stg, critTransitions);
			
			for (STG comp: this.components) {
				critTransitions = new HashSet<Node>();
				for (List<Transition> selfTrigger : selfTriggers.keySet()) {
					for (STG oldComp: selfTriggers.get(selfTrigger)) {
						// such a complex condition is necessary since it is possible that
						// the components are new objects due to a second decomposition pass
						if ( comp.getSignals(Signature.OUTPUT).containsAll
								(oldComp.getSignals(Signature.OUTPUT)) ) {
							critTransitions.addAll(selfTrigger);
							break;
						}
					}
				}
				stg2CritTrans.put(comp, critTransitions);
			}
		}
		
				
		for (STG stg2show: stg2CritTrans.keySet()) {
			stg2show.showPS(stg2CritTrans.get(stg2show));
		}
			
	}
	
	public abstract boolean avoidIrrCSCConflicts() throws IOException, STGException;
	
	
	public void refinePlaceHolderTransitions(STG stg) throws STGException {
		pHrefiner.execute(stg);
	}
	
}
