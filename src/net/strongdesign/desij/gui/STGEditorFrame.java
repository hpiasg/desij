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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

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
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxLayoutManager;

import net.strongdesign.desij.DesiJ;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.ParsingException;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.Transition;


//import org.jgraph.JGraph;
//import org.jgraph.graph.DefaultGraphModel;
//import org.jgraph.graph.GraphModel;


/**
* This is the main class of the DesiJ GUI. It contains a graphical representation of STGs and 
* a navigation view for navigating between different STGs.
*/
public class STGEditorFrame extends JFrame implements  Runnable, ActionListener {
	private static final long serialVersionUID = 7606945539229848668L;
	
	public final STGEditorAction OPEN 				= new STGEditorAction("Open", KeyEvent.VK_O, 'o', 0, this);
	public final STGEditorAction NEW 				= new STGEditorAction("New", KeyEvent.VK_N, 'n', 0, this);

	public final STGEditorAction LAYOUT 			= new STGEditorAction("Spring layout", KeyEvent.VK_S , 'l', 0, this);
	
	public final STGEditorAction SAVE 				= new STGEditorAction("Save", KeyEvent.VK_S , 'S', 0, this);
	public final STGEditorAction SAVE_AS 			= new STGEditorAction("Save as", KeyEvent.VK_A , null, 0, this);
	public final STGEditorAction EXIT 				= new STGEditorAction("Exit", KeyEvent.VK_X , null, 0, this);
	public final STGEditorAction INITIAL_PARTITION 	= new STGEditorAction("Initial partition", KeyEvent.VK_I , null, 0, this);
	public final STGEditorAction RG 				= new STGEditorAction("Create reachability graph", KeyEvent.VK_R , null, 0, this);
	public final STGEditorAction REDUCE 			= new STGEditorAction("Reduce Component", KeyEvent.VK_R, null, 0, this);
	public final STGEditorAction SIGNAL_TYPE 		= new STGEditorAction("Change signal types", KeyEvent.VK_C, null, 0, this);
	public final STGEditorAction COPY_STG 			= new STGEditorAction("Copy STG", KeyEvent.VK_Y , 'C', 0, this);

	public final STGEditorAction ABOUT 				= new STGEditorAction("About JDesi", KeyEvent.VK_A , null, 0, this);
	
	
	public final STGEditorAction LAYOUT1 			= new STGEditorAction("Organic", KeyEvent.VK_1 , '1', 0, this);
	public final STGEditorAction LAYOUT2 			= new STGEditorAction("Circle", KeyEvent.VK_2 , '2', 0, this);
	public final STGEditorAction LAYOUT3 			= new STGEditorAction("Compact Tree", KeyEvent.VK_3 , '3', 0, this);
	public final STGEditorAction LAYOUT4 			= new STGEditorAction("Parallel Edge", KeyEvent.VK_4 , '4', 0, this);
	public final STGEditorAction LAYOUT5 			= new STGEditorAction("Partition", KeyEvent.VK_5 , '5', 0, this);
	public final STGEditorAction LAYOUT6 			= new STGEditorAction("Stack", KeyEvent.VK_6 , '6', 0, this);

		
	public final static Font 	STANDARD_FONT 	= new Font("Arial",Font.PLAIN, 16);
	public final static Font 	SMALL_FONT 		= new Font("Arial",Font.PLAIN, 12);
	
	public final static Color	INPUT 			= new Color(255,200,200);
	public final static Color	OUTPUT 			= new Color(200,200,255);
	public final static Color	INTERNAL		= new Color(200,255,200);
	public final static Color	DUMMY			= new Color(200,200,200);
	public static final Color 	NAV_COLOR 		= new Color(255,255,200);
	
	
	
	
	/**The current layout cache.*/
//	private STGLayoutCache cache;

	/**The current graph model.*/
	private mxGraphModel model;
	
	/**The current graph, representing the current STG.*/
	private final mxGraph graph;
	private final mxGraphComponent graphComponent;
	private final mxGraphOutline graphOutline;
	protected mxRubberband rubberband;	
	
	/**The navigation view.*/
	private STGEditorNavigation navigationView;
	
	/**The menu bar.*/
	private STGEditorMenuBar menuBar;
	
	/**The split pane containing the navigation view and the current graph.*/
	private JSplitPane splitPane;
	
	
	private String label;
	
	public boolean isVertexIgnored(Object vertex)
	{
		return !graph.getModel().isVertex(vertex)
				|| !graph.isCellVisible(vertex);
	}
	
	public mxRectangle getVertexBounds(Object vertex)
	{
		mxRectangle geo = graph.getModel().getGeometry(vertex);
		return new mxRectangle(geo);
	}
	
	public mxRectangle setVertexLocation(Object vertex, double x, double y)
	{
		mxIGraphModel model = graph.getModel();
		mxGeometry geometry = model.getGeometry(vertex);
		mxRectangle result = null;

		if (geometry != null)
		{
			result = new mxRectangle(x, y, geometry.getWidth(), geometry
					.getHeight());

			if (geometry.getX() != x || geometry.getY() != y)
			{
				geometry = (mxGeometry) geometry.clone();
				geometry.setX(x);
				geometry.setY(y);
				model.setGeometry(vertex, geometry);
			}
		}

		return result;
	
	}
	
	public void initSTG(STG stg, mxGraph graph) {
		
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try
		{
			Map<Node, mxCell> nc = new HashMap<Node, mxCell>();
			
			
			for (Node node : stg.getNodes()) {
				mxCell cell=null;
				
				if (node instanceof Place ) {
					//cell = new PlaceCell((Place)node);
					cell = (mxCell) graph.insertVertex(parent, null, ((Place)node).getMarking(), 50, 50, 25, 25, "shape=ellipse;perimeter=ellipsePerimeter");
				}
				
				else if (node instanceof Transition) {
//					cell = new TransitionCell((Transition)node, stg);
					int signalID = ((Transition)node).getLabel().getSignal();
					String style = "fontColor=black";
					if (stg.getSignature(signalID)==Signature.INPUT) style = "fontColor=blue";
					if (stg.getSignature(signalID)==Signature.OUTPUT) style = "fontColor=blue";
					if (stg.getSignature(signalID)==Signature.INTERNAL) style = "fontColor=green";
					if (stg.getSignature(signalID)==Signature.ANY) style = "fontColor=black;fillColor=yellow";
					
					cell = (mxCell) graph.insertVertex(parent, null, ((Transition)node).getString(Transition.UNIQUE), 50, 50, 40, 20, style);
				}
				
				nc.put(node, cell);
				graph.addCell(cell);
				
			}
			
			for (Node node : stg.getNodes()) {
				mxCell source=nc.get(node);
				
				
				for (Node child : node.getChildren()) {
					mxCell target=nc.get(child);
					
					graph.insertEdge(parent, null, null, source, target);
					
//					source.add(new DefaultPort());
//					target.add(new DefaultPort());
//					DefaultEdge edge = new DefaultEdge();
//					
//					edge.setSource(source.getChildAt(0));
//					edge.setTarget(target.getChildAt(0));
					
//					GraphConstants.setFont(edge.getAttributes(), STGEditorFrame.STANDARD_FONT);
//					GraphConstants.setLabelPosition(edge.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE/2, 10));
					
//					int v = node.getChildValue(child);
//					if (v>1)
//						edge.setUserObject(v);
					
//					int arrow = GraphConstants.ARROW_TECHNICAL;
//					GraphConstants.setLineEnd(edge.getAttributes(), arrow);
//					GraphConstants.setEndFill(edge.getAttributes(), true);
					
//					insert(edge);
				}
			}
			
			mxGraphLayout cl = new mxOrganicLayout(graph);
//			mxCircleLayout cl = new mxCircleLayout(graph);
			
			cl.execute(parent);
			// 
			
			///// shift nodes to the top left corner
			double max = 0;
			Double top = null;
			Double left = null;
			
			
			List<Object> vertices = new ArrayList<Object>();
			int childCount = model.getChildCount(parent);

			for (int i = 0; i < childCount; i++)
			{
				Object cell = model.getChildAt(parent, i);

				if (!isVertexIgnored(cell))
				{
					vertices.add(cell);
					mxRectangle bounds = getVertexBounds(cell);

					if (top == null) top = bounds.getY();
					else
						top = Math.min(top, bounds.getY());

					
					if (left == null)
						left = bounds.getX();
					else
						left = Math.min(left, bounds.getX());

					max = Math.max(max, Math.max(bounds.getWidth(), bounds.getHeight()));
				}
			}
			
			for (Object obj :vertices)
			{
				if (!graph.isCellMovable(obj)) continue;
				
				double x, y;
				mxRectangle bounds = getVertexBounds(obj);
				x = bounds.getX()-left+50;
				y = bounds.getY()-top+50;
				
				setVertexLocation(obj, x, y);
			}
			
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		
	}
	
	/**
	 * Constructs an instance.
	 * @param windowLabel The label of the window.
	 * @param stg The initial STG.
	 */
	public STGEditorFrame(String windowLabel, STG stg){
		
		//Initialise window
		super(windowLabel);		
		setBounds(new Rectangle(50, 50, 800,600));	    
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);  
		this.label = windowLabel;
		
		//Initialise navigation view
//		navigationView = new STGEditorNavigation(stg, this);
				
		//The initial model and layout cache
		
		
		//cache = new STGLayoutCache(stg, model);
		model = new mxGraphModel();
		graph = new mxGraph(model);
		initSTG(stg, graph);
		
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
		
		//graph = new mxGraph(model, cache);
		
		graphComponent = new mxGraphComponent(graph);
		graphComponent.setSwimlaneSelectionEnabled(true);
		graphComponent.setConnectable(false);
		
		graphOutline = new mxGraphOutline(graphComponent);
		
//		graphComponent.setAntiAlias(true);
		
		
		//Build up the graph corresponding to the STG
//		cache.init();
		
		//Put it all together
		JSplitPane vert = 
			new JSplitPane(
					JSplitPane.VERTICAL_SPLIT,
					new JScrollPane(navigationView), 
					graphOutline
			);
		vert.setDividerLocation(450);

		splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				vert,
				graphComponent);
		
		splitPane.setDividerLocation(250);
		
		getContentPane().add(splitPane, BorderLayout.CENTER);
		rubberband = new mxRubberband(graphComponent);
		
		//Create menu bar
		
		menuBar = new STGEditorMenuBar(this);//, cache);
		
		setJMenuBar(menuBar);
	}
	
	
	
	public void saveProject(String fileName) throws FileNotFoundException {
		
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileName));
		out.setComment("Generated by DesiJ");
		
	}
	
//	public STGEditorFrame(String fileName) {
//		FileSupport.loadFileFromDisk(fileName);
//		
//		
//	}
	
//	public void run() {
//		if (backgroundMethod.equals("reduce")) {
//			try {
//				reduce();
//			}
//			catch (OutOfMemoryError e) {
//				JOptionPane.showMessageDialog(this, "Out of memory error while reducing", "JDesi - Error", JOptionPane.ERROR_MESSAGE);
//				return;
//			}
//			JOptionPane.showMessageDialog(this, "Finished reduction", "JDesi", JOptionPane.INFORMATION_MESSAGE);
//		}
//	}
//	
	public void setNav(STGEditorNavigation nav) {
//		navigation = nav;
//	//	navModel = navigation.getModel();
//		currentNode = (STGEditorTreeNode)navModel.getRoot();        
	}
	
	public  String getFileName() {
		return navigationView.getCurrentNode().getLabel();
	}
	
	public void setFileName(String fileName) {
		navigationView.getCurrentNode().setLabel(fileName);
	}
	
	public void updateSTG(STG stg) {
		//editor.setSTG(stg, null);
	}

	public void run() {
		
	}


	public void setSTG(STGEditorTreeNode node){
		
		if (!node.isSTG()) return;

//		model=new DefaultGraphModel();
//		cache = new STGLayoutCache(node.getSTG(), model);
//		graph.setGraphLayoutCache(cache);
		
//		cache.init();

		splitPane.validate();
	}
		
//		/*	//Former STG will be saved in STGEditorTreeNode
//		 if (editor !=  null)  
//		 if (currentNode.isSTG())
//		 currentNode.setSTG(STGFile.convertToG(editor.getSTG(), true)+STGEditor.convertToG(editor.getSTG(), true, coordinates));
//		 */
//		//Remove event handlers
		//removeKeyListener(editor);
		
		//Generate new STG from new node
/*		try {
			stg = STGFile.convertToSTG(node.getSTG());
		} catch (STGException e) {
			JOptionPane.showMessageDialog(this, "Internal parsing error", "JDesi - Error", JOptionPane.ERROR_MESSAGE);
		}
		*/
		
		
		/*
		coordinates = STGEditor.convertToCoordinates(node.getSTG(), stg);
		editor = new STGEditor(stg, scrollPane, this, coordinates, options);
		
		//register new listener
		addKeyListener(editor);
		menuBar.setEditor(editor);
		
		//save scroll bar status, make new editor visible and restore scroll bar status
		int verticalValue = scrollPane.getVerticalScrollBar().getValue();
		int horizontalValue = scrollPane.getHorizontalScrollBar().getValue();
		scrollPane.setViewportView(editor); 
		scrollPane.getVerticalScrollBar().setValue(verticalValue);
		scrollPane.getHorizontalScrollBar().setValue(horizontalValue);
		
		*/
		
		
		//select nin navigation
		//navigation.selectNode(node);
		
		
		
		//set new title
//		setTitle(node + " - JDesi");
//		
//		currentNode = node;
//	}
	
	
	
	
//	public void performOperation(Node node) throws STGException{		
//		//this does not work for arbitrary node removal, since isolated nodes do not occur in
//		//the file representation
//		if (node instanceof Place) {
//			if (ConditionFactory.getRedundantPlaceCondition(currentNode.getSTG(), 0).fulfilled((Place)node)) {
//				STG newSTG = currentNode.getSTG().clone();
//		//		STGEditorCoordinates newCoord = currentNode.getCoordinates().clone(newSTG);
//				Place p = newSTG.getPlaces(ConditionFactory.getEqualTo((Place)node)).get(0);
//				newSTG.removePlace(p);
//				newCoord.remove(p);
//								
//				setSTG(addChild(newSTG, newCoord, "Redundant place: "+node.getString(Node.SIMPLE), false));				
//				repaint();
//			}
//		}
//		else if (node instanceof Transition) {
//			if (ConditionFactory.getRedundantTransitionCondition(currentNode.getSTG()).fulfilled((Transition)node)) {
//				STG newSTG = currentNode.getSTG().clone();
//				STGEditorCoordinates newCoord = currentNode.getCoordinates().clone(newSTG);
//				Transition t = newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
//				newSTG.removeNode(t);
//				newCoord.remove(t);
//								
//				setSTG(addChild(newSTG, newCoord, "Redundant transition: "+node.getString(Node.UNIQUE), false));				
//				repaint();
//			}
//			else if (ConditionFactory.SECURE_CONTRACTABLE.fulfilled((Transition)node)) {
//				int nrn = (node.getParents().size() * node.getChildren().size());
//				double nn = 2*Math.PI / nrn;
//				double arc = 0;
//				int newPlaceRadius;
//				if (nrn == 1 )
//					newPlaceRadius = 0;
//				else
//					newPlaceRadius = 30 + (int) (0.3*nrn);
//				
//				Point oldPoint = currentNode.getCoordinates().get(node);			
//				
//				STG newSTG = currentNode.getSTG().clone();
//				STGEditorCoordinates newCoord = currentNode.getCoordinates().clone(newSTG);
//
//				Transition t = newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
//				newSTG.contract(t);
//				
//				
//				
//				for (Node nk : newSTG.getNodes())
//					if (!newCoord.keySet().contains(nk)) {
//						
//						Point nP = new Point( oldPoint.x + (int) (Math.sin(arc)*newPlaceRadius), oldPoint.y + (int) (Math.cos(arc)*newPlaceRadius));
//						if (nP.x<0) nP.x = 0;
//						if (nP.y<0) nP.y = 0;
//						
//						newCoord.put(nk,  nP );
//						arc += nn;
//					}
//
//				setSTG(addChild(newSTG, newCoord, "Contracted: "+node.getString(Node.UNIQUE), false));				
//				repaint();
//			}
//		}
//	}
//	
	public void initialPartition() throws STGException {
		STGEditorTreeNode curNode = navigationView.getCurrentNode();
		if (! curNode.isSTG())
			return;
		STG curSTG = curNode.getSTG();
		
		STGEditorTreeNode newNode = new STGEditorTreeNode("Initial components", navigationView.getCurrentNode());
		navigationView.addNode(newNode, "Partition");
		
		for (STG s : Partition.splitByPartition(curSTG, Partition.getFinestPartition(curSTG,null))) {
			
			StringBuilder signalNames = new StringBuilder();
			for (Integer sig : s.collectUniqueCollectionFromTransitions(ConditionFactory.getSignatureOfCondition(Signature.OUTPUT), CollectorFactory.getSignalCollector()))
				signalNames.append(sig.toString());
			
			STGEditorTreeNode nn = new STGEditorTreeNode(signalNames.toString(), s, true, newNode);
			navigationView.addNode(nn, signalNames.toString());
		}
		
		navigationView.setCurrentNode(newNode);
	}
//	
//	private STGEditorTreeNode addChild(STG stg, STGEditorCoordinates coordinates, String mes, boolean procreative) {
////		STGEditorTreeNode newNode = new STGEditorTreeNode(mes, stg, coordinates, procreative);
////		
////		if (currentNode.getParent()!=null && currentNode.getParent().getIndex(currentNode) != currentNode.getParent().getChildCount()-1)
////			currentNode.setProcreative();
////		
////		STGEditorTreeNode newParent = currentNode;
////		while (!newParent.isProcreative())
////			newParent = (STGEditorTreeNode) newParent.getParent();
////		
////		navModel.insertNodeInto(newNode, newParent, newParent.getChildCount());
//		return null;
//		
//	}
//
//private STGEditorTreeNode addChild(STGEditorTreeNode parent, STG stg, STGEditorCoordinates coordinates, String mes, boolean procreative) {
////	STGEditorTreeNode newNode = new STGEditorTreeNode(
////			mes, stg, coordinates, procreative);
////	
////	
////	navModel.insertNodeInto(newNode, parent, parent.getChildCount());
////	return newNode;
//	return null;
//	
//}
//
//	
//	
//	public void springLayout() { 
////		//SPRING_LAYOUT.setEnabled(false);
////		for (int t=0; t<=10 ; ++t)
////			STGEditorLayout.applySpringLayout(editor.getAllCoordinates(), 2);
////		editor.repaint();
////		//SPRING_LAYOUT.setEnabled(true);
//	}
//	
//
//	
//	
//	public void deleteNode(Node node) {
//		STG newSTG = currentNode.getSTG().clone();
//		STGEditorCoordinates newCoord = currentNode.getCoordinates().clone(newSTG);
//		Node n=null;
//		if (node instanceof Transition)
//			n = newSTG.getTransitions(ConditionFactory.getEqualTo((Transition)node)).get(0);
//		if (node instanceof Place)
//			n = newSTG.getPlaces(ConditionFactory.getEqualTo((Place)node)).get(0);
//		
//		
//		newSTG.removeNode(n);
//		newCoord.remove(n);
//						
//		setSTG(addChild(newSTG, newCoord, "Deleted: "+node.getString(Node.UNIQUE), false));				
//		repaint();
//	}
//	
//	
	public void exit() {	
		dispose();
	}

	
	public void open()   {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(STGFileFilter.STANDARD);
		
		fileChooser.showOpenDialog(this);
		label = fileChooser.getSelectedFile().getAbsolutePath();
		String fileName = label;
		
		try {
			
			File f= new File(fileName);
			if (! f.exists() ) {
				f = new File(fileName+".g");
				if (! f.exists())
					throw new FileNotFoundException(fileName);          
			}       

			String file = FileSupport.loadFileFromDisk(f.getAbsolutePath());
			STG stg = STGEditorFile.convertToSTG(file, true);
//			STGEditorCoordinates coordinates = STGEditorFile.convertToCoordinates(file);


//			navigationView = new STGEditorNavigation(stg, this);
			//The initial model and layout cache
			
			model = new mxGraphModel();
//			cache = new STGLayoutCache(stg, model);
			
			//Build up the graph corresponding to the STG
//			cache.init();
			
			
//			String file = FileSupport.loadFileFromDisk(fileName);
//			STG stg = STGFile.convertToSTG(file);
//			STGEditorCoordinates coordinates = STGEditorFile.convertToCoordinates(file);
//			new STGEditorFrame(fileName, stg, coordinates).setVisible(true);
		} 
		catch (ParseException e) {
			JOptionPane.showMessageDialog(this, "Could not parse file: "+fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Could not load file: "+fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (STGException e) {
				JOptionPane.showMessageDialog(this, "Could not parse file: "+fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
		}
		
	
	}
	
	private void save() throws IOException {
		String name = getFileName();
		if (name == null) {
			JFileChooser fileChooser=null;
			try {
				fileChooser = new JFileChooser(new File(".").getCanonicalPath());				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			fileChooser.setSelectedFile(new File( navigationView.getCurrentNode().toString().replaceAll(" ", "_").replaceAll(":", "")+".g"));
			fileChooser.setFileFilter(STGFileFilter.STANDARD);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			name = fileChooser.getSelectedFile().getAbsolutePath();
			if (name == null)
				return;
			label = name;
		}
		
		FileSupport.saveToDisk(STGFile.convertToG(navigationView.getCurrentNode().getSTG()), label);
	}
	
	private void saveAs() throws IOException {
		String name = label;
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(STGFileFilter.STANDARD);
		if (name != null)
			fileChooser.setSelectedFile(new File(label));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		name = fileChooser.getSelectedFile().getAbsolutePath();
		if (name == null)
			return;
		label = name;

		FileSupport.saveToDisk(STGFile.convertToG(navigationView.getCurrentNode().getSTG()), label);
	}
	
	public void setLayout(int type) {
		
		Object parent = graph.getDefaultParent();
		graph.getModel().beginUpdate();
		try
		{
			mxGraphLayout gl;
			if (type==1) gl = new mxOrganicLayout(graph);
			else if (type==2) gl = new mxCircleLayout(graph);
			else if (type==3) gl = new mxCompactTreeLayout(graph, false, false);
			else if (type==4) gl = new mxParallelEdgeLayout(graph, 30);
			else if (type==5) gl = new mxPartitionLayout(graph, false, 30);
			else if (type==6) gl = new mxStackLayout(graph, false, 30);
			else
				gl = new mxOrganicLayout(graph);
			
			gl.execute(parent);
		} finally {
			graph.getModel().endUpdate();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		try {
//			if (source == SPRING_LAYOUT) springLayout();
			if (source == OPEN) open();
			else if (source == SAVE) save();
			else if (source == SAVE_AS) saveAs();
			else if (source == EXIT) exit();
//			else if (source == RG) rg();
			else if (source == COPY_STG) copySTG();
			else if (source == INITIAL_PARTITION) initialPartition();
//			else if (source == SIGNAL_TYPE) changeSignalType();
//			else if (source == SPRING_LAYOUT_EXCLUDE);
//			else if (source == SPRING_LAYOUT_INCLUDE);
//			else if (source == REDUCE) {backgroundMethod = "reduce"; new Thread(this).start(); }
//			//else if (source == REDUCE) {new DecompositionOptions("Decomposition", currentNode.getSTG(), new DesijCommandLineWrapper(new String[]{""})).setVisible(true);}
//			else if (source == ABOUT) {new STGEditorAbout(this).setVisible(true);}
			else if (source == LAYOUT1) setLayout(1);
			else if (source == LAYOUT2) setLayout(2);
			else if (source == LAYOUT3) setLayout(3);
			else if (source == LAYOUT4) setLayout(4);
			else if (source == LAYOUT5) setLayout(5);
			else if (source == LAYOUT6) setLayout(6);

		}
		catch (Exception ee) {
			ee.printStackTrace();
		}
	}
//	
//	
	private void copySTG() {
//		STG stg = currentNode.getSTG().clone();
//		STGEditorCoordinates coordinates = currentNode.getCoordinates().clone(stg);
//		
//		setSTG(addChild(currentNode, currentNode.getSTG().clone(), coordinates, "Copy of "+currentNode, false));
		
	}

//	private void changeSignalType() {
//		signalChooser = new STGEditorSignalChooser("JDesi - Signals of "+ currentNode, currentNode.getSTG(), this);
//		signalChooser.setAlwaysOnTop(true);
//		signalChooser.setModal(true);
//		signalChooser.setVisible(true);
//		
//		
//	}
//
////	
////	private STGEditorCoordinates deepCorrectedCoordinates(STGEditorCoordinates oldCoordinates, STG stg) {
////		STGEditorCoordinates result = new STGEditorCoordinates();
////		
////		//make a 'deep' copy of the coordinate set
////		Map<String, Point> c = new HashMap<String, Point>();
////		for (Node node : oldCoordinates.keySet()) {
////			if (node instanceof Transition)
////				c.put(node.getString(Node.UNIQUE), (Point) oldCoordinates.get(node).clone());
////			else
////				c.put(node.getString(Node.SIMPLE), (Point) oldCoordinates.get(node).clone());
////		}
////		
////		//After this loop all existing coordinates are copied for the new stg
////		for (Node node : stg.getNodes()) {
////			Point p;
////			if (node instanceof Transition)
////				p = c.get(node.getString(Node.UNIQUE));
////			else 
////				p = c.get(node.getString(Node.SIMPLE));
////			if (p != null);
////				result.put( node, p  );  
////		}
////		
////		
////		//layout all unknown nodes
////		for (Node node : stg.getNodes()) {
////			Point p;
////			if (node instanceof Transition)
////				p = c.get(node.getString(Node.UNIQUE));
////			else 
////				p = c.get(node.getString(Node.SIMPLE));
////			
////			if (p == null) {
////				Point center = new Point(0,0);
////				int i = 0;
////
////				for (Node node2 : node.getNeighbours()) {
////					Point nP = result.get(node2);
////					if (nP != null) {
////						center.translate(nP.x, nP.y);
////						++i;
////					}
////				}
////				
////				if (i!=0) {
////					center.x /= i; center.y /= i;
////					result.put(node, center);
////				}
////				else
////					result.put(node, new Point(0,0));
////			}
////		}
////	
////		STGEditorLayout.applySpringLayout(result, result.keySet(), 5);
////		return result;		
////	}
//	
//	private void reduce() {
//		if (!currentNode.isSTG()) {
//			JOptionPane.showMessageDialog(this, "No STG selected", "JDesi - Reduce", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//			
//		class Deco extends BasicDecomposition {
//			
//			private STGEditorTreeNode parent;
//
//		
//			
//			public Deco(STGEditorTreeNode parent)  {
//				super();
//				this.parent = parent;
//
//				
//			//	navigation.getModel().insertNodeInto(new STGEditorTreeNode("1. Try"), parent, parent.getChildCount());
//				
//				
//				
//			}
//		
//			public void logging(DecompositionParameter decoPara, DecompositionEvent event, Object affectedNodes) {
//				if (affectedNodes != null && affectedNodes instanceof Collection && ((Collection)affectedNodes).size()==0)
//					return;
//				
//				
//				//
//		
//				
//				if (event == DecompositionEvent.BACKTRACKING) {
//			//		navigation.getModel().insertNodeInto(new STGEditorTreeNode("Added signal: "+affectedNodes), parent, parent.getChildCount());
//					
//				}
//			}		
//		}
//		
//		//setSTG(currentNode);
//		
//		currentNode.setProcreative();
//		
//		DecompositionParameter decoPara = new DecompositionParameter();
//		decoPara.stg = currentNode.getSTG().clone();
//		DesiJ.risky = false;
//
//		
//		Deco deco = new Deco(currentNode);
//		try {
//			deco.reduce(decoPara);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	private void rg() {
//	
//	
//		STG rg = STGUtil.generateReachabilityGraph(currentNode.getSTG());
//	
//		currentNode.setProcreative();
//		addChild(rg, null, "Reachability graph", false);
//		
//		
//		
//	}
//	
//	private void save(String file, String fileName) {
//		try {
//			FileSupport.saveToDisk(file, fileName);
//		}
//		catch (IOException e) {
//			JOptionPane.showMessageDialog(this, "Could not save file: "+fileName, "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//	}
//
//}
}