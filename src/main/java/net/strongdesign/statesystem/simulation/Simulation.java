

package net.strongdesign.statesystem.simulation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.statesystem.simulation.RelationPropagator.PropagationException;

public class Simulation {

	/**
	 * Tries to find a parametrised simulation between two state systems, if there is any.
	 * @param <State> Class which is used for defining states.
	 * @param <Event> Class which is used for defining events.
	 * @param sys1 State system 1.
	 * @param sys2 State system 1.
	 * @param relProp The desired @link RelationPropagator.
	 * @return The simulation as set of the respective elements. null if the systems are not in this kind of relation.
	 * @throws PropagationException 
	 * @throws Exception 
	 */
	public static <StateA, StateB, Event> Set<RelationElement<StateA, StateB>> findSimulation(
			StateSystem<StateA, Event> sys1, 
			StateSystem<StateB, Event> sys2, 
			RelationPropagator<StateA, StateB, Event> relProp ) throws PropagationException  	{
		
		//initialise the relation propagator
		relProp.setSystems(sys1, sys2);
		
		//start elements have to be included (usually only one element)
		Set<RelationElement<StateA, StateB>> bisim = new LinkedHashSet<RelationElement<StateA, StateB>>(relProp.getStartRelation()); 

		for (RelationElement<StateA, StateB> el : bisim) {
			List<Set<RelationElement<StateA, StateB>>> p = relProp.propagateElement(el);
			if (p==null)
				return null;
			if (! prop(p, bisim, relProp))
				return null;
			}
		
		return bisim;		
	}
	
	/**
	 * Propagates a set of elements, see @link RelationPropagator#propagateElement(RelationElement) for the details of a.
	 * @param <State>
	 * @param <Event>
	 * @param a see above
	 * @param bisim The bisimulation known or assumed so far.
	 * @param relProp The corresponding @link RelationPropagator
	 * @return True, if the propagation was successful
	 * @throws PropagationException
	 */
	protected static <StateA, StateB, Event> boolean prop(
			List<Set<RelationElement<StateA, StateB>>> a, 
			Set<RelationElement<StateA, StateB>> bisim, 
			RelationPropagator<StateA, StateB, Event> relProp) throws PropagationException {
		
		set:
		for (Set<RelationElement<StateA, StateB>> c : a) {
			
			for (RelationElement<StateA, StateB> e : c) {
				if (bisim.contains(e)) continue set;
				bisim.add(e);
				List<Set<RelationElement<StateA, StateB>>> p = relProp.propagateElement(e);
				
				if (p==null) {
					bisim.remove(e);
				}
				else {
					if (prop(p, bisim, relProp))
						continue set;
					else
						bisim.remove(e);
				}
			}
			return false;
		}
	return true;
	}
}











