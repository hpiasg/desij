

package net.strongdesign.stg;

import java.util.Collection;

public interface TransitionQueue {
	Transition pop();
	void registerAffectedNodes(Collection<Node> nodes);
	void removeNodes(Collection<Node> nodes);
	int size();
	int getContractibleTransitionsCount();
}
