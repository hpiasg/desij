

package net.strongdesign.desij.decomposition.avoidconflicts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
// import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;

import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.tree.TreeDecomposition;
import net.strongdesign.desij.CLW;

// import net.strongdesign.util.FileSupport;
import net.strongdesign.util.Pair;


public class CAGeneral extends ComponentAnalyser {
	private Set<Integer> internals = null;
	
	private IPlaceHolderInsertionStrategy placeHolderInserter;
	public boolean iterativeConflictCheck = false;
	
	public CAGeneral(STG stg, Collection<STG> components, String filePrefix) {
		super(stg,components, filePrefix);
		
		// set concrete place insertion strategy
		if (CLW.instance.INSERTION_STRATEGY.getValue().equals("mg"))
			placeHolderInserter = new ShortcutPlaceHolderInsertion(stg, 
				components, additionalNodeInfos);
		else if (CLW.instance.INSERTION_STRATEGY.getValue().equals("general"))
			placeHolderInserter = new GeneralPlaceHolderInsertion(stg, // old: SequentialPlaceHolderInsertion(stg, 
					components, additionalNodeInfos);
	}
		
	
	@Override
	public boolean avoidIrrCSCConflicts() throws IOException, STGException {
		boolean withoutErrors = true;
		
		if (CLW.instance.CONFLICT_TYPE.getValue().endsWith("st"))
			for (List<Transition> selfTrigger: selfTriggers.keySet()) {
				Collection<STG> criticalComponents = selfTriggers.get(selfTrigger);
				if (!criticalComponents.isEmpty()) {
					java.util.Iterator<STG> compIterator = criticalComponents.iterator();
					STG currentComponent = compIterator.next(); 
					placeHolderInserter.initializeTraversal(currentComponent, selfTrigger);
					if (!placeHolderInserter.execute(selfTrigger.get(0), selfTrigger.get(1))) {
						withoutErrors = false;
						continue;
					}
					// insert placeholder according to myTraversalResult
					Transition insertedPlaceHolder = placeHolderInserter.doPlaceHolderInsertion(currentComponent);
					STGUtil.removeRedundantPlaces(this.stg); // in particular the self-trigger place is removed
					if (compIterator.hasNext()) 
						placeHolderInserter.doInsertionForSameConflict(compIterator, insertedPlaceHolder);
				}
//				for (STG comp: selfTriggers.get(selfTrigger)) { 
//					placeHolderInserter.initializeTraversal(comp, selfTrigger);
//					if (!placeHolderInserter.execute(selfTrigger.get(0), selfTrigger.get(1))) {
//						withoutErrors = false;
//						continue;
//					}
//					STGUtil.removeRedundantPlaces(this.stg); // in particular the self-trigger place is removed
//				}
			}
		else if (CLW.instance.CONFLICT_TYPE.getValue().equals("general"))
			for (Set<Pair<Transition,Transition>> irrCSCConflict: this.entryExitPairs2Components.keySet()) {
				Collection<STG> criticalComponents = this.entryExitPairs2Components.get(irrCSCConflict);
				if (!criticalComponents.isEmpty()) {
					java.util.Iterator<STG> compIterator = criticalComponents.iterator();
					STG currentComponent = compIterator.next(); 
					boolean conflictIsSolved = false;
					for (Pair<Transition,Transition> entryExit: irrCSCConflict) {
						List<Transition> ctp = new ArrayList<Transition>(2);
						ctp.add(entryExit.a);
						ctp.add(entryExit.b);
						this.placeHolderInserter.initializeTraversal(currentComponent, ctp);
						if (placeHolderInserter.execute(entryExit.a, entryExit.b)) {
							conflictIsSolved = true;
							break;
						}
					}
					if (!conflictIsSolved) {
						withoutErrors = false;
						continue;
					}
					// insert placeholder according to myTraversalResult
					Transition insertedPlaceHolder = placeHolderInserter.doPlaceHolderInsertion(currentComponent);
					STGUtil.removeRedundantPlaces(this.stg);
					if (compIterator.hasNext()) 
						placeHolderInserter.doInsertionForSameConflict(compIterator, insertedPlaceHolder);
				}
			}
//				for (STG comp: this.entryExitPairs2Components.get(irrCSCConflict)) {
//					boolean conflictIsSolved = false;
//					for (Pair<Transition,Transition> entryExit: irrCSCConflict) {
//						List<Transition> ctp = new ArrayList<Transition>(2);
//						ctp.add(entryExit.a);
//						ctp.add(entryExit.b);
//						this.placeHolderInserter.initializeTraversal(comp, ctp);
//						if (placeHolderInserter.execute(entryExit.a, entryExit.b)) {
//							conflictIsSolved = true;
//							break;
//						}
//					}
//					if (!conflictIsSolved) {
//						withoutErrors = false;
//						continue;
//					}
//					STGUtil.removeRedundantPlaces(this.stg);
//				}
		else
			return false; // wrong CONFLICT_TYPE parameter
		
		// temporarily for debugging
		// stg.showPS();
		// FileSupport.saveToDisk(STGFile.convertToG(stg), "NewSpecification_Debug.g");
		
		// transform internal signals in this.stg into explicit outputs
		hideInternals(false);
		
		//this.newInsertedSignals += placeHolderInserter.getInsertedPlaceholderTransitionCount();

		
		// recalculate the critical and delay components
		boolean recoveryInfo = CLW.instance.ALLOW_INCOMPLETE_PARTITION.isEnabled();
		
		if (!recoveryInfo) {
			CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(true);
		}
		
		Partition incompPart = placeHolderInserter.getNewPartition();
		
		Collection<STG> newComponents = null;
		if (!incompPart.getPartition().isEmpty()) {
			newComponents = 
				new TreeDecomposition(filePrefix).decompose(stg, incompPart); // maybe CSC-aware deco could be useful
			
			this.components.removeAll(placeHolderInserter.getReplacedComponents());
			this.components.addAll(newComponents);
		}
		
		// recovery of CLW parameter
		if (!recoveryInfo) {
			CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(false);
		}
		
		// hide some outputs of the components, i.e. 
		// change certain outputs back to internal signals
		hideInternals(true);
		
		if (iterativeConflictCheck && newComponents != null) 
			investigateSynthesisability(newComponents);
		
		this.newInsertedSignals += placeHolderInserter.getInsertedPlaceholderTransitionCount();
		
		return withoutErrors;
	}
		
	private void hideInternals(boolean hide) {
		if (hide) {
			if (internals != null) {
				// set internals back, but only those which are produced in a component 
				// and not used as input from another component
				for (STG component : this.components) {
					for (Integer signal : component.getSignals())
						if (component.getSignature(signal) == Signature.OUTPUT
								&& this.internals.contains(signal))
							component.setSignature(signal, Signature.INTERNAL);
				}
				// ... and for the specification STG, too
				for (Integer signal : this.stg.getSignals())
					if (this.stg.getSignature(signal) == Signature.OUTPUT &&
							this.internals.contains(signal))
						this.stg.setSignature(signal, Signature.INTERNAL);
			}
		}
		else  { // hide == false
			// memorize internal signals and change signature to output,
			// they will be set back to internal after decomposition
			internals = this.stg.collectUniqueCollectionFromTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.INTERNAL),
					CollectorFactory.getSignalNameCollector());
			// change internals to outputs
			stg.setSignature(internals, Signature.OUTPUT);
		}
	}
	
	private void investigateSynthesisability(Collection<STG> newComponents) throws STGException, IOException {
		
		boolean withoutErrors = false;
		int iteration = 1; // now we start the second iteration
		
		while (!withoutErrors && iteration < 10) {
			withoutErrors = true;
			++iteration;					
			Map<Set<Pair<Transition,Transition>>,Collection<STG>> entryExitPairs = 
				new HashMap<Set<Pair<Transition,Transition>>, Collection<STG>>();
			placeHolderInserter.initialzePartition();
			
			
			for (STG component: newComponents) {
				Collection<Set<Pair<Transition,Transition>>> irrCSCConflicts;
				try { // mpsat check
					irrCSCConflicts = 
						IrreducibleCSCConflictIdentifier.getAllIrreducibleCSCConflicts(component, true);
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage());
				}
				
				if (irrCSCConflicts.isEmpty())
					continue;
				
				// some output to console
				List<Signature> outputs = new LinkedList<Signature>();
				outputs.add(Signature.OUTPUT);
				outputs.add(Signature.INTERNAL);
				StringBuilder signalNames = new StringBuilder();

				for (Integer s : component.collectUniqueCollectionFromTransitions(ConditionFactory.getSignatureOfCondition(outputs), 
						CollectorFactory.getSignalCollector()))
					signalNames.append(component.getSignalName(s));
				
				System.out.println("Iteration " + iteration + "- Avoiding further conflicts in component: " + signalNames);
				
				for (Set<Pair<Transition,Transition>> irrCSCConflict : irrCSCConflicts) {
					Set<Pair<Transition,Transition>> projectedConflictPairs = 
						new HashSet<Pair<Transition,Transition>>(irrCSCConflicts.size());
					for (Pair<Transition,Transition> entryExit : irrCSCConflict) {
						Pair<Transition,Transition> newPair = new Pair<Transition, Transition>(
								this.stg.getTransition(entryExit.a.getIdentifier()),	// projection to specification 
								this.stg.getTransition(entryExit.b.getIdentifier()) );	// projection to specification
						Assert.assertNotNull(newPair.a);
						Assert.assertNotNull(newPair.b);
						projectedConflictPairs.add(newPair);
					}
					if (entryExitPairs.get(projectedConflictPairs) == null)
						entryExitPairs.put(projectedConflictPairs, new LinkedList<STG>());
					entryExitPairs.get(projectedConflictPairs).add(component);
				}
			}
			
			for (Set<Pair<Transition,Transition>> irrCSCConflict: entryExitPairs.keySet()) {
				Collection<STG> criticalComponents = entryExitPairs.get(irrCSCConflict);
				if (!criticalComponents.isEmpty()) {
					java.util.Iterator<STG> compIterator = criticalComponents.iterator();
					STG currentComponent = compIterator.next(); 
					boolean conflictIsSolved = false;
					for (Pair<Transition,Transition> entryExit: irrCSCConflict) {
						List<Transition> ctp = new ArrayList<Transition>(2);
						ctp.add(entryExit.a);
						ctp.add(entryExit.b);
						this.placeHolderInserter.initializeTraversal(currentComponent, ctp);
						if (placeHolderInserter.execute(entryExit.a, entryExit.b)) {
							conflictIsSolved = true;
							break;
						}
					}
					if (!conflictIsSolved) {
						withoutErrors = false;
						continue;
					}
					// insert placeholder according to myTraversalResult
					Transition insertedPlaceHolder = placeHolderInserter.doPlaceHolderInsertion(currentComponent);
					STGUtil.removeRedundantPlaces(this.stg);
					if (compIterator.hasNext()) 
						placeHolderInserter.doInsertionForSameConflict(compIterator, insertedPlaceHolder);
				}
			}
			
			hideInternals(false);
			
			boolean recoveryInfo = CLW.instance.ALLOW_INCOMPLETE_PARTITION.isEnabled();
			if (!recoveryInfo) {
				CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(true);
			}
			
			Partition incompPart = placeHolderInserter.getNewPartition();
			
			if (!incompPart.getPartition().isEmpty()) {
				newComponents = 
					new TreeDecomposition(filePrefix).decompose(stg, incompPart); // maybe CSC-aware deco could be useful
				
				this.components.removeAll(placeHolderInserter.getReplacedComponents());
				this.components.addAll(newComponents);
			}
			
			// recovery of CLW parameter
			if (!recoveryInfo) {
				CLW.instance.ALLOW_INCOMPLETE_PARTITION.setEnabled(false);
			}
			
			// hide some outputs of the components, i.e. 
			// change certain outputs back to internal signals
			hideInternals(true);
		}
	}
	
}

