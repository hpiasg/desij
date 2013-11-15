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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.strongdesign.desij.decomposition.AbstractDecomposition.Reason;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.partitioning.PartitionerBreezePartition;
import net.strongdesign.desij.decomposition.partitioning.PartitionerCommonCauseSubnet;
import net.strongdesign.stg.NeighbourTrackingNodeRemover;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.NodeRemover;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGCoordinates;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.SignalValue;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.ConditionFactory;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class STGGraphComponent extends mxGraphComponent {
	
	class STGGraphComponentPopupMenu extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1908959334150872792L;
		STGGraphComponent component;
		
		public JMenuItem contractTransition = new JMenuItem("Contract selected transition"); 
		public JMenuItem makeSignalDummy = new JMenuItem("Dummify selected signal");
		public JMenuItem dummifyAllButSelected = new JMenuItem("Dummify all but selected signals"); 
		
		public JMenuItem fireTransition = new JMenuItem("Fire selected transition"); 
		public JMenuItem unFireTransition = new JMenuItem("Unfire selected transition");
		public JMenuItem renameSignal = new JMenuItem("Rename signal");
		
		public JMenuItem injectiveLabelling = new JMenuItem("Apply injective labelling"); 
		
		public JMenuItem showSurrounding = new JMenuItem("Show node surrounding"); 
		
		public JMenuItem showTransitionInfo = new JMenuItem("Show transition info"); 
		public JMenuItem showPlaceInfo = new JMenuItem("Show place info"); 
		
		public JMenuItem showLockedSignals = new JMenuItem("Show locked signals"); 
		public JMenuItem showPossibleCSCConflicts = new JMenuItem("Show potential CSC violations"); 
		public JMenuItem showOutputsWithCSC = new JMenuItem("Show outputs with CSC"); 
		
		public JMenuItem showNextSequenceTransition = new JMenuItem("Show next sequence transition"); 
		public JMenuItem showPrevSequenceTransition = new JMenuItem("Show previous sequence transition");
		
		public JMenuItem createComponent = new JMenuItem("Create a component from selected transition(s)");
		public JMenuItem createLevel2Component = new JMenuItem("Create level 2 component from the selected transition");
		public JMenuItem createSandwichComponent = new JMenuItem("Create a sandwich component from the selected transition"); 
		
		
		public JMenuItem reportCommonCause = new JMenuItem("Report Common Cause signals");
		public JMenuItem reportCommonCauseIgnored = new JMenuItem("Report Common Cause signals (with ignore)");
		
		
		public JMenuItem makeExclusive = new JMenuItem("Make selected signals exclusive"); 


		public List<Transition> transitionsToProcess = new LinkedList<Transition>();
		public List<Node> nodesToProcess = new LinkedList<Node>();
		
		
		
		public STGGraphComponentPopupMenu(STGGraphComponent component, boolean transitions) {
			super();
			this.component = component;
			
			
			// actions only available for transitions
			if (transitions) {
				add(contractTransition);
				add(makeSignalDummy);
				add(dummifyAllButSelected);
				add(fireTransition);
				add(unFireTransition);
				add(renameSignal);
				add(showTransitionInfo);
				add(showNextSequenceTransition);
				add(showPrevSequenceTransition);
				
				add(createComponent);
				add(createLevel2Component);
				add(createSandwichComponent);
				add(makeExclusive);
				
				add(reportCommonCause);
				add(reportCommonCauseIgnored);
				
				contractTransition.addActionListener(this);
				makeSignalDummy.addActionListener(this);
				dummifyAllButSelected.addActionListener(this);
				
				fireTransition.addActionListener(this);
				unFireTransition.addActionListener(this);
				renameSignal.addActionListener(this);
				showTransitionInfo.addActionListener(this);
				showNextSequenceTransition.addActionListener(this);
				showPrevSequenceTransition.addActionListener(this);
				createComponent.addActionListener(this);
				createLevel2Component.addActionListener(this);
				createSandwichComponent.addActionListener(this);
				makeExclusive.addActionListener(this);
				reportCommonCause.addActionListener(this);
				reportCommonCauseIgnored.addActionListener(this);
				
			} else { // actions only available for places
				
				add(showPlaceInfo);
				showPlaceInfo.addActionListener(this);
			}
			
			// actions for all nodes
			add(showLockedSignals);
			add(showPossibleCSCConflicts);
			add(injectiveLabelling);
			add(showSurrounding);
			add(showOutputsWithCSC);
			showSurrounding.addActionListener(this);
			injectiveLabelling.addActionListener(this);
			showLockedSignals.addActionListener(this);
			showPossibleCSCConflicts.addActionListener(this);
			showOutputsWithCSC.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (component.activeSTG==null) return;
			if (activeSTG==null) return;
			
			
			if (e.getSource()==injectiveLabelling) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				if (nodesToProcess.get(0) instanceof Place) {
					
					STGUtil.tryZipUp(component.activeSTG, (Place)nodesToProcess.get(0), null);
					
					STGUtil.tryZipDown(component.activeSTG, (Place)nodesToProcess.get(0));
				} else {
					STGUtil.enforceInjectiveLabelling(component.activeSTG, transitionsToProcess.get(0));
				}
				
				
				component.initSTG(activeSTG, component.frame.isShorthand());
				component.frame.refreshSTGInfo();
			}
			
			if (e.getSource()==contractTransition) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				try {
					
					BasicDecomposition deco = new BasicDecomposition("basic", activeSTG);
					Transition t = transitionsToProcess.get(0);
					Reason r = deco.isContractable(t);
					if (r!=Reason.OK) {
						JOptionPane.showMessageDialog(null, deco.lastMessage);
					} else {
						deco.contract(activeSTG, transitionsToProcess);
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				component.initSTG(activeSTG, component.frame.isShorthand());
				component.frame.refreshSTGInfo();
			}
			
			if (e.getSource()==makeSignalDummy) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				Transition t = transitionsToProcess.get(0);
				component.activeSTG.setSignature(t.getLabel().getSignal(), Signature.DUMMY);
				
				component.initSTG(activeSTG, component.frame.isShorthand());
				component.frame.refreshSTGInfo();
			}
			
			
			if (e.getSource()==dummifyAllButSelected) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				HashSet<Integer> signals = new HashSet<Integer>();
				
				for (Transition t: transitionsToProcess) {
					signals.add(t.getLabel().getSignal());
				}
				
				
				for (Transition t: activeSTG.getTransitions(ConditionFactory.ALL_TRANSITIONS)) {
					
					if (!signals.contains(t.getLabel().getSignal())) {
						component.activeSTG.setSignature(t.getLabel().getSignal(), Signature.DUMMY);
					}
					
				}
				
				component.initSTG(activeSTG, component.frame.isShorthand());
				component.frame.refreshSTGInfo();
			}
			
			
			if (e.getSource()==fireTransition) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				Transition t = transitionsToProcess.get(0);
				component.activeSTG.fireTransition(t);
				
				component.initSTG(activeSTG, component.frame.isShorthand());
			}
			
			if (e.getSource()==unFireTransition) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				Transition t = transitionsToProcess.get(0);
				component.activeSTG.unFireTransition(t);
				
				component.initSTG(activeSTG, component.frame.isShorthand());
			}
			
			if (e.getSource()==renameSignal) {
				
				storeCoordinates(component.activeSTG.getCoordinates());
				
				Transition t = transitionsToProcess.get(0);
				String oldName = activeSTG.getSignalName(t.getLabel().getSignal());
				
				String newName = JOptionPane.showInputDialog(null, "Change signal name: ", oldName);
				if (newName!=null) newName = newName.trim();
				
				if (newName!=null&&newName!="") {
					HashMap<String,String> signalRenaming = new HashMap<String,String>();
					
					signalRenaming.put(oldName, newName);
					try {
						activeSTG.renameSignals(signalRenaming);
					} catch (STGException e1) {
						e1.printStackTrace();
					}
				}
				
				component.initSTG(activeSTG, component.frame.isShorthand());
			}
			
			
			if (e.getSource()==showTransitionInfo&&activeSTG!=null) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					String result = "";
					Node n = nodesToProcess.get(0);
					int i=0;
					
					TreeSet<String> strings = new TreeSet<String>();
					for (Entry<Integer, SignalValue> en: partitioner.getTransitionSignalInfo((Transition)n).entrySet()) {
						strings.add(activeSTG.getSignalName(en.getKey())+"="+en.getValue());
					}
					
					for (String s: strings) {
						result+=s+" ";
						i++;
						if (i%10==0) result+="\n";
						
					}
					result+=".";
					
					
					JOptionPane.showMessageDialog(null,result);
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==reportCommonCause) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					// create a new cluster from a given transition
					HashSet<Transition> triggers = new HashSet<Transition>();
					HashSet<Transition> triggered = new HashSet<Transition>();
					
					triggered.add((Transition)transitionsToProcess.get(0));
					
					partitioner.gatherPartition(triggers, triggered);
					
					HashSet<Integer> processed = new HashSet<Integer>();
					
					
					processed.clear();
					System.out.println("\nTriggers:");
					for (Transition tran: triggers) {
						if (processed.contains(tran.getLabel().getSignal())) continue;
						processed.add(tran.getLabel().getSignal());
						System.out.print(" "+activeSTG.getSignalName(tran.getLabel().getSignal()));
					}
					
					processed.clear();
					System.out.println("\nTriggered:");
					for (Transition tran: triggered) {
						if (processed.contains(tran.getLabel().getSignal())) continue;
						processed.add(tran.getLabel().getSignal());
						System.out.print(" "+activeSTG.getSignalName(tran.getLabel().getSignal()));
					}
					System.out.println();
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			
			if (e.getSource()==reportCommonCauseIgnored) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					// create a new cluster from a given transition
					HashSet<Transition> triggers = new HashSet<Transition>();
					HashSet<Transition> triggered = new HashSet<Transition>();
					
					Integer ignoredSignal = transitionsToProcess.get(0).getLabel().getSignal();
					
					triggered.add((Transition)transitionsToProcess.get(0));
					triggers.add((Transition)transitionsToProcess.get(0));
					
					partitioner.gatherPartition(triggers, triggered);
					
					HashSet<Integer> processed = new HashSet<Integer>();
					
					processed.clear();
					System.out.println("\nTriggers:");
					for (Transition tran: triggers) {
						if (tran.getLabel().getSignal().equals(ignoredSignal)) continue;
						if (processed.contains(tran.getLabel().getSignal())) continue;
						processed.add(tran.getLabel().getSignal());
						System.out.print(" "+activeSTG.getSignalName(tran.getLabel().getSignal()));
					}
					
					processed.clear();
					System.out.println("\nTriggered:");
					for (Transition tran: triggered) {
						if (tran.getLabel().getSignal().equals(ignoredSignal)) continue;
						if (processed.contains(tran.getLabel().getSignal())) continue;
						processed.add(tran.getLabel().getSignal());
						System.out.print(" "+activeSTG.getSignalName(tran.getLabel().getSignal()));
					}
					System.out.println();
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			
			if (e.getSource()==createComponent) {
				try {
				
					Partition partition = new Partition();
					HashSet<Integer> addedSignals = new HashSet<Integer>();
					for (Node n: nodesToProcess) {
						
						if (!(n instanceof Transition)) continue;
						Transition t = (Transition)n;
						
						
						if (addedSignals.contains(t.getLabel().getSignal())) continue;
								
						if (activeSTG.getSignature(t.getLabel().getSignal()) != Signature.INTERNAL
						  &&activeSTG.getSignature(t.getLabel().getSignal()) != Signature.OUTPUT) continue;
						
						partition.addSignal(activeSTG.getSignalName(t.getLabel().getSignal()));
						
						addedSignals.add(t.getLabel().getSignal());
						
					}
					
					frame.createPartitionNodes(null, activeSTG, activeSTG.getCoordinates(), partition);
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==createSandwichComponent) {
				try {
					
					Transition t = transitionsToProcess.get(0);
					
					PartitionerBreezePartition part = new PartitionerBreezePartition(activeSTG);
					
					
					STG newStg = part.createSandwichComponent(t.getLabel().getSignal());
					
					frame.addSTG(newStg, activeSTG.getSignalName(t.getLabel().getSignal()));
					
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			

			if (e.getSource()==createLevel2Component) {
				try {
					
					Transition t = transitionsToProcess.get(0);
					
					PartitionerBreezePartition part = new PartitionerBreezePartition(activeSTG);
					
					STG newStg = part.createLevel2Component(t.getLabel().getSignal());
					
					frame.addSTG(newStg, activeSTG.getSignalName(t.getLabel().getSignal()));
					
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==makeExclusive) {
				try {
					
					HashSet<Integer> addedSignals = new HashSet<Integer>();
					
					
					
					for (Node n: nodesToProcess) {
						if (!(n instanceof Transition)) continue;
						
						Transition t = (Transition)n;
						if (activeSTG.getSignature(t.getLabel().getSignal()) != Signature.INTERNAL
							&&activeSTG.getSignature(t.getLabel().getSignal()) != Signature.OUTPUT
							&&activeSTG.getSignature(t.getLabel().getSignal()) != Signature.INPUT) continue;
						
						addedSignals.add(t.getLabel().getSignal());
					}
					
					if (addedSignals.size()<2) throw new Exception("Must select more than one signal");
					
					
					STGUtil.makeSignalsExclusive(activeSTG, addedSignals);
					
					component.initSTG(activeSTG, component.frame.isShorthand());
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			
			
			if (e.getSource()==showNextSequenceTransition&&activeSTG!=null) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					Node n = nodesToProcess.get(0);
					Transition next = partitioner.nextSequenceTransition((Transition)n, null, null, null);
					if (next==null) {
						JOptionPane.showMessageDialog(null,"Next not found");
					} else {
						JOptionPane.showMessageDialog(null,next);
					}
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			
			if (e.getSource()==showNextSequenceTransition&&activeSTG!=null) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					Node n = nodesToProcess.get(0);
					Transition next = partitioner.nextSequenceTransition((Transition)n, null, null, null);
					if (next==null) {
						JOptionPane.showMessageDialog(null,"Next not found");
					} else {
						JOptionPane.showMessageDialog(null,next);
					}
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==showPrevSequenceTransition&&activeSTG!=null) {
				try {
				
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					Node n = nodesToProcess.get(0);
					Transition prev = partitioner.prevSequenceTransition((Transition)n, null, null, null);
					if (prev==null) {
						JOptionPane.showMessageDialog(null,"Previous not found");
					} else {
						JOptionPane.showMessageDialog(null,prev);
					}
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			
			if (e.getSource()==showPossibleCSCConflicts&&activeSTG!=null) {
				try {
					
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					Node n = nodesToProcess.get(0);
					int i=0;
					
					partitioner.outputPossibleCSCConflicts();
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==showOutputsWithCSC&&activeSTG!=null) {
				try {
					
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					String result = "";
					Node n = nodesToProcess.get(0);
					int i=0;
					
					partitioner.outputSignalsWithCSC();
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==showLockedSignals&&activeSTG!=null) {
				try {
					
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					String result = "";
					Node n = nodesToProcess.get(0);
					int i=0;
					
					partitioner.outputLocked();
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==showPlaceInfo&&activeSTG!=null) {
				try {
					if (partitioner == null) {
						partitioner = new PartitionerCommonCauseSubnet(activeSTG);
						partitioner.gatherSignalInfo(activeSTG);
					}
					
					String result = "";
					Node n = nodesToProcess.get(0);
					int i=0;
					
					TreeSet<String> strings = new TreeSet<String>();
					for (Entry<Integer, SignalValue> en: partitioner.getPlaceSignalInfo((Place)n).entrySet()) {
						strings.add(activeSTG.getSignalName(en.getKey())+"="+en.getValue());
					}
					
					for (String s: strings) {
						result+=s+" ";
						i++;
						if (i%10==0) result+="\n";
						
					}
					result+=".";
					
					JOptionPane.showMessageDialog(null,result);
					
				} catch (STGException e1) {
					
					e1.printStackTrace();
				} catch (Exception e1) {
					
					e1.printStackTrace();
				}
				
			}
			
			
			if (e.getSource()==showSurrounding) {
				storeCoordinates(component.activeSTG.getCoordinates());
				
				Node n = nodesToProcess.get(0);
				
				for (int i=2;i<7;i++) {
					STG stg = STGUtil.getNodeSurrounding(n.getSTG(), n, i);
					
					String s="surrounding";
					if (n instanceof Transition) s=((Transition)n).getString(Node.UNIQUE);
					if (n instanceof Place) s=((Place)n).getString(Node.UNIQUE);
					
					component.frame.addSTG(stg, "surrounding for "+s);
					component.setLayout(7);
				}
			}
		}
	}
	
	private void showGraphPopupMenu(MouseEvent e) {
		
//		boolean transitionSelected = (getGraph().getSelectionCell() instanceof TransitionCell);
//		boolean placeSelected =  (getGraph().getSelectionCell() instanceof PlaceCell);
		
		STGGraphComponentPopupMenu menu = new STGGraphComponentPopupMenu(this, true);
		Node n = cell2Node.get(getGraph().getSelectionCell());
		menu.nodesToProcess.add(n);
		
		if (n instanceof Transition) menu.transitionsToProcess.add((Transition)n);
		
		for (Object o: getGraph().getSelectionCells()) {
			
			if (getGraph().getSelectionCell()==o) continue;
			
			n = cell2Node.get(o);
			menu.nodesToProcess.add(n);
			if (n instanceof Transition) menu.transitionsToProcess.add((Transition)n);
		}
		
		
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), this);
		menu.show(this, pt.x, pt.y);
		e.consume();
		
	}
	
	private static final long serialVersionUID = 7755698755334362626L;
	
	/** The current graph, representing the current STG. */
	private mxIGraphModel model;
	private final mxGraph graph;
	protected mxRubberband rubberband;
	protected STG activeSTG; 
	public PartitionerCommonCauseSubnet partitioner = null;

	HashMap<mxCell, Node> cell2Node;
	Map<Node, mxCell> node2Cell;
	Map<Integer, mxCell> id2Cell;
	
	private STGEditorFrame frame;
	
	public boolean isVertexIgnored(Object vertex) {
		return !graph.getModel().isVertex(vertex)
				|| !graph.isCellVisible(vertex);
	}

	public mxRectangle getVertexBounds(Object vertex) {
		mxRectangle geo = graph.getModel().getGeometry(vertex);
		return new mxRectangle(geo);
	}

	public mxRectangle setVertexLocation(Object vertex, double x, double y) {
		mxIGraphModel model = graph.getModel();
		mxGeometry geometry = model.getGeometry(vertex);
		mxRectangle result = null;

		if (geometry != null) {
			result = new mxRectangle(x, y, geometry.getWidth(),
					geometry.getHeight());

			if (geometry.getX() != x || geometry.getY() != y) {
				geometry = (mxGeometry) geometry.clone();
				geometry.setX(x);
				geometry.setY(y);
				model.setGeometry(vertex, geometry);
			}
		}

		return result;

	}

	/** moves model nodes to the top left corner */
	public void shiftModel() {
		double max = 0;
		Double top = null;
		Double left = null;
		Object parent = graph.getDefaultParent();

		List<Object> vertices = new ArrayList<Object>();
		int childCount = model.getChildCount(parent);

		for (int i = 0; i < childCount; i++) {
			Object cell = model.getChildAt(parent, i);

			if (!isVertexIgnored(cell)) {
				vertices.add(cell);
				mxRectangle bounds = getVertexBounds(cell);

				if (top == null)
					top = bounds.getY();
				else
					top = Math.min(top, bounds.getY());

				if (left == null)
					left = bounds.getX();
				else
					left = Math.min(left, bounds.getX());

				max = Math.max(max,
						Math.max(bounds.getWidth(), bounds.getHeight()));
			}
		}

		for (Object obj : vertices) {
			if (!graph.isCellMovable(obj))
				continue;

			double x, y;
			mxRectangle bounds = getVertexBounds(obj);
			x = bounds.getX() - left + 50;
			y = bounds.getY() - top + 50;

			setVertexLocation(obj, x, y);
		}
	}
	
	
	public void setNodeLocationById(Integer id, int x, int y) {
		Object o = id2Cell.get(id);
		if (o!=null) {
			setVertexLocation(o, x, y);
		}
	}
	
	public void selectNodeById(Integer id) {
		Object o = id2Cell.get(id);
		scrollCellToVisible(o, true);
		selectCellsForEvent(new Object[]{o}, null);
		
	}
	
	public void storeCoordinates(STGCoordinates coordinates) {
		
		coordinates.clear();
		for (Map.Entry<mxCell, Node> en: cell2Node.entrySet()) {
			mxGeometry g = en.getKey().getGeometry();
			coordinates.put(en.getValue(), new Point((int)g.getCenterX(), (int)g.getCenterY()));
		}
	}
	
	public static boolean isShorthandPlace(Node node, Node []frto) {
		
		if (!(node instanceof Place)) return false;
		if (((Place)node).getMarking()!=0) return false;
		Collection<Node> ch= node.getChildren();
		Collection<Node> pa= node.getParents();
		if (ch.size()!=1||pa.size()!=1) return false;
		Node a=(Node)pa.toArray()[0];
		Node b=(Node)ch.toArray()[0];
		if (a==b) return false;
		if (frto!=null) {
			frto[0]=a;
			frto[1]=b;
		}
		return true;
	}
	
	
/**
 * Initialises Graph component with given STG and coordinates  
 * @param stg
 * @param coordinates 
 */
	public void initSTG(STG stg, boolean isShorthand) {

		((mxGraphModel)graph.getModel()).clear();
		
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		
		try {
			cell2Node.clear();
			node2Cell.clear();
			id2Cell.clear();
			
			activeSTG = stg; // remember the STG being shown 
			partitioner = null;

			if (stg==null) return;
			
			STGCoordinates coordinates=stg.getCoordinates();
			
			for (Node node : stg.getNodes()) {
				
				mxCell cell = null;

				if (node instanceof Place) {
					
					if (isShorthand&&isShorthandPlace((Place)node, null)) continue;
					
					cell = new PlaceCell((Place)node);			
					
				} else if (node instanceof Transition) {
					cell = new TransitionCell((Transition)node, stg);
				
				}
				cell2Node.put(cell, node);
				node2Cell.put(node, cell);
				id2Cell.put(node.getIdentifier(), cell);
				graph.addCell(cell, parent);
			}

			for (Node node : stg.getNodes()) {
				mxCell source = node2Cell.get(node);
				if (source==null) continue;
				
				if (coordinates!=null) {
					Point p = coordinates.get(node);
					if (p!=null) {
						double h=source.getGeometry().getHeight();
						double w=source.getGeometry().getWidth();
						source.getGeometry().setX(p.x-w/2);
						source.getGeometry().setY(p.y-h/2);
					} else {
						// if no coordinate is given, find the average from its neighbours
						int cnt=0;
						int px=0;
						int py=0;
						for (Node n : node.getChildren()) {
							Point p2 = coordinates.get(n);
							if (p2!=null) {
								cnt++;
								px+=p2.x;
								py+=p2.y;
							}
						}
						
						for (Node n : node.getParents()) {
							Point p2 = coordinates.get(n);
							if (p2!=null) {
								cnt++;
								px+=p2.x;
								py+=p2.y;
							}
						}
						
						if (cnt>1) {
							double h=source.getGeometry().getHeight();
							double w=source.getGeometry().getWidth();
							source.getGeometry().setX((double)px/cnt-w/2);
							source.getGeometry().setY((double)py/cnt-h/2);
						}
					}
				}
				
				for (Node child : node.getChildren()) {
					if (child instanceof Place) {
						Node []frto=new Node[2];
						
						if (isShorthand) {
							if (isShorthandPlace((Place)child, frto)) {
								mxCell target = node2Cell.get(frto[1]);
								graph.insertEdge(parent, null, null, source, target);
							}
						}
					}
					
					mxCell target = node2Cell.get(child);
					if (source!=null&&target!=null)
						graph.insertEdge(parent, null, null, source, target);
				}
			}
			
			// do default layout, if coordinates are not given
			if (coordinates==null||coordinates.size()==0) {
				
				// only start the layout if there are not too many transitions
				mxCompactTreeLayout cl = new mxCompactTreeLayout(graph, false);
				cl.execute(parent);
				
				
				
				shiftModel();
				
				/*
				coordinates = new STGCoordinates();
				// store new coordinates for the STG
				for (Object ob: graph.getChildCells(parent, true, false)) {
					if (ob instanceof mxCell) {
						if (ob instanceof TransitionCell || ob instanceof PlaceCell) {
							double dx = ((mxCell)ob).getGeometry().getCenterX();
							double dy = ((mxCell)ob).getGeometry().getCenterY();
							Node nd = cell2Node.get(ob);
							coordinates.put(nd, new Point((int)dx, (int)dy));
						}
					}
				}
				*/
			}
			
		} finally {
			graph.getModel().endUpdate();
		}
		
	}

	protected Map<String, Object> setupStyles(mxGraph graph) {
		mxStylesheet stylesheet = graph.getStylesheet();

		Map<String, Object> style = new Hashtable<String, Object>();

		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		style.put(mxConstants.STYLE_FILLCOLOR, "white");
		style.put(mxConstants.STYLE_STROKECOLOR, "black");
		style.put(mxConstants.STYLE_FONTCOLOR, "black");

		stylesheet.setDefaultVertexStyle(style);

		style = new Hashtable<String, Object>();

		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
		style.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
		style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
		style.put(mxConstants.STYLE_STROKECOLOR, "black");
		style.put(mxConstants.STYLE_FONTCOLOR, "#446299");

		stylesheet.setDefaultEdgeStyle(style);

		return style;
	}

	public void setLayout(int type) {

		if (type==8) {
			STGTreeLayout.doLayout(activeSTG, frame.isShorthand());
			initSTG(activeSTG, frame.isShorthand());
		}
		
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try {
			
			if (type<7) {
				mxGraphLayout gl;
				if (type == 1)
					gl = new mxOrganicLayout(graph);
				else if (type == 2)
					gl = new mxCircleLayout(graph);
				else if (type == 3)
					gl = new mxCompactTreeLayout(graph, false, false);
				else if (type == 4)
					gl = new mxParallelEdgeLayout(graph, 30);
				else if (type == 5)
					gl = new mxPartitionLayout(graph, false, 30);
				else if (type == 6)
					gl = new mxStackLayout(graph, false, 30);
				else
					gl = new mxOrganicLayout(graph);

				gl.execute(parent);
			} else if (type==7) {
				STGDotLayout.doLayout(activeSTG, this);
			}
			
			shiftModel();
		} finally {
			graph.getModel().endUpdate();
		}
		
	}
	
	public STGGraphComponent(STGEditorFrame frame) {
		super(new mxGraph(new mxGraphModel()));

		this.frame = frame;
		graph = getGraph();
		model = getGraph().getModel();
		
		setupStyles(graph);

		graph.setAutoSizeCells(false);
		graph.setCellsEditable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsCloneable(false);
		graph.setCellsResizable(false);
		graph.setCellsDeletable(false);
		graph.setCellsBendable(false);
		graph.setCellsDeletable(false);
		graph.setAllowDanglingEdges(false);
		graph.setCellsSelectable(true);
		graph.setAutoSizeCells(true);
		
		this.setSwimlaneSelectionEnabled(true);
		this.setConnectable(false);
		rubberband = new mxRubberband(this);
		cell2Node = new HashMap <mxCell, Node>();
		node2Cell = new HashMap<Node, mxCell>();
		id2Cell = new HashMap<Integer, mxCell>();
		
		this.getGraphControl().addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				mouseReleased(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showGraphPopupMenu(e);
				}
			}

		});
	}

	
}
