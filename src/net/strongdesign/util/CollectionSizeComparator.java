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

package net.strongdesign.util;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author Dominic Wist
 * 
 * Comparison criteria in order to sort Lists or TreeSets
 */
public class CollectionSizeComparator implements Comparator<Collection> {

	
	public int compare(Collection o1, Collection o2) {
		if (o1.size() < o2.size())
			return -1;
		else if (o1.size() > o2.size())
			return +1;
		else  // o1.size() == o2.size()
			return 0;
	}

	

}
