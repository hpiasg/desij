package net.strongdesign.desij.decomposition.partitioning;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
 * 
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	
	// TODO: rename to "addLocal"?
	public boolean addSignal(String name) {
		// if a signal is added, it is not among the inputs anymore
		inputs.remove(name);
		
		return signals.add(name);
	}
	
	public boolean addInput(String name) {
		return inputs.add(name);
	}
}
