

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
