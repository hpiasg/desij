package net.strongdesign.balsa.hcexpressionparser.terms;

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

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;

public class HCEnclosureTerm extends HCTerm {
	public HCChannelTerm channel;
	public HCTerm component;
	
	
	@Override
	public HCTerm expand(ExpansionType type, int scale, HCChannelSenseController sig, boolean oldChoice) throws Exception {
		
		HCInfixOperator ret = new HCInfixOperator();
		ret.operation = Operation.SEQUENCE;
		
		String dir = "+";
		if (type==ExpansionType.DOWN) dir = "-";
		
		HCTransitionTerm t1 = new HCTransitionTerm();
		t1.channel = channel.toString();
		t1.direction = dir;
		t1.wire = "r";
		
		
		HCTransitionTerm t2 = new HCTransitionTerm();
		t2.channel = channel.toString();
		t2.direction = dir;
		t2.wire = "a";
		
		HCTerm tmp = component.expand(type, scale, sig, oldChoice);
		
		ret.components.add(t1);
		
		if (tmp!=null) {
			ret.components.add(tmp);
		}
		
		ret.components.add(t2);
		
		return ret;
	}

	@Override
	public Object clone() {
		HCEnclosureTerm ret = (HCEnclosureTerm) super.clone();
		ret.channel = (HCChannelTerm)channel.clone();
		ret.component = (HCTerm)component.clone();
		return ret;
	}

	@Override
	public String toString() {
		if (component instanceof HCChannelTerm || component instanceof HCTransitionTerm)
			return channel.toString()+":"+component.toString();
		return channel.toString()+":("+component.toString()+")";
	}

	@Override
	public void setInstanceNumber(int num, HCChannelSenseController sig) {
		channel.setInstanceNumber(num, sig);
		component.setInstanceNumber(num, sig);
	}

}
