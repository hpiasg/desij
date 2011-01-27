package net.strongdesign.statesystem.simulation;

import java.util.List;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;


/**
 * This interface models a relationpropagator regarding some form of common behaviour 
 * between two statesystems, for instance a bisimulation. Implemenations should be used
 * together with {@link net.strongdesign.statesystem.simulation.Simulation}.
 *  
 * @param <StateA> States which are used by the first system.
 * @param <StateB> States which are used by the second system.
 * @param <Event> The events which are used by the systems. Must be the same for both systems.
 */
public interface RelationPropagator<StateA, StateB, Event> {
	/**
	 * Initialises the RelationPropagator with two statesystems.
	 * @param sys1
	 * @param sys2
	 */
	public void setSystems(StateSystem<StateA, Event> sys1, StateSystem<StateB, Event> sys2);
	
	/**
	 * Returns the states which have to be in the relation initially, normally just the intial states. 
	 * @return
	 */
	public Set<RelationElement<StateA, StateB>> getStartRelation();
	
	/**
	 * Outgoing from an element of the relation, other elements which have to be included are returned in the 
	 * following way: outgoing from an element which should be included in the corresponding relation,
	 * for each event (the list level) a collection of possible following elements (the set level) are returned. The
	 * interpretation is that from each Set only one element has to be included in the final relation.
	 * 
	 * @param el The respective element
	 * @throws PropgationException 
	 * @throws Exception If el cannot be element of the relation
	 */
	public List<Set<RelationElement<StateA, StateB>>> propagateElement(RelationElement<StateA, StateB> el) throws PropagationException  ;
	
	
	/**
	 * An exception for the use of relation propagamtors.
	 *
	 */
	public class PropagationException extends Exception {
		private static final long serialVersionUID = -4213707875969099805L;

		public PropagationException(String mes) {
			super(mes);
		}
	}
}
