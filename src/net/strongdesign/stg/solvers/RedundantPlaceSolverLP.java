package net.strongdesign.stg.solvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;

public class RedundantPlaceSolverLP {
	
	static public long totalSetupMills;  // number of milliseconds used for setting up the task
	static public long totalSolverMills; // number of milliseconds solver was thinking (accumulates)
	static public long totalFound;       // total number of the redundants found
	
	/**
	 * Procedure checks for structural redundancy. It returns "true" if a place is structurally
	 * redundant
	 * 
	 * @param stg  - STG to check
	 * @param place - place of interest
	 * @param depth - depth of the subgraph
	 * @return
	 */
	public boolean isRedundant(STG stg, Place place, int depth) {
		int nroPlaces;
		
		long startSetup = System.currentTimeMillis();
		long startSolver;
		Set<Place> places = new HashSet<Place>();
		Set<Transition> transitions = new HashSet<Transition>();
		
		// find the STG subgraph for the desired depth
		STG.getSubgraphNodes(stg, place, depth, places, transitions);
		
		nroPlaces = places.size();
		
		// create a mapping from each place to some number
		HashMap<Place, Integer> map = new HashMap<Place, Integer>();
		int num=0;
		int redPlace = 0;
		for (Place p: places) {
			map.put(p, num);
			if (p==place) redPlace=num;
			++num;
		}
		
		try {
			double[] curRow = new double[nroPlaces+2];
			
			//Create a new LP
			LpSolve lp = LpSolve.makeLp(0, nroPlaces + 1);
			
			lp.setVerbose(0);
			
			//Set the target function, which is constant because only feasibility must be checked
			lp.setObjFn(curRow); 
			
			 
			//The valuation of the redundant place must be strictly greater than 0
			//the exact value is not important
			lp.setLowbo(redPlace+1, 1);
			
			//set first constraint, for redundancy condition 1: V(p)M_N(p) - \sum_{q\in Q} V(q)M_N(q) - c = 0
			for (Place p: places) {
				int idx = map.get(p);
				curRow[idx+1] = -p.getMarking();
			}
			curRow[redPlace+1] = -curRow[redPlace+1];
			curRow[nroPlaces+1] = -1;
			lp.addConstraint(curRow, LpSolve.EQ, 0);
			
			//for debugging
			//lp.setRowName(1, "marking");
			
			
			//set second set of constraints for condition 2: 
			// \forall t\in T : V(p)\Delta_t(p) - \sum_{q\in Q} V(q)\Delta_t(q) \geq 0
			
			//for every constraint 0 -- variable c not important here
			curRow[nroPlaces+1] = 0;
			for (Transition t: transitions) {
				curRow = new double[nroPlaces+2];
				
				for (Node p: t.getNeighbours()) {
					if (!places.contains(p)) continue;
					
					int idx = map.get(p);
					curRow[idx+1] = -(t.getChildValue(p)-p.getChildValue(t));
				}
				
				curRow[redPlace+1] = -curRow[redPlace+1];
				lp.addConstraint(curRow, LpSolve.GE, 0);					
			}

			
			//third set of constraints for condition 3: 
			// \forall t\in T : V(p)W(p,t) - \sum_{q\in Q} V(q)W(q,t) -c \leq 0
			curRow[nroPlaces+1] = -1;
			for (Transition t: transitions) {
				curRow = new double[nroPlaces+2];
				
				for (Node p: t.getNeighbours()) {
					if (!places.contains(p)) continue;
					int idx = map.get(p);
					curRow[idx+1] = -p.getChildValue(t);
				}
				
				curRow[redPlace+1] = -curRow[redPlace+1];
				
				lp.addConstraint(curRow, LpSolve.LE, 0);					
			}
			
			//for debugging
			/*
			for (Node node : mapping.keySet()) {
				if (node instanceof Place)
					lp.setColName(mapping.get(node)+1, node.toString());
				else {
					lp.setRowName(mapping.get(node)+2, node.toString()+"-2");
					lp.setRowName(mapping.get(node)+2+nroTransitions, node.toString()+"-3");
				}
			}
			*/
			
			// solve the problem, if it is feasible lp_solv returns immediately with 0 after the first vertex was encountered
			// because the object function is constant, then the place is redundant
			// if the problem is infeasible the place is not redundant
			startSolver = System.currentTimeMillis();
			int res = lp.solve();
			totalSetupMills+=startSolver-startSetup;
			totalSolverMills+=System.currentTimeMillis()-startSolver;
			
			if (res == 0) {
				totalFound++;
				return true;
			} else
				return false;
			
			
		} catch (LpSolveException e) {
			System.err.println("Internal error while checking redundancy with lp_solv");
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean isRedundant2(STG stg, Place place, int depth) {
		
		long startSetup = System.currentTimeMillis();
		long startSolver;
		Set<Place> places = new HashSet<Place>();
		Set<Transition> transitions = new HashSet<Transition>();
		
		// find the STG subgraph for the desired depth
		STG.getSubgraphNodes(stg, place, depth, places, transitions);
		// launch solver
		
		SolverFactory factory = new SolverFactoryLpSolve();
		factory.setParameter(Solver.VERBOSE, 0); // no messages
		factory.setParameter(Solver.TIMEOUT, 0); // no timeout
		
		Problem problem  = new Problem();
		
		
		
		for (Place p: places) {
			problem.setVarType(p, Double.class);
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType(p, Integer.class);
			else
				problem.setVarType(p, Double.class);
		}
		
		if (!CLW.instance.NOILP.isEnabled())
			problem.setVarType("c", Integer.class);
		else
			problem.setVarType("c", Double.class);
		
		Linear linear = new Linear();
		
		for (Place p: places) {
			linear.add(0, p);
		}
		linear.add(0, "c");
		
		//Set the target function, which is constant because only feasibility must be checked
		problem.setObjective(linear);
		//The valuation of the redundant place must be strictly greater than 0
		//the exact value is not important
		problem.setVarLowerBound(place, 1);
		
		
		//set first constraint, for redundancy condition 1: V(p)M_N(p) - \sum_{q\in Q} V(q)M_N(q) - c = 0
		linear = new Linear();
		for (Place p: places) {
			if (p==place) {
				linear.add(p.getMarking(), p);
			} else {
				linear.add(-p.getMarking(), p);
			}
		}
		linear.add((double)-1, "c");
		problem.add(linear, Operator.EQ, 0);
		
		//set second set of constraints for condition 2: 
		// \forall t\in T : V(p)\Delta_t(p) - \sum_{q\in Q} V(q)\Delta_t(q) \geq 0
		
		//for every constraint 0 -- variable c not important here
		for (Transition t: transitions) {
			linear = new Linear();
			
			for (Node p: t.getNeighbours()) {
				if (!places.contains(p)) continue;
				if (p==place) {
					linear.add(t.getChildValue(p)-p.getChildValue(t), p);
				} else {
					linear.add(-(t.getChildValue(p)-p.getChildValue(t)), p);
				}
			}
			problem.add(linear, Operator.GE, 0);
		}

		
		//third set of constraints for condition 3: 
		// \forall t\in T : V(p)W(p,t) - \sum_{q\in Q} V(q)W(q,t) -c \leq 0
		for (Transition t: transitions) {
			linear = new Linear();
			
			for (Node p: t.getNeighbours()) {
				if (!places.contains(p)) continue;
				
				if (p==place) {
					linear.add(p.getChildValue(t), p);
				} else {
					linear.add(-p.getChildValue(t), p);
				}
			}
			
			linear.add((double)-1, "c");
			
			problem.add(linear, Operator.LE, 0);					
		}
		
		// solve the problem, if it is feasible lp_solv returns immediately with 0 after the first vertex was encountered
		// because the object function is constant, then the place is redundant
		// if the problem is infeasible the place is not redundant
		startSolver = System.currentTimeMillis();
		
		Result res = factory.get().solve(problem);
		
		totalSetupMills+=startSolver-startSetup;
		totalSolverMills+=System.currentTimeMillis()-startSolver;
		
		if (res != null) {
			totalFound++;
			return true;
		} else
			return false;
		
		
	}
	
	public boolean isRedundant(STG stg, Place place) {
		
		return isRedundant(stg, place, 0);
	}
}

