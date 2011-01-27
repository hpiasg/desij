package net.strongdesign.desij.gui;

public class STGEditorOptions {
	/**The current zoom level*/
	public int actZoom=5;

	/**Show node with a possible operations*/
	public boolean showPossibleOperations = true;
	
	/**Show the grid*/
	public boolean showGrid = false;
    
	/**Will the nodes be aligned*/
	public boolean alignToGrid = false;

	/**Force deletion of selected nodes*/
	public boolean forceDeletion = false;
    
	/**Show place names*/
	public boolean showPlaceNames = false;
	
	/**Show node shadows*/
	public boolean showShadows = true;
	
	/**Show dummy names*/
	public boolean showDummyNames = false;
	
	
	
	public Object clone() {
		STGEditorOptions result = new STGEditorOptions();
		
		result.actZoom = actZoom;
		result.alignToGrid = alignToGrid;
		result.forceDeletion = forceDeletion;
		result.showDummyNames = showDummyNames;
		result.showGrid = showGrid;
		result.showPlaceNames = showPlaceNames;
		result.showPossibleOperations = showPossibleOperations;
		result.showShadows = showShadows;
		
		
		return result;
	}
	
	
    public void toggleShowPossibleOperations() {
		showPossibleOperations = ! showPossibleOperations;        
    }
    
    
    public void toggleGrid() {
        showGrid = !showGrid;
    }

    public void toggleAlignToGrid() {
        alignToGrid = ! alignToGrid;
    }

	public void toggleShowPlaceNames() {
		showPlaceNames = !showPlaceNames;
	}
	
	public void toggleShowShadows() {
		showShadows = !showShadows;
	}
	
	

}
