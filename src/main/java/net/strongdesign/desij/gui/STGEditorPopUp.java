

package net.strongdesign.desij.gui;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class STGEditorPopUp extends JPopupMenu {
	private static final long serialVersionUID = -2817995166137265146L;

	public STGEditorPopUp(STGEditor editor) {
		super();
		setBorder(BorderFactory.createRaisedBevelBorder());
		
		add(editor.ADD_TRANSITION);
		add(editor.ADD_PLACE);
		add(editor.ADD_ARC_PLACE_TRANSITION);
		add(editor.ADD_ARC_TRANSITION_PLACE);
		add(editor.SUB_ARC_PLACE_TRANSITION);
		add(editor.SUB_ARC_TRANSITION_PLACE);		
		add(new JSeparator());
		add(editor.FORCED_DELETION);
		add(editor.PLACE_NAMES);
		add(editor.DUMMY_NAMES);
		add(editor.DECOMPOSITION);
		
		
		
		/*
		newItem((options.forceDeletion?"Disable":"Enable") + " forced deletion", listener);
		newItem((options.showPlaceNames?"Hide":"Show") + " place names", listener);
		newItem((options.showDummyNames?"Hide":"Show") + " dummy names", listener);
		newItem((options.showPossibleOperations?"Disable":"Enable") + " decomposition", listener);
		
		add(new JSeparator());
		
		newItem("Show cycles with selected nodes", listener);
		
		add(new JSeparator());
		
		newItem((options.alignToGrid?"Disable":"Enable") + " align to grid", listener);
		newItem((options.showGrid?"Hide":"Show") + " grid", listener);
		newItem((options.showShadows?"Hide":"Show") + " shadows", listener);
		
		add(new JSeparator());

		newItem("Copy STG", listener);
		
		add(new JSeparator());
		
		
		newItem("Add Place", listener);
		newItem("Add Transition", listener);
		newItem("Add Arc Place -> Transition", listener);
		newItem("Add Arc Transition -> Place", listener);
		newItem("Remove Arc Place -> Transition", listener);
		newItem("Remove Arc Transition -> Place", listener);
		
		
		
		
		
		add(new JSeparator());
		
		
				
		newItem("Save", listener);
		newItem("Save as", listener);
		newItem("Print", listener);
		*/
		
		
	}
	

	
	
}
