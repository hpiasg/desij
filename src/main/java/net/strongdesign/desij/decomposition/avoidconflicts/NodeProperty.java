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
