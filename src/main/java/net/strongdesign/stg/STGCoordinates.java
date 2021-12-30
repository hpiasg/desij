

package net.strongdesign.stg;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;


import net.strongdesign.desij.gui.STGGraphComponent;

public class STGCoordinates extends HashMap<Node, Point> {
	private static final long serialVersionUID = 1708074655039330258L;

	public STGCoordinates clone(STG stg) {
		STGCoordinates result = new STGCoordinates();
		
		for (Node node : stg.getNodes()) {
			Point np =  get(node);
			if (np!=null)
				result.put(  (Node) node.clone(),   (Point)np.clone());
		}
		return result;
	}
	
	public STGCoordinates() {
		super();
	}
	
	public STGCoordinates(STGGraphComponent component) {
		
	}
	
}
