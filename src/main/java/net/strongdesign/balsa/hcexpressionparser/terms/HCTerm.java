package net.strongdesign.balsa.hcexpressionparser.terms;

public abstract class HCTerm implements Cloneable {
	public enum ExpansionType {UP, DOWN};
	
	abstract public HCTerm expand(ExpansionType type, int scale, HCChannelSenseController sig, boolean oldChoice) throws Exception;
	abstract public String toString();
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	abstract public void setInstanceNumber(int num, HCChannelSenseController sig);
	
}
