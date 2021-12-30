

package net.strongdesign.desij.decomposition.avoidconflicts;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author dwist
 * Placeholder transitions will be changed to toggle transitions 
 */
class ToggleTransitionRefiner extends PlaceHolderRefiner {
	
	/**
	 * not in use
	 */
	public ToggleTransitionRefiner() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderRefiner#execute(net.strongdesign.stg.STG)
	 */
	@Override
	public void execute(STG stg) throws STGException {
		for (Transition transition: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if ( (stg.getSignature(transition.getLabel().getSignal()) == Signature.INTERNAL || stg.getSignature(transition.getLabel().getSignal()) == Signature.INPUT) &&
					transition.getLabel().getDirection() == EdgeDirection.UNKNOWN &&
					stg.getSignalName(transition.getLabel().getSignal()).startsWith("ic") ) {
				// only change the direction of the label of the placeholder transition
				transition.getLabel().setDirection(EdgeDirection.TOGGLE);
				// TODO: initial values for toggle signals should be defined
			}
		}
	}
}
