package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public class HCChannelTerm extends HCTerm {
	public String channel;
	public HCChannelCountController countController;
	
	public int instanceNumber=0;

	public HCChannelTerm(HCChannelCountController c) {
		countController = c;
	}
	
	@Override
	public HCTerm expand(ExpansionType type) {
		HCInfixOperator ret = new HCInfixOperator();
		ret.operation = Operation.SEQUENCE;
		
		
		HCTransitionTerm t1 = new HCTransitionTerm(countController);
		t1.channel = channel;
		String dir = "+";
		if (type==ExpansionType.DOWN) dir = "-";
		t1.instanceNumber = instanceNumber;
		t1.direction = dir;
		t1.wire = "r";
		

		HCTransitionTerm t2 = new HCTransitionTerm(countController);
		t2.channel = channel;
		t2.instanceNumber = instanceNumber;
		t2.direction = dir;
		t2.wire = "a";
		
		
		ret.components.add(t1);
		ret.components.add(t2);
		
		return ret;
	}

	@Override
	public String toString() {
		int cnt = instanceNumber;
		if (cnt>0) {
			return ""+channel+cnt;
		}
		
		return ""+channel;
	}

	@Override
	public int getMaxCount() {
		return countController.getCount(channel);
	}

	@Override
	public void setInstanceNumber(int num) {
		int tmp = num%countController.getCount(channel);
		instanceNumber = tmp;
	}

	

}
