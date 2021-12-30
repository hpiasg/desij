

package net.strongdesign.desij.net;

import java.rmi.RemoteException;

import net.strongdesign.desij.decomposition.DecompositionEvent;

public interface IDecompositionStatistics {

	public int getEventCount() throws RemoteException;

	public int getEventCount(DecompositionEvent event) throws RemoteException;

	
}
