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


/**
 * A Place represents a place of an STG. Additional to an {@link stg.AbstractNode}
 * a place has a <it>marking</it> which can be modified.
 * 
 * 18.02.2005: Added {@link #equals(Object)}
 * 
 * @author Mark Sch�fer
 *
  */
public class Place  extends Node {
	private String label;
	private int marking;
	
	public static final int TOKENS = 8; 
	
	/**
	 * Constructs a new place from a label and an identifier. The identifier should be unique. 
	 * @param label The label of the place.
	 * @param identifier The identifier.
	 */
	public Place(String label, Integer identifier, STG stg) {
		this(label, identifier, 0, stg);
	}
	
	/**
	 * Constructs a new place from a label and an identifier. The identifier should be unique. 
	 * @param label The label of the place.
	 * @param identifier The identifier.
	 */
	public Place(String label, Integer identifier, int marking, STG stg) {
		super(identifier, stg);
		this.label = label;
		this.marking = marking;		 
	}
	
	
//	XXX: clone with original stg??
	@Override
	public Place clone() {
		return  new Place(label.toString(), new Integer(getIdentifier().intValue()), marking, stg);
	}
	
	

	/**
	 * Returns the current label.
	 * @return The current label
	 */
	public String getLabel() {
		if (label.length()==0)
			return getIdentifier().toString();
		return label;
	}

	/**
	 * Returns the current marking
	 * @return The current marking
	 */
	public int getMarking() {
		return marking;
	}

	
	
	/**
	 * Sets the arc-weight for a parent node, see {@link stg.AbstractNode#setParentValue(Node, int)}.
	 * It is checked wheter the parent is a {@link stg.Transition} as needed for STGs.
	 * @param node the parent node
	 * @param value the new arc value
	 */
	public void setChildValue(Node node, int value)  {
		if (node instanceof Transition) super.setChildValue(node, value);	
	}
	
	/**
	 * Set a new label.
	 * @param label The new label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets a new marking
	 * @param i The new marking, negative values will be truncated to zero.
	 */
	public void setMarking(int i) {
		if (i<0) i=0;
		marking = i;
	}

	/**
	 * Sets the arc-weight for a child node, see {@link stg.AbstractNode#setChildValue(Node, int)}.
	 * It is checked wheter the parent is a {@link stg.Transition} as needed for STGs.
	 * @param node the parent node
	 * @param value the new arc value
	 */
	public void setParentValue(Node node, int value)  {
		if (node instanceof Transition) super.setParentValue(node, value);	
	}
	
	public String getString(int mode) {
	    StringBuilder result = new StringBuilder();
	    boolean unique 		= 	(mode & UNIQUE) != 0;
	    boolean recursive	= 	(mode & RECURSIVE) != 0;
	    boolean tokens		= 	(mode & TOKENS) != 0;
	    
	    result.append(label);
	    if (unique && getIdentifier() != 0)
	        result.append("_"+getIdentifier());
	    
	    
	    if (tokens && marking!=0)
	        result.append("("+marking+")");
	    
	    if (recursive) {
	        result.append(" Postset:");
	        for (Node child : getChildren())
	            result.append(" " + child.getString(mode & ~RECURSIVE) );

	        result.append(" Preset:");
	        for (Node parent : getParents())
	            result.append(" " + parent.getString(mode & ~RECURSIVE) );
	    }   
	    
	    return result.toString();
	}


	public String toString() {
		return getString(TOKENS | UNIQUE);
	}
	
}
