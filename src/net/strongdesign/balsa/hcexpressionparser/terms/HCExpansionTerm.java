package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public class HCExpansionTerm extends HCTerm {
	public Operation operation;
	public HCTerm component;
	public HCChannelCountController countController;
	
	public HCExpansionTerm(HCChannelCountController c) {
		countController = c;
	}
	
	@Override
	public HCTerm expand(ExpansionType type) throws Exception {
		
		int len = getMaxCount();
		
		if (operation==HCInfixOperator.Operation.ENCLOSE) {
			// for now only support the infix operator
			return null;
		} else {
			HCInfixOperator inf=new HCInfixOperator();
			inf.operation = operation;
			
			// first, expand the repetition
			for (int i=0;i<len;i++) {
				HCTerm t = (HCTerm)component.clone();
				t.setInstanceNumber(i);
				
				inf.components.add(t);
			}
			
			// now return the expansion of the generated expression
			return inf.expand(type);
			
		}
	}

	@Override
	public String toString() {
		if (component instanceof HCChannelTerm || component instanceof HCTransitionTerm)
			return "#"+Operation.toString(operation)+component.toString();
		
		return "#"+Operation.toString(operation)+"("+component.toString()+")";
	}

	
	@Override
	public int getMaxCount() {
		return component.getMaxCount();
	}

	@Override
	public void setInstanceNumber(int num) {
		// empty, shouldn't be called
		try {
			throw new Exception("Requesting instance number from the expansion term");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public Object clone() {
		// expansion cannot be cloned 
		System.out.println("ERROR!");
		return null;
	}

}

