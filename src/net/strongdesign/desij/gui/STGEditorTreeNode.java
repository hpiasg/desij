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

import javax.swing.tree.DefaultMutableTreeNode;

import net.strongdesign.stg.STG;

public class STGEditorTreeNode extends DefaultMutableTreeNode{

	private static final long serialVersionUID = 971545642212448511L;

	private STG stg;
//	private STGCoordinates coordinates;
	
    private String label;
    private String fileName; // related file name (if it exists)
	private boolean procreative;
	private boolean isSTG;
    
	public STGEditorTreeNode(String label, STG stg, boolean procreative) {
		super(label);
        this.stg = stg;
        this.procreative = procreative;
		this.label = label;
		isSTG = true;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public STGEditorTreeNode(String label) {
		super(label);
		this.stg = null;
		this.procreative = true;
		this.label = label;
		isSTG = false;
	}
	
	public STGEditorTreeNode getParent() {
		return (STGEditorTreeNode)parent;
	}
	
	public boolean isSTG() {
		return isSTG;
	}
	
    public STG getSTG() {
        return stg;
    }
    
    
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
    public void setSTG(STG stg) {
        this.stg = stg;
    }
    
    public boolean isProcreative() {
		return procreative;
    }
	
	public void setProcreative() {
		procreative = true;
	}
	
	public String toString() {
		return label;
	}
	
/*
	public void setCoordinates(STGCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	public STGCoordinates getCoordinates() {
		return coordinates;
	}*/
    
}
