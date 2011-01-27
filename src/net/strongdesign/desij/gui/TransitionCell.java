package net.strongdesign.desij.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

public class TransitionCell extends DefaultGraphCell implements ApplyAttributes{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3924546389856822747L;
	private Transition transition;
	
	public TransitionCell(Transition transition, STG stg) {
		super(transition.getLabel());
		
		this.transition = transition; 
		
		Point co = transition.getSTG().getCoordinates(transition);
		if (co==null)
			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,22,22));
		else
			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(co.x,co.y,22,22));
		
		GraphConstants.setAutoSize(getAttributes(), true);
		GraphConstants.setOpaque(getAttributes(), true);
		GraphConstants.setFont(getAttributes(), STGEditorFrame.STANDARD_FONT);
		GraphConstants.setBorderColor(getAttributes(), Color.BLACK);
		GraphConstants.setInset(getAttributes(), 2);
		
		
		
		switch (stg.getSignature(transition.getLabel().getSignal())) {
		case INPUT: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.INPUT); break;
		case OUTPUT: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.OUTPUT); break;
		case DUMMY: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.DUMMY); break;
		case INTERNAL: GraphConstants.setGradientColor(getAttributes(), STGEditorFrame.INTERNAL); break;		
		}
	}

	public void applyAttributes() {		
		Rectangle2D pos = GraphConstants.getBounds(getAttributes());
		try {
			transition.getSTG().setCoordinates(transition, new Point((int)pos.getX(), (int)pos.getY()));
		} catch (STGException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
