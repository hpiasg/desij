

package net.strongdesign.desij.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGUtil;

public class STGEditorTreeNode extends DefaultMutableTreeNode{

	private static final long serialVersionUID = 971545642212448511L;

	private STG stg;
//	private STGCoordinates coordinates;
	
    private String label;
    private String stgInfo;
    private String fileName; // related file name (if it exists)
	private boolean procreative;
	private boolean isSTG;
	public Partition partition;
    
	
	
	
	public STGEditorTreeNode(String label, STG stg, boolean procreative) {
		super(label);
		
        this.stg = stg;
        this.procreative = procreative;
        this.label = label;
        
        stgInfo = "";
        if (stg!=null)
        	stgInfo = stg.getSTGInfo();
        
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
		stgInfo = "";
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
		stgInfo = "";
        if (stg!=null)
        	stgInfo = stg.getSTGInfo();
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
		return label+stgInfo;
	}
	
/*
	public void setCoordinates(STGCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	public STGCoordinates getCoordinates() {
		return coordinates;
	}*/
    
}
