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
