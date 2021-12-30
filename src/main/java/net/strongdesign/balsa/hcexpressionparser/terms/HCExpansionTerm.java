package net.strongdesign.balsa.hcexpressionparser.terms;



import java.util.LinkedList;

import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;

public class HCExpansionTerm extends HCTerm {
	public Operation operation;
	public HCTerm component;
	
	
	@Override
	public HCTerm expand(ExpansionType type, int scale, HCChannelSenseController sig, boolean oldChoice) throws Exception {
		
		int len = scale;
		
		if (operation==HCInfixOperator.Operation.ENCLOSE) {
			
			LinkedList<HCTerm> l = new LinkedList<HCTerm>();
			
			for (int i=0;i<len;i++) {
				HCTerm t = (HCTerm)component.clone();
				t.setInstanceNumber(i, sig);
				l.add(t);
			}
			
			if (len==0) return null;
			
			if (len==1) return component.expand(type, scale, sig, oldChoice);
			
			HCEnclosureTerm enc = new HCEnclosureTerm();
			HCEnclosureTerm encp = enc;
			
			for (int i=0;i<len-1;i++) {
				encp.channel = (HCChannelTerm)l.get(i);
				
				if (i==len-2) {
					encp.component = l.get(i+1);
				} else {
					encp.component = new HCEnclosureTerm();
					encp = (HCEnclosureTerm)encp.component;
				}

			}
			
			return enc.expand(type, scale, sig, oldChoice);
			
		} else {
			HCInfixOperator inf=new HCInfixOperator();
			inf.operation = operation;
			
			// first, expand the repetition
			for (int i=0;i<len;i++) {
				HCTerm t = (HCTerm)component.clone();
				t.setInstanceNumber(i, sig);
				
				inf.components.add(t);
			}
			
			// now return the expansion of the generated expression
			return inf.expand(type, scale, sig, oldChoice);
			
		}
	}

	@Override
	public String toString() {
		if (component instanceof HCChannelTerm || component instanceof HCTransitionTerm)
			return "#"+Operation.toString(operation)+component.toString();
		
		return "#"+Operation.toString(operation)+"("+component.toString()+")";
	}

	

	@Override
	public void setInstanceNumber(int num, HCChannelSenseController sig) {
		// empty, shouldn't be called
		try {
			throw new Exception("Requesting instance number from the expansion term");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public Object clone() {
		try {
			throw new Exception("Expansion cannot be cloned!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

