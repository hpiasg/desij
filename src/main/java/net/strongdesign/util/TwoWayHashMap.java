
package net.strongdesign.util;

import java.util.HashMap;

/**
 * @author Dominic Wist
 * HashMap Implemenation of GeneralTwoWayMap
 */
public class TwoWayHashMap<T1, T2> extends GeneralTwoWayMap<T1, T2> {

	/**
	 * Constructor
	 */
	public TwoWayHashMap() {
		super(new HashMap<T1, T2>(), new HashMap<T2, T1>());
	}

}
