package net.strongdesign.desij.gui;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;

class Column extends LinkedList<Node> {
	private static final long serialVersionUID = 8735548112604915717L;
	final static int dx = 50;
	final static int dy = 20;
	final static int xmargin = 40;
	final static int ymargin = 30;
	
	LinkedList<Column> children = new LinkedList<Column>();
	int curWidth=0;
	int curHeight=0;
	
	int getWidth() {
		if (curWidth>0) return curWidth;
		
		int w=0;
		
		for (Column c: children) {
			w+=c.getWidth()+xmargin;
		}
		
		w-=xmargin;
		curWidth = Math.max(dx, w); 
		return curWidth;
	}
	
	int getHeight() {
		if (curHeight>0) return curHeight;
		
		int h=size()*(dy+ymargin);
		
		
		int mx=0;
		
		for (Column c: children) {
			mx=Math.max(mx, c.getHeight());
		}
		
		if (mx==0) h-=ymargin;
		
		curHeight = h;
		return curHeight;
	}
	
	void position(STG stg, int x, int y) throws STGException {
		for (Node n: this) {
			stg.setCoordinates(n, new Point(x, y));
			y+=dy+ymargin;
		}
		
		x-=getWidth()/2;
			
		for (Column c: children) {
			x+=c.getWidth()/2;
			c.position(stg, x, y);
			x+=c.getWidth()/2+xmargin;
		}
	}
};

public class STGTreeLayout {
	
	static HashMap<Node, Column> node2col = new HashMap<Node, Column>();
	
	static LinkedList<Column> mainColumns = new LinkedList<Column>();
	
	static HashSet<Node> current;
	static HashSet<Node> next;
	static boolean isShortHand;
	
	static Set<Node> nextChildNodes(Node node) {
		if (node instanceof Place||!isShortHand) {
			return node.getChildren();
		}
		
		// now for each transition return its children, however, ignore shorthand places
		// for each shorthand place return its child instead
		Set<Node> ret = new HashSet<Node>();
		
		for (Node n: node.getChildren()) {
			
			if (STGGraphComponent.isShorthandPlace(n, null)) {
				
				ret.addAll(n.getChildren());
			} else ret.add(n);
		}
		
		return ret;
	}
	
	// Accumulate different nodes into columns
	static private void solve() {
		int v=0;
		while (next.size()>0) {
			v++;
			current = next;
			next = new HashSet<Node>();
			
			for (Node n : current) {
				Set<Node> c = nextChildNodes(n);
				Column parent = node2col.get(n);
				
				if (c.size()==1) {
					Node nd = c.iterator().next();
					if (!node2col.containsKey(nd)) {
						node2col.put(nd, parent);
						parent.add(nd);
						next.add(nd);
					}
				} else if (c.size()>1) {
					
					for (Node nd: c) {
						if (!node2col.containsKey(nd)) {
							Column cl = new Column();
							node2col.put(nd, cl);
							parent.children.add(cl);
							cl.add(nd);
							next.add(nd);
						}
					}
				}
			}
		}
	}
	
	static public void doLayout(STG stg, boolean isShortHand) {
		STGTreeLayout.isShortHand = isShortHand;
		mainColumns.clear();
		node2col.clear();
		
		boolean found=true;
		while (found) {
			found=false;
			next = new HashSet<Node>();
			for (Node n: stg.getNodes()) {
				if (n.getParents().size()==0&&!node2col.containsKey(n)) {//||((Place)n).getMarking()>0) {
						next.add(n);
						Column c = new Column();
						mainColumns.add(c);
						c.add(n);
						node2col.put(n, c);
						found=true;
						break;
				}
			}
			if (found)
				STGTreeLayout.solve();
		}
		
		found=true;
		while (found) {
			found=false;
			next = new HashSet<Node>();
			for (Node n: stg.getNodes()) {
				if (n instanceof Place) {
					if (((Place)n).getMarking()>0&&!node2col.containsKey(n)) {
						next.add(n);
						Column c = new Column();
						mainColumns.add(c);
						c.add(n);
						node2col.put(n, c);
						found=true;
						break;
					}
				}
			}
			if (found)
				STGTreeLayout.solve();
		}
		
		
		stg.getCoordinates().clear();
		
		int x=0;
		int w;
		for (Column c: mainColumns) {
			w = c.getWidth()/2;
			x+=w;
			
			try {
				c.position(stg, x, 0);
			} catch (STGException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x+=w+Column.xmargin;
		}
	}
}
