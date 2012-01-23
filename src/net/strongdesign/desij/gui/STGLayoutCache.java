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

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.Transition;


//import org.jgraph.event.GraphModelEvent;
//import org.jgraph.event.GraphModelListener;
//import org.jgraph.graph.DefaultEdge;
//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.DefaultPort;
//import org.jgraph.graph.GraphConstants;
//import org.jgraph.graph.GraphLayoutCache;
//import org.jgraph.graph.GraphModel;

public class STGLayoutCache {// extends LayoutCache implements GraphModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8642803346457408895L;
	private STG stg;
	
//	private GraphModel model;
	
//	public STGLayoutCache(STG stg, GraphModel model) {
//		super(model, new STGViewFactory());
//		this.stg = stg;
//		
//		this.model = model;
//		
//		
//	}
	
	public void init() {
		
//		Map<Node, DefaultGraphCell> nc = new HashMap<Node, DefaultGraphCell>();
//		
//		
//		for (Node node : stg.getNodes()) {
//			DefaultGraphCell cell=null;
//			if (node instanceof Place )
//				cell = new PlaceCell((Place)node);
//			else if (node instanceof Transition)
//				cell = new TransitionCell((Transition)node, stg);
//			
//			
//			
//			nc.put(node, cell);
//			insert(cell);
//			
//		}
//		
//		for (Node node : stg.getNodes()) {
//			DefaultGraphCell source=nc.get(node);
//			
//			
//			for (Node child : node.getChildren()) {
//				DefaultGraphCell target=nc.get(child);
//				
//				source.add(new DefaultPort());
//				target.add(new DefaultPort());
//				
//				DefaultEdge edge = new DefaultEdge();
//				edge.setSource(source.getChildAt(0));
//				edge.setTarget(target.getChildAt(0));
//				
//				GraphConstants.setFont(edge.getAttributes(), STGEditorFrame.STANDARD_FONT);
//				GraphConstants.setLabelPosition(edge.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE/2, 10));
//				
//				
//				
//				int v = node.getChildValue(child);
//				if (v>1)
//					edge.setUserObject(v);
//				
//				int arrow = GraphConstants.ARROW_TECHNICAL;
//				GraphConstants.setLineEnd(edge.getAttributes(), arrow);
//				GraphConstants.setEndFill(edge.getAttributes(), true);
//				
//				
//				insert(edge);
//			}
//		}
//		
//		model.addGraphModelListener(this);
		
	}


//	public void graphChanged(GraphModelEvent event) {
//		Map change = event.getChange().getAttributes();
//		
//		for (Object o : change.keySet()) {
//			if (o instanceof ApplyAttributes)
//				((ApplyAttributes)o).applyAttributes();
//		}
//	}
	
	
	
	
}
