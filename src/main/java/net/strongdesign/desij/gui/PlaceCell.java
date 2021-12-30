

package net.strongdesign.desij.gui;

import java.awt.Point;

import net.strongdesign.stg.Place;

//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.GraphConstants;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

public class PlaceCell extends mxCell implements ApplyAttributes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5858658330662109563L;
	
	private int identifier;
	
	public PlaceCell(Place place) {		
		super(place.getMarking()>0?place.getMarking():"");
		
		setIdentifier(place.getIdentifier());
		
		Point co = place.getSTG().getCoordinates(place);
		if (co==null) {
			co = new Point(50,50);
		}
		
		mxGeometry geometry = new mxGeometry(co.x, co.y, 25, 25);
		
		setId(null);
		setConnectable(true);
		setVertex(true);
		setGeometry(geometry);
		
		setStyle("shape=ellipse;perimeter=ellipsePerimeter");
		
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

	public void setIdentifier(int nodeId) {
		this.identifier = nodeId;
	}

	public int getIdentifier() {
		return identifier;
	}


}
