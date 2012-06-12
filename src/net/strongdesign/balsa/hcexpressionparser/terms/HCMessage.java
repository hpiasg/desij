package net.strongdesign.balsa.hcexpressionparser.terms;


/*
 * Helper class, to store messages returned by the hc parser
 */
public class HCMessage extends HCTerm {
	public String message;
	
	@Override
	public HCTerm expand(ExpansionType type) throws Exception {
		return null;
	}

	@Override
	public String toString() {
		return message;
	}

	@Override
	public int getMaxCount() {
		return 0;
	}

	@Override
	public void setInstanceNumber(int num) {
	}

}
