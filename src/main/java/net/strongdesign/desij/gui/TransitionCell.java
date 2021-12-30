

package net.strongdesign.desij.gui;

import java.awt.Point;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

public class TransitionCell extends mxCell implements ApplyAttributes{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6514074588507586122L;

	private int identifier;
	
	public TransitionCell(Transition transition, STG stg) {
		super(transition.getString(Transition.UNIQUE));
		
		
		Point co = transition.getSTG().getCoordinates(transition);
		if (co==null) {
			co = new Point(50,50);
		}
		
		mxGeometry geometry = new mxGeometry(co.x, co.y, 70, 20);
		setId(null);
		setConnectable(true);
		setVertex(true);
		setGeometry(geometry);
		
		
		setIdentifier(transition.getIdentifier());
		String label=transition.getString(Transition.UNIQUE);
		setValue(label);
		
		int signalID=transition.getLabel().getSignal();
		
		if (stg.getSignature(signalID) == Signature.INPUT)
			setStyle("fontColor=red");
		if (stg.getSignature(signalID) == Signature.OUTPUT)
			setStyle("fontColor=blue");
		if (stg.getSignature(signalID) == Signature.INTERNAL)
			setStyle("fontColor=green");
		if (stg.getSignature(signalID) == Signature.ANY)
			setStyle("fontColor=black;fillColor=yellow");

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

	public void setIdentifier(int nodeId) {
		identifier = nodeId;
	}

	public int getIdentifier() {
		return identifier;
	}
	
}
