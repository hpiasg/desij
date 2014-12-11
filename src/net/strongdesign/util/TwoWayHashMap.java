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
