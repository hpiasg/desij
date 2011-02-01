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
			if (stg.getSignature(transition.getLabel().getSignal()) == Signature.INTERNAL &&
					transition.getLabel().getDirection() == EdgeDirection.UNKNOWN) {
				// only change the direction of the label of the placeholder transition
				transition.getLabel().setDirection(EdgeDirection.TOGGLE);
				// TODO: initial values for toggle signals should be defined
			}
		}
	}
}
