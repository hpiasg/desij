/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
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
