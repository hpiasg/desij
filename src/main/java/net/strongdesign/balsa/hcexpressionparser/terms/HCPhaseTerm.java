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

public class HCPhaseTerm extends HCTerm {
	public ExpansionType phase;
	public HCTerm component;
	
	@Override
	public HCTerm expand(ExpansionType type, int scale, HCChannelSenseController sig, boolean oldChoice) throws Exception {
		
		if (type!=ExpansionType.UP) return null;//?
		
		return component.expand(phase, scale, sig, oldChoice);
	}

	@Override
	public String toString() {
		String s = "up(";
		if (phase == ExpansionType.DOWN) s="down(";
		return s+component.toString()+")";
	}

	
	@Override
	public Object clone() {
		HCPhaseTerm ret = (HCPhaseTerm)super.clone();
		ret.component = (HCTerm)component.clone();
		return ret;
	}
	

	@Override
	public void setInstanceNumber(int num, HCChannelSenseController sig) {
		component.setInstanceNumber(num, sig);
	}
}
