

package net.strongdesign.stg;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import net.strongdesign.desij.DesiJException;
import net.strongdesign.stg.traversal.ConditionFactory;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * @author Dominic Wist
 *
 */
public class CSCCheckerLPSolvePreCaching implements ICSCCheckLPStrategy {
	
	private static CSCCheckerLPSolvePreCaching instance; // singleton implementation
	private static STG stg;
		
	private LpSolve lp;
	private int Ncol; // number of (scalar) variables
	
	
	private Dictionary<String, Integer> variable2Int; // mapping from variable names to variable identifiers (in both directions)
	private List<CachedRow> cachedRowsForReachability; // static part of the model
	// later you can derive from this dictionary the equations for C' v1 - C' v2 = 0
	private Dictionary<Integer,CachedRow> cachedRowsForEncodingEquality; // for each Signal s: C_s v1 - C_s v2 = 0
	private Dictionary<Transition,Set<CachedRow>> cachedRowsForNonInputActivation; // dictionary mapping: t -> M1[t>
	private Dictionary<Transition,CachedRow> cachedRowsForNonInputDeActivation; // dictionary mapping: t -> not(M2[t>)
	private final class CachedRow { // use it as a pascal-like record, but not as a 'real' class
		int[] colNo;
		double[] row;
		int comparison; // smaller, larger, equal, etc.
		int constantValue;
	}
	
	// to find out an unbalanced signal in order to avoid a CSC conflict --> see getUnbalancedSignals()
	private Dictionary<Integer,Dictionary<Transition,Integer>> signalVectors; // the vectors C_s, according to Carmona 
	
	// last result from LP solving
	private double[] lastResult;
		
	private CSCCheckerLPSolvePreCaching(STG stg) {
		CSCCheckerLPSolvePreCaching.stg = stg;
		
		int j = 0; // a variable identifier
		// characterization of one row
		int[] colNo; // variable identifiers --> calculated as index+1
		double[] row;
		
		// How many variables has the model?
		// Variables are token counts for all places for markings M1 and M2 
		// as well as firing counts for all transitions of v1 and v2
		Ncol = stg.getNumberOfPlaces() * 2 + stg.getNumberOfTransitions() * 2;
		
		// create enough space for one row
		colNo = new int[Ncol];
		row = new double[Ncol];
		// initially a row contains just zeros
		for (j = 0; j < Ncol; ++j) {
			colNo[j] = j+1;
			row[j] = 0.0;
		}
		
		// it's necessary to construct the model for every execution again and again, 
		// hence caching of the static model part might be a good idea for faster construction
		
		// 1. make all variables accessible via their names:
		
		variable2Int = new Hashtable<String, Integer>(Ncol);
		String key;
		
		j = 0; // variable identifier (in colNo)
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			// represents a reachable marking M1
			key = "M1" + p.getIdentifier();
			variable2Int.put(key, ++j);
			// represents a reachable marking M2
			key = "M2" + p.getIdentifier();
			variable2Int.put(key, ++j);
		}
		
		for (Transition t : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			// represents the Parikh vector v1
			key = "v1" + t.getIdentifier();
			variable2Int.put(key, ++j);
			// represents the Parikh vector v2
			key = "v2" + t.getIdentifier();
			variable2Int.put(key, ++j);
		}
		
		// 2. construct the rows for the cache:
		
		cachedRowsForReachability = new LinkedList<CSCCheckerLPSolvePreCaching.CachedRow>();
		Stack<Integer> rowUndoStack = new Stack<Integer>(); // saves indices of the changed values in row
		
		// M1 = MN + I v1 --> -MN = -M1 + I v1 
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			row[rowUndoStack.push(variable2Int.get("M1" + p.getIdentifier()) - 1)] = -1; // -M1(p)
			for (Node transition : p.getNeighbours()) {
				row[rowUndoStack.push(variable2Int.get("v1" + transition.getIdentifier()) - 1)] = // + I v1(transition) 
						transition.getChildValue(p) - transition.getParentValue(p);
			}
			
			// put it in the cache
			CachedRow entry = new CachedRow();
			entry.colNo = colNo;
			entry.row = row.clone();
			entry.comparison = LpSolve.EQ; // testing for equality
			entry.constantValue = -1*p.getMarking();
			cachedRowsForReachability.add(entry);
			
			// re-initialize row array with zeros only
			while(!rowUndoStack.isEmpty())
				row[rowUndoStack.pop()] = 0.0;
		}
		
		// M2 = MN + I v2 --> -MN = -M2 + I v2
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			row[rowUndoStack.push(variable2Int.get("M2" + p.getIdentifier()) - 1)] = -1; // -M2(p)
			for (Node transition : p.getNeighbours()) {
				row[rowUndoStack.push(variable2Int.get("v2" + transition.getIdentifier()) - 1)] = // + I v2(transition)
						transition.getChildValue(p) - transition.getParentValue(p);
			}
			
			// put it in the cache
			CachedRow entry = new CachedRow();
			entry.colNo = colNo;
			entry.row = row.clone();
			entry.comparison = LpSolve.EQ; // testing for equality
			entry.constantValue = -1*p.getMarking();
			cachedRowsForReachability.add(entry);
			
			// re-initialize row array with zeros only
			while(!rowUndoStack.isEmpty())
				row[rowUndoStack.pop()] = 0.0;
		}
		
		// --> in lpsolve all variables M1, M2, v1, v2 have a lower bound of zero (by default)
		// so nothing to do here for me
				
		// pre-calculate all potentially necessary constraints for checking encoding equality:
		cachedRowsForEncodingEquality = new Hashtable<Integer, CSCCheckerLPSolvePreCaching.CachedRow>(stg.getSignals().size());
		for (int s : stg.getSignals()) {
			if (stg.getSignature(s) != Signature.DUMMY) {
				// build C_s v1 - C_s v2 = 0
				for (Transition t : stg.getTransitions(ConditionFactory.getSignalOfCondition(s)) ) {
					if (t.getLabel().direction == EdgeDirection.UP) {
						row[rowUndoStack.push(variable2Int.get("v1" + t.getIdentifier()) - 1)] = 1;
						row[rowUndoStack.push(variable2Int.get("v2" + t.getIdentifier()) - 1)] = -1;
					}
					else if (t.getLabel().direction == EdgeDirection.DOWN) {
						row[rowUndoStack.push(variable2Int.get("v1" + t.getIdentifier()) - 1)] = -1;
						row[rowUndoStack.push(variable2Int.get("v2" + t.getIdentifier()) - 1)] = 1;
					}
				}
				
				CachedRow entry = new CachedRow();
				entry.colNo = colNo;
				entry.row = row.clone();
				entry.comparison = LpSolve.EQ;
				entry.constantValue = 0;
				cachedRowsForEncodingEquality.put(s, entry);
				
				// re-initialize row array with zeros only
				while (!rowUndoStack.isEmpty())
					row[rowUndoStack.pop()] = 0.0;
			}
		}
		
		// pre-calculate all constraints for testing the activation status of each 
		// internal or output transition t: 
		
		// 1. testing t for activation under the reachable marking M1, i.e. M1[t>
		cachedRowsForNonInputActivation = new Hashtable<Transition, Set<CachedRow>>();
		for (Transition t : stg.getTransitions(ConditionFactory.LOCAL_TRANSITIONS)) {
			Set<CachedRow> enoughTokens = new HashSet<CSCCheckerLPSolvePreCaching.CachedRow>();
			for (Node place : t.getParents()) {
				int changedRowIndex = variable2Int.get("M1" + place.getIdentifier()) - 1; // for undoing
				row[changedRowIndex] = 1;
				CachedRow entry = new CachedRow();
				entry.colNo = colNo;
				entry.row = row.clone();
				entry.comparison = LpSolve.GE;
				entry.constantValue = t.getParentValue(place);
				enoughTokens.add(entry);
				row[changedRowIndex] = 0.0; // undo the change in row
			}
			cachedRowsForNonInputActivation.put(t, enoughTokens);
		}
		
		// 2. testing t for deactivation under the reachable marking M2, i.e. not(M2[t>)
		// --> we only check the sufficient condition whether the sum of tokens on *t
		// is smaller than the sum of edge weights from *t to t
		// --> this condition is also necessary for safe nets
		cachedRowsForNonInputDeActivation = new Hashtable<Transition, CSCCheckerLPSolvePreCaching.CachedRow>();
		for (Transition t : stg.getTransitions(ConditionFactory.LOCAL_TRANSITIONS)) {
			int sumOfEdgeWeights = 0;
			for (Node place : t.getParents()) {
				row[rowUndoStack.push(variable2Int.get("M2" + place.getIdentifier()) - 1)] = 1;
				sumOfEdgeWeights += t.getParentValue(place);
			}
			CachedRow entry = new CachedRow();
			entry.colNo = colNo;
			entry.row = row.clone();
			entry.comparison = LpSolve.LE; // <=
			entry.constantValue = sumOfEdgeWeights - 1; // we need a check on smaller, but not equal
			cachedRowsForNonInputDeActivation.put(t, entry);
			
			// re-initialize row array with zeros only
			while (!rowUndoStack.isEmpty())
				row[rowUndoStack.pop()] = 0.0;
		}
		
		// now initialize the C matrix (but without Dummy Signals) --> needed for getUnbalancedSignals()
		// for efficiency, we don't save zero values for each signal vector
		signalVectors = new Hashtable<Integer, Dictionary<Transition,Integer>>(stg.getSignals().size());
		for (int s : stg.getSignals()) {
			if (stg.getSignature(s) != Signature.DUMMY) {
				Dictionary<Transition,Integer> sVector = new Hashtable<Transition, Integer>();
				for (Transition t : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS) ) {
					if (t.label.signal == s) {
						if (t.label.direction == EdgeDirection.UP)
							sVector.put(t, 1);
						else if (t.label.direction == EdgeDirection.DOWN)
							sVector.put(t, -1);
						else throw new DesiJException("There is a Non-Dummy-Signal without an EdgeDirection.");
					}
					else { // don't put zeros inside, so let sVector be as small as possible
						// sVector.put(t, 0);
					}
				}
				signalVectors.put(s, sVector);
			}
		}
		 
	}
	
	/** Singleton implementation
	 * @param stg - the STG subject for CSC check
	 * @return
	 */
	public static CSCCheckerLPSolvePreCaching getCSCCheckerLPSolvePreCaching(STG stg) {
		
		if (CSCCheckerLPSolvePreCaching.instance == null || CSCCheckerLPSolvePreCaching.stg != stg)
			CSCCheckerLPSolvePreCaching.instance = new CSCCheckerLPSolvePreCaching(stg);
		
		return CSCCheckerLPSolvePreCaching.instance;
	}
	
	public boolean execute(Set<Integer> neededSignals) throws LpSolveException {
		Set<Integer> locals = new HashSet<Integer>();
		for (int sig : neededSignals) 
			if (stg.getSignature(sig) == Signature.INTERNAL || stg.getSignature(sig) == Signature.OUTPUT)
				locals.add(sig);
		
		// check CSC for each local signal
		for (int sig : locals) {
			List<Transition> sigTransitions = stg.getTransitions(ConditionFactory.getSignalOfCondition(sig));
			for (Transition t1 : sigTransitions) {
				if (solveLP(t1,sigTransitions,neededSignals))
					return false; // CSC may not be satisfied
			}
		}
		
		return true; // CSC is satisfied, since all LP problems were not solvable
	}
	
	/**
	 * Find signals s for which Cs v1 != Cs v2
	 * @param neededSignals - necessary signals for acting according to the specified I/O interface 
	 * and for CSC satisfaction 
	 * @return unbalanced signals w.r.t. lastResult (generated by execute(...))
	 */
	public Set<Integer> getUnbalancedSignals(Set<Integer> neededSignals) {
		Set<Integer> silentSignals = getSilentInternals(neededSignals); // for the neededSignals always holds Cs v1 = Cs v2
		Set<Integer> result = new HashSet<Integer>();
		
		for (int s : silentSignals) { 
			Dictionary<Transition,Integer> Cs = signalVectors.get(s);
			double CsV1 = 0.0, CsV2 = 0.0;
			for (Transition t : Collections.list(Cs.keys()) ) { // no zeros inside Cs
				CsV1 += Cs.get(t) * lastResult[variable2Int.get("v1" + t.getIdentifier()) - 1];
				CsV2 += Cs.get(t) * lastResult[variable2Int.get("v2" + t.getIdentifier()) - 1];
			}
			if (CsV1 != CsV2)
				result.add(s);
		}
		
		return result;
	}

	/**
	 * @param neededSignals - necessary signals for acting according to the specified I/O interface
	 * and for CSC satisfaction
	 * @return - silent signals which are characterized as unnecessary
	 */
	private Set<Integer> getSilentInternals(Set<Integer> neededSignals) {
		Set<Integer> result = new HashSet<Integer>(stg.getSignals(Signature.INTERNAL)); // all encoding signals
		result.removeAll(neededSignals); // some encoding signals are already specified as necessary 			
		return result;
	}

	private boolean solveLP(Transition t1, List<Transition> transHavingSignalOft1,
			Set<Integer> projectedSignals) throws LpSolveException {
		int ret = 0; // for error tracking during lp solving
			
		// create the static part of each LP problem
		lp = LpSolve.makeLp(0, Ncol);
		if (lp.getLp() == 0)
			throw new DesiJException("Couldn't construct a new lp model.");
		
		if (ret == 0) {
			lp.setAddRowmode(true); // it is easier to build the models row by row
			
			// M1 = MN + I v1
			// M2 = MN + I v2
			for (CachedRow constraint : cachedRowsForReachability) {
				lp.addConstraintex(Ncol, constraint.row, constraint.colNo, 
						constraint.comparison, constraint.constantValue);
			}
			
			// C' v1 = C' v2
			for (Integer sig : projectedSignals) {
				CachedRow constraint = cachedRowsForEncodingEquality.get(sig);
				lp.addConstraintex(Ncol, constraint.row, constraint.colNo, 
						constraint.comparison, constraint.constantValue);
			}
			
			// M1[t1>
			for (CachedRow constraint : cachedRowsForNonInputActivation.get(t1)) {
				lp.addConstraintex(Ncol, constraint.row, constraint.colNo, 
						constraint.comparison, constraint.constantValue);
			}
			
			// not(M2[t2>), for all transition t2 where l(t2) = l(t1)
			for (Transition t2 : transHavingSignalOft1) {
				CachedRow constraint = cachedRowsForNonInputDeActivation.get(t2);
				lp.addConstraintex(Ncol, constraint.row, constraint.colNo, 
						constraint.comparison, constraint.constantValue);
			}
			
			lp.setAddRowmode(false); // finished: adding the constraints
			
			// so far no objective function
			
			lp.setVerbose(LpSolve.IMPORTANT); // just important messages on screen while solving 
			ret = lp.solve();
			if (ret == LpSolve.OPTIMAL || ret == LpSolve.SUBOPTIMAL || ret == LpSolve.FEASFOUND) {
				ret = 0;
			}
			else if (ret == LpSolve.INFEASIBLE || ret == LpSolve.NOFEASFOUND) {
				ret = 2;
			}
			else 
				throw new LpSolveException("An Error during LpSolving occurred!");
		}
		
		if (ret == 0) { // a solution was calculated
			lastResult = new double[Ncol];
			lp.getVariables(lastResult);
			lp.deleteLp(); // free memory used by lpsolve
			return true;
		}
		
		lastResult = null; // no result was calculated
		// clean up such that all used memory by lpsolve is freed
        if(lp.getLp() != 0)
          lp.deleteLp();
		return false; // no solution was calculated
	}

}
