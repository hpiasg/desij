

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

