package net.strongdesign.desij.gui;

import java.awt.Point;
import java.util.HashMap;


import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;

public class STGEditorCoordinates extends HashMap<Node, Point> {
	private static final long serialVersionUID = 1708074655039330258L;

	public STGEditorCoordinates clone(STG stg) {
		STGEditorCoordinates result = new STGEditorCoordinates();
		
		for (Node node : stg.getNodes()) {
			Point np =  get(node);
			if (np!=null)
				result.put(  (Node) node.clone(),   (Point)np.clone());
		}
		return result;
	}
	
}
