

package net.strongdesign.desij.gui;


import net.strongdesign.stg.STG;


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
