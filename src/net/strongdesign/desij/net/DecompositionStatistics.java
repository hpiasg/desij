package net.strongdesign.desij.net;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import net.strongdesign.desij.decomposition.DecompositionEvent;
import net.strongdesign.stg.STG;

public class DecompositionStatistics extends UnicastRemoteObject implements IDecompositionStatistics  {

	private int eventCounter = 0;
	private Map<DecompositionEvent, Integer> events	= new HashMap<DecompositionEvent, Integer>();

	public void logging(STG stg, DecompositionEvent event, Object affectedComponents) {
		++eventCounter;
		
		Integer count = events.get(event);
		if (count == null) {
			count = 0;
		}
		
		events.put(event, count+1);

	}

	public DecompositionStatistics() throws RemoteException {
		super();
		logging(null, DecompositionEvent.STAT_SERVER_STARTED, null);		
	}

	public int getEventCount() {
		return eventCounter;
	}

	public int getEventCount(DecompositionEvent event) {
		return events.get(event);
	}




}
