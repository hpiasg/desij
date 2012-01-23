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

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STGException;

//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.GraphConstants;

import com.mxgraph.model.mxCell;

public class PlaceCell extends mxCell implements ApplyAttributes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5858658330662109563L;
	private Place place;
	public PlaceCell(Place place) {		
		super(place.getMarking()>0?place.getMarking():"");
		this.place = place;
		
		Point co = place.getSTG().getCoordinates(place);
		
//		if (co==null)
//			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,22,22));
//		else
//			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(co.x,co.y,22,22));
		
//		GraphConstants.setOpaque(getAttributes(), true);
//		GraphConstants.setBorderColor(getAttributes(), Color.BLACK);
//		GraphConstants.setFont(getAttributes(), STGEditorFrame.STANDARD_FONT);
		
	}

	public void applyAttributes() {
		
//		Rectangle2D pos = GraphConstants.getBounds(getAttributes());		
//		try {
//			place.getSTG().setCoordinates(place, new Point((int)pos.getX(), (int)pos.getY()));
//		} catch (STGException e) {
//			e.printStackTrace();
//		}
	}

}
