package net.strongdesign.stg.solvers;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryLpSolve;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;



public class SharedPlaceSolver {
	
	private static long places[]; // places
	private static long lbits; // last bits, that have to be 1111...
	private static int len;    // total number of places
	//private static int total;  // number of transitions (preset + postset)
	private static long count = 0;
	
	public static boolean solve(int num, long curval) {
		if ((curval&lbits)==lbits) return true;
		
		/// if this is taking too long, simply return false
		if (count>10000000) {
			if (count==10000001) {
				System.err.print("SharedPlaceSolver: Timeout failure\n");
				count++;
			}
			return false;
		}
		count++;
		
		
		for (int i=num;i<len;i++) {
			
			if ( (curval&places[i]& (~lbits)) ==0) 
				if (solve(i+1, curval|places[i])) return true; //found a solution
		}
		
		return false;
	}
	
	private static boolean findOut(long[] pl, int postSet) {
		
		places=pl;
		len = places.length;
	
		lbits=1; // last bits 
		for (int i=0;i<postSet;i++) lbits*=2;
		lbits--;
		// now the critical part of the code
		return solve(0,0);
	}
	
	
	static private boolean checkWeights(Place place) {
		for (Node n: place.getParents())
			if (place.getParentValue(n)>1) return false;
		
		for (Node n: place.getChildren())
			if (place.getChildValue(n)>1) return false;
		
		return true;
	}
	
	/**
	 * This class provides detection, whether a given place is redundant based on neighbouring places
	 * It only works with safe nets, 0- and 1-weighted arcs
	 * 
	 * @return true if a place is implicit, false if don't know
	 */
	static public boolean isImplicit(Place place) {

		count=0;
		
		if (!place.hasChildren()) return true;
		
		
		HashSet<Node> preset = new HashSet<Node>();
		preset.addAll(place.getParents());
		
		HashSet<Node> postset = new HashSet<Node>();
		postset.addAll(place.getChildren());
		
		if(!checkWeights(place)) return false;
		
		HashSet<Place> nPlaces = new HashSet<Place>(); // neighbour places
		
		for (Node t: postset) {
			for (Node p: t.getParents()) {
				if (p==place) continue;
				
				// check that p preset is completely included in the preset on place
				HashSet<Node> pl = new HashSet<Node>();
				pl.addAll(p.getParents());
				pl.removeAll(place.getParents());
				if (!pl.isEmpty()) continue;
				
				if (((Place)p).getMarking()<2&&checkWeights((Place)p))
					nPlaces.add((Place)p);
			}
		}
		
		int total = postset.size()+preset.size(); 
		int k = postset.size();
		
		if (total>63) return false; // cannot work on larger sets
		
		long numbers[] = new long[nPlaces.size()];
		
		// now for each neighbour place creates a number
		int i=0;
		for (Place p: nPlaces) {
			
			for (Node n: preset) {
				
				numbers[i]<<=1;
				if (n.getChildValue(p)==1) {
					numbers[i]|=1;
				}
				
			}
			
			// add the bit from marking
			numbers[i]<<=1;
			if (p.getMarking()>0) numbers[i]|=1;
			
			for (Node n: postset) {
				
				numbers[i]<<=1;
				if (p.getChildValue(n)==1) {
					numbers[i]|=1;
				}
				
			}
			
			i++;
		}

		return findOut(numbers, k);
	}

	
	
	
	
	/**
	 * Detects whether a given place is implicit using LP solver
	 * @param place
	 * @return
	 */
	static private Result lpSolve(int test, String []names, double[][] v) {
		
		// launch solver
		SolverFactory factory = new SolverFactoryLpSolve();
		
		factory.setParameter(Solver.VERBOSE, 0); // no messages
		factory.setParameter(Solver.TIMEOUT, 0); // no timeout
		
		Problem problem = new Problem();
		
		for (int j = 0; j < v[0].length; j++)
			problem.setVarType(names[j], Double.class);
		
		for (int i = 0; i < v.length; i++) {
			Linear linear = new Linear();
			
			for (int j = 0; j < v[i].length; j++) {
				linear.add(v[i][j], names[j]);
			}

			if (test == i) {
				problem.add(linear, "<=", 1);
				problem.setObjective(linear);
			} else {
				problem.add(linear, "<=", 0);
			}
		}

		problem.setOptimizationType(OptType.MAX);

		Result result = factory.get().solve(problem);

		return result;
	}
	
	public static boolean isImplicitLP(Place place) {
		STG stg = place.getSTG();
		
		// launch solver
		SolverFactory factory = new SolverFactoryLpSolve();
		
		factory.setParameter(Solver.VERBOSE, 0); // no messages
		factory.setParameter(Solver.TIMEOUT, 0); // no timeout
		
		Problem problem = new Problem();
		
		List<Transition> tran = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS); 
		
		for (Transition t: tran) {
			problem.setVarType("T"+t.getIdentifier(), Double.class);
		}
		
		problem.setVarType("M", Double.class);
		
		for (Place p: stg.getPlaces()) {
			Linear linear = new Linear();
			
			HashSet<Node> test = new HashSet<Node>();
			
			// add preset
			for (Node n: p.getParents()) {
				linear.add(-p.getParentValue(n), "T"+n.getIdentifier());
			}
			
			// add postset
			for (Node n: p.getChildren()) {
				linear.add(p.getChildValue(n), "T"+n.getIdentifier());
			}
			
			// add marking
			linear.add(-p.getMarking(), "M");
			
			
			if (p == place) {
				problem.add(linear, "<=", 1);
				problem.setObjective(linear);
			} else {
				problem.add(linear, "<=", 0);
			}
		}
		
		problem.setOptimizationType(OptType.MAX);
		
		Result result = factory.get().solve(problem);
		
		if (result!=null) {
			String str = result.toString();
			if (str.startsWith("Objective: 0.0")) return true;
		}

		return false;
	}
	
	static public void main(String[] args) {
		int test = 1;
		String names[] = {"x","y","z"};
		double[][] v = { { -1, 0, 1 }, { -1, 1, 0 }, { 0, -1, 1 } };
		
		System.out.print(lpSolve(test, names, v).toString());
		
		// test
//		count=0;
//		long values[] = new long[] {
//			Long.parseLong("000000000000001000000000001000000000000000000000000000000001011", 2),
//			Long.parseLong("000000000000000000000100001000000000000000000000000000000000110", 2),
//			Long.parseLong("000000000000000000000000100100000000000000000000000000000001101", 2),
//			Long.parseLong("000000000000000000000001000000000000000000000000000000000001000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000000000010000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000000000100000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000000001000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000000010000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000000100000000", 2),
//			Long.parseLong("000000000000000000100000000000000000000000000000000001000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000010000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000000100000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000001000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000010000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000000100000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000001000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000010000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000000100000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000001000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000010000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000000100000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000001000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000010000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000000100000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000001000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000010000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000000100000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000001000000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000010000000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000000100000000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000001000000000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000010000000000000000000000000000000", 2),
//			Long.parseLong("000000000000000000000000000000100000000000000000000000000000000", 2),
//		};
//		
//		
//		System.out.println("result:"+findOut(values, 3));
//		System.out.println("count:"+count);
		
	}

}
