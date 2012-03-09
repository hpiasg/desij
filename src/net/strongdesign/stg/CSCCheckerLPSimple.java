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

package net.strongdesign.stg;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.stg.traversal.ConditionFactory;

import lpsolve.LpSolveException;

/**
 * @author dominic.wist
 *
 */
public class CSCCheckerLPSimple implements ICSCCheckLPStrategy {
	
	private STG stg;
	
	// stores the last LP solution
	private Result lastResult;
	
	// stores the matrix C of all signal vectors, according to Josep's CSC detection approach
	// i.e. C = Sigma x T
	// --> for efficiency, just the values unequal to zero are stored 
	private Dictionary<Integer,Dictionary<Transition,Integer>> signalVectors;
	
	public CSCCheckerLPSimple(STG stg) {
		this.stg = stg;
		
		// build the matrix C (but without Dummy Signals)
		// for efficiency, we don't save zero values for each signal vector
		signalVectors = new Hashtable<Integer, Dictionary<Transition,Integer>>(stg.getSignals().size());
		for (int s : stg.getSignals())
			if (stg.getSignature(s) != Signature.DUMMY) {
				Dictionary<Transition,Integer> sVector = new Hashtable<Transition, Integer>();
				for (Transition t : stg.getTransitions(ConditionFactory.getSignalOfCondition(s)) ) {
					if (t.label.direction == EdgeDirection.UP)
						sVector.put(t, 1); 
					else if (t.label.direction == EdgeDirection.DOWN)
						sVector.put(t, -1);
					else throw new DesiJException("There is a Non-Dummy-Signal without an EdgeDirection: " + t.label);
				}
				signalVectors.put(s, sVector);
			}
		
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.stg.ICSCCheckLPStrategy#execute(java.util.Set)
	 */
	@Override
	public boolean execute(Set<Integer> neededSignals) throws LpSolveException {
		Set<Integer> locals = new HashSet<Integer>();
		for (int sig : neededSignals) {
			if (stg.getSignature(sig) == Signature.INTERNAL || stg.getSignature(sig) == Signature.OUTPUT)
				locals.add(sig);
		}
		
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

	private boolean solveLP(Transition t1, List<Transition> transHavingSignalOft1,
			Set<Integer> projectedSignals) {
		
		SolverFactory factory = new SolverFactoryLpSolve();
		factory.setParameter(Solver.VERBOSE, 0); // we don't need messages from the solver
		factory.setParameter(Solver.TIMEOUT, 0); // no timeout
		
		Problem problem = new Problem();
		Linear linear;
		Linear linear2;
		int id;
		
		// M_1 = M_N + I v_1 --> -M_N = -M_1 + I v_1
		// M_2 = M_N + I v_2 --> -M_N = -M_2 + I v_2
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			id = p.getIdentifier();
			linear = new Linear();
			linear.add(-1, "M1" + id);
			linear2 = new Linear();
			linear2.add(-1, "M2" + id);
			// avoid building the IncidenceMatrix and compute on children and parents of p, only
			for (Node transition : p.getNeighbours()) {
				linear.add(transition.getChildValue(p) - transition.getParentValue(p), "v1" + transition.getIdentifier());
				linear2.add(transition.getChildValue(p) - transition.getParentValue(p), "v2" + transition.getIdentifier());
			}
			
			problem.add(linear, "=", -1*p.getMarking());
			problem.add(linear2, "=", -1*p.getMarking());
		}
		
		linear = new Linear();
		linear2 = new Linear();
		// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem
		// M_2 > 0 and v_2 >= 0 and solve as (I)LP problem
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			id = p.getIdentifier();
			problem.setVarLowerBound("M1" + id, 0);
			problem.setVarLowerBound("M2" + id, 0);
			linear.add(1, "M1" + id);
			linear2.add(1, "M2" + id);
			if (!CLW.instance.NOILP.isEnabled()) {
				problem.setVarType("M1" + id, Integer.class);
				problem.setVarType("M2" + id, Integer.class);
			}
			else {
				problem.setVarType("M1" + id, Double.class);
				problem.setVarType("M2" + id, Double.class);
			}
		}
		problem.add(linear, ">=", 1);
		problem.add(linear2, ">=", 1);
		
		for (Transition t : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			id = t.getIdentifier();
			problem.setVarLowerBound("v1" + id, 0);
			problem.setVarLowerBound("v2" + id, 0);
			if (!CLW.instance.NOILP.isEnabled()) {
				problem.setVarType("v1" + id, Integer.class);
				problem.setVarType("v2" + id, Integer.class);
			}
			else {
				problem.setVarType("v1" + id, Double.class);
				problem.setVarType("v2" + id, Double.class);
			}
		}
		
		// C' v1 = C' v2 --> C' v1 - C' v2 = 0
		// i.e. M_1 and M_2 have the same encoding
		for (int sig: projectedSignals) {
			linear = new Linear();
			for (Transition t : stg.getTransitions(ConditionFactory.getSignalOfCondition(sig)) ) {
				id = t.getIdentifier();
				if (t.getLabel().direction == EdgeDirection.UP) {
					linear.add(1, "v1" + id); // Csig v1
					linear.add(-1, "v2" + id); // - Csig v2
				}
				else if (t.getLabel().direction == EdgeDirection.DOWN) {
					linear.add(-1, "v1" + id); // Csig v1
					linear.add(1, "v2" + id); // - Csig v2
				}
				else 
					throw new DesiJException("Error in solving LP: There are projected signals without an edge direction: " + t.getLabel());
			}
			problem.add(linear, "=", 0);
		}
		
		// M1[t1>
		for (Node place : t1.getParents()) {
			id = place.getIdentifier();
			linear = new Linear();
			linear.add(1, "M1" + id);
			problem.add(linear, ">=", t1.getParentValue(place));
		}
		
		// not(M2[t2>) for all transition t2 where l(t2) = l(t1)
		// --> we only check the sufficient condition whether the sum of tokens on *t2
		// is smaller than the sum of edge weights going from *t2 to t2
		// --> this condition is also necessary for safe nets
		for (Transition t2 : transHavingSignalOft1) {
			linear = new Linear();
			int sumOfEdgeWeights = 0;
			for (Node place : t2.getParents()) {
				id = place.getIdentifier();
				linear.add(1, "M2" + id); // sum of tokens on *t2
				sumOfEdgeWeights += t2.getParentValue(place);
			}
			problem.add(linear, "<=", sumOfEdgeWeights-1);	
		}		
		
		// define the objective function: the sum of all silent event occurrences in v1 and v2 should be minimal
		// this definition is a bit deviated from Josep's, but more general applicable
		linear = new Linear();
		for (int sig : getSilentInternals(projectedSignals)) {
			for (Transition t : stg.getTransitions(ConditionFactory.getSignalOfCondition(sig))) {
				id = t.getIdentifier();
				linear.add(1, "v1" + id);
				linear.add(1, "v2" + id);
			}
		}
		problem.setObjective(linear, OptType.MIN);
		
		// solve the problem
		lastResult = factory.get().solve(problem);
		
		if (lastResult == null) // problem is infeasible
			return false;
		else
			return true; // solution is stored in lastResult
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.stg.ICSCCheckLPStrategy#getUnbalancedSignals(java.util.Set)
	 */
	@Override
	public Set<Integer> getUnbalancedSignals(Set<Integer> neededSignals) {
		Set<Integer> silentSignals = getSilentInternals(neededSignals); // for the neededSignals always holds Cs v1 = Cs v2
		Set<Integer> result = new HashSet<Integer>();
		int id;
		
		for (int s : silentSignals) { 
			Dictionary<Transition,Integer> Cs = signalVectors.get(s); // non-zero components of the signal vector of s
			double CsV1 = 0.0, CsV2 = 0.0;
			for (Transition t : Collections.list(Cs.keys()) ) { // non-zero components
				id = t.getIdentifier();
				CsV1 += Cs.get(t) * lastResult.get("v1" + id).doubleValue(); // doesn't matter when we consider just integers
				CsV2 += Cs.get(t) * lastResult.get("v2" + id).doubleValue();
			}
			if (CsV1 != CsV2) // Is 's' an unbalanced signal?
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

}
