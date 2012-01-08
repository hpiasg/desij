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

//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.GraphConstants;

import com.mxgraph.layout.hierarchical.model.mxGraphAbstractHierarchyCell;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyNode;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;

public class NavigationCell extends mxGraphHierarchyNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8790152046891872397L;
	private STGEditorTreeNode node;
	public NavigationCell(STGEditorTreeNode node, String label) {
		super(label);
		this.node = node;
		
//		mxConstants.setAutoSize(getAttributes(), true);
//		mxConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,20,20));
//		mxConstants.setOpaque(getAttributes(), true);
//		
//		mxConstants.setBorderColor(getAttributes(), Color.BLACK);
//		mxConstants.setInset(getAttributes(), 1);
//		mxConstants.setGradientColor(getAttributes(), STGEditorFrame.NAV_COLOR);
		
		
//GraphConstants.setFont(getAttributes(), STGEditorFrame.SMALL_FONT);
//		GraphConstants.setMoveable(getAttributes(), false);
//		GraphConstants.setEditable(getAttributes(), false);
		
	}
	
	public STGEditorTreeNode getTreeNode() {
		return node;
	}
	
}
