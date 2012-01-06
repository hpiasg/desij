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

package net.strongdesign.desij.decomposition.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.DecompositionEvent;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.statesystem.StateSystemException;
import net.strongdesign.statesystem.StateSystems;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Marking;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGAdapterFactory;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;
import net.strongdesign.util.PresetTree;
import net.strongdesign.util.StreamGobbler;

public class IrrCscAwareDecomposition extends AbstractTreeDecomposition {

	public IrrCscAwareDecomposition(String filePrefix) {
		super(filePrefix);
	}



	/**
	 * Performs decomposition according to a given decomposition tree.
	 * 
	 * @param decoPara decomposition parameter
	 * @param tree The decomposition tree
	 * @param components the resulting components
	 */

	protected void decomposeTree(
			STG stg, 
			PresetTree<Integer, Collection<Integer>> tree, 
			Collection<STG> components) 
	throws STGException, IOException {

		/*
		 * General outline of the implementation
		 * -------------------------------------
		 * 
		 * We start at the root of the preset tree, the current node is stored in (guess what)
		 * currentNode. This tree is traversed not with recursion but in an infinite loop (traversal).
		 * In order to traverse each node exactly once (only incoming from the parent counts) we keep 
		 * track of the actual child of each node which has to be entered next in a stack of iterators
		 * (nextNodeStack).
		 * 
		 * When a node is entered for the first time the following operation are performed:
		 */


		
// ******************************************************************************
// Preparation
// ******************************************************************************

		
		//current position in the decompositon tree
		PresetTree<Integer, Collection<Integer>> currentNode = tree;

		//stores the task concerning csc solving which have to be performed in the respective nodes
		Map<PresetTree<Integer, Collection<Integer>>, Set<SolveCSC>> nodeTasks = 
			new HashMap<PresetTree<Integer, Collection<Integer>>, Set<SolveCSC>>();


		//the stack which stores the iterators for the next child to be entered
		//instead of using a stack of iterators a queue is used to avoid a
		//ConcurrentModificationException when deleting subtrees during component aggregation
		//The additional effort is linear in the size of the tree,
		Stack<Queue<PresetTree<Integer, Collection<Integer>>>> nextNodeStack = 
			new Stack<Queue<PresetTree<Integer, Collection<Integer>>>>(); 

		//in which direction was a node entered?
		//true if and only if the node is entered for the first time coming from the parent/root
		boolean down = true;


		
		
		
// ******************************************************************************
// Main loop
// ******************************************************************************
	
		traversal: while (true) {
	
			//indicates if we are in leaf, needed to make the right decision in the "up or down" section below
			//it can happen that we store an iterator of the subtrees of a node but aggregate this very node
			//and the iterator
			boolean leaf = false;
			if (down) {
				// remember the subtrees which where not finished yet
				nextNodeStack.push( new LinkedList<PresetTree<Integer, Collection<Integer>>>(currentNode.getSubtrees() ));
				
				// decompose the current node
				decomposeNode(stg, currentNode);
				
				// perform component aggregation and build components after checking CSC
				leaf = buildComponent(stg, components, currentNode, nodeTasks);
			}
			else {			
				//perform the CSC solving, i.e. take care of the still unfinished inverse projections 
				handleTasks(stg, components, currentNode, nodeTasks);
			}


			//where is the next node? up or down??
			Queue<PresetTree<Integer, Collection<Integer>>> curNextSubtrees = nextNodeStack.peek();
			
			if (!leaf && !curNextSubtrees.isEmpty()) {
				PresetTree<Integer, Collection<Integer>> child = curNextSubtrees.poll();
				child.setParent(currentNode);
				currentNode = child;
				down = true;
			}
			else {
				//otherwise, go one node up, update the stack and the STG
				currentNode = currentNode.getParent();
				nextNodeStack.pop();
				down = false;
				stg.undoToMarker(UndoMarker.ENTERED_NODE);
			}
			
			if (currentNode == null)
				break traversal;
			
			
			
		}
	}



	private void handleTasks(
			STG stg, 
			Collection<STG> components, 
			PresetTree<Integer, Collection<Integer>> currentNode, 
			Map<PresetTree<Integer, Collection<Integer>>, Set<SolveCSC>> nodeTasks) 
	throws STGException, IOException {
		
		
		Set<SolveCSC> tasks = nodeTasks.get(currentNode);
		if (tasks != null) {
			
			//don't handle it over and over and over and over ... (insert long time here) ... and over again
			nodeTasks.remove(currentNode);
			tasks: for (SolveCSC solve : tasks) {
				
			//the current task was moved up enough -> because of irreducible CSC no synthesis so far -> wait for internal communication
				if (solve.level > CLW.instance.CSC_BACKTRACKING_LEVEL.getIntValue()) {
					logging(stg, DecompositionEvent.CSC_LEVEL_EXCEEDED, solve.level);
					logging(stg, DecompositionEvent.FINISHED, null);

					//XXX solve csc externally
					System.out.print("cloning ...");
					STG component = stg.clone();
					System.out.println(" finished");
					updateSignature(solve.outputs, component);						
					components.add(component);
					continue tasks;
				}

				
				//otherwise proceed with this task
				
				
				//perform inverse projection for the task in this node/component
				SolveCSC newSolve = inverseTraceProjection(solve, stg);
				
				
				if (newSolve.violationTraces.isEmpty()) {
					logging(stg, DecompositionEvent.CSC_SOLVED, null);
					//CSC was solved, but maybe we have new conflicts -> check for CSC again   		
					List<Pair<List<SignalEdge>, List<SignalEdge>>> violationTraces = getIrrCSCViolationTraces(stg);

					if ( violationTraces.isEmpty()) {
						//CSC was solved
						logging(stg, DecompositionEvent.FINISHED, null);

						//try to conract non destroying signals again
						
						//only signals which were delambdarised but did not solve CSC are
						//candidates for repeated contraction
						solve.delambdarisedSignals.removeAll(solve.coreDestroyingSignals);
						
						int nroDelambda = solve.delambdarisedSignals.size();
						boolean recontract = CLW.instance.TRY_RECONTRACT.isEnabled() && nroDelambda > 0;
						
						if (recontract) {								
							logging(stg, DecompositionEvent.CSC_RECONTRACT, solve.delambdarisedSignals);

							stg.addUndoMarker(UndoMarker.FINAL_REDUCTION);

							int nroSignals = stg.getSignals().size();
							//Lambdarise these signals
							stg.setSignature(solve.delambdarisedSignals, Signature.DUMMY);
							

							STGInOutParameter stgParam = new STGInOutParameter(stg);
							new BasicDecomposition(filePrefix).reduce(stgParam);
							// stg = stgParam.stg; // not necessary for BasicDeco!
							
							int nroContracted = nroSignals - stg.getSignals().size();
							logging(stg, DecompositionEvent.CSC_FINAL_REDUCTION, nroContracted + " of " + nroDelambda + " signals removed.");
						}
						

						STG component = stg.clone();
						
						if (recontract)
							stg.undoToMarker(UndoMarker.FINAL_REDUCTION);
						
						
						updateSignature(solve.outputs, component);						
						components.add(component);
						continue tasks;

					}
					else {
						//no CSC -> add task for handling above
						logging(stg, DecompositionEvent.CSC_NEW_CONFLICT, violationTraces);
						
						solve.delambdarisedSignals.addAll(currentNode.getValue());
						newSolve = new SolveCSC(
								violationTraces, 
								stg.getSignals(), 
								newSolve.outputs, 
								newSolve.level+1,
								solve.delambdarisedSignals);

					}
				}

				//!! newSolve may have been changed above, therefore a simple else is not sufficient
				//csc was not solved -> add new task to parent node
				if (!newSolve.violationTraces.isEmpty()) {
					PresetTree<Integer, Collection<Integer>> parent = currentNode.getParent();
					if (parent == null) {
						//oh-oh, we reached the root and csc was not solved
						logging(stg, DecompositionEvent.CSC_REACHED_ROOT, null);
						logging(stg, DecompositionEvent.FINISHED, null);
						
						
						//XXX solve csc externally
						STG component = stg.clone();						
						updateSignature(solve.outputs, component);						
						components.add(component); // never synthesisable -> deal with it later						
						continue tasks;							
					}
					else {
						//add the task in the parent node
						Set<SolveCSC> parentTasks = nodeTasks.get(parent);
						if (parentTasks == null) {
							parentTasks = new HashSet<SolveCSC>();
							nodeTasks.put(parent, parentTasks);
						}

						parentTasks.add(newSolve);
					}
				}
			}
		}
	}



	private boolean buildComponent(STG stg, Collection<STG> components, PresetTree<Integer, Collection<Integer>> currentNode, Map<PresetTree<Integer, Collection<Integer>>, Set<SolveCSC>> nodeTasks) throws IOException {
		boolean leaf;
		//component aggregation
		if (CLW.instance.AGGREGATION.isEnabled())
			if (! currentNode.getSubtrees().isEmpty() && stg.getSignals().size() <= 
				CLW.instance.MAX_COMPONENT_SIZE.getDoubleValue()) {
				aggregateSubtree(currentNode);
				logging(stg, DecompositionEvent.AGGR_AGGRD_TREE, null);
			}

		//no childs? yes -> add to components
		leaf = false;
		if (currentNode.getSubtrees().isEmpty()) {
			leaf = true;

			//update signature, will be undone when going up, important for other components
			updateSignature(currentNode.getAdditionalValue(), stg);
			
			//resulting component
			STG component = null;

			//check for IRREDUCIBLE CSC    		
			List<Pair<List<SignalEdge>, List<SignalEdge>>> violationTraces = getIrrCSCViolationTraces(stg);
			
			if ( violationTraces.isEmpty() ) {
				logging(stg, DecompositionEvent.TREE_FINISHED_LEAF, stg.getSignals(Signature.OUTPUT));

				//preserve the result
				component = stg.clone();						
				updateSignature(currentNode.getAdditionalValue(), component);						
				components.add(component);
			}
			else if ( CLW.instance.CSC_BACKTRACKING_LEVEL.getIntValue()<1 ) { // there are (or might be) irreducible CSC conflicts --> synthesis impossible
				logging(stg, DecompositionEvent.TREE_FINISHED_LEAF, stg.getSignals(Signature.OUTPUT));

				//preserve the result
				component = stg.clone();						
				updateSignature(currentNode.getAdditionalValue(), component);						
				components.add(component);
				
				// throw new SynthesisException("irreducible CSC conflict", component);
				// leave the irreducible conflict inside --> later it can be dealt with by using internal communication
			}
			
			else {
				//irreducible CSC conflicts -> add task for handling below
				logging(stg, DecompositionEvent.CSC_IREDUCIBLE, violationTraces);

				
				PresetTree<Integer, Collection<Integer>> tParent = currentNode.getParent();
				//tParent==null means that the root is itself a leaf, should not happen, but alas!
				if (tParent != null) {
					Set<SolveCSC> tasks = nodeTasks.get(tParent);
					if (tasks == null) {
						tasks = new LinkedHashSet<SolveCSC>();
						nodeTasks.put(tParent, tasks);
					}
					Set<Integer> componentSignals = new HashSet<Integer>(stg.getSignals());
					if (CLW.instance.OD.isEnabled()) { // remove all dummies from componentSignals, because they are not in violationTraces
						componentSignals.removeAll(stg.getSignals(Signature.DUMMY));
					}
					tasks.add(new SolveCSC(
							violationTraces, 
							componentSignals, 
							new HashSet<Integer>(currentNode.getAdditionalValue()), 
							0, 
							new HashSet<Integer>(currentNode.getValue()) ));
				}						

			}					
		}
		return leaf;
	}



	private void decomposeNode(STG stg, PresetTree<Integer, Collection<Integer>> currentNode) throws STGException {
		
		logging(stg, DecompositionEvent.TREE_NEW_NODE, currentNode.getValue());

		
		//prepare undoing when going up
		stg.addUndoMarker(UndoMarker.ENTERED_NODE);
		
		//Lambdarise signals and decompose them
		stg.setSignature(currentNode.getValue(), Signature.DUMMY);

		STGInOutParameter stgParam = new STGInOutParameter(stg);
		List<Transition> dummies = new BasicDecomposition(filePrefix, specification).reduce(stgParam);
		// stg = stgParam.stg; // not necessary for BasicDeco!

		
		//check for non-contractable signals //XXX easier now
		Collection<Integer> nonContractable = stg.collectUniqueCollectionFromTransitions(
				ConditionFactory.getSignalOfCondition(currentNode.getValue()), 
				CollectorFactory.getSignalCollector());
		
		for (Transition d : dummies) {
			nonContractable.remove(d.getLabel().getSignal());
		}

		//if there are any add them to descandent nodes
		if (nonContractable.size() != 0) {
			logging(stg, DecompositionEvent.TREE_SIGNAL_POSTPONED, nonContractable);
			for (PresetTree<Integer, Collection<Integer>> child : currentNode.getSubtrees()) {
				child.getValue().addAll(nonContractable);
			}
		}
	}


	

	/**
	 * Computes the inverse projection for one {@link SolveCSC}-task.
	 * @param currentComponent The component in which the inv. projection is computed
	 * @param solve Contains the necessary parameters.
	 * @param decoPara
	 * @return
	 * @throws STGException
	 */
	private SolveCSC inverseTraceProjection(
				SolveCSC solve,		
				STG stg) throws STGException {

		//the inverse projection of all violation traces
		//they are stored if CSC cannot be solved in this iteration
		List<Pair<List<SignalEdge>, List<SignalEdge>>> newTraces = new LinkedList<Pair<List<SignalEdge>, List<SignalEdge>>>();

	

		//calculate the proper EventCondition
		//only  signals which are in currentComponent but not in the STG after contraction
		//are allowed between signals of the trace
		Set<Integer> sigTreeUpper = new HashSet<Integer>(stg.getSignals());
		Collection<Integer> sigTreeLower = solve.componentSignals;
		sigTreeUpper.removeAll(sigTreeLower);
		SignalEventCondition ec = new SignalEventCondition(sigTreeUpper);
		

		//consider each trace pair
		
		
		for (Pair<List<SignalEdge>, List<SignalEdge>> vt : solve.violationTraces) {
			
			//build the inverse projections
			List<SignalEdge> inverseProjectionA = null;
			List<SignalEdge> inverseProjectionB = null;

			try {
				StateSystem<Marking, SignalEdge> stateSystemAdapter = STGAdapterFactory.getStateSystemAdapter(stg);
				inverseProjectionA = StateSystems.inverseProjection(stateSystemAdapter, vt.a, ec);
				inverseProjectionB = StateSystems.inverseProjection(stateSystemAdapter, vt.b, ec);
				if (CLW.instance.OD.isEnabled()) {
					removeDummiesFromTrace(stg, inverseProjectionA);
					removeDummiesFromTrace(stg, inverseProjectionB);
				}
			} catch (StateSystemException e) {
				throw new STGException("Error during inverse Projection: "+e.getMessage());					
			}

			//build state vectors
			Map<Integer, Integer> codeChangeA = getCodeChange(inverseProjectionA, true);
			Map<Integer, Integer> codeChangeB = getCodeChange(inverseProjectionB, true);


			//store it for possible next iteration
			if (codeChangeA.equals(codeChangeB)) {
				newTraces.add(Pair.getPair(inverseProjectionA, inverseProjectionB));				
			}
			//conflict destroyed, store signals
			else {
				solve.coreDestroyingSignals.addAll(difference(codeChangeA, codeChangeB));				
			}
		}

		//CHECK if reusing old task possible
		
		Set<Integer> componentSignals = new HashSet<Integer>(stg.getSignals());
		if (CLW.instance.OD.isEnabled()) { // remove all dummies from componentSignals, because they are not in violationTraces
			componentSignals.removeAll(stg.getSignals(Signature.DUMMY));
		}
		return new SolveCSC( 
				newTraces, 
				componentSignals,
				solve.outputs, 
				solve.level+1, 
				solve.delambdarisedSignals, 
				solve.coreDestroyingSignals) ;
	}
	
	
	private void removeDummiesFromTrace(STG stg, List<SignalEdge> trace) {
		List<SignalEdge> subjectToDelete = new LinkedList<SignalEdge>();
		for (SignalEdge e : trace) {
			if (stg.getSignature(e.getSignal()) == Signature.DUMMY)
				subjectToDelete.add(e);
		}
		
		if (!subjectToDelete.isEmpty())
			trace.removeAll(subjectToDelete);
	}

	

	/**
	 * Returns all signals which have a different codechange
	 * @param codeChangeA
	 * @param codeChangeB
	 * @return
	 */
	protected Collection<? extends Integer> difference(Map<Integer, Integer> codeChangeA, Map<Integer, Integer> codeChangeB) {
		Set<Integer> result = new HashSet<Integer>();
		
		for (Integer sig : codeChangeA.keySet()) {
			Integer other = codeChangeB.get(sig);
			if (sig == null || !codeChangeA.get(sig).equals(other))
				result.add(sig);
		}

		for (Integer sig : codeChangeB.keySet()) {
			if (!result.contains(sig)) {
				Integer other = codeChangeA.get(sig);
				if (sig == null || !codeChangeB.get(sig).equals(other))
					result.add(sig);
			}
		}
		
		return result;
	}




	protected class SignalEventCondition implements StateSystems.EventCondition<SignalEdge> {
		private Set<Integer> signals;

		public SignalEventCondition(Collection<Integer> signals) {
			this.signals = new HashSet<Integer>(signals);
		}

		public boolean fulfilled(SignalEdge event) {
			return signals.contains(event.getSignal());
		}

		public String toString() {
			return signals.toString();
		}
	}


//	/**
//	 * Tries to solve CSC for currentComponent by going upwards in the decomposition tree and finding formerly contracted signals.
//	 * @param currentComponent
//	 * @param tree
//	 * @param violationTraces
//	 * @param decoPara 
//	 * @return
//	 * @throws STGException 
//	 * @throws StateSystemException 
//	 * @deprecated
//	 */
//	protected STG solveCsc(	STG currentComponent, 
//			PresetTree<String, Collection<String>> tree, 
//			List<Pair<List<SignalEdge>, List<SignalEdge>>> violationTraces, DecompositionParameter decoPara) throws STGException {
//
//
//		//indicates during the following loop if CSC was solved
//		boolean cscSolved;
//
//		//the signals of the component which is deeper in the tree
//		//initially the signals of the component which is associated with a leaf
//		Set<String> sigAfterDeco = currentComponent.getSignals();
//
//		csc: do {
//			//build the signal event condition -- only contracted signals are valid between signals of the trace
//			currentComponent.undoToMarker(UndoMarker.ENTERED_NODE);
//			logging(stg, DecompositionEvent.CSC_UP, null);
//
//			//go one step to the root and fetch the signals
//			//sigAfterDeco should be a proper subset of sigBeforeDeco
//			Set<String> sigBeforeDeco = currentComponent.getSignals();
//			sigBeforeDeco.removeAll(sigAfterDeco);
//			SignalEventCondition ec = new SignalEventCondition(sigBeforeDeco);
//
//			//hope the best
//			cscSolved = true;
//
//			//the inverse projection of all vioalation traces
//			//they are stored if CSC cannot be solved in this iteration
//			List<Pair<List<SignalEdge>, List<SignalEdge>>> newTraces = new LinkedList<Pair<List<SignalEdge>, List<SignalEdge>>>();
//
//			//consider each trace pair
//			check: for (Pair<List<SignalEdge>, List<SignalEdge>> vt : violationTraces) {
//				//build the inverse projections
//				List<SignalEdge> inverseProjectionA = null;
//				List<SignalEdge> inverseProjectionB = null;
//
//				try {
//					StateSystem<Marking, SignalEdge> stateSystemAdapter = STGAdapterFactory.getStateSystemAdapter(currentComponent);
//					inverseProjectionA = StateSystems.inverseProjection(stateSystemAdapter, vt.a, ec);
//					inverseProjectionB = StateSystems.inverseProjection(stateSystemAdapter, vt.b, ec);
//				} catch (StateSystemException e) {
//					throw new STGException("Error during inverse Projection: "+e.getMessage());					
//				}
//
//				//store it for possible next iteration
//				newTraces.add(Pair.getPair(inverseProjectionA, inverseProjectionB));
//
//				//build state vectors
//				Map<String, Integer> codeChangeA = getCodeChange(inverseProjectionA, true);
//				Map<String, Integer> codeChangeB = getCodeChange(inverseProjectionB, true);
//
//				//check if CSC was solved
//				//we cannot abort if cscSolved becomes false because all inverse projections must be build
//				//for the next iteration
//				cscSolved = cscSolved && codeChangeA.equals(codeChangeB);
//			}
//
//			if (cscSolved)
//				logging(stg, DecompositionEvent.CSC_SOLVED, null);
//			else
//				logging(stg, DecompositionEvent.CSC_NOT_SOLVED, null);
//
//
////			//prepare next iteration
////			//go up in the decomposition tree
////			tree = tree.getParent();
////			if (tree == null) {
////			logging(decoPara, DecompositionEvent.CSC_REACHED_ROOT, null);
////			break csc;
////			}
//
////			sigAfterDeco = sigBeforeDeco;
////			violationTraces = newTraces;
//
//		} 
//		while (false && !cscSolved);
//
//		//return the final component with CSC
//		return currentComponent.clone();
//
//	}









	/**
	 * Extract the irreducible CSC violation traces from an STG. Uses punf and mpsat, see above.
	 * @param stg
	 * @return
	 * @throws IOException
	 */
	private List<Pair<List<SignalEdge>, List<SignalEdge>>> getIrrCSCViolationTraces(STG stg) throws IOException {
		//where the STG is saved
		File tmpSTG = File.createTempFile("desij", ".g");

		//where the unfolding is saved
		File tmpUNF = File.createTempFile("desij", ".unf");

		//where the CSC violating traces are saved
		File tmpCONF = File.createTempFile("desij", ".conf");

		//save the STG, generate the unfolding and extract CSC violating traces
		FileSupport.saveToDisk(STGFile.convertToG(stg, false), tmpSTG.getCanonicalPath());

		try {
			
			Process punf = HelperApplications.startExternalTool(HelperApplications.PUNF, 
					" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
					HelperApplications.SECTION_START+tmpUNF.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpSTG.getCanonicalPath()+HelperApplications.SECTION_END );
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(punf.getInputStream(), "punf", System.out);
				StreamGobbler.createGobbler(punf.getErrorStream(), "punf", System.err);
			}
			punf.waitFor();
			punf.getErrorStream().close();
			punf.getInputStream().close();
			punf.getOutputStream().close();
			

			Process mpsat = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -C -a " +
					HelperApplications.SECTION_START+tmpUNF.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpCONF.getCanonicalPath()+HelperApplications.SECTION_END );
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(mpsat.getInputStream(), "mpsat", System.out);
				StreamGobbler.createGobbler(mpsat.getErrorStream(), "mpsat", System.err);
			}
			mpsat.waitFor();
			mpsat.getErrorStream().close();
			mpsat.getInputStream().close();
			mpsat.getOutputStream().close();
			
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving punf/mpsat.");
		}

		//parse the conflict traces
		String conflicts = FileSupport.loadFileFromDisk(tmpCONF.getCanonicalPath());
		

		//no CSC conflict
		if (conflicts.startsWith("NO"))
			return Collections.emptyList();    	

		//the final result with all traces
		List<Pair<List<SignalEdge>, List<SignalEdge>>> result = new LinkedList<Pair<List<SignalEdge>, List<SignalEdge>>>();

		//help variables containing the current traces
		List<SignalEdge> trace0 = new LinkedList<SignalEdge>();
		List<SignalEdge> trace1 = new LinkedList<SignalEdge>();


		//split the line and convert the entries
		BufferedReader reader = new BufferedReader(new StringReader(conflicts));
		String line;
		int i=0;
		while ( (line = reader.readLine()) != null ) {
			if (line.startsWith("YES") || line.startsWith("_SEQ"))
				continue;

			if (i==0) {
				i=1;
				trace0 = getTrace(stg, line);
			} 
			else {
				i=0;
				trace1 = getTrace(stg, line);
				if ( isIrreducibleConflict(trace0, trace1, stg) )
					result.add(new Pair<List<SignalEdge>, List<SignalEdge>> (trace0, trace1));
			}
		}
		
		return result;
	}

	private boolean isIrreducibleConflict(List<SignalEdge> trace0,
			List<SignalEdge> trace1, STG stg) {
		
		boolean diffTrace0HasAllInputs = true;
		boolean diffTrace1HasAllInputs = true;
		
		Iterator<SignalEdge> trace0Iter = trace0.iterator();
		Iterator<SignalEdge> trace1Iter = trace1.iterator();
		SignalEdge element0;
		SignalEdge element1;
		
		while (trace0Iter.hasNext() || trace1Iter.hasNext()) {
			element0 = null;
			element1 = null;
			if (trace0Iter.hasNext())
				element0 = trace0Iter.next();
			if (trace1Iter.hasNext())
				element1 = trace1Iter.next();
			
			if (element0 != null) {
				if ( !element0.equals(element1) ) {
					if (stg.getSignature(element0.getSignal()) != Signature.INPUT) {
							diffTrace0HasAllInputs = false;
							break; // for better performance
					}
					if (element1 != null) {
						if (stg.getSignature(element1.getSignal()) != Signature.INPUT) {
							diffTrace1HasAllInputs = false;
							break; // for better performance
						}
					}
				}
			} else { // element0 == null is definitely different from element1 != null
				if (stg.getSignature(element1.getSignal()) != Signature.INPUT) {
					diffTrace1HasAllInputs = false;
					break; // for better performance
				}
			}
		}
		
		if (diffTrace0HasAllInputs && diffTrace1HasAllInputs)
			return true;
		else
			return false;
	}



	/**
	 * Converts a single trace given as String delivered by mpsat into an appropriate representation. 
	 * @param line
	 * @return
	 */
	private List<SignalEdge> getTrace(STG stg, String line) {
		List<SignalEdge> result = new LinkedList<SignalEdge>();
		boolean dummySignal = false; // might be a specification dummy without signal direction

		for (String edge : line.split(",")) {
			if (edge.startsWith("i") || edge.startsWith("I")) {
			} 
			else if (edge.startsWith("o") || edge.startsWith("O") ) {
			}
			else if (edge.startsWith("d") || edge.startsWith("D") ) {
				dummySignal = true;
			}
			else if (edge.matches("[ \t]*"))
				continue;
			else
				throw new DesiJException("Unknown signature in "+line);

			String sig = edge.replaceAll(".*\\.|/.*","");

			EdgeDirection direction;
			if (sig.endsWith("+"))
				direction = EdgeDirection.UP;
			else if (sig.endsWith("-"))
				direction = EdgeDirection.DOWN;
			else if (!dummySignal)
				throw new DesiJException("Unknown direction in "+line);
			else
				direction = EdgeDirection.UNKNOWN;

			String signalName = sig.replaceAll("\\+|-|_" ,  "");
			Integer signal = Integer.parseInt(signalName);
			result.add(new SignalEdge(signal, direction));
		}

		return result;
	}


//
//	/**
//	 * Performs the inverse projection
//	 * @param <State>
//	 * @param stg
//	 * @param trace
//	 * @return
//	 * @throws STGException
//	 */
//	protected <State> List<SignalEdge> inverseProjection(StateSystem<State, SignalEdge> stg, List<SignalEdge> trace) throws STGException {
//		List<SignalEdge> invProjection = new LinkedList<SignalEdge>();
//
//
//		State currentState = stg.getInitialState();	
//
//		for (SignalEdge edge : trace) {
//			if (stg.getEvents(currentState).contains(edge)) {
//				invProjection.add(edge);
//				Set<State> nextStates = stg.getNextStates(currentState, edge);
//				if (nextStates.size()>1)
//					throw new STGException("Found non-determinism.");
//				currentState = nextStates.iterator().next();
//			}
//			else {
//
//
//			}
//
//		}
//
//		return invProjection;
//
//	}



	private Map<Integer,Integer> getCodeChange(List<SignalEdge> trace, boolean normalise) throws STGException {
		Map<Integer,Integer> result = new HashMap<Integer,Integer>();

		for (SignalEdge edge : trace) {
			Integer signal = edge.getSignal();

			Integer curCodeChange = result.get(signal);
			if (curCodeChange == null) {
				curCodeChange = 0;				
			}

			switch (edge.getDirection()) {
			case UP: 
				if (curCodeChange.equals(1)) {
					throw new STGException("Inconsistent STG.");
				}
				++curCodeChange;
				break;

			case DOWN: 
				if (curCodeChange.equals(-1)) {
					throw new STGException("Inconsistent STG.");
				}
				--curCodeChange;
				break;

			default:
				throw new STGException("Unkown signal edge: "+edge);
			}

			if (!(normalise && curCodeChange.equals(0))) {
				result.remove(signal);
			}
			else {				
				result.put(signal, curCodeChange);
			}
		}


		return result;

	}

//	private <State> boolean isCscSolved(StateSystem<State, SignalEdge> stg, Pair<List<SignalEdge>,List<SignalEdge>> cscTraces) throws STGException {
//		List<SignalEdge> invA = inverseProjection(stg, cscTraces.a);
//		List<SignalEdge> invB = inverseProjection(stg, cscTraces.b);
//
//		return !getCodeChange(invA,true).equals(getCodeChange(invB,true));
//	}



	/**
	 * Aggregates the given tree, i.e. collect all additional signals (the outputs of the components) and merge them
	 * in this tree.
	 * @param tree
	 */
	protected void aggregateSubtree(PresetTree<Integer, Collection<Integer>> tree) {
		//recursion finished, nothing to do
		if (tree.getSubtrees().size() == 0) {
			return;
		}

		if (tree.getAdditionalValue() == null) {
			tree.setAdditionalValue(new HashSet<Integer>());
		}

		//first, aggregate subtrees    	
		for (PresetTree<Integer, Collection<Integer>> subtree : tree.getSubtrees()) 
			aggregateSubtree(subtree);

		//second, now every subtree is a leaf, merge them in tree
		for (PresetTree<Integer, Collection<Integer>> subtree : tree.getSubtrees()) {    		
			tree.getAdditionalValue().addAll(subtree.getAdditionalValue());
		}    	

		//remove children
		tree.getSubtrees().clear();
	}

}
