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

package net.strongdesign.desij.gui;

import java.awt.Point;
import java.util.HashMap;


import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;

public class STGEditorCoordinates extends HashMap<Node, Point> {
	private static final long serialVersionUID = 1708074655039330258L;

	public STGEditorCoordinates clone(STG stg) {
		STGEditorCoordinates result = new STGEditorCoordinates();
		
		for (Node node : stg.getNodes()) {
			Point np =  get(node);
			if (np!=null)
				result.put(  (Node) node.clone(),   (Point)np.clone());
		}
		return result;
	}
	
}
