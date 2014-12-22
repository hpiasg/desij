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

package net.strongdesign.stg;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import net.strongdesign.desij.CLW;
import net.strongdesign.stg.STGUtil.Reason;
import net.strongdesign.stg.traversal.ConditionFactory;

public class TransitionQueueWithArrayList implements TransitionQueue {

	private final List<Transition> contract;
	private final STG stg;
	
	public TransitionQueueWithArrayList(STG stg)
	{
		this.stg = stg;
		contract = stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY));
	}
	
	@Override
	public Transition pop() {
		int resultIndex = -1;
		
		for (int i = 0; i < contract.size(); i++)
		{
			if(STGUtil.isContractable(stg, contract.get(i)) == Reason.OK)
			{
				if (resultIndex == -1)
				{
					resultIndex = i;
				}
				if (CLW.instance.ORDER_DUMMY_TRANSITIONS.isEnabled()) { 
					
					// find smallest element in linear time --> makes no further updating of keys in a priority queue necessary
					int actPre = contract.get(resultIndex).getChildren().size();
					int actPost = contract.get(resultIndex).getParents().size();
					int resultCost = (actPre-1)*(actPost-1)-1;
					int iCost = (contract.get(i).getChildren().size()-1) * (contract.get(i).getParents().size()-1)-1;
					if(ConditionFactory.SAFE_CONTRACTABLE.fulfilled(contract.get(i)))
					{
						Assert.assertEquals(iCost, -1);
					}
					else
					{
						Assert.assertTrue(iCost > -1);
					}
					if (resultCost > iCost)
						resultIndex = i;
				}
				else
				{
					break;
				}
			}
		}
		if(resultIndex == -1)
			return null;
		else
			return contract.remove(resultIndex);
	}

	@Override
	public void registerAffectedNodes(Collection<Node> nodes) {
	}

	@Override
	public void removeNodes(Collection<Node> nodes) {
		contract.removeAll(nodes);
	}

	@Override
	public int size() {
		return contract.size();
	}

	@Override
	public int getContractibleTransitionsCount() {
		int result = 0;
		for (int i = 0; i < contract.size(); i++)
			if(STGUtil.isContractable(stg, contract.get(i)) == Reason.OK)
				result++;
		return result;
	}

}
