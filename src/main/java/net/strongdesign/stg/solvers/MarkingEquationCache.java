

package net.strongdesign.stg.solvers;

import java.util.HashSet;
import java.util.Set;

import net.sf.javailp.Linear;
import net.sf.javailp.Problem;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author Dominic Wist
 * Caches for specification the corresponding LP problems for the marking equation
 */
public class MarkingEquationCache {
	
	// private static Map<STG, List<Constraint>> markingEquation = new HashMap<STG, List<Constraint>>();
	
	
	/**
	 * Caches and returns for specifications (stg) the corresponding 
	 * marking equation using M_1 and v_1 as variables:
	 * M_1 = M_N + I v_1 where M_1 > 0 and v_1 >= 0
	 * M_1 --> "M1" + place.getIdentifier()
	 * v_1 --> "v1" + transition.getIdentifier()
	 * 
	 * @param stg - the key for the Map
	 * @return - the marking equation subject for (I)LP solving
	 */
	public static Problem getMarkingEquation(STG stg) {
		
		// caching of marking equation --> doesn't work, because of variable naming problems, e.g. "v1"+id != "v1"+id
//		if (markingEquation.get(stg) == null) {
//			List<Constraint> constraints = new LinkedList<Constraint>();
//			Linear linear;
//			int id;
//			
//			// M_1 = M_N + I v_1 --> -M0 = -M1 + I v_1
//			for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
//				id = p.getIdentifier();
//				linear = new Linear();
//				linear.add(-1, "M1" + id);
//				// avoid building the IncidenceMatrix and compute on children and parents of p, only
//				for (Node transition : p.getNeighbours())
//					linear.add(transition.getChildValue(p) - transition.getParentValue(p), "v1" + transition.getIdentifier());
//				
//				constraints.add( new Constraint(linear, "=", -1*p.getMarking()) );
//			}
//									
//			markingEquation.put(stg, constraints);
//		}
		
		Problem problem = new Problem();
		Linear linear;
		int id;
		
		// M_1 = M_N + I v_1 --> -M0 = -M1 + I v_1
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			id = p.getIdentifier();
			linear = new Linear();
			linear.add(-1, "M1" + id);
			// avoid building the IncidenceMatrix and compute on children and parents of p, only
			for (Node transition : p.getNeighbours())
				linear.add(transition.getChildValue(p) - transition.getParentValue(p), "v1" + transition.getIdentifier());
			
			problem.add(linear, "=", -1*p.getMarking());
		}
		
		// cached marking equation
//		for ( Constraint c : markingEquation.get(stg) )
//			problem.add(c);
		
		linear = new Linear();
		// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem 
		for (Place p : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			id = p.getIdentifier();
			problem.setVarLowerBound("M1" + id, 0);
			linear.add(1, "M1" + id);
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType("M1" + id, Integer.class);
			else
				problem.setVarType("M1" + id, Double.class);
		}
		problem.add(linear, ">=", 1);
		
		for (Transition t : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			id = t.getIdentifier();
			problem.setVarLowerBound("v1" + id, 0);
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType("v1" + id, Integer.class);
			else
				problem.setVarType("v1" + id, Double.class);
		}
				
		return problem;
	}
	
	
	/**
	 * This function creates a marking equation of a limited depth for a given STG and a place
	 * @param stg
	 * @param place - the central place from which to construct the limited STG
	 * @param depth - depth for which to construct the marking equation
	 * @return
	 */
	public static Problem getMarkingEquation(STG stg, Place place, int depth, Set<Place> places, Set<Transition> transitions) {
		// depth=0 means full depth will be returned
		if (depth==0) return getMarkingEquation(stg);
		
		STG.getSubgraphNodes(stg, place, depth, places, transitions);
		
		Problem problem = new Problem();
		Linear linear;
		int id;
		
		// M_1 = M_N + I v_1 --> -M0 = -M1 + I v_1
		for (Place p : places) {
			id = p.getIdentifier();
			linear = new Linear();
			linear.add(-1, "M1" + id);
			
			// avoid building the IncidenceMatrix and compute on children and parents of p, only
			for (Node transition : p.getNeighbours()) {
				if (!transitions.contains(transition)) continue;
				
				linear.add(transition.getChildValue(p) - transition.getParentValue(p), "v1" + transition.getIdentifier());
			}
			
			problem.add(linear, "=", -1*p.getMarking());
		}
		
		linear = new Linear();
		// M_1 > 0 and v_1 >= 0 and solve as (I)LP problem 
		for (Place p : places) {
			id = p.getIdentifier();
			problem.setVarLowerBound("M1" + id, 0);
			linear.add(1, "M1" + id);
			
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType("M1" + id, Integer.class);
			else
				problem.setVarType("M1" + id, Double.class);
		}
		
		problem.add(linear, ">=", 1);
		
		for (Transition t : transitions) {
			id = t.getIdentifier();
			problem.setVarLowerBound("v1" + id, 0);
			if (!CLW.instance.NOILP.isEnabled())
				problem.setVarType("v1" + id, Integer.class);
			else
				problem.setVarType("v1" + id, Double.class);
		}
				
		return problem;
	}
	
}
