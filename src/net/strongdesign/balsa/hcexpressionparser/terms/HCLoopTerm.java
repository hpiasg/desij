package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public class HCLoopTerm extends HCTerm implements HCSTGGenerator {
	public HCTerm component;

	@Override
	public HCTerm expand(ExpansionType type) throws Exception  {
		if (type==ExpansionType.UP) {
			HCInfixOperator ret = new HCInfixOperator();
			HCTerm up = component.expand(ExpansionType.UP);
			HCTerm down = component.expand(ExpansionType.DOWN);
			if (up!=null) ret.components.add(up);
			if (down!=null) ret.components.add(down);
			
			ret.operation = Operation.SEQUENCE;
			
			HCLoopTerm lp = new HCLoopTerm();
			lp.component = ret;
			
			return lp; // return the expanded loop component
		}
		
		return null;
	}

	@Override
	public String toString() {
		
		return "#("+component.toString()+")";
	}

	@Override
	public int getMaxCount() {
		return component.getMaxCount();
	}

	@Override
	public void setInstanceNumber(int num) {
		component.setInstanceNumber(num);
	}

	@Override
	public Object clone() {
		HCLoopTerm ret = (HCLoopTerm)super.clone();
		ret.component = (HCTerm)component.clone();
		return ret;
	}

	@Override
	public void generateSTG(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace) {
		
		HCSTGGenerator hc = (HCSTGGenerator)component;
		hc.generateSTG(stg, sig, inPlace, inPlace);
	}
	
}

