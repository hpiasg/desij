/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg;

import java.util.*;

/**
 * 
 * 
 * <p>
 * <b>History: </b> <br>
 * 25.09.2004: Created <br>
 * 25.01.2005: Fixed error in {@link #fire()} method
 * 18.02.2005: Added {@link #equals(Object)}
 * <p>
 * 
 * @author Mark Sch�fer
 */
public class Transition extends Node {
    public static final int TYPED 		= 4;
    
	protected SignalEdge label;
	
	public Transition (SignalEdge label, Integer identifier, STG stg) {
		super(identifier, stg);
		setLabel(label);
	}
	
	public String getString(int mode) {
	    StringBuilder result = new StringBuilder();
	    boolean unique 		= 	(mode & UNIQUE) != 0;
	    boolean recursive	= 	(mode & RECURSIVE) != 0;
	    boolean typed		= 	(mode & TYPED) != 0;
	    
	    Integer signal = label.getSignal();
		result.append(stg.getSignalName(signal));
	    
	    if (typed)
	    	result.append(stg.getSignature(signal));
	    
	    if (stg.getSignature(signal) != Signature.DUMMY)
	    	result.append(label.getDirection());	        
	    
	    if (unique)
	        result.append("/"+getIdentifier());
	     
	    	    
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
		return getString(UNIQUE | TYPED );
	}
	
	//XXX: clone with original stg??
	@Override
	public Transition clone() {
	    return new Transition((SignalEdge)label.clone(), new Integer(getIdentifier()), stg);
	}
	
	
	public void setLabel(SignalEdge label) {
		this.label = label;
	}


	public SignalEdge getLabel() {
		return label;
	}
	

	public void setChildValue(Node node, int value)  {
		if (node instanceof Place) super.setChildValue(node, value);	
	}

	public void setParentValue(Node node, int value)  {
		if (node instanceof Place) super.setParentValue(node, value);	
	}

	public boolean isActivated() {
		Set<Node> parents = getParents();
		
		for (Iterator<Node> itParents = parents.iterator(); itParents.hasNext();) {
			Place actParent = (Place)itParents.next();
			if (actParent.getMarking()<getParentValue(actParent)) return false;
		}
			
		
		return true;
	}

	public void fire() {
		if (!isActivated()) return;
		
		Set<Node> parents = getParents();
		for (Iterator<Node> itParents = parents.iterator(); itParents.hasNext();) {
			Place actParent = (Place) itParents.next();
			actParent.setMarking(actParent.getMarking()-getParentValue(actParent));
		}

		Set<Node> children = getChildren();
		for (Iterator<Node> itChildren = children.iterator(); itChildren.hasNext();) {
			Place actChild = (Place)itChildren.next();
			actChild.setMarking(actChild.getMarking()+getChildValue(actChild));
		}		
	}
	

}
