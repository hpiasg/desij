/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.strongdesign.desij.gui;

import java.util.HashMap;
import java.util.Map;

import net.strongdesign.stg.STG;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphSelectionModel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.tree.JGraphTreeLayout;


public class STGEditorNavigation extends JGraph implements GraphSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1790108133777960660L;
	private GraphModel model;
    private GraphLayoutCache cache;
    
	private STGEditorFrame frame;
	private STGEditorTreeNode root;
	private STGEditorTreeNode currentNode;
	
	private JGraphFacade facade;
    
    
    
    private Map<STGEditorTreeNode, DefaultGraphCell> cells;
    
    public STGEditorNavigation(STG rootSTG, STGEditorFrame frame) {
        super ();
        this.frame = frame;
        
        cells = new HashMap<STGEditorTreeNode, DefaultGraphCell>();
        root = new STGEditorTreeNode("Start", rootSTG, true, null);
        currentNode = root;
     
        model = new DefaultGraphModel();
        cache = new GraphLayoutCache(model, new DefaultCellViewFactory());
                
        setModel(model);
        setGraphLayoutCache(cache);
    
        facade = new JGraphFacade(this, getRoots());
        
        frame.setNav(this);
        
        
        addNode(root, "Start");
        
        
        addGraphSelectionListener(this);
        getSelectionModel().setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
        
    }
    
    
    
    
    public STGEditorTreeNode getCurrentNode() {
		return currentNode;
	}
    
    
	public void setCurrentNode(STGEditorTreeNode currentNode) {
		this.currentNode = currentNode;		
	}




	public void addNode(STGEditorTreeNode node, String label) {
    	DefaultGraphCell cell = new NavigationCell(node, label);
    	cells.put(node, cell);
    	
    	
    	DefaultGraphCell parent = cells.get(node.getParent());
    	
    	if (parent!=null) {    		
    		DefaultPort parentPort =new DefaultPort();
    		parent.add(parentPort);
    		
    		DefaultPort cellPort =new DefaultPort();
    		cell.add(cellPort);
    		
    		DefaultEdge edge = new DefaultEdge();
    		edge.setSource(parentPort);
    		edge.setTarget(cellPort);
    		
    		int arrow = GraphConstants.ARROW_CLASSIC;
    		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
    		GraphConstants.setEndFill(edge.getAttributes(), true);
    		GraphConstants.setSelectable(edge.getAttributes(), false);
    		
    		cache.insert(edge);
    		
    	}
    	cache.insert(cell);
    	
    	
    	
    	new JGraphTreeLayout().run(facade);
    	cache.edit(facade.createNestedMap(true, false));
    	
    	setSelectionCell(null);
    	
    }




	public void valueChanged(GraphSelectionEvent event) {
		Object cell =  event.getCell();
		
		if ( !(cell instanceof NavigationCell))
			return;
		
		STGEditorTreeNode node = ((NavigationCell)cell).getTreeNode();
		if (node==currentNode)
			return;
		
		setCurrentNode(node);
		
		if (event.isAddedCell()) {
			frame.setSTG( ((NavigationCell)cell).getTreeNode());
		}
	

		
	}
    
    
}
    
//	public void selectNode (STGEditorTreeNode node) {
//		tree.setSelectionPath(new TreePath(node.getPath()));
//	}
//    
//    public void valueChanged(TreeSelectionEvent e){
//		try {
//            STGEditorTreeNode node = (STGEditorTreeNode) e.getPath().getLastPathComponent();
//		    frame.setSTG(node);
//        }
//        catch (Exception ee) {
//            ee.printStackTrace();
//        }
//    }
//    
//    public DefaultTreeModel getModel() {
//        return model;
//    }
//    
//    public void showNode(STGEditorTreeNode node) {
//        tree.setSelectionPath(new TreePath( node.getPath()));  
//		tree.expandPath(new TreePath( node.getPath()));
//    }
//	
//	public void mouseReleased(MouseEvent e)  {		
//	}
//	public void mousePressed(MouseEvent e)  {	
//		if (e.getButton() == MouseEvent.BUTTON3) {
//			JPopupMenu pop = new STGEditorNavigationPopUp(this);
//			pop.show(e.getComponent(), e.getX(), e.getY());
//			//selectedNode = (DefaultMutableTreeNode) tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
//		}
//	}
//	public void mouseClicked(MouseEvent e)  {		
//	}
//	public void mouseExited(MouseEvent e)  {		
//	}
//	public void mouseEntered(MouseEvent e)  {		
//	}
//
//	public void actionPerformed(ActionEvent e)  {
//		String cmd = e.getActionCommand();
//		
//		if (cmd.equals("Delete selected nodes"))
//			deleteSelectedNodes();
//	}
//	
//	public void deleteSelectedNodes()  {
//		for (TreePath path : tree.getSelectionPaths()) {
//			model.removeNodeFromParent((DefaultMutableTreeNode) path.getLastPathComponent());
//		}	
//	}
//}
