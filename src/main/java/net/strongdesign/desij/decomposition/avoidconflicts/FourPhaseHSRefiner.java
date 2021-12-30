

package net.strongdesign.desij.decomposition.avoidconflicts;

import java.util.HashMap;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.ConditionFactory;

/**
 * @author dwist
 * Placeholder transitions will be refined into toggle nets
 * Regard that the transition ids AND the signal ids of the overall STG and its components 
 * will not correlate after that anymore, but of course the signal names will correlate
 * ONLY applicable for CAAvoidRecalculation but not for CAGeneral
 */
class FourPhaseHSRefiner extends PlaceHolderRefiner {
	/**
	 * not in use
	 */
	public FourPhaseHSRefiner() {
		super();
	}

	/* (non-Javadoc)
	 * @see net.strongdesign.desij.decomposition.avoidconflicts.PlaceHolderRefiner#execute(net.strongdesign.stg.STG)
	 */
	@Override
	public void execute(STG stg) throws STGException {
		for (Transition transition: stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
			if (stg.getSignature(transition.getLabel().getSignal()) == Signature.INTERNAL &&
					transition.getLabel().getDirection() == EdgeDirection.UNKNOWN) {
				Place pHigh = stg.addPlace("p", 0);
				Place pMiddle = stg.addPlace("p", 0);
				Place pBottom = stg.addPlace("p", 0);
				
				// change signal names from ic[n] to ic[2n] and ic[2n+1]
				Integer oldSignal = transition.getLabel().getSignal();
				// extract n, by cutting prefix "ic" from signalname
				int n = Integer.parseInt(stg.getSignalName(oldSignal).substring(2));
				// rename old signal
				HashMap<String,String> signalRenaming = new HashMap<String,String>(1);
				signalRenaming.put(stg.getSignalName(oldSignal), "ic_" + (2*n));
				stg.renameSignals(signalRenaming);
				
				Integer	newSignal = stg.getHighestSignalNumber() + 1;
				stg.setSignalName(newSignal, "ic_" + (2*n+1));
				
				Signature oldSignature = stg.getSignature(oldSignal);
				if (oldSignature == Signature.OUTPUT) { // critical component
					stg.setSignature(oldSignal, Signature.INPUT);
					stg.setSignature(newSignal, Signature.OUTPUT);
				}
				else if (oldSignature == Signature.INPUT) { // delay component 
					stg.setSignature(oldSignal, Signature.OUTPUT);
					stg.setSignature(newSignal, Signature.INPUT);
				}
				else if (oldSignature == Signature.INTERNAL) { // overall STG
					stg.setSignature(newSignal, Signature.INTERNAL);
				}
				else {
					throw new STGException("At least one placeholder transition has a wrong signature!");
				}
				
				Transition req1 = stg.addTransition(new SignalEdge(oldSignal, EdgeDirection.UP));
				Transition ack1 = stg.addTransition(new SignalEdge(oldSignal, EdgeDirection.DOWN));
				Transition req2 = stg.addTransition(new SignalEdge(newSignal, EdgeDirection.UP));
				Transition ack2 = stg.addTransition(new SignalEdge(newSignal, EdgeDirection.DOWN));
								
				for (Node place: transition.getParents()) {
					place.setChildValue(req1, 1);
				}
				
				for (Node place: transition.getChildren()) {
					place.setParentValue(ack2, 1);
				}
				
				req1.setChildValue(pHigh, 1);
				pHigh.setChildValue(req2, 1);
				req2.setChildValue(pMiddle, 1);
				pMiddle.setChildValue(ack1, 1);
				ack1.setChildValue(pBottom, 1);
				pBottom.setChildValue(ack2, 1);
				
				// delete old placeholder transition
				stg.removeTransition(transition);
			}
		}
	}

}
