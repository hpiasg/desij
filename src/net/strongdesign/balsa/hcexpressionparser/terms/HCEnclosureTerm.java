package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public class HCEnclosureTerm extends HCTerm {
	public HCChannelTerm channel;
	public HCTerm component;
	public HCChannelCountController countController;
	
	
	public HCEnclosureTerm(HCChannelCountController c) {
		countController = c;
	}
	
	@Override
	public HCTerm expand(ExpansionType type) throws Exception {
		
		HCInfixOperator ret = new HCInfixOperator();
		ret.operation = Operation.SEQUENCE;
		
		String dir = "+";
		if (type==ExpansionType.DOWN) dir = "-";
		
		HCTransitionTerm t1 = new HCTransitionTerm(countController);
		t1.channel = channel.toString();
		t1.direction = dir;
		t1.wire = "r";
		

		HCTransitionTerm t2 = new HCTransitionTerm(countController);
		t2.channel = channel.toString();
		t2.direction = dir;
		t2.wire = "a";
		
		HCTerm tmp = component.expand(type);
		
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
	public int getMaxCount() {
		return Math.max(channel.getMaxCount(), component.getMaxCount());
	}

	@Override
	public void setInstanceNumber(int num) {
		channel.setInstanceNumber(num);
		component.setInstanceNumber(num);
	}

}