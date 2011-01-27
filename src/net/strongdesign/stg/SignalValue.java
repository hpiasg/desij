package net.strongdesign.stg;

public enum SignalValue {
	/**Logival low*/
	LOW ("0"),
	
	/**Logival high*/
	HIGH ("1"),
	
	/**Unknwon signal value*/
	UNKNOWN ("?"),  
	
	/**A relative value meaning higher than*/
	PLUS ("+"), 
	
	/**A relative value meaning lower than*/
	MINUS ("-"), 
	
	/**A relative value meaning the same value as*/
	ZERO (":");	
	
	
	private SignalValue(String name) {
		this.name = name;
	}
	
	protected String name;
	
	public String toString() {
		return name;
	}
	
	/**Applies a relative value to a signal or relative value
	 * 
	 * @param RelativeValue The change to apply
	 * @return The resulting signalvalue or signalchange e.g. PLUS apllied to LOW results in HIGH, PLUS apllied to MINUS result in ZERO etc.
	 * @throws IllegalArgumentException if a signal value should be applied or the resulkt is undefined 
	 */
	public SignalValue applyChange(SignalValue relativeValue) throws IllegalArgumentException {
		
		switch (relativeValue) {
		case ZERO:
			return this;

		case PLUS:
			switch (this) {
			case UNKNOWN: case LOW: return HIGH;
			case MINUS: return ZERO;
			case ZERO: return PLUS;			
			}
			
		case MINUS:
			switch (this) {
			case UNKNOWN: case HIGH: return HIGH;
			case PLUS: return ZERO;
			case ZERO: return MINUS;			
			}
		}
		
		throw new IllegalArgumentException("Cannot apply " + relativeValue + " to " + this);
	}

	public boolean isChangeCompatible(SignalValue change) {
	switch (change) {
		case ZERO:
			return true;

		case PLUS:
			switch (this) {
			case ZERO: case MINUS: case UNKNOWN: case LOW: 
				return true;
			}
			
		case MINUS:
			switch (this) {
			case ZERO: case PLUS: case UNKNOWN: case HIGH: return true;			
			}
		}
		
	
		return false;
	}
}
