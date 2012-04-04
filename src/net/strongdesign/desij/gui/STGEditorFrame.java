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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.mxgraph.swing.mxGraphOutline;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.decomposition.AbstractDecomposition;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.LazyDecompositionMultiSignal;
import net.strongdesign.desij.decomposition.LazyDecompositionSingleSignal;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.desij.decomposition.tree.CscAwareDecomposition;
import net.strongdesign.desij.decomposition.tree.IrrCscAwareDecomposition;
import net.strongdesign.desij.decomposition.tree.TreeDecomposition;
import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGCoordinates;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;

/**
 * This is the main class of the DesiJ GUI. It contains a graphical
 * representation of STGs and a navigation view for navigating between different
 * STGs.
 */
public class STGEditorFrame extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 7606945539229848668L;

	public final STGEditorAction OPEN = new STGEditorAction("Open",
			KeyEvent.VK_O, 'o', 0, this);
	public final STGEditorAction NEW = new STGEditorAction("New",
			KeyEvent.VK_N, 'n', 0, this);

	public final STGEditorAction LAYOUT = new STGEditorAction("Spring layout",
			KeyEvent.VK_L, 'l', 0, this);

	public final STGEditorAction SAVE = new STGEditorAction("Save",
			KeyEvent.VK_S, 'S', 0, this);
	public final STGEditorAction SAVE_AS = new STGEditorAction("Save as",
			KeyEvent.VK_A, null, 0, this);
	public final STGEditorAction EXIT = new STGEditorAction("Exit",
			KeyEvent.VK_X, null, 0, this);

	public final STGEditorAction INITIAL_PARTITION = new STGEditorAction(
			"Initial partition", 0, null, 0, this);
	public final STGEditorAction FINEST_PARTITION = new STGEditorAction(
			"Finest partition", 0, null, 0, this);
	public final STGEditorAction ROUGHEST_PARTITION = new STGEditorAction(
			"Roughest partition", 0, null, 0, this);
	public final STGEditorAction MULTISIGNAL_PARTITION = new STGEditorAction(
			"Signal re-use heuristic", 0, null, 0, this);
	public final STGEditorAction AVOIDCSC_PARTITION = new STGEditorAction(
			"Avoid CSC", 0, null, 0, this);
	public final STGEditorAction REDUCECONC_PARTITION = new STGEditorAction(
			"Reduce concurrency", 0, null, 0, this);
	public final STGEditorAction LOCKED_PARTITION = new STGEditorAction(
			"Locked signals partition", 0, null, 0, this);
	public final STGEditorAction BEST_PARTITION = new STGEditorAction(
			"Best partition", 0, null, 0, this);
	
	public final STGEditorAction RG = new STGEditorAction(
			"Create reachability graph", KeyEvent.VK_R, null, 0, this);
	
	public final STGEditorAction REDUCE = new STGEditorAction("Reduce Component", 0, null, 0, this);
	
	public final STGEditorAction DECOMPOSE = new STGEditorAction("Decompose", 0, null, 0, this);
	public final STGEditorAction DECO_BASIC = new STGEditorAction("Basic", 0, null, 0, this);
	public final STGEditorAction DECO_SINGLE_SIG = new STGEditorAction("Single signal", 0, null, 0, this);
	public final STGEditorAction DECO_MULTI_SIG = new STGEditorAction("Multi signal", 0, null, 0, this);
	public final STGEditorAction DECO_TREE = new STGEditorAction("Tree", 0, null, 0, this);
	public final STGEditorAction DECO_CSC_AWARE = new STGEditorAction("CSC aware", 0, null, 0, this);
	public final STGEditorAction DECO_ICSC_AWARE = new STGEditorAction("Irr. CSC aware", 0, null, 0, this);
	
	public final STGEditorAction RESOLVE_INTERNAL = new STGEditorAction("Resolve internal signals", 0, null, 0, this);
	
	
	public final STGEditorAction SIGNAL_TYPE = new STGEditorAction(
			"Change signal types", KeyEvent.VK_C, null, 0, this);
	public final STGEditorAction COPY_STG = new STGEditorAction("Copy STG",
			KeyEvent.VK_Y, 'C', 0, this);

	public final STGEditorAction ABOUT = new STGEditorAction("About JDesi",
			KeyEvent.VK_A, null, 0, this);

	
	public JCheckBoxMenuItem IS_SHORTHAND = new JCheckBoxMenuItem("Shorthand notation");
	
	public final STGEditorAction LAYOUT1 = new STGEditorAction("Organic",	KeyEvent.VK_1, '1', 0, this);
	public final STGEditorAction LAYOUT2 = new STGEditorAction("Circle",	KeyEvent.VK_2, '2', 0, this);
	public final STGEditorAction LAYOUT3 = new STGEditorAction("Compact Tree",	KeyEvent.VK_3, '3', 0, this);
	public final STGEditorAction LAYOUT4 = new STGEditorAction("Parallel Edge",	KeyEvent.VK_4, '4', 0, this);
	public final STGEditorAction LAYOUT5 = new STGEditorAction("Partition",		KeyEvent.VK_5, '5', 0, this);
	public final STGEditorAction LAYOUT6 = new STGEditorAction("Stack",			KeyEvent.VK_6, '6', 0, this);

	public final static Font STANDARD_FONT = new Font("Arial", Font.PLAIN, 16);
	public final static Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);

	public final static Color INPUT = new Color(255, 200, 200);
	public final static Color OUTPUT = new Color(200, 200, 255);
	public final static Color INTERNAL = new Color(200, 255, 200);
	public final static Color DUMMY = new Color(200, 200, 200);
	public static final Color NAV_COLOR = new Color(255, 255, 200);
	
	private final STGGraphComponent graphComponent;
	private final mxGraphOutline graphOutline;

	Map<String, Object> transitionStyle;
	Map<String, Object> placeStyle;
	
	private JFileChooser fileChooser = new JFileChooser();

	/** The navigation view. */
	private STGEditorNavigation navigationView;

	/** The menu bar. */
	private STGEditorMenuBar menuBar;

	/** The split pane containing the navigation view and the current graph. */
	private JSplitPane splitPane;

	//private String label;

	private boolean useShorthand;
	
	public JFileChooser getFileChooser() {
		return fileChooser;
	}
	
	/**
	 * Constructs an instance.
	 * 
	 * @param windowLabel
	 *            The label of the window.
	 * @param stg
	 *            The initial STG.
	 */
	public STGEditorFrame() {

		// Initialise window
		//super(windowLabel);
		super();
		setBounds(new Rectangle(50, 50, 800, 600));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
//		this.label = windowLabel;

		// Initialise navigation view
		// navigationView = new STGEditorNavigation(stg, this);

		// The initial model and layout cache

		// cache = new STGLayoutCache(stg, model);

		// graph = new mxGraph(model, cache);

		graphComponent = new STGGraphComponent();
		
		graphOutline = new mxGraphOutline(graphComponent);



		navigationView = new STGEditorNavigation(this, graphComponent);

		// Put it all together
		JSplitPane vert = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(navigationView), graphOutline);

		vert.setDividerLocation(450);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vert,
				graphComponent);

		splitPane.setDividerLocation(250);

		getContentPane().add(splitPane, BorderLayout.CENTER);

		// Create menu bar

		menuBar = new STGEditorMenuBar(this);// , cache);

		setJMenuBar(menuBar);
		IS_SHORTHAND.addItemListener(this);
		
	}

	public void saveProject(String fileName) throws FileNotFoundException {

		ZipOutputStream out = new ZipOutputStream(
				new FileOutputStream(fileName));
		out.setComment("Generated by DesiJ");

	}

	// public STGEditorFrame(String fileName) {
	// FileSupport.loadFileFromDisk(fileName);
	//
	//
	// }

	// public void run() {
	// if (backgroundMethod.equals("reduce")) {
	// try {
	// reduce();
	// }
	// catch (OutOfMemoryError e) {
	// JOptionPane.showMessageDialog(this, "Out of memory error while reducing",
	// "JDesi - Error", JOptionPane.ERROR_MESSAGE);
	// return;
	// }
	// JOptionPane.showMessageDialog(this, "Finished reduction", "JDesi",
	// JOptionPane.INFORMATION_MESSAGE);
	// }
	// }
	//
	public void setNav(STGEditorNavigation nav) {
		// navigation = nav;
		// // navModel = navigation.getModel();
		// currentNode = (STGEditorTreeNode)navModel.getRoot();
	}

	public String getFileName() {
		return navigationView.getSelectedNode().getFileName();
	}

	public void setFileName(String fileName) {
		navigationView.getSelectedNode().setFileName(fileName);
		navigationView.getSelectedNode().setLabel(fileName);
	}

	public void updateSTG(STG stg) {
		//editor.setSTG(stg, null);
	}



	/*
	public void setSTG(STGEditorTreeNode node) {

		if (!node.isSTG()) return;

		// model=new DefaultGraphModel();
		// cache = new STGLayoutCache(node.getSTG(), model);
		// graph.setGraphLayoutCache(cache);

		// cache.init();
		STGEditorCoordinates coordinates = node.getCoordinates();
		if (coordinates==null) return;
		
		for (Node n: node.getSTG().getNodes()) {
			
		}

		splitPane.validate();
	}
*/
	
	// /* //Former STG will be saved in STGEditorTreeNode
	// if (editor != null)
	// if (currentNode.isSTG())
	// currentNode.setSTG(STGFile.convertToG(editor.getSTG(),
	// true)+STGEditor.convertToG(editor.getSTG(), true, coordinates));
	// */
	// //Remove event handlers
	// removeKeyListener(editor);

	// Generate new STG from new node
	/*
	 * try { stg = STGFile.convertToSTG(node.getSTG()); } catch (STGException e)
	 * { JOptionPane.showMessageDialog(this, "Internal parsing error",
	 * "JDesi - Error", JOptionPane.ERROR_MESSAGE); }
	 */

	/*
	 * coordinates = STGEditor.convertToCoordinates(node.getSTG(), stg); editor
	 * = new STGEditor(stg, scrollPane, this, coordinates, options);
	 * 
	 * //register new listener addKeyListener(editor);
	 * menuBar.setEditor(editor);
	 * 
	 * //save scroll bar status, make new editor visible and restore scroll bar
	 * status int verticalValue = scrollPane.getVerticalScrollBar().getValue();
	 * int horizontalValue = scrollPane.getHorizontalScrollBar().getValue();
	 * scrollPane.setViewportView(editor);
	 * scrollPane.getVerticalScrollBar().setValue(verticalValue);
	 * scrollPane.getHorizontalScrollBar().setValue(horizontalValue);
	 */

	// select nin navigation
	// navigation.selectNode(node);

	// set new title
	// setTitle(node + " - JDesi");
	//
	// currentNode = node;
	// }

	// public void performOperation(Node node) throws STGException{
	// //this does not work for arbitrary node removal, since isolated nodes do
	// not occur in
	// //the file representation
	// if (node instanceof Place) {
	// if (ConditionFactory.getRedundantPlaceCondition(currentNode.getSTG(),
	// 0).fulfilled((Place)node)) {
	// STG newSTG = currentNode.getSTG().clone();
	// // STGEditorCoordinates newCoord =
	// currentNode.getCoordinates().clone(newSTG);
	// Place p =
	// newSTG.getPlaces(ConditionFactory.getEqualTo((Place)node)).get(0);
	// newSTG.removePlace(p);
	// newCoord.remove(p);
	//
	// setSTG(addChild(newSTG, newCoord,
	// "Redundant place: "+node.getString(Node.SIMPLE), false));
	// repaint();
	// }
	// }
	// else if (node instanceof Transition) {
	// if
	// (ConditionFactory.getRedundantTransitionCondition(currentNode.getSTG()).fulfilled((Transition)node))
	// {
	// STG newSTG = currentNode.getSTG().clone();
	// STGEditorCoordinates newCoord =
	// currentNode.getCoordinates().clone(newSTG);
	// Transition t =
	// newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
	// newSTG.removeNode(t);
	// newCoord.remove(t);
	//
	// setSTG(addChild(newSTG, newCoord,
	// "Redundant transition: "+node.getString(Node.UNIQUE), false));
	// repaint();
	// }
	// else if
	// (ConditionFactory.SECURE_CONTRACTABLE.fulfilled((Transition)node)) {
	// int nrn = (node.getParents().size() * node.getChildren().size());
	// double nn = 2*Math.PI / nrn;
	// double arc = 0;
	// int newPlaceRadius;
	// if (nrn == 1 )
	// newPlaceRadius = 0;
	// else
	// newPlaceRadius = 30 + (int) (0.3*nrn);
	//
	// Point oldPoint = currentNode.getCoordinates().get(node);
	//
	// STG newSTG = currentNode.getSTG().clone();
	// STGEditorCoordinates newCoord =
	// currentNode.getCoordinates().clone(newSTG);
	//
	// Transition t =
	// newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
	// newSTG.contract(t);
	//
	//
	//
	// for (Node nk : newSTG.getNodes())
	// if (!newCoord.keySet().contains(nk)) {
	//
	// Point nP = new Point( oldPoint.x + (int) (Math.sin(arc)*newPlaceRadius),
	// oldPoint.y + (int) (Math.cos(arc)*newPlaceRadius));
	// if (nP.x<0) nP.x = 0;
	// if (nP.y<0) nP.y = 0;
	//
	// newCoord.put(nk, nP );
	// arc += nn;
	// }
	//
	// setSTG(addChild(newSTG, newCoord,
	// "Contracted: "+node.getString(Node.UNIQUE), false));
	// repaint();
	// }
	// }
	// }
	//
	
	public void initialPartition(Object source) throws STGException {
		
		STGEditorTreeNode projectNode = navigationView.getProjectNode();
		if (navigationView.getSelectedNode()==projectNode) {
			graphComponent.storeCoordinates(projectNode.getSTG().getCoordinates());
		}
		
		if (!projectNode.isSTG()) return;
		
		STG curSTG = projectNode.getSTG();
		projectNode.removeAllChildren();
		
		String partitionString = "";
		if (source==FINEST_PARTITION) partitionString = "finest";
		if (source==ROUGHEST_PARTITION) partitionString = "roughest";
		if (source==MULTISIGNAL_PARTITION) partitionString = "multisignaluse";
		if (source==AVOIDCSC_PARTITION) partitionString = "avoidcsc";
		if (source==REDUCECONC_PARTITION) partitionString = "reduceconc";
		if (source==LOCKED_PARTITION) partitionString = "lockedsignals";
		if (source==BEST_PARTITION) partitionString = "best";
		
		STGEditorTreeNode initComponents = new STGEditorTreeNode(partitionString);
		
		projectNode.add(initComponents);
		//navigationView.addNode(initComponents, "Partition");
		
		projectNode.partition=null;
		
		if (source==FINEST_PARTITION)
			projectNode.partition = Partition.getFinestPartition(curSTG,null);
		else if (source==ROUGHEST_PARTITION)
			projectNode.partition = Partition.getRoughestPartition(curSTG, null);
		else if (source==MULTISIGNAL_PARTITION)
			projectNode.partition = Partition.getMultipleSignalUsagePartition(curSTG);
		else if (source==AVOIDCSC_PARTITION)
			projectNode.partition = Partition.getCSCAvoidancePartition(curSTG);
		else if (source==REDUCECONC_PARTITION)
			projectNode.partition = Partition.getPartitionConcurrencyReduction(curSTG);
		else if (source==LOCKED_PARTITION)
			projectNode.partition = Partition.getLockedSignalsPartition(curSTG);
		else if (source==BEST_PARTITION)
			projectNode.partition = Partition.getBestPartition(curSTG);
		else
			return;
		
		
		for (STG s : Partition.splitByPartition(curSTG, projectNode.partition)) {

			StringBuilder signalNames = new StringBuilder();
			for (Integer sig : s.collectUniqueCollectionFromTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.OUTPUT),
					CollectorFactory.getSignalCollector()))
				signalNames.append(" "+curSTG.getSignalName(sig));

			STGEditorTreeNode nn = new STGEditorTreeNode(
					signalNames.toString(), s, true);
			
			nn.getSTG().copyCoordinates(projectNode.getSTG().getCoordinates());
			
			
			
			initComponents.add(nn);
			//navigationView.addNode(nn, signalNames.toString());
		}
		
		navigationView.updateUI();
		
		navigationView.showNode(initComponents);
	}

	//
	// private STGEditorTreeNode addChild(STG stg, STGEditorCoordinates
	// coordinates, String mes, boolean procreative) {
	// // STGEditorTreeNode newNode = new STGEditorTreeNode(mes, stg,
	// coordinates, procreative);
	// //
	// // if (currentNode.getParent()!=null &&
	// currentNode.getParent().getIndex(currentNode) !=
	// currentNode.getParent().getChildCount()-1)
	// // currentNode.setProcreative();
	// //
	// // STGEditorTreeNode newParent = currentNode;
	// // while (!newParent.isProcreative())
	// // newParent = (STGEditorTreeNode) newParent.getParent();
	// //
	// // navModel.insertNodeInto(newNode, newParent,
	// newParent.getChildCount());
	// return null;
	//
	// }
	//
	// private STGEditorTreeNode addChild(STGEditorTreeNode parent, STG stg,
	// STGEditorCoordinates coordinates, String mes, boolean procreative) {
	// // STGEditorTreeNode newNode = new STGEditorTreeNode(
	// // mes, stg, coordinates, procreative);
	// //
	// //
	// // navModel.insertNodeInto(newNode, parent, parent.getChildCount());
	// // return newNode;
	// return null;
	//
	// }
	//
	//
	//
	// public void springLayout() {
	// // //SPRING_LAYOUT.setEnabled(false);
	// // for (int t=0; t<=10 ; ++t)
	// // STGEditorLayout.applySpringLayout(editor.getAllCoordinates(), 2);
	// // editor.repaint();
	// // //SPRING_LAYOUT.setEnabled(true);
	// }
	//
	//
	//
	//
	// public void deleteNode(Node node) {
	// STG newSTG = currentNode.getSTG().clone();
	// STGEditorCoordinates newCoord =
	// currentNode.getCoordinates().clone(newSTG);
	// Node n=null;
	// if (node instanceof Transition)
	// n =
	// newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
	// if (node instanceof Place)
	// n = newSTG.getPlaces(ConditionFactory.getEqualTo((Place)node)).get(0);
	//
	//
	// newSTG.removeNode(n);
	// newCoord.remove(n);
	//
	// setSTG(addChild(newSTG, newCoord,
	// "Deleted: "+node.getString(Node.UNIQUE), false));
	// repaint();
	// }


	public void exit() {
		dispose();
	}

	public void open(String fileName) {
		
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(STGFileFilter.STANDARD);
		
		
		if (fileName==null) {
			if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return; 
			fileName = fileChooser.getSelectedFile().getAbsolutePath();
		}

		try {

			File f = new File(fileName);
			if (!f.exists()) {
				f = new File(fileName + ".g");
				if (!f.exists())
					throw new FileNotFoundException(fileName);
			}
			
			String file = FileSupport.loadFileFromDisk(f.getAbsolutePath());
			//fileName = f.getAbsolutePath();
			STG stg = STGEditorFile.convertToSTG(file, true);
			
			// add new tree element
			
			STGEditorTreeNode node = navigationView.addSTGNode(stg, fileName, true);
			navigationView.showNode(node);
			setFileName(fileName);
			
			// STGEditorCoordinates coordinates =
			// STGEditorFile.convertToCoordinates(file);

			// navigationView = new STGEditorNavigation(stg, this);
			// The initial model and layout cache

//			model = new mxGraphModel();
			// cache = new STGLayoutCache(stg, model);

			// Build up the graph corresponding to the STG
			// cache.init();

			// String file = FileSupport.loadFileFromDisk(fileName);
			// STG stg = STGFile.convertToSTG(file);
			// STGEditorCoordinates coordinates =
			// STGEditorFile.convertToCoordinates(file);
			// new STGEditorFrame(fileName, stg, coordinates).setVisible(true);
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, "Could not parse file: "
					+ fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Could not load file: "
					+ fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		} catch (STGException e) {
			JOptionPane.showMessageDialog(this, "Could not parse file: "
					+ fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void save() throws IOException {
		
		STGEditorTreeNode selectedNode = navigationView.getSelectedNode();
		graphComponent.storeCoordinates(selectedNode.getSTG().getCoordinates());
		
		String name = getFileName();
		if (name == null) {
			fileChooser.setMultiSelectionEnabled(false);
			
/*			JFileChooser fileChooser = null;
			try {
				fileChooser = new JFileChooser(new File(".").getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
			
/*			fileChooser.setSelectedFile(new File(selectedNode.toString().replaceAll(" ", "_").replaceAll(":", "")
					+ ".g"));*/
			
			fileChooser.setFileFilter(STGFileFilter.STANDARD);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			name = fileChooser.getSelectedFile().getAbsolutePath();
			if (name == null) return;
		}

		FileSupport.saveToDisk(STGFile.convertToG(selectedNode.getSTG()), name);
		
	}

	private void saveAs() throws IOException {
		String name = null;

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(STGFileFilter.STANDARD);
		
//		if (name != null)
//			fileChooser.setSelectedFile(new File(name));
		
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		name = fileChooser.getSelectedFile().getAbsolutePath();
		if (name == null) return;

		FileSupport.saveToDisk(
				STGFile.convertToG(navigationView.getSelectedNode().getSTG()), name);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		try {
			// if (source == SPRING_LAYOUT) springLayout();
			if (source == OPEN)
				open(null);
			else if (source == SAVE)
				save();
			else if (source == SAVE_AS)
				saveAs();
			else if (source == EXIT)
				exit();
			// else if (source == RG) rg();
			else if (source == COPY_STG)
				copySTG();
			else if (source == FINEST_PARTITION||
					source == ROUGHEST_PARTITION||
					source == MULTISIGNAL_PARTITION||
					source == AVOIDCSC_PARTITION||
					source == REDUCECONC_PARTITION||
					source == LOCKED_PARTITION||
					source == BEST_PARTITION
					) {
				initialPartition(source);
			} else if (
					source == DECO_MULTI_SIG||
					source == DECO_SINGLE_SIG||
					source == DECO_BASIC||
					source == DECO_TREE||
					source == DECO_CSC_AWARE||
					source == DECO_ICSC_AWARE) {
				
				decompose(source);
			} else if (source == REDUCE) {
//				 //new DecompositionOptions("Decomposition", currentNode.getSTG(), new
////						 DesijCommandLineWrapper(new String[]{""})).setVisible(true);
				 reduce();
			} else if (source == ABOUT) {
				new STGEditorAbout(this).setVisible(true);
			}
			else if (source == LAYOUT1)	graphComponent.setLayout(1);
			else if (source == LAYOUT2)	graphComponent.setLayout(2);
			else if (source == LAYOUT3)	graphComponent.setLayout(3);
			else if (source == LAYOUT4)	graphComponent.setLayout(4);
			else if (source == LAYOUT5)	graphComponent.setLayout(5);
			else if (source == LAYOUT6)	graphComponent.setLayout(6);

		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	//
	//
	private void copySTG() {
		// STG stg = currentNode.getSTG().clone();
		// STGEditorCoordinates coordinates =
		// currentNode.getCoordinates().clone(stg);
		//
		// setSTG(addChild(currentNode, currentNode.getSTG().clone(),
		// coordinates, "Copy of "+currentNode, false));

	}

	// private void changeSignalType() {
	// signalChooser = new STGEditorSignalChooser("JDesi - Signals of "+
	// currentNode, currentNode.getSTG(), this);
	// signalChooser.setAlwaysOnTop(true);
	// signalChooser.setModal(true);
	// signalChooser.setVisible(true);
	//
	//
	// }
	//
	// //
	// // private STGEditorCoordinates
	// deepCorrectedCoordinates(STGEditorCoordinates oldCoordinates, STG stg) {
	// // STGEditorCoordinates result = new STGEditorCoordinates();
	// //
	// // //make a 'deep' copy of the coordinate set
	// // Map<String, Point> c = new HashMap<String, Point>();
	// // for (Node node : oldCoordinates.keySet()) {
	// // if (node instanceof Transition)
	// // c.put(node.getString(Node.UNIQUE), (Point)
	// oldCoordinates.get(node).clone());
	// // else
	// // c.put(node.getString(Node.SIMPLE), (Point)
	// oldCoordinates.get(node).clone());
	// // }
	// //
	// // //After this loop all existing coordinates are copied for the new stg
	// // for (Node node : stg.getNodes()) {
	// // Point p;
	// // if (node instanceof Transition)
	// // p = c.get(node.getString(Node.UNIQUE));
	// // else
	// // p = c.get(node.getString(Node.SIMPLE));
	// // if (p != null);
	// // result.put( node, p );
	// // }
	// //
	// //
	// // //layout all unknown nodes
	// // for (Node node : stg.getNodes()) {
	// // Point p;
	// // if (node instanceof Transition)
	// // p = c.get(node.getString(Node.UNIQUE));
	// // else
	// // p = c.get(node.getString(Node.SIMPLE));
	// //
	// // if (p == null) {
	// // Point center = new Point(0,0);
	// // int i = 0;
	// //
	// // for (Node node2 : node.getNeighbours()) {
	// // Point nP = result.get(node2);
	// // if (nP != null) {
	// // center.translate(nP.x, nP.y);
	// // ++i;
	// // }
	// // }
	// //
	// // if (i!=0) {
	// // center.x /= i; center.y /= i;
	// // result.put(node, center);
	// // }
	// // else
	// // result.put(node, new Point(0,0));
	// // }
	// // }
	// //
	// // STGEditorLayout.applySpringLayout(result, result.keySet(), 5);
	// // return result;
	// // }
	//

	private void reduce() {
		
		STGEditorTreeNode currentNode = navigationView.getSelectedNode();

		if (!currentNode.isSTG()) {
			JOptionPane.showMessageDialog(this, "No STG selected", "DesiJ - Reduce",
			JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (!currentNode.isSTG()) return;
		
//		class Deco extends BasicDecomposition {
//			private STGEditorTreeNode parent;
//			public Deco(STGEditorTreeNode parent) {
//				super("basic");
//				this.parent = parent;
//			}
//			
///*			 public void logging(DecompositionParameter decoPara, DecompositionEvent
//			 event, Object affectedNodes) {
//				 
//				 if (affectedNodes != null && affectedNodes instanceof Collection &&
//						 ((Collection)affectedNodes).size()==0)
//					 return;
//				 //
//				
//				 if (event == DecompositionEvent.BACKTRACKING) {
//					 // navigation.getModel().insertNodeInto(new
//					 STGEditorTreeNode("Added signal: "+affectedNodes), parent,
//					 parent.getChildCount());
//				
//				 }
//			 }*/
//			 
//		 }
		 
		
		 //setSTG(currentNode);
		
//		
//		 currentNode.setProcreative();
//		
//		 DecompositionParameter decoPara = new DecompositionParameter();
//		 decoPara.stg = currentNode.getSTG().clone();
//		 DesiJ.risky = false;
//		
		
		// 1. make a copy of current node, write it to the parameter
		
		STG stg = currentNode.getSTG().clone();
		STGInOutParameter componentParameter = new STGInOutParameter(stg);
		
		
		// 2. run reduce on it, add it to the tree

		try {
			
			BasicDecomposition deco = new BasicDecomposition("basic");
			deco.reduce(componentParameter);
			
			STGEditorTreeNode nn = new STGEditorTreeNode("reduced", stg, true);
			nn.getSTG().copyCoordinates((STGCoordinates)currentNode.getSTG().getCoordinates());
			currentNode.add(nn);
			
			navigationView.updateUI();
			navigationView.showNode(nn);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void decompose(Object source) {
		 
//		 STGEditorTreeNode currentNode = navigationView.getSelectedNode(); 
	 
		STGEditorTreeNode projectNode = navigationView.getProjectNode();

		if (!projectNode.isSTG()) {
			JOptionPane.showMessageDialog(this, "No STG selected", "DesiJ - Reduce",
			JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (navigationView.getSelectedNode()==projectNode) {
			graphComponent.storeCoordinates(projectNode.getSTG().getCoordinates());
		}
		
		if (!projectNode.isSTG()) return;
		
		STG curSTG = projectNode.getSTG();
		projectNode.removeAllChildren();
		 
		
		 
//		class Deco extends BasicDecomposition {
//			private STGEditorTreeNode parent;
//			public Deco(STGEditorTreeNode parent) {
//				super("Deco calls");
//				this.parent = parent;
//			}
			
/*			 public void logging(DecompositionParameter decoPara, DecompositionEvent
			 event, Object affectedNodes) {
				 
				 if (affectedNodes != null && affectedNodes instanceof Collection &&
						 ((Collection)affectedNodes).size()==0)
					 return;
				 //
				
				 if (event == DecompositionEvent.BACKTRACKING) {
					 // navigation.getModel().insertNodeInto(new
					 STGEditorTreeNode("Added signal: "+affectedNodes), parent,
					 parent.getChildCount());
				
				 }
			 }*/
			 
//		 }
		 
		
		 //setSTG(currentNode);
//		
//		 currentNode.setProcreative();
//		
//		 DecompositionParameter decoPara = new DecompositionParameter();
//		 decoPara.stg = currentNode.getSTG().clone();
//		 DesiJ.risky = false;
//		
		
		// 1. make a copy of current node, write it to the parameter
		
		STG stg = projectNode.getSTG().clone();
		STGInOutParameter componentParameter = new STGInOutParameter(stg);
		
		AbstractDecomposition deco = null;
		
		// 2. run reduce on it, add it to the tree

		try {
			
			//memorize internal signals and change signature to output,
			//they will be set back to internal after decomposition
			Set<Integer> internals = stg.collectUniqueCollectionFromTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.INTERNAL),
					CollectorFactory.getSignalNameCollector());


			//change internals to outputs
			stg.setSignature(internals, Signature.OUTPUT);
			
			if (source==DECO_BASIC) deco = new BasicDecomposition("basic");
			if (source==DECO_SINGLE_SIG) deco = new LazyDecompositionSingleSignal("lazy_single");
			if (source==DECO_MULTI_SIG) deco = new LazyDecompositionMultiSignal("lazy_multi");
			if (source==DECO_TREE) deco = new TreeDecomposition("tree");
			if (source==DECO_CSC_AWARE) deco = new CscAwareDecomposition("csc_aware");
			if (source==DECO_ICSC_AWARE) deco = new IrrCscAwareDecomposition("icsc_aware");
			
			if (deco==null) return;
			
			
			Collection<STG> components=null;
			final Integer BEFORE_ALL = new Integer(23);
			stg.addUndoMarker(BEFORE_ALL);
			
			if (projectNode.partition==null) {
				projectNode.partition = Partition.getFinestPartition(projectNode.getSTG(),null);
			}
			
			components = deco.decompose(stg, projectNode.partition);

			// set internals back, but only those which are produced in a component and not used as input from
			// another component
			for (STG component : components) {
				for (Integer signal : component.getSignals())
					if (component.getSignature(signal) == Signature.OUTPUT
							&& internals.contains(signal))
						component.setSignature(signal, Signature.INTERNAL);
			}
			
			String deco_name  = deco.getClass().getSimpleName();
			
			STGEditorTreeNode initComponents = new STGEditorTreeNode(deco_name);
			projectNode.add(initComponents);
			
			for (STG s : components) {
				StringBuilder signalNames = new StringBuilder();
				for (Integer sig : s.collectUniqueCollectionFromTransitions(
						ConditionFactory.getSignatureOfCondition(Signature.OUTPUT),
						CollectorFactory.getSignalCollector()))
					signalNames.append(" "+curSTG.getSignalName(sig));

				STGEditorTreeNode nn = new STGEditorTreeNode(
						signalNames.toString(), s, true);
				
				nn.getSTG().copyCoordinates(projectNode.getSTG().getCoordinates());
				
				
				
				initComponents.add(nn);
				//navigationView.addNode(nn, signalNames.toString());
			}
			
			navigationView.updateUI();
			
			navigationView.showNode(initComponents);
			
			
/*			STGEditorTreeNode nn = new STGEditorTreeNode(deco_name, stg, true);
			nn.getSTG().copyCoordinates((STGCoordinates)projectNode.getSTG().getCoordinates());
			projectNode.add(nn);
			
			navigationView.updateUI();
			navigationView.showNode(nn);*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

	public void setShorthand(boolean useShorthand) {
		
		this.useShorthand = useShorthand;
	}

	public boolean isShorthand() {
		return useShorthand;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (isShorthand()!=IS_SHORTHAND.isSelected()) {
			setShorthand(IS_SHORTHAND.isSelected());
			//update visual
			navigationView.refreshSelection();
		}
	}

	 
//	 private void rg() {
//	
//	
//	 STG rg = STGUtil.generateReachabilityGraph(currentNode.getSTG());
//	
//	 currentNode.setProcreative();
//	 addChild(rg, null, "Reachability graph", false);
//	
//	
//	
//	 }
//	
//	 private void save(String file, String fileName) {
//	 try {
//	 FileSupport.saveToDisk(file, fileName);
//	 }
//	 catch (IOException e) {
//	 JOptionPane.showMessageDialog(this, "Could not save file: "+fileName,
//	 "JDesi Error", JOptionPane.ERROR_MESSAGE);
//	 }
//	 }
//	
//	 }
}