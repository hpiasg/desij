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
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

public class NavigationCell extends DefaultGraphCell {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8790152046891872397L;
	private STGEditorTreeNode node;
	public NavigationCell(STGEditorTreeNode node, String label) {
		super(label);
		this.node = node;
		GraphConstants.setAutoSize(getAttributes(), true);
		GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,20,20));
		GraphConstants.setOpaque(getAttributes(), true);
//		GraphConstants.setMoveable(getAttributes(), false);
//		GraphConstants.setEditable(getAttributes(), false);
		
		//GraphConstants.setFont(getAttributes(), STGEditorFrame.SMALL_FONT);
		GraphConstants.setBorderColor(getAttributes(), Color.BLACK);
		GraphConstants.setInset(getAttributes(), 1);
		GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.NAV_COLOR);
	}
	
	public STGEditorTreeNode getTreeNode() {
		return node;
	}
	
}
