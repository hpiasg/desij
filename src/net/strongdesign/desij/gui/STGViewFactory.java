package net.strongdesign.desij.gui;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.VertexView;

public class STGViewFactory extends DefaultCellViewFactory  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8799917022203992830L;

	@Override
	protected VertexView createVertexView(Object arg0) {
		if (arg0 instanceof PlaceCell)
			return new PlaceView(arg0);
		return super.createVertexView(arg0);
		
	}

	

}
