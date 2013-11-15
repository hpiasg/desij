package net.strongdesign.desij.decomposition.partitioning;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PartitionComponent {
	
	protected Set<String> signals = new HashSet<String>();
	protected Set<String> inputs = new HashSet<String>();
	
	// different signals can be added here
	public Set<String> getSignals() {
		return Collections.unmodifiableSet(signals);
	}
	
	// here signals, that are only presented as inputs in the component
	public Set<String> getInputs() {
		return Collections.unmodifiableSet(inputs);
	}
	
	
	
	public boolean addSignal(String name) {
		// if a signal is added, it is not among the inputs anymore
		inputs.remove(name);
		
		return signals.add(name);
	}
	
	public boolean addInput(String name) {
		return inputs.add(name);
	}
}
