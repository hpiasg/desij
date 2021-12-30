package net.strongdesign.balsa.hcexpressionparser.terms;



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
