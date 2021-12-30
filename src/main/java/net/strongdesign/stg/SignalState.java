

package net.strongdesign.stg;

import java.util.Collection;
import java.util.HashMap;



public class SignalState extends HashMap<Integer, SignalValue> {
	
	private static final long serialVersionUID = 5186790013115179677L;

	public SignalState(Collection<Integer> signals, SignalValue value) {
		super();
		for (Integer signal : signals)
			put(signal, value);	
	}
	
	public SignalState(SignalState state) {
		super();
		for (Integer signal : state.keySet())
			put(signal, state.get(signal));	
	}
	
	
	
	
	public boolean isEdgeCompatible(SignalEdge signalEdge) throws IllegalArgumentException {
		SignalValue value = get(signalEdge.getSignal());
		SignalValue change;
		switch (signalEdge.getDirection()) {
		case UP: change = SignalValue.PLUS; break;
		case DOWN: change = SignalValue.MINUS; break;
		
		default: throw new IllegalArgumentException("Unsupported signal edge: "+signalEdge.getDirection());
		}
		
		return value.isChangeCompatible(change);
	}
	
	public SignalState applySignalEdge(SignalEdge signalEdge) throws IllegalArgumentException {
			SignalValue value = get(signalEdge.getSignal());
			SignalValue change;
			switch (signalEdge.getDirection()) {
			case UP: change = SignalValue.PLUS; break;
			case DOWN: change = SignalValue.MINUS; break;
			
			default: throw new IllegalArgumentException("Unsupported signal edge: "+signalEdge.getDirection());
			}
			
			SignalState result = new SignalState(this);
			
			result.put(signalEdge.getSignal(), value.applyChange(change));
			return result;
	}

	public SignalState applyChangeVector(SignalState state) {
		SignalState result = new SignalState(this);

		for (Integer signal : result.keySet()) 
			result.put(signal, result.get(signal).applyChange(state.get(signal)));
		

		return result;
	}
	
}
