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

package net.strongdesign.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class NeighbourTrackingNodeRemover implements NodeRemover
{
	private final STG stg;
	private final Collection<Node> neighbours = new ArrayList<Node>();
	private final Collection<Node> removed = new ArrayList<Node>();

	public NeighbourTrackingNodeRemover(STG stg)
	{
		this.stg = stg;
	}
	
	public Collection<Node> getNeighbours()
	{
		return Collections.unmodifiableCollection(neighbours);
	}
	
	public Collection<Node> getRemoved()
	{
		return Collections.unmodifiableCollection(removed);
	}
	
	@Override
	public void removePlace(Place place) {
		neighbours.addAll(place.getNeighbours());
		removed.add(place);
		stg.removePlace(place);
	}

	@Override
	public void removeTransition(Transition transition) {
		neighbours.addAll(transition.getNeighbours());
		removed.add(transition);
		stg.removeTransition(transition);
	}
}

