package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public abstract class HCTerm implements Cloneable {
	public enum ExpansionType {UP, DOWN};
	
	abstract public HCTerm expand(ExpansionType type) throws Exception;
	abstract public String toString();
	abstract public int getMaxCount();
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	abstract public void setInstanceNumber(int num);
	
}
