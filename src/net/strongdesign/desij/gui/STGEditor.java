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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.traversal.Condition;
import net.strongdesign.stg.traversal.ConditionFactory;


public class STGEditor extends JPanel 
implements Scrollable, MouseListener, MouseMotionListener, ActionListener {

	private static final long serialVersionUID = -1954217772319190176L;

	/**Color of secure contractable transitions leading to a new structural autoconflict*/
	private static final Color contractableTransAutoConfColor 	= Color.RED;
	/**Color of secure contractable transitions*/
	private static final Color contractableTransitionColor 	= Color.ORANGE;
	/**Radius of a token*/
    private static final int dotRadius = 3;
	/**Color of dummy transition*/
	private static final Color dummyColor					= Color.BLACK;
	/**The predefined grid width*/
    private static final int gridX = 40;
	private static final int gridY = 40;
	/**Color of input transition*/
	private static final Color inputColor					= Color.RED;
	/**Color of internal transition*/
	private static final Color internalColor				= Color.GREEN;
	/**Color of normal transition, should not appear*/
	private static final Color normalColor					= Color.BLACK;
	/**Color of output transition*/
	private static final Color outputColor					= Color.BLUE;
	/**Radius of a place*/
	private static final int placeRadius = 11;
	/**Color of redundant place*/
	private static final Color redPlaceColor 				= Color.ORANGE;
	/**Color of redundant transition*/
	private static final Color redTransitionColor			= Color.CYAN;
	/**Color of node shadow*/
	private static final Color shadowColor				 	= Color.GRAY;
	/**x and -y offset of shadow*/
	private static final int shadowOffset = 3;
	/**Height of transitions*/
//	private static final int transitionHeight = 20;
	
	
	
	
	
	
	
	public final STGEditorAction ADD_ARC_PLACE_TRANSITION = new STGEditorAction("Add arc place -> transition", 0 , ',', 0, this);
	public final STGEditorAction ADD_ARC_TRANSITION_PLACE = new STGEditorAction("Add arc transition -> place", 0 , '.', 0, this);
	public final STGEditorAction ADD_PLACE = new STGEditorAction("Add place", KeyEvent.VK_P , null, 0, this);
	public final STGEditorAction ADD_TRANSITION = new STGEditorAction("Add transition", KeyEvent.VK_T , null, 0, this);
	public final STGEditorAction CYCLES = new STGEditorAction("Show cycles", KeyEvent.VK_C , 'c', 0, this);
	public final STGEditorAction ENLARGE = new STGEditorAction("Enlarge", KeyEvent.VK_E , 'E', 0, this);
    public final STGEditorAction PRINT = new STGEditorAction("Print", KeyEvent.VK_P , 'P', 0, this);
	public final STGEditorAction ZOOM_IN = new STGEditorAction("Zoom in", 0 , '+', 0, this);
	public final STGEditorAction RENAME_PLACES = new STGEditorAction("Rename places", 0 , null, 0, this);
	public final STGEditorAction ROTATE_ACW = new STGEditorAction("Rotate anti-clockwise", KeyEvent.VK_C , '8', 0, this);    
	public final STGEditorAction ROTATE_CW = new STGEditorAction("Rotate clockwise", KeyEvent.VK_C , '9', 0, this);
	public final STGEditorAction SHOW_ALL= new STGEditorAction("Show all", KeyEvent.VK_A , null, 0, this);
	public final STGEditorAction SHRINK = new STGEditorAction("Shrink", KeyEvent.VK_S , 'S', 0, this);
	public final STGEditorAction SUB_ARC_PLACE_TRANSITION = new STGEditorAction("Remove arc place -> transition", 0 , ';', 0, this);
	public final STGEditorAction SUB_ARC_TRANSITION_PLACE = new STGEditorAction("Remove arc transition -> place", 0 , ':', 0, this);
	public final STGEditorAction ZOOM_OUT = new STGEditorAction("Zoom out", 0 , '-', 0, this);
    public final STGEditorAction PRINT_VISIBLE = new STGEditorAction("Print visible", 0 , null, 0, this);

    public final STGEditorSwitchingAction ALIGN_GRID;
	public final STGEditorSwitchingAction FORCED_DELETION;
	public final STGEditorSwitchingAction PLACE_NAMES;
	public final STGEditorSwitchingAction SHADOWS;
	public final STGEditorSwitchingAction DECOMPOSITION;
	public final STGEditorSwitchingAction DUMMY_NAMES;

	
	
	
	
	/**The currently selected noded for moving*/
	protected Node activatedNode;
	
	
    
	/**The height of the drawing area*/
	protected final int baseHeight = 5000;
	
	/**The width of the drawing area*/
	protected final int baseWidth = 5000;

	/**The bounding box of all nodes*/
	private Map<Node, Rectangle> boundingBox;
	
	
	/**All transitions, which are dummy and contractable*/
	private Collection<Transition> conTrans;
	/**The coordinates of the nodes*/
    private STGEditorCoordinates coordinates;	

	
	
	/**The frame which contains this editor*/
    private STGEditorFrame frame;
	
	public final STGEditorSwitchingAction GRID;
	
	/**Used for determine the relative change of position when dragging*/
	private Point lastPos = new Point(0,0);
	
	/**The set of all nodes*/
	private Collection<Node> nodes;
	
	/**The node which lies under the cursor*/
	protected Node nodeUnderCursor;
	
	/**All other options*/
	private STGEditorOptions options;
	
	
	
	/**The reference set plus neighboured transitions for redundant places*/
	private Map<Node, Object> redPlaceReasons;
	
	/**For different graphical representations*/ 
    private Collection<Place> redPlaces;
	
	/**The set of all redundant transitions*/
	private Collection<Transition> redTrans;

	/**The {@link java.awt.ScrollPane} which shows the component*/
	private JScrollPane scrollPane;

	public final STGEditorAction SELECT_ALL= new STGEditorAction("Select all", KeyEvent.VK_A , 'A', 0, this);

	/**For the selection of several nodes*/
    private Set<Node> selectedNodes = new HashSet<Node>();

	/**Coordinates of the start corner of the selection box*/
    private Point selectionEnd = null;

	/**Coordinates of the end corner of the selection box*/
	private Point selectionStart = null;
	
    /**The STG which is drawn*/
	private STG stg;
	
	
	//***************************************************
	
	//**************************************************
		
	/**All possible zoom values*/
	private double[] zoomLevels = {0.2, 0.3, 0.4, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0};

	/**
	 * Creates a new editor with predefined coordinates
	 * @param stg The stg to show
	 * @param coordinates The coordinstes of the nodes, if null random coordinates are generated
	 * @param frame The overall frame for all components
	 */
	public STGEditor(STG stg, STGEditorCoordinates coordinates, STGEditorFrame frame, STGEditorOptions options) {
		super();
		
		this.stg = stg;
		this.coordinates = coordinates;
        //if (this.coordinates == null)
          //  this.coordinates = STGEditorLayout.applyRandomLayout(getNodes());
		
		this.frame = frame;
		this.options = options;
		
		this.scrollPane = (JScrollPane) getParent();
		
		addMouseListener(this);
		addMouseMotionListener(this);        
		//addKeyListener(this);        
        setFocusable(true);
		requestFocusInWindow();
		
        setBackground(Color.WHITE);
        setZoom(options.actZoom);
		
        
    	DECOMPOSITION = new STGEditorSwitchingAction("Disable decomposition", "Enable decompositin", options.showPossibleOperations, KeyEvent.VK_D , 'd', 0, this);
    	SHADOWS = new STGEditorSwitchingAction("Hide shadows", "Show shadows", options.showShadows, KeyEvent.VK_S , 's', 0, this);
    	FORCED_DELETION = new STGEditorSwitchingAction("No forced deletion", "Forced deletion", options.forceDeletion, 0 , null, 0, this);
    	PLACE_NAMES = new STGEditorSwitchingAction("Hide place names", "Show place names", options.showPlaceNames, KeyEvent.VK_N , 'p', 0, this);
    	GRID  = new STGEditorSwitchingAction("Hide grid", "Show grid", options.showGrid, KeyEvent.VK_G , 'G', 0, this);
    	ALIGN_GRID = new STGEditorSwitchingAction("No align to grid", "Align to grid", options.alignToGrid, KeyEvent.VK_A , 'g', 0, this);
    	DUMMY_NAMES = new STGEditorSwitchingAction("Hide dummy names", "Show dummy names", options.showDummyNames, KeyEvent.VK_D , null, 0, this);

        
        
        
		setup();
	}
       
	/**
	 * The implementation of the {@link ActionListener} interface. Unhandled actions are 
	 * forwarded to {@link #frame}.
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		try {
			
			 
			if (source == ROTATE_ACW) rotate(2*Math.PI/100);
			else if (source == ROTATE_CW) rotate(-2*Math.PI/100);
			else if (source == ZOOM_IN) zoomIn();
			else if (source == ZOOM_OUT) zoomOut();
			else if (source == SHOW_ALL) showAll();
			else if (source == PRINT) print();
			else if (source == PRINT_VISIBLE) printVisible();
			else if (source == SHRINK) scale(0.9);
			else if (source == ENLARGE) scale(1/0.9);
			else if (source == DECOMPOSITION) { options.showPossibleOperations = ! options.showPossibleOperations; repaint(); }
			else if (source == SHADOWS) {options.showShadows = ! options.showShadows; repaint();}
			else if (source == FORCED_DELETION) {options.forceDeletion = ! options.forceDeletion; repaint();}
			else if (source == PLACE_NAMES) {options.showPlaceNames = ! options.showPlaceNames; repaint();}
			else if (source == GRID) {options.showGrid = ! options.showGrid; repaint();}
			else if (source == ALIGN_GRID) {options.alignToGrid = ! options.alignToGrid; repaint();}
			else if (source == DUMMY_NAMES) {options.showDummyNames = ! options.showDummyNames; repaint();}
			else if (source == ADD_PLACE) { 
				Place p = stg.addPlace("p", 0); 
				setup(); 
				repaint();
				coordinates.put(p, (Point) lastPos.clone());
			}
			else if (source == ADD_TRANSITION)  { 
				Transition t = stg.addTransition(new SignalEdge(stg.getSignalNumber("eps"), EdgeDirection.UNKNOWN));  
				setup(); 
				repaint();
				coordinates.put(t, (Point) lastPos.clone());
			} 
			else if (source == ADD_ARC_PLACE_TRANSITION) {addPlaceTransitionArc(); setup(); repaint(); }
			else if (source == ADD_ARC_TRANSITION_PLACE) {addTransitionPlaceArc(); setup(); repaint();}
			else if (source == SUB_ARC_PLACE_TRANSITION) {subPlaceTransitionArc(); setup(); repaint();}
			else if (source == SUB_ARC_TRANSITION_PLACE) {subTransitionPlaceArc(); setup(); repaint();}
			else if (source == RENAME_PLACES) renamePlaces();
			else if (source == SELECT_ALL) {selectedNodes.addAll(nodes); repaint();}
			

		/*	else if (cmd.equals("Show cycles with selected nodes")) {
				java.util.List<Vector<Node>> cycles = GraphOperations.getCyclesWith(selectedNodes);
				for (Vector<Node> cycle : cycles) {
					specialNodes = new HashSet<Node>(cycle);
					specialNodesSet.add( specialNodes );
					specialIndex = specialNodesSet.size()-1;
				}	
				repaint();
			}*/
		}
		catch (Exception ee) {
			ee.printStackTrace();
		}
	}
    
	
	private void addPlaceTransitionArc() {
		if (selectedNodes.size() != 2)
			return;
		
		Iterator<Node> it = selectedNodes.iterator();
		Node n1 = it.next();
		Node n2 = it.next();
		
		if (n1 instanceof Place && n2 instanceof Transition )
			n1.addToChildValue(n2, 1);
		if (n1 instanceof Transition && n2 instanceof Place )
			n2.addToChildValue(n1, 1);		
	}

	@SuppressWarnings("all")
	private void addTransition() {
		new STGEditorNewTransition(this).setVisible(true);				
	}
	
	private void addTransitionPlaceArc() {
		if (selectedNodes.size() != 2)
			return;
		
		Iterator<Node> it = selectedNodes.iterator();
		Node n1 = it.next();
		Node n2 = it.next();
		
		if (n1 instanceof Place && n2 instanceof Transition )
			n2.addToChildValue(n1, 1);
		if (n1 instanceof Transition && n2 instanceof Place )
			n1.addToChildValue(n2, 1);		
	}
	
	/**
	 * Paints all arcs outgoing from a node
	 * @param g Graphic context
	 * @param n The node
	 */
	private void drawArc(Graphics g, Node n) {
		for (Node node : n.getChildren()) {

			int v = n.getChildValue(node);
			
			Point s = getCoordinates(n);            
			Point e = getCoordinates(node);
			
			if (Math.abs(s.x-e.x)+Math.abs(s.y-e.y) < 10) continue;
			
			Point start = getBoundingBoxIntersection(n, e);
			Point end = getBoundingBoxIntersection(node, s);

//			TODO draw arc value better
			if (v>1)
				g.drawString( ""+v, (end.x+start.x)/2, (end.y+start.y)/2);
			
			
			g.drawLine(start.x, start.y, end.x, end.y);
			g.fillPolygon(getArrowHead(end, start));
			
		}
	}
	
	private void drawGrid(Graphics graphic) {
        if (options.showGrid) {
            graphic.setColor(new Color(200, 200, 200));
            for (int x = gridX; x<=baseWidth; x+=gridX)
                for (int y = gridY; y<=baseHeight; y+=gridY)
                    graphic.drawRect(x-2,y-2, 4, 4);
            graphic.setColor(Color.BLACK);
        }
        
        if (activatedNode != null && options.alignToGrid) {
            Point p = getNextGridPoint(getCoordinates(activatedNode));
            graphic.setColor(Color.PINK);
            graphic.fillRect(p.x-10, p.y-10, 20, 20);
            graphic.setColor(Color.BLACK);
            
        }
        
    }
	
	

	/**
	 * Draws a node
	 * @param g The graphic context
	 * @param node The node
	 */
	private void drawNode(Graphics2D g, Node node) {
		Point center = getCoordinates(node);
		
		if (node instanceof Place) {
			//Draw place name starting at the top right corner
			if (options.showPlaceNames)
				g.drawString(node.getString(Node.SIMPLE), center.x+placeRadius, center.y-placeRadius);
			
			//Determine bounding box for this place
			Rectangle bb = new Rectangle(center.x - placeRadius, center.y - placeRadius, 2*placeRadius, 2*placeRadius);
			boundingBox.put(node, bb);
			
			//Shadows?
			if (options.showShadows) {
				g.setColor(shadowColor);
				g.fillOval(bb.x+shadowOffset, bb.y+shadowOffset, bb.width, bb.height);
				g.setColor(getBackground());
				g.fillOval(bb.x, bb.y, bb.width, bb.height);
				g.setColor(normalColor);
			}
				
			
			//draw the interior if desired and always the border
			if (options.showPossibleOperations && redPlaces.contains(node)) {
				g.setColor(redPlaceColor);
				g.fillOval(bb.x, bb.y, bb.width, bb.height);
				g.setColor(normalColor);
			}
			g.drawOval(bb.x, bb.y, bb.width, bb.height);;
			

			//Draw the marking, as a dot for M(place)=1, as a number if >1
			int m = ((Place) node).getMarking();
			if (m == 1)
				g.fillOval(center.x - dotRadius, center.y - dotRadius, 2*dotRadius, 2*dotRadius);
			if (m > 1) {
				TextLayout tl = new TextLayout("" + m, g.getFont(), g
						.getFontRenderContext());
				int th = (int) tl.getBounds().getHeight();
				int tw = (int) tl.getBounds().getWidth();
								
                g.drawString("" + m, center.x - tw / 2, center.y + th / 2);
			}
			
		}
		
		
		//**************************************************
		
		else if (node instanceof Transition) {
			Signature sig = node.getSTG().getSignature(((Transition) node).getLabel().getSignal());
			
			if (sig == Signature.INPUT)		g.setColor(inputColor);
			if (sig == Signature.OUTPUT)	g.setColor(outputColor);
			if (sig == Signature.INTERNAL)	g.setColor(internalColor);
			if (sig == Signature.DUMMY)		g.setColor(dummyColor);
			
			//Draw a normal (non-dummy)transition as a box conatining its label
			//If it is a duplicate, it is filled in redTransitionColor 
			if (sig != Signature.DUMMY) {
				//Prepare the label, its width is neededfor the width of the transition
				String text = node.getString(Node.SIMPLE);			
				TextLayout tl = new TextLayout(text, g.getFont(), g.getFontRenderContext());
				int th = (int) tl.getBounds().getHeight();
				int tw = (int) tl.getBounds().getWidth();
				
				//Prepare the bounding box for dummies
				Rectangle bb = new Rectangle(center.x - tw / 2 - 2,	center.y - 10, tw + 4, 20);
				boundingBox.put(node, bb);
				
				//Shadows?
				if (options.showShadows) {
					Color oldColor = g.getColor();					
					g.setColor(shadowColor);
					g.fillRect(bb.x+shadowOffset, bb.y+shadowOffset, bb.width, bb.height);
					g.setColor(getBackground());
					g.fillRect(bb.x, bb.y, bb.width, bb.height);
					g.setColor(oldColor);
				}

				if (options.showPossibleOperations && redTrans.contains(node)) {
					g.setColor(redTransitionColor);
					g.fillRect(bb.x, bb.y, bb.width,bb.height);
					g.setColor(normalColor);				
				}
                else
                    g.drawRect(bb.x, bb.y, bb.width,bb.height);
				
				//Label drawing
			    g.drawString(text, center.x - tw / 2, center.y + th / 2);
				

				//Draw dummies as small boxed without label
			} else {
				//Set bounding box
				Rectangle bb = new Rectangle(center.x - 10,	center.y - 4, 20, 8);
				boundingBox.put(node, bb);

				if (options.showDummyNames)
					g.drawString(node.getString(Node.UNIQUE), bb.x+10, bb.y-10);
				
				//Shadows?
				if (options.showShadows) {
					Color oldColor = g.getColor();
					g.setColor(shadowColor);
					g.fillRect(bb.x+shadowOffset, bb.y+shadowOffset, bb.width, bb.height);
					g.setColor(getBackground());
					g.fillRect(bb.x, bb.y, bb.width, bb.height);
					g.setColor(oldColor);
				}

				if (options.showPossibleOperations) 
					if (redTrans.contains(node)) {
						g.setColor(redTransitionColor);
						g.fillRect(bb.x, bb.y, bb.width, bb.height);
						g.setColor(normalColor);
					}	
					else if (conTrans.contains(node)) {
						if (ConditionFactory.NEW_AUTOCONFLICT_PAIR.fulfilled((Transition) node))
							g.setColor(contractableTransAutoConfColor);
						else 
							g.setColor(contractableTransitionColor);
						g.fillRect(bb.x, bb.y, bb.width, bb.height);
						g.setColor(normalColor);
					}
				
				g.drawRect(bb.x, bb.y, bb.width, bb.height);
			}	
		}
		if (node == nodeUnderCursor) {
			g.setColor(Color.RED);
			g.drawString(node.getString(Node.SIMPLE), center.x+5, center.y-15);
			g.setColor(Color.BLACK);
		}

		g.setColor(normalColor);
	}
	
    
	
    private void drawSelectionBox(Graphics2D g, Node node) {
		Point center = getCoordinates(node);

		g.setColor(new Color(220, 240, 255));
		g.fillRect(center.x-20, center.y-20, 40, 40);
		g.setColor(Color.BLACK);
	}
    
	private void drawSpecialBox(Graphics2D g, Node node) {
    	Point center = getCoordinates(node);
    	
    	g.setColor(new Color(220, 0, 0));
    	g.drawRect(center.x-22, center.y-22, 44, 44);
    	g.setColor(Color.BLACK);
    }
	
	/** 
     * @return The coordinate set of all nodes
     */
    public STGEditorCoordinates getAllCoordinates() {
        return coordinates;
    }
	
    /**
	 * Calculates and arrow head for a line given by start and end point
	 * @param tip The end point
	 * @param origin The start point
	 * @return A {@link Shape} in form of an arrow head at the desired position
	 */
	private Polygon getArrowHead(Point tip, Point origin) {
		Point[] arrow = new Point[3];
		arrow[0] = new Point(0, 0);
		arrow[1] = new Point(-4, 10);
		arrow[2] = new Point(4, 10);
		
		AffineTransform tra = new AffineTransform();
		tra.rotate(Math.atan2(tip.y-origin.y, tip.x-origin.x)+Math.PI/2);
		
		tra.transform(arrow, 0, arrow, 0, 3);
		
		tra = new AffineTransform();
		tra.translate(tip.x, tip.y);
		
		tra.transform(arrow, 0, arrow, 0, 3);
		
		Polygon result = new Polygon();
		result.addPoint(arrow[0].x, arrow[0].y);
		result.addPoint(arrow[1].x, arrow[1].y);
		result.addPoint(arrow[2].x, arrow[2].y);
		
		return result;
	}
      
   
   
    /**
	 * Returns the point where a line crosses the edge of a node
	 * @param node The node
	 * @param start The start point of the line
	 * @return The crossing point
	 */
	private Point getBoundingBoxIntersection(Node node, Point start) {
		Rectangle bb = boundingBox.get(node);
		//bb.height+=1;
		//bb.width+=1;
		int cx = bb.width / 2 + bb.x;
		int cy = bb.height / 2 + bb.y;
		
		double arc = Math.atan2(cy - start.y, cx - start.x);
		
		if (node instanceof Place)
			return new Point((int) (cx - Math.cos(arc) * bb.width / 2),
					(int) (cy - Math.sin(arc) * bb.height / 2));
		
		if (node instanceof Transition) {
			if (bb.height * Math.abs(cx - start.x) <= Math.abs(cy - start.y)
					* bb.width)
				return new Point(cx + (start.x - cx) * bb.height / 2
						/ Math.abs((start.y - cy)), cy + bb.height / 2
						* ((cy < start.y) ? 1 : -1));
			
			
			return new Point(cx + bb.width / 2 * ((cx < start.x) ? 1 : -1),
					cy + (start.y - cy) * bb.width / 2 / Math.abs((start.x - cx)));
			
		}
		return null;
	}
	
	/**
     * Returns the coordinates of a node. If the  node does not have ones, they will be generated randomly
     * @param node A node
     * @return The coordinates of node
     */
	public Point getCoordinates(Node node) {
        Point p=coordinates.get(node);
        if (p!=null)
            return p;
        p = new Point((int) (Math.random()*100), (int) (Math.random()*100));
        coordinates.put(node, p);
        return p;
    }
	
	
	/**
	 * Returns the nearest grid point of a point
	 * @param p A point
	 * @return
	 */
    public Point getNextGridPoint(Point p) {
        if (p == null)
            return null;
        return new Point(  (p.x+gridX/2)/gridX*gridX,  (p.y+gridY/2)/gridY*gridY );
    }
    
	public Collection<Node> getNodes() {
        return nodes;
    }
    
	
	
    public STGEditorOptions getOptions() {
		return options;
	}
	
	
   
	/**
	 * See {@link Scrollable}
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	/**
	 * See {@link Scrollable}
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 40;
		
	}
    
    /**
	 * See {@link Scrollable}
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
    
    /**
	 * See {@link Scrollable}
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	
	/**
	 * See {@link Scrollable}
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}
	
	public STG getSTG() {
		return stg;
	}
	
	
	/**
	 * 
	 * @return The current zoom Level
	 */
	public double getZoomLevel() {
		return zoomLevels[options.actZoom];
	}

	@SuppressWarnings("all") 
	public void keyPressed(KeyEvent e) {}

	@SuppressWarnings("all")
	public void keyReleased(KeyEvent e) {
    }
	
	public void mouseClicked(MouseEvent e) {	
		requestFocusInWindow();
    }
	
	public void mouseDragged(MouseEvent e) {
		int x = (int) (e.getX() / getZoomLevel());
		int y = (int) (e.getY() / getZoomLevel());

		
		
		if (activatedNode != null) {
			
			if (selectedNodes.contains(activatedNode)) {
				for (Node sn : selectedNodes) {
					Point p = coordinates.get(sn);
					p.translate(x-lastPos.x, y-lastPos.y);
					coordinates.put(sn, p);
				}
				lastPos = new Point(x, y);
			}
			
			else 
				coordinates.put(activatedNode, new Point(x, y));
			
			repaint();
		}
		
		
		if (selectionStart != null) {
			selectionEnd = new Point(x,y);
			repaint();
		}
		
	}
	
	
	//************************************************
	
	
	
	

	public void mouseEntered(MouseEvent e) {
		requestFocusInWindow();
    }
	
	public void mouseExited(MouseEvent e) {
    }
	
	//************************************************
	
	public void mouseMoved(MouseEvent e) {
		Point mouse = e.getPoint();
		mouse.x /= getZoomLevel();
		mouse.y /= getZoomLevel();
		
		Node old = nodeUnderCursor;
		nodeUnderCursor = null;
		
		for (Node n : getSTG().getNodes()) {
			Point np = getCoordinates(n);          
			if (Math.abs(np.x-mouse.x)+Math.abs(np.y-mouse.y)<=20) {
				nodeUnderCursor = n;
				break;
			}
		}
		if (old != nodeUnderCursor)
			repaint();
	}
    
    //******************************************************
    
   
    
//  protected Node activatedNode = null;
    
    
      
    public void mousePressed(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (!e.isControlDown()) {
				int x = (int) (e.getX() / getZoomLevel());
				int y = (int) (e.getY() / getZoomLevel());
				
				lastPos = new Point(x,y);
				
				for (Node node : getNodes()) {
					Point p = getCoordinates(node);
					
					activatedNode = null;                
					if (Math.abs(p.x -x)+Math.abs(p.y-y)<=20) { 
						activatedNode = node;
						return;
					}
				}
				
				//left click but no node selected
				selectionStart = new Point(x,y);
				
				
			}
			
			else  {
				int x = (int) (e.getX() / getZoomLevel());
				int y = (int) (e.getY() / getZoomLevel());
				
				for (Node node : getNodes()) {
					Point p = getCoordinates(node);
					
					if (Math.abs(p.x -x)+Math.abs(p.y-y)<=20)  {
						try {
//							if (options.forceDeletion)
//								frame.deleteNode(node);
//							else if (options.showPossibleOperations)
//								frame.performOperation(node);
						} catch (Exception ee){ee.printStackTrace();
						}
						return;
					}
				}            
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			int x = (int) (e.getX() / getZoomLevel());
			int y = (int) (e.getY() / getZoomLevel());
			
			lastPos = new Point(x,y);
			
			for (Node node : getNodes()) {
				Point p = getCoordinates(node);
				
				activatedNode = null;                
				if (Math.abs(p.x -x)+Math.abs(p.y-y)<=20)  
					activatedNode = node;
			}
			
           
			STGEditorPopUp popUp = new STGEditorPopUp(this);
			
			
			popUp.show(e.getComponent(),
                    e.getX(), e.getY());
                    
			
		}
	}
    
    public void mouseReleased(MouseEvent e) {
	     int x = (int) (e.getX() / getZoomLevel());
         int y = (int) (e.getY() / getZoomLevel());
    
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (selectionStart !=null) {
				Rectangle selection = new Rectangle(selectionStart);
				selection.add(new Point(x,y));
				
				
				if (!e.isControlDown())
					selectedNodes = new HashSet<Node>();
				for (Node node : coordinates.keySet()) {
					if (selection.contains(coordinates.get(node)))
						if (!selectedNodes.contains(node))
							selectedNodes.add(node);
						else
							selectedNodes.remove(node);
				}
				
				selectionStart = null;
				repaint();
			}
			
			else if (options.alignToGrid && activatedNode != null) {
				coordinates.put(
						activatedNode, 
						getNextGridPoint(getCoordinates(activatedNode)));
				
				activatedNode = null;
				repaint();
			}
		}
	}
    

	/**
	 * Paints the component
	 */
	protected void paintComponent(Graphics g) {
//		 paint background
		if (isOpaque()) { 
			if (options.forceDeletion) 
				g.setColor(new Color(255, 200, 200));
			else
				g.setColor(getBackground());
			

			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(getForeground());
		}
		
		//Set extended graphic funtionallity and prepare antialiasing
		Graphics2D graphic = (Graphics2D) g.create();
		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphic.setRenderingHints(hints);
		
		graphic.scale(zoomLevels[options.actZoom], zoomLevels[options.actZoom]);
	
		//Do the paint from here
		
        drawGrid(graphic);
		
		
		if (selectionStart != null) {
			g.setColor(new Color(200, 220, 255));			
			Rectangle rect = new Rectangle(selectionStart);
			if (selectionEnd!=null)
                rect.add(selectionEnd);
			g.drawRect((int) (rect.x*getZoomLevel()), (int) (rect.y*getZoomLevel()), (int) (rect.width*getZoomLevel()), (int) (rect.height*getZoomLevel()));
			g.setColor(Color.BLACK);			
		}
		
		
		for (Node node : selectedNodes)
			drawSelectionBox(graphic, node);
		
				
		for (Node node : nodes) {
			drawNode(graphic, node);
		}
       
		for (Node node : nodes) {
			drawArc(graphic, node);
		}
				
		if (nodeUnderCursor != null && nodeUnderCursor instanceof Place && redPlaceReasons.containsKey(nodeUnderCursor)) {
			for (Object node : (Set) redPlaceReasons.get(nodeUnderCursor)) {
				drawSpecialBox(graphic, (Node)node);
			}
		}
        
		graphic.dispose();
		
	}
	
	


	
	private void print()   {
//		try {
//			Rectangle bb = getBoundingBox();
//			File file = File.createTempFile("jdesi-print", ".eps");       
//		//	VectorGraphics g = new PSGraphics2D(file, new Dimension(bb.x+bb.width+40, bb.y+bb.height+40));
//			
//			g.startExport(); 
//			int scale = options.actZoom;
//			setZoom(5);
//			
//			paintComponent(g); 
//			
//			options.actZoom = scale;
//			g.endExport();
//			
//			Runtime.getRuntime().exec("kprinter "+file.getAbsolutePath()).waitFor();
//		}
//		catch (IOException e) {
//			JOptionPane.showMessageDialog(frame, "Internal error - Could not create temporal file for printing", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		catch (InterruptedException e) {
//			JOptionPane.showMessageDialog(frame, "Internal error - Could not launch kprinter", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		
	}
    
    private void printVisible()   {
//		try {
//			Rectangle bb = getVisibleRect();
//			File file = File.createTempFile("jdesi-print", ".eps");       
//			VectorGraphics g = new PSGraphics2D(file, new Dimension(bb.x+bb.width+40, bb.y+bb.height+40));
//			
//			
//			g.startExport(); 
//			int scale = options.actZoom;
//			setZoom(5);
//
//			g.setClip(bb);
//			
//			paintComponent(g); 
//			
//			options.actZoom = scale;
//			g.endExport();
//			
//			Runtime.getRuntime().exec("kprinter "+file.getAbsolutePath()).waitFor();
//		}
//		catch (IOException e) {
//			JOptionPane.showMessageDialog(frame, "Internal error - Could not create temporal file for printing", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		catch (InterruptedException e) {
//			JOptionPane.showMessageDialog(frame, "Internal error - Could not launch kprinter", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		
	}
    
    private void renamePlaces() {
		int i = 0;
		for (Place place : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			place.setLabel("p"+i++);
		}
		repaint();
	}
    
    private void rotate(double degree) {
		AffineTransform transform = new AffineTransform();
		transform.rotate(-degree);		
		Point minPoint = new Point(10000, 10000);
		
		for (Node node : coordinates.keySet()) {
			Point pos = getCoordinates(node);
			transform.transform(pos, pos);
			if (pos.x < minPoint.x) minPoint.x = pos.x;
			if (pos.y < minPoint.y) minPoint.y = pos.y;			
		}
		for (Node node : coordinates.keySet()) {
			Point pos = getCoordinates(node);
			pos.translate(-minPoint.x+30, -minPoint.y+30);			
		}
		repaint();
    }
    
	private void scale(double f) {
        AffineTransform t = new AffineTransform();
        t.scale(f, f);
        for (Node node : coordinates.keySet())  {
            Point p = coordinates.get(node);
            t.transform(p, p);
        }
        repaint();
    }
    
	@SuppressWarnings("all")
	private void scroll(int horizontal, int vertical) {
		Rectangle r = getVisibleRect();
		r.translate(horizontal, vertical);
		scrollRectToVisible(r);
	}
	
	
	
	
	public void setCoordinates(Node node, Point point) {
        if (point.x > baseWidth) point.x = baseWidth;
        if (point.y > baseHeight) point.y = baseHeight;
        if (point.x < 0) point.x = 0;
        if (point.y < 0) point.y = 0;
        
        coordinates.put(node, point);
    }
    
	/** 
     * @return The coordinates of all nodes
     */
    public  void setCoordinates(STGEditorCoordinates coordinates) {
        this.coordinates=coordinates;
    }

    
    


    public void setSTG(STG stg, STGEditorCoordinates coordinates) {
		this.stg = stg;
		if (coordinates != null) 
			this.coordinates = coordinates;
		setup();
		repaint();
	}
    
    /**Sets up Collections for storing redundant transitions and things like that for the concrete STG*/
	private void setup() {
        boundingBox = new HashMap<Node, Rectangle>();
		//nodes=stg.getNodes();

		nodeUnderCursor = null;
		redPlaceReasons = new HashMap<Node, Object>();
		redPlaces = new HashSet<Place>();
		Condition<Place> cond = ConditionFactory.getRedundantPlaceCondition(stg);
        for (Place place : stg.getPlaces(ConditionFactory.ALL_PLACES)) {
			if (cond.fulfilled(place)) {
				redPlaces.add(place);
				redPlaceReasons.put(place, cond.becauseOf());
			}
        }
		
        redTrans = stg.getTransitions(ConditionFactory.getRedundantTransitionCondition(stg));
        conTrans = stg.getTransitions(ConditionFactory.getContractableCondition(stg));
		
	}
    
    
    
    //***************************************
    
    

  
    /**
	 * Sets the zoom level, the actual zoom level will be the smalles predefined one
	 * which is graeter than zoom
	 * @param zoom The desired zoom level
	 */
	public void setZoom(double zoom) {
		int zL = 0;
		while (zL<=10 && zoomLevels[zL]<zoom )
			++zL;
		
		setZoom(zL);
	}

    
    /**Sets the zoom level to a predefined one, 0<=zoom<=10
	 * @param newZoom The desired zoom level	 
	 */
	public void setZoom(int newZoom) {
		options.actZoom = newZoom;
		if (options.actZoom < 0)
			options.actZoom = 0;
		if (options.actZoom > 10)
			options.actZoom = 10;
		
		setPreferredSize(new Dimension((int) (baseWidth * zoomLevels[options.actZoom]),
				(int) (baseHeight * zoomLevels[options.actZoom])));
		revalidate();
		
	}
	
	/**
	 * Scrolls and zooms such that the whole STG is visible
	 */
	public void showAll() {
		int maxX = 0; 
		int maxY = 0;
		int minX = baseWidth;
		int minY = baseHeight;
		
		for (Node node : nodes) {
			Point pos = getCoordinates(node);
			maxX = Math.max(maxX, pos.x);
			maxY = Math.max(maxY, pos.y);
			minX = Math.min(minX, pos.x);
			minY = Math.min(minY, pos.y);
		}
		
		maxX+=20; maxY+=20; minX-=20; minY-=20;
		Dimension size = scrollPane.getSize();
		
		int width=maxX-minX;
		int height=maxY-minY;
		double scrollFactor = Math.min(size.width/width, size.height/height);
		
		setZoom(scrollFactor);
		double zoom = zoomLevels[options.actZoom];
		
		scrollRectToVisible(new Rectangle( (int) (zoom*minX), (int) (zoom*minY), (int)(zoom*(maxX-minX)),
				(int) (zoom*(maxY-minY))));
	}
	
	private void subPlaceTransitionArc() {
		if (selectedNodes.size() != 2)
			return;
		
		Iterator<Node> it = selectedNodes.iterator();
		Node n1 = it.next();
		Node n2 = it.next();
		
		if (n1 instanceof Place && n2 instanceof Transition )
			n1.addToChildValue(n2, -1);
		if (n1 instanceof Transition && n2 instanceof Place )
			n2.addToChildValue(n1, -1);		
	}
	
	private void subTransitionPlaceArc() {
		if (selectedNodes.size() != 2)
			return;
		
		Iterator<Node> it = selectedNodes.iterator();
		Node n1 = it.next();
		Node n2 = it.next();
		
		if (n1 instanceof Place && n2 instanceof Transition )
			n2.addToChildValue(n1, -1);
		if (n1 instanceof Transition && n2 instanceof Place )
			n1.addToChildValue(n2, -1);		
	}
	
	/**
	 * Increases the zoom factor
	 */
	public void zoomIn() {
		setZoom(++options.actZoom);
	}
	
	/**
	 * Decreases the zoom factor
	 */
	public void zoomOut() {
		setZoom(--options.actZoom);
	}

    
    
    
	
    
    
}
