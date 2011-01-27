package net.strongdesign.desij.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STGException;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

public class PlaceCell extends DefaultGraphCell implements ApplyAttributes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5858658330662109563L;
	private Place place;
	public PlaceCell(Place place) {		
		super(place.getMarking()>0?place.getMarking():"");
		this.place = place;
		
		Point co = place.getSTG().getCoordinates(place);
		if (co==null)
			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(Math.random()*50,Math.random()*50,22,22));
		else
			GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(co.x,co.y,22,22));
		GraphConstants.setOpaque(getAttributes(), true);
		GraphConstants.setBorderColor(getAttributes(), Color.BLACK);
		GraphConstants.setFont(getAttributes(), STGEditorFrame.STANDARD_FONT);
	}

	public void applyAttributes() {
		
		Rectangle2D pos = GraphConstants.getBounds(getAttributes());		
		try {
			place.getSTG().setCoordinates(place, new Point((int)pos.getX(), (int)pos.getY()));
		} catch (STGException e) {
			e.printStackTrace();
		}
	}

}
