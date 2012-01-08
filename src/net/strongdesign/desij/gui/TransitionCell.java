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

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;

//import org.jgraph.graph.DefaultGraphCell;

//import org.jgraph.graph.GraphConstants;

public class TransitionCell extends mxCell implements ApplyAttributes{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3924546389856822747L;
	private Transition transition;
	
	public TransitionCell(Transition transition, STG stg) {
		super(transition.getLabel());
		
		this.transition = transition; 
		
		Point co = transition.getSTG().getCoordinates(transition);
		
//		SG:
//		if (co==null)
//			mxConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,22,22));
//		else
//			mxConstants.setBounds(getAttributes(), new Rectangle2D.Double(co.x,co.y,22,22));
//		
//		mxConstants.setAutoSize(getAttributes(), true);
//		mxConstants.setOpaque(getAttributes(), true);
//		mxConstants.setFont(getAttributes(), STGEditorFrame.STANDARD_FONT);
//		mxConstants.setBorderColor(getAttributes(), Color.BLACK);
//		mxConstants.setInset(getAttributes(), 2);
//		
//		
//		
//		switch (stg.getSignature(transition.getLabel().getSignal())) {
//		case INPUT: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.INPUT); break;
//		case OUTPUT: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.OUTPUT); break;
//		case DUMMY: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.DUMMY); break;
//		case INTERNAL: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.INTERNAL); break;		
//		}
		
	}

	public void applyAttributes() {
		
//		SG:
//		Rectangle2D pos = GraphConstants.getBounds(getAttributes());
//		try {
//			transition.getSTG().setCoordinates(transition, new Point((int)pos.getX(), (int)pos.getY()));
//		} catch (STGException e) {
//			e.printStackTrace();
//		}
		
	}
	
	
	
}
