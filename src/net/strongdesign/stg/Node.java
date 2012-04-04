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

/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *  
 * <p>An implementation of the Node interfaces which provides
 * the basic functionality of all Nodes of an STG. 
 * This are mainly the arc-weight setting and getting and 
 * related functions.
 * 
 * <p><b>History:</b><br>
 * 25.09.2004: Generated<br>
 * 20.12.2004: Fixed error in {@link #setParentValue(Node, int)}
 * 01.03.2005: Changed concept for string representation, added {@link #getString(int)} 
 * 
 * <p>
 * @since 25.09.2004
 * @version 20.12.2004 
 * @author Mark Sch�fer 
 */
public abstract class Node  implements Cloneable {
	//different modes for the print function, values are differnt powers of 2
    //!! it is important not  to change to concrete values
    public static final int SIMPLE		= 1;
    public static final int UNIQUE		= 2;
    public static final int RECURSIVE 	= 4;
    
    
    /**The STGto which the node belongs*/ 
    protected STG stg;
    
    /**A unique identifier, i.e. unique for every STG and node type.*/
    private Integer identifier;

    /**The values of the arcs for the parent nodes.*/
	private Map<Node, Integer> parentValues;
	
	/**The values of the arcs for the child nodes.*/
	private Map<Node, Integer> childValues;
	
	@Override
	public abstract Node clone();
	
	

	/**
	 * Constructs a new Node with the given identifier belonging to the given STG.
	 * The identifiers of all instances of AbstractNode in a particular
	 * STG are supposed to be unique, this may be a point of further change. 
	 * @param identifier A new identifier for the AbstractNode
	 */
	public Node(Integer identifier, STG stg) {
		this.stg = stg;
		this.identifier = identifier;
		
		parentValues = new HashMap<Node, Integer>();
		childValues = new HashMap<Node, Integer>();		
	}

	/**
	 * Nodes are identified only by their id. 
	 */
	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (  o.getClass() != this.getClass()  ) return false;
		return identifier.equals(((Node)o).identifier);
	}
	

	
	/**
	 * Return the identifier of the current instance
	 * @return The current identifier of the instance
	 */
	public final Integer getIdentifier() {
		return identifier;
	}


	public final void setIdentifier(Integer id) {
		identifier = id;
	}

	public Set<Node> getNeighbours() {
		Set<Node> result = new HashSet<Node>();
		
		for (Node node : childValues.keySet())
			result.add(node);

		for (Node node : parentValues.keySet())
			result.add(node);
		
		return result;
	}
	
	
	public Set<Node> getSibblings() {
		Set<Node> result = new HashSet<Node>();
		
		for (Node node : getChildren())
			result.addAll(node.getParents());

		for (Node node : getParents())
			result.addAll(node.getChildren());
		
		result.remove(this);
		return result;
	}
	
	public STG getSTG() {
		return stg;
	}
	
	protected void setSTG(STG stg) {
		this.stg = stg;
	}

	/**
	 * Sets the arc-weight for a node as a parent of this abstract node.
	 * The arc-weight values of node are updated, too.
	 *  
	 * @param node The node which is supposed to be the parent
	 * @param value The new value of the related arc. If lower-equal 0, node is removed from the parent list 
	 */
	public void setParentValue(Node node, int value) {
		boolean nodeExists = parentValues.keySet().contains(node);
		
		if (nodeExists && value > 0) {
			if (value != (parentValues.get(node)).intValue()) {
				parentValues.put(node,  new Integer(value));
				node.setChildValue(this, value);
			}		
			return;
		}
		
		if (!nodeExists && value > 0) {
			//parents.add(node);
			parentValues.put(node, new Integer(value));
			node.setChildValue(this, value);
			return;		
		}
		
		if (nodeExists && value <= 0) {
			//parents.remove(node);
			parentValues.remove(node);
			node.setChildValue(this, value);
			return;	
		}		
	}

	/**
	 * Sets the arc-weight for a node as a child of this abstract node.
	 * The arc-weight values of node are updated, too.
	 *  
	 * @param node The node which is supposed to be the child
	 * @param value The new value of the related arc. If lower-equal 0, node is removed from the parent list 
	 */
	public void setChildValue(Node node, int value) {
		boolean nodeExists = childValues.keySet().contains(node);
		
		if (nodeExists && value > 0) {
			if (value != (childValues.get(node)).intValue()) {
				childValues.put(node,  new Integer(value));
				node.setParentValue(this, value);
			}		
			return;
		}
		
		if (!nodeExists && value > 0) {
			//children.add(node);
			childValues.put(node, new Integer(value));
			node.setParentValue(this, value);
			return;		
		}
		
		if (nodeExists && value <= 0) {
			//children.remove(node);
			childValues.remove(node);
			node.setParentValue(this, value);
			return;	
		}		
	}

	
	/**
	 * Adds a value to the arc-weight for a node as a parent of this abstract node.
	 * The arc-weight values of node are updated, too.
	 *  
	 * @param node The node which is supposed to be the parent
	 * @param value The new value to be added to the related arc-weight. 
	 */
	public void addToParentValue(Node node, int value){
		setParentValue(node, value + getParentValue(node));		
	}
	
	public int hashCode() {
		return identifier.hashCode();
	}

	/**
	 * Adds a value to the arc-weight for a node as a child of this abstract node.
	 * The arc-weight values of node are updated, too.
	 *  
	 * @param node The node which is supposed to be the child
	 * @param value The new value to be added to the related arc-weight. 
	 */
	public void addToChildValue(Node node, int value){
		setChildValue(node, value + getChildValue(node));
	}

	/**
	 * Returns the parents of the instance.
	 * @return A list containing the parents.
	 */
	public final Set<Node> getParents() {
		return parentValues.keySet();
	}
	
	public final boolean hasParents() {
		return !parentValues.isEmpty();
	}

	/**
	 * Returns the children of the instance.
	 * @return A list containing the children.
	 */
	public final Set<Node> getChildren() {
		return  childValues.keySet();
	}

	public final boolean hasChildren() {
		return !childValues.isEmpty();
	}
	
	/**
	 * Gets the arc-weight to a node as a parent of this instance.
	 * @param node The parent node.
	 * @return The arc value for this parent, 0 if the node is not known to the instance
	 */
	public int getParentValue(Node node) {
		Integer i = parentValues.get(node);
		if (i == null)
			return 0;
		return i;
	}

	/**
	 * Gets the arc-weight to a node as a child of this instance.
	 * @param node The child node.
	 * @return The arc value for this child, 0 if the node is not known to the instance
	 */
	public int getChildValue(Node node) {
		Integer i = childValues.get(node);
		if (i == null)
			return 0;
		return i;
	}

	/**
	 * Returns a string representation of the node
	 * @see {@link #UNIQUE_NONRECURSIVE}, {@link #UNIQUE_RECURSIVE}, {@link #PURE_NONRECURSIVE} and {@link #PURE_RECURSIVE}
	 * @return A string representation of the instance
	 * @throws STGException 
	 */
	public abstract String getString(int mode);
	
	public abstract String toString();


	public void disconnect() {
		for (Node node : getChildren()) 
			node.parentValues.remove(this);
		
		for (Node node : getParents()) 
			node.childValues.remove(this);
		
		
	}
	
	public void reconnect() {
		for (Node node : getChildren()) 
			node.parentValues.put(this, getChildValue(node));
		
		for (Node node : getParents()) 
			node.childValues.put(this, getParentValue(node));
	}
	
	
	public static Set<Transition> getTransitions(Collection<Node> coll) {
		
		Set<Transition> res = new HashSet<Transition>();
		
		for (Node n : coll) {
			if (n instanceof Transition)
				res.add((Transition) n);
		}
		
		return res;
		
	}
	
	public static Set<Place> getPlace(Collection<Node> coll) {
	
	Set<Place> res = new HashSet<Place>();
	
	for (Node n : coll) {
		if (n instanceof Place)
			res.add((Place) n);
	}
	
	return res;
	
}


	
}


