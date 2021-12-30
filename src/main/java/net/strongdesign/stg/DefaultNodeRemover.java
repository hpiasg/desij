

package net.strongdesign.stg;

public final class DefaultNodeRemover implements NodeRemover {
	private final STG stg;

	public DefaultNodeRemover(STG stg) {
		this.stg = stg;
	}

	@Override
	public void removePlace(Place place) {
		stg.removePlace(place);	
	}

	@Override
	public void removeTransition(Transition transition) {
		stg.removeTransition(transition);
	}
}
