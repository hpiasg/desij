

package net.strongdesign.desij.gui;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;

public class STGEditorNavigationPopUp extends JPopupMenu {
	private static final long serialVersionUID = 2993881479268447533L;
	
	public STGEditorNavigationPopUp(STGEditorNavigation listener) {
		super();
		setBorder(BorderFactory.createRaisedBevelBorder());
		
		add(listener.DELETE_SELECTED);
		add(listener.PARALLEL_COMPOSITION);
		add(listener.SYNCHRONOUS_PRODUCT);
		
		add(listener.DUMMY_SURROUNDING);
		
		add(listener.RELAX_INJECTIVE);
		add(listener.RELAX_INJECTIVE2);
		add(listener.SIMPLE_DUMMY_REMOVAL);
		
		add(listener.PETRIFY);
		add(listener.PETRIFY_CSC);

		add(listener.MPSAT_CSC);
		add(listener.REPORT_IMPLEMENTABLE);
		add(listener.REPORT_IMPLEMENTABLE_SW);
		add(listener.REPORT_PROBLEMATIC_TRIGGERS);
		
		add(listener.DUMMIFY_RECURRING_SIGNALS);
		add(listener.REMOVE_DEAD_TRANSITIONS);
	}
	
}
