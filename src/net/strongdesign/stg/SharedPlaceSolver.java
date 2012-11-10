package net.strongdesign.stg;

import java.util.HashSet;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;


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
	static public boolean isImplicitLP(Place place) {

		try {
			// Create a problem with 4 variables and 0 constraints
			LpSolve solver = LpSolve.makeLp(0, 2);
			
			solver.setVerbose(0); // no info from lpsolver
			solver.setTimeout(2); // set timeout to two seconds
			
			// add all places apart from the ones restricting preset  
			
			
			solver.strAddConstraint("-1 0 1", LpSolve.LE, 0);
			solver.strAddConstraint("-1 1 0", LpSolve.LE, 0);
			solver.strAddConstraint("0 -1 1", LpSolve.LE, 1);
			
			// set objective function
//			solver.strSetObjFn("-1 0 1");
//			solver.strSetObjFn("-1 1 0");
			solver.strSetObjFn("0 -1 1");
			
			solver.setMaxim();

			// solve the problem
			int ret = solver.solve();
			
			// if no solution is found, then the place is implicit
			if (ret == 0) {
				/* a solution is calculated, now lets get some results */

				/* objective value */
				System.out.println("Objective value: " + solver.getObjective());

				/* we are done now */
			}
			
			solver.deleteLp();
		}

		catch (LpSolveException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	static public void main(String[] args) {
		
		isImplicitLP(null);
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
