

package net.strongdesign.desij.decomposition.avoidconflicts;

/**
 * 
 * @author Dominic Wist
 * This class will be used like a struct in C.
 * It specifies additional information for a node of an STG which is only relevant for the graph 
 * traversals in order to avoid irreducible CSC conflicts.
 */
class NodeProperty {
	boolean isEntryTransition = false;
	boolean isExitTransition = false;
	boolean isRelevant = false;
	// to avoid cycles in the search
	boolean isVisited = false;
	boolean isVisitedBy2ndSearch = false; 	// only for the searches leading to the exit transition, 
											// after an output was found
}
