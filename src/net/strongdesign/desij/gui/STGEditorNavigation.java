/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011,2012 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.strongdesign.balsa.breezefile.ComponentSTGFactory;
import net.strongdesign.stg.STG;

//import org.jgraph.JGraph;
//import org.jgraph.event.GraphSelectionEvent;
//import org.jgraph.event.GraphSelectionListener;
//import org.jgraph.graph.DefaultCellViewFactory;
//import org.jgraph.graph.DefaultEdge;
//import org.jgraph.graph.DefaultGraphCell;
//import org.jgraph.graph.DefaultGraphModel;
//import org.jgraph.graph.DefaultPort;
//import org.jgraph.graph.GraphConstants;
//import org.jgraph.graph.GraphLayoutCache;
//import org.jgraph.graph.GraphModel;
//import org.jgraph.graph.GraphSelectionModel;
//
//import com.jgraph.layout.JGraphFacade;
//import com.jgraph.layout.tree.JGraphTreeLayout;


public class STGEditorNavigation extends JTree implements
		TreeSelectionListener, ActionListener, KeyListener, MouseListener {

	public final STGEditorAction DELETE_SELECTED = new STGEditorAction("Delete selected nodes", 0 , null, 0, this);
	public final STGEditorAction PARALLEL_COMPOSITION = new STGEditorAction("Parallel composition", 0 , null, 0, this);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1790108133777960660L;


	private STGEditorFrame frame;
	private STGGraphComponent graphComponent;
	private STGEditorTreeNode root;
	
	STGEditorTreeNode oldNode; // node remembered as the last node active, to store coordinates


	public STGEditorTreeNode getOldNode() {
		return oldNode;
	}


	public void setOldNode(STGEditorTreeNode oldNode) {
		this.oldNode = oldNode;
	}

	public STGEditorNavigation(STGEditorFrame frame, STGGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
		this.frame = frame;
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		
		root = new STGEditorTreeNode("root node");
		setModel(new DefaultTreeModel(root));
		
		addTreeSelectionListener(this);
		addKeyListener(this);
		addMouseListener(this);
	}
	
	public STGEditorTreeNode getRootNode() {
		return root;
	}

	public STGEditorTreeNode getSelectedNode() {
		return (STGEditorTreeNode)getLastSelectedPathComponent();
	}
	
	/**
	 * returns STGEditorNode of a selected project
	 * @return
	 */
	public STGEditorTreeNode getProjectNode() {
		STGEditorTreeNode sel = getSelectedNode();
		STGEditorTreeNode par = sel.getParent();
		// every node should lead to root, then it is a project node (?)
		while (par!=root) {
			sel=par;
			par = sel.getParent();
		}
		return sel;
	}

	public STGEditorTreeNode addSTGNode(STG stg,
			String label, boolean isRoot) {
		STGEditorTreeNode parent = root;
		
		if (!isRoot) parent = getSelectedNode();
		
		STGEditorTreeNode node = new STGEditorTreeNode(label, stg, false);
		
		parent.add(node);
		
	//	coordinates = graphComponent.initSTG(stg, coordinates);
		
		updateUI();
		
		return node;
	}

	public void addNode(STGEditorTreeNode node) {
		STGEditorTreeNode parent = root;
		
		parent = getSelectedNode();
		
		//STGEditorTreeNode node = new STGEditorTreeNode(label);
		parent.add(node);
		
	//	coordinates = graphComponent.initSTG(stg, coordinates);
		
		updateUI();

		// DefaultGraphCell cell = new NavigationCell(node, label);
		// cells.put(node, cell);
		//
		//
		// DefaultGraphCell parent = cells.get(node.getParent());
		//
		// if (parent!=null) {
		// DefaultPort parentPort =new DefaultPort();
		// parent.add(parentPort);
		//
		// DefaultPort cellPort =new DefaultPort();
		// cell.add(cellPort);
		//
		// DefaultEdge edge = new DefaultEdge();
		// edge.setSource(parentPort);
		// edge.setTarget(cellPort);
		//
		// int arrow = GraphConstants.ARROW_CLASSIC;
		// GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		// GraphConstants.setEndFill(edge.getAttributes(), true);
		// GraphConstants.setSelectable(edge.getAttributes(), false);
		//
		// cache.insert(edge);
		//
		// }
		// cache.insert(cell);
		//
		//
		// new JGraphTreeLayout().run(facade);
		// cache.edit(facade.createNestedMap(true, false));

		// setSelectionCell(null);

		// public void valueChanged(GraphSelectionEvent event) {
		// Object cell = event.getCell();
		//
		// if ( !(cell instanceof NavigationCell))
		// return;
		//
		// STGEditorTreeNode node = ((NavigationCell)cell).getTreeNode();
		// if (node==currentNode)
		// return;
		//
		// setCurrentNode(node);
		//
		// if (event.isAddedCell()) {
		// frame.setSTG( ((NavigationCell)cell).getTreeNode());
		// }
		//
		//
		//
	}

	public void refreshSelection() {
		try {
			STGEditorTreeNode node = getSelectedNode();
			
			frame.setTitle(node.getLabel());
			if (oldNode!=null&&oldNode.getSTG()!=null) {
				graphComponent.storeCoordinates(oldNode.getSTG().getCoordinates());
			}
			graphComponent.initSTG(node.getSTG(), frame.isShorthand());
			oldNode = node;
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		try {
			
			
			if (e.getPath().getLastPathComponent() instanceof STGEditorTreeNode) {
				
				STGEditorTreeNode node = (STGEditorTreeNode) e.getPath().getLastPathComponent();
				
				if (node==oldNode) return;
				
				frame.setTitle(node.getLabel());
				
				if (oldNode!=null&&oldNode.getSTG()!=null) {
					graphComponent.storeCoordinates(oldNode.getSTG().getCoordinates());
				}
				
				graphComponent.initSTG(node.getSTG(), frame.isShorthand());
				oldNode = node;
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}

	}

	public void selectNode(STGEditorTreeNode node) {
		setSelectionPath(new TreePath(node.getPath()));
	}

	public void showNode(STGEditorTreeNode node) {
		if (node==null) {
			graphComponent.initSTG(null, frame.isShorthand());
			frame.setTitle("DesiJ");
		} else {
			setSelectionPath(new TreePath(node.getPath()));
			expandPath(new TreePath(node.getPath()));
			frame.setTitle(node.getLabel());
		}
	}

	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu pop = new STGEditorNavigationPopUp(this);
			pop.show(e.getComponent(), e.getX(), e.getY());
			// selectedNode = (DefaultMutableTreeNode)
			getClosestPathForLocation(e.getX(), e.getY())
					.getLastPathComponent();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == DELETE_SELECTED) deleteSelectedNodes();
		
		if (source == PARALLEL_COMPOSITION) parallelComposition();
		
	}

	public void parallelComposition() {
		
		int len = getSelectionPaths().length;
		LinkedList<STG> stgs = new LinkedList<STG>();
		
		for (int i=0;i<len;i++) {
			TreePath path = getSelectionPaths()[i]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			stgs.add(node.getSTG());
		}
		
		STG stg= ComponentSTGFactory.parallelComposition(stgs, null);
		
		if (stg!=null) {
			frame.addSTG(stg, "Composed");
		}
		
	}
	
	public void deleteSelectedNodes() {
		// find some parent node to show next
		STGEditorTreeNode next =  getSelectedNode().getParent();
		
//		getSelectionPaths();
		
	//	((DefaultTreeModel)getModel()).removeNodeFromParent(getSelectedNode());

		for (TreePath path : getSelectionPaths()) {
			((DefaultTreeModel)getModel()).removeNodeFromParent(
					(MutableTreeNode) path.getLastPathComponent());
			
		}
		
		if (next==root) {
			if (root.getChildCount()>0) {
				next=(STGEditorTreeNode)root.getFirstChild();
			} else {
				next=null;
			}
		}
		showNode(next);
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		//
	}


	@Override
	public void keyReleased(KeyEvent e) {
		//
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar()==KeyEvent.VK_DELETE) {
			deleteSelectedNodes();
		}
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
