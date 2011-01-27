package net.strongdesign.desij.gui;

import net.strongdesign.stg.STG;



public class STGEditorTreeNode {

	
    private STG stg;
    private String label;	
	private boolean procreative;
	private boolean isSTG;
	
	private STGEditorTreeNode parent;
	
    
	public STGEditorTreeNode(String label, STG stg, boolean procreative, STGEditorTreeNode parent) {
        this.stg = stg;
        this.procreative = procreative;
		this.label = label;
		this.parent = parent;
		isSTG = true;
	}
	
	public STGEditorTreeNode(String label, STGEditorTreeNode parent) {
		this.stg = null;
		this.procreative = true;		
		this.label = label;
		this.parent = parent;
		isSTG = false;		
	}
	
	public STGEditorTreeNode getParent() {
		return parent;
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
    
    
}
