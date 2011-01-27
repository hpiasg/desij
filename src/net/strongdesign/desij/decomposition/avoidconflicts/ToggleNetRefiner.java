/**
 * 
 */
package net.strongdesign.desij.decomposition.avoidconflicts;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author dwist
 * Placeholder transitions will be refined into toggle nets
 * Regard that the transition ids of the overall STG and its components will not correlate after that 
 */
class ToggleNetRefiner extends PlaceHolderRefiner {

	/**
	 * not in use
	 */
	public ToggleNetRefiner() {
		super();
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderRefiner#execute(net.strongdesign.stg.STG)
	 */
	@Override
	public void execute(STG stg) throws STGException  {
		int placeNumber = 0;
		for (Transition transition: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (stg.getSignature(transition.getLabel().getSignal()) == Signature.INTERNAL &&
					transition.getLabel().getDirection() == EdgeDirection.UNKNOWN) {
				Place p_H = stg.addPlace("p_H" + (++placeNumber), 0);
				Place p_L = stg.addPlace("p_L" + placeNumber, 1);
				Transition t_plus = stg.addTransition(new SignalEdge(transition.getLabel().getSignal(), EdgeDirection.UP));
				Transition t_minus = stg.addTransition(new SignalEdge(transition.getLabel().getSignal(), EdgeDirection.DOWN));
				
				for (Node place: transition.getParents()) {
					place.setChildValue(t_plus, 1);
					place.setChildValue(t_minus, 1);
				}
				
				for (Node place: transition.getChildren()) {
					place.setParentValue(t_plus, 1);
					place.setParentValue(t_minus, 1);
				}
				
				p_L.setChildValue(t_plus, 1);
				t_plus.setChildValue(p_H, 1);
				p_H.setChildValue(t_minus, 1);
				t_minus.setChildValue(p_L, 1);
				
				// delete old placeholder transition
				stg.removeTransition(transition);
			}
		}
	}

}
