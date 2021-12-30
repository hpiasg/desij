

package net.strongdesign.stg.synthesis;

import net.strongdesign.stg.STG;

public class SynthesisException extends Exception {
	private static final long serialVersionUID = 1343346652018020336L;
	private STG stg;

	public SynthesisException(String mes, STG stg) {
		super(mes);
		this.stg = stg;
	}
	
	public STG getSTG() {
		return this.stg;
	}
}
