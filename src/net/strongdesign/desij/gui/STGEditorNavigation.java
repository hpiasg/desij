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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.strongdesign.balsa.breezefile.ComponentSTGFactory;
import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.partitioning.PartitionerBreezePartition;
import net.strongdesign.desij.decomposition.partitioning.PartitionerCommonCauseSubnet;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGCoordinates;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.solvers.CSCSolver;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;
import net.strongdesign.util.StreamGobbler;


public class STGEditorNavigation extends JTree implements
		TreeSelectionListener, ActionListener, KeyListener, MouseListener {

	public final STGEditorAction DELETE_SELECTED = new STGEditorAction("Delete selected nodes", 0 , null, 0, this);
	public final STGEditorAction PARALLEL_COMPOSITION = new STGEditorAction("Parallel composition", 0 , null, 0, this);
	public final STGEditorAction SYNCHRONOUS_PRODUCT = new STGEditorAction("Synchronous product (same signature on both STGs)", 0 , null, 0, this);
	public final STGEditorAction DUMMY_SURROUNDING = new STGEditorAction("Show surrounding for each dummy", 0 , null, 0, this);

	public final STGEditorAction PETRIFY = new STGEditorAction("Process STG with Petrify", 0 , null, 0, this);
	public final STGEditorAction PETRIFY_CSC = new STGEditorAction("Use Petrify to solve CSC", 0 , null, 0, this);
	public final STGEditorAction MPSAT_CSC = new STGEditorAction("Use Punf/Mpsat to solve CSC", 0 , null, 0, this);
	
	public final STGEditorAction REPORT_IMPLEMENTABLE = new STGEditorAction("Report implementable signals", 0 , null, 0, this);
	public final STGEditorAction REPORT_IMPLEMENTABLE_SW = new STGEditorAction("Report implementable sw partitions", 0 , null, 0, this);
	
	public final STGEditorAction REPORT_PROBLEMATIC_TRIGGERS = new STGEditorAction("Report problematic triggers", 0, null, 0, this);

	
	public final STGEditorAction RELAX_INJECTIVE = new STGEditorAction("Split common paths", 0 , null, 0, this);
	public final STGEditorAction RELAX_INJECTIVE2 = new STGEditorAction("Split merged places", 0 , null, 0, this);
	
	public final STGEditorAction SIMPLE_DUMMY_REMOVAL = new STGEditorAction("Simple dummy removal", 0 , null, 0, this);
	
	
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
		
		if (source == SYNCHRONOUS_PRODUCT) synchronousProduct();
		
		if (source == DUMMY_SURROUNDING) dummySurrounding();
		
		if (source == PETRIFY) usePetrify(null);
		if (source == PETRIFY_CSC) usePetrify("-csc");
		
		if (source == MPSAT_CSC) solveCSCWithMpsat();
		
		
		if (source == REPORT_IMPLEMENTABLE) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			try {
				
				int cnt = stgin.getTransitions(ConditionFactory.ALL_TRANSITIONS).size();
				int i=0;
				int success=0;
				int failure=0;
				HashSet<Integer> processed = new HashSet<Integer>();
				HashSet<Integer> succeeded = new HashSet<Integer>();
				
				for (Transition transition: stgin.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
					i++;
					if (stgin.getSignature(transition.getLabel().getSignal())!=Signature.OUTPUT&&
						stgin.getSignature(transition.getLabel().getSignal())!=Signature.INTERNAL) continue;
					
					if (processed.contains(transition.getLabel().getSignal())) continue;
					
					
					Integer signal = transition.getLabel().getSignal(); 
					processed.add(signal);
					
					Partition partition = new Partition();
					String sigName = stgin.getSignalName(signal);
					partition.addSignal(sigName);
					
					System.out.println("Trying signal:"+sigName+" ("+i+" of "+cnt+") so far succeeded:"+success+" failed:"+failure);
					
					for (STG stg: Partition.splitByPartition(stgin, partition)) {
						
						STGUtil.removeDummiesBreeze(stg, true, false);
						
						try {
							stg = CSCSolver.solveCSCWithMpsat(stg);
							
							if (stg!=null) {
								System.out.println("Success!");
								success++;
								succeeded.add(signal);
								frame.addSTG(stg, sigName);
							} else {
								System.out.println("Failure");
								failure++;
							}
						} catch (IOException e1) {
							failure++;
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							failure++;
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
					}
					
				}
				
				System.out.println("succeeded:"+success+" failed:"+failure+" \nSignals:");
				for (Integer sig: succeeded) {
					System.out.print(stgin.getSignalName(sig)+" ");
				}
				
				
			} catch (STGException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if (source == REPORT_PROBLEMATIC_TRIGGERS) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			PartitionerCommonCauseSubnet partition;
			
			try {
				partition = new PartitionerCommonCauseSubnet(stgin);
				partition.reportProblematicTriggers();
			} catch (STGException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if (source == REPORT_IMPLEMENTABLE_SW) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			PartitionerBreezePartition partition;
			try {
				partition = new PartitionerBreezePartition(stgin);
				partition.gatherSignalInfo();
				
			} catch (STGException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
//			
//			try {
//				
//				int cnt = stgin.getTransitions(ConditionFactory.ALL_TRANSITIONS).size();
//				int i=0;
//				int success=0;
//				int failure=0;
//				HashSet<Integer> processed = new HashSet<Integer>();
//				HashSet<Integer> succeeded = new HashSet<Integer>();
//				
//				for (Transition transition: stgin.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
//					i++;
//					if (stgin.getSignature(transition.getLabel().getSignal())!=Signature.OUTPUT&&
//						stgin.getSignature(transition.getLabel().getSignal())!=Signature.INTERNAL) continue;
//					
//					if (processed.contains(transition.getLabel().getSignal())) continue;
//					
//					
//					
//					Integer signal = transition.getLabel().getSignal(); 
//					processed.add(signal);
//					
//					Partition partition = new Partition();
//					String sigName = stgin.getSignalName(signal);
//					partition.addSignal(sigName);
//					
//					System.out.println("Trying signal:"+sigName+" ("+i+" of "+cnt+") so far succeeded:"+success+" failed:"+failure);
//					
//					for (STG stg: Partition.splitByPartition(stgin, partition)) {
//						
//						STGUtil.removeDummiesBreeze(stg, true, false);
//						
//						try {
//							stg = CSCSolver.solveCSCWithMpsat(stg);
//							
//							if (stg!=null) {
//								System.out.println("Success!");
//								success++;
//								succeeded.add(signal);
//								frame.addSTG(stg, sigName);
//							} else {
//								System.out.println("Failure");
//								failure++;
//							}
//						} catch (IOException e1) {
//							failure++;
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						} catch (InterruptedException e1) {
//							failure++;
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						
//					}
//					
//				}
//				
//				System.out.println("succeeded:"+success+" failed:"+failure+" \nSignals:");
//				for (Integer sig: succeeded) {
//					System.out.print(stgin.getSignalName(sig)+" ");
//				}
//				
//				
//			} catch (STGException e1) {
//				e1.printStackTrace();
//			}
			
		}
		
		if (source == RELAX_INJECTIVE) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			STG stg = stgin.clone();
			
			STGUtil.relaxInjectiveSplitSharedPath2(stg);
			frame.addSTG(stg, "Relaxed");
		}
		
		if (source == RELAX_INJECTIVE2) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			STG stg = stgin.clone();
			
			STGUtil.relaxInjectiveSplitMergePlaces(stg);
			frame.addSTG(stg, "Relaxed2");
		}
		
		
		if (source == SIMPLE_DUMMY_REMOVAL) {
			TreePath path = getSelectionPaths()[0]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			STG stgin = node.getSTG();
			graphComponent.storeCoordinates(stgin.getCoordinates());
			
			STG stg = stgin.clone();
			
			STGUtil.simpleDummyRemoval2(stg);
			frame.addSTG(stg, "Simplified");
			
		}
	}
	
	public void usePetrify(String options) {
		if (options==null) options="";
		
		TreePath path = getSelectionPaths()[0]; 
		STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
		STG stgin = node.getSTG();
		
		try {
			
			
			STG stg = CSCSolver.petrifySTG(stgin, options);
			
			
			if (stg!=null) {
				frame.addSTG(stg, "Petrified");
				frame.setLayout(7);
			}
			
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving petrify:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error involving petrify: " + e.getMessage());
		}
	}
	
	
	public void solveCSCWithMpsat() {
		
		TreePath path = getSelectionPaths()[0]; 
		STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
		STG stgin = node.getSTG();
		
		try {
			
			STG stg = CSCSolver.solveCSCWithMpsat(stgin);
			
			if (stg!=null) {
				frame.addSTG(stg, "Solved CSC (mpsat)");
				frame.setLayout(7);
			}
			
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving petrify:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error involving petrify: " + e.getMessage());
		}
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
	
	public void dummySurrounding() {
		
		
		TreePath path = getSelectionPaths()[0];
		STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
	
		STG stg = node.getSTG().clone();
		STGInOutParameter componentParameter = new STGInOutParameter(stg);
		
		try {
			
			BasicDecomposition deco = new BasicDecomposition("basic", stg);
			deco.reduce(componentParameter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Transition t: stg.getTransitions(ConditionFactory.IS_DUMMY)) {
			STG sur = STGUtil.getNodeSurrounding(stg, t, 2);
			frame.addSTG(sur, "surrounding for "+t.getString(Node.UNIQUE));
			//frame.setLayout(7);
		}
		
	}
	
	public void synchronousProduct() {
		
		int len = getSelectionPaths().length;
		LinkedList<STG> stgs = new LinkedList<STG>();
		
		for (int i=0;i<len;i++) {
			TreePath path = getSelectionPaths()[i]; 
			STGEditorTreeNode node= (STGEditorTreeNode)path.getLastPathComponent();
			stgs.add(node.getSTG());
		}
		
		if (stgs.size()!=2) return;
		
		STG stg= STGUtil.synchronousProduct(stgs, true);
		
		if (stg!=null) {
			frame.addSTG(stg, "Product");
		}
		
	}
	
	public void deleteSelectedNodes() {
		if (getSelectedNode()==null) return;
		
		// find some parent node to show next
		STGEditorTreeNode next =  getSelectedNode().getParent();
		
//		getSelectionPaths();
		
	//	((DefaultTreeModel)getModel()).removeNodeFromParent(getSelectedNode());

		for (TreePath path : getSelectionPaths()) {
			((DefaultTreeModel)getModel()).removeNodeFromParent(
					(MutableTreeNode) path.getLastPathComponent());
			
		}
		
		if (next!=null) {
			if (next==root) {
				if (root.getChildCount()>0) {
					next=(STGEditorTreeNode)root.getFirstChild();
				} else {
					next=null;
				}
			}
			showNode(next);
		}
		
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
