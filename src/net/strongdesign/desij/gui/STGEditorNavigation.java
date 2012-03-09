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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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
		TreeSelectionListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1790108133777960660L;

	// private GraphLayoutCache cache;
	// private JGraphFacade facade;

	private STGEditorFrame frame;
	private STGGraphComponent graphComponent;
	private STGEditorTreeNode root;
	
	STGEditorTreeNode currentNode;//?
	STGEditorTreeNode oldNode; // node remembered as the last node active, to store coordinates


	public STGEditorTreeNode getOldNode() {
		return oldNode;
	}


	public void setOldNode(STGEditorTreeNode oldNode) {
		this.oldNode = oldNode;
	}


	public STGEditorTreeNode getCurrentNode() {
		return currentNode;
	}


	public void setCurrentNode(STGEditorTreeNode currentNode) {
		this.currentNode = currentNode;
	}

/*
	public DefaultTreeModel createTestTree() {

		// add tree nodes test
		root = new STGEditorTreeNode("root node");
		
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode book = null;

		DefaultTreeModel dtm = new DefaultTreeModel(root);
		
		category = new STGEditorTreeNode("Books for Java Programmers");
		root.add(category);

		// original Tutorial
		book = new STGEditorTreeNode(
				"The Java Tutorial: A Short Course on the Basics");
		category.add(book);

		// Tutorial Continued
		book = new STGEditorTreeNode(
				"The Java Tutorial Continued: The Rest of the JDK");
		category.add(book);

		// Swing Tutorial
		book = new STGEditorTreeNode(
				"The Swing Tutorial: A Guide to Constructing GUIs");
		category.add(book);

		// ...add more books for programmers...

		category = new STGEditorTreeNode("Books for Java Implementers");
		root.add(category);

		// VM
		book = new STGEditorTreeNode(
				"The Java Virtual Machine Specification");
		category.add(book);
		
		// Language Spec
		book = new STGEditorTreeNode("The Java Language Specification");
		category.add(book);
		
		return dtm;
	}*/


	public STGEditorNavigation(STGEditorFrame frame, STGGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
		this.frame = frame;
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		
		root = new STGEditorTreeNode("root node");
		setModel(new DefaultTreeModel(root));
		
		addTreeSelectionListener(this);
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

	public STGEditorTreeNode addSTGNode(STG stg, STGEditorCoordinates coordinates,
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

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		try {
			if (e.getPath().getLastPathComponent() instanceof STGEditorTreeNode) {
				STGEditorTreeNode node = (STGEditorTreeNode) e.getPath().getLastPathComponent();
				if (node==oldNode) return;
//				frame.setSTG(node);
				frame.setTitle(node.getLabel());
				
				if (oldNode!=null) {
					graphComponent.storeCoordinates(oldNode.getCoordinates());
				}
				
				graphComponent.initSTG(node.getSTG(), node.getCoordinates());
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
		setSelectionPath(new TreePath(node.getPath()));
		expandPath(new TreePath(node.getPath()));
		frame.setTitle(node.getLabel());
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
		String cmd = e.getActionCommand();

		if (cmd.equals("Delete selected nodes"))
			deleteSelectedNodes();
	}

	public void deleteSelectedNodes() {
		for (TreePath path : getSelectionPaths()) {
			((DefaultTreeModel)getModel()).removeNodeFromParent(
					(DefaultMutableTreeNode) path.getLastPathComponent());
		}
	}
}
