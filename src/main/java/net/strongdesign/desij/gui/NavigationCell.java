

package net.strongdesign.desij.gui;


//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.GraphConstants;

//import com.mxgraph.layout.hierarchical.model.mxGraphAbstractHierarchyCell;
import com.mxgraph.layout.hierarchical.model.mxGraphHierarchyNode;
//import com.mxgraph.swing.mxGraphComponent;
//import com.mxgraph.util.mxConstants;

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
