package net.strongdesign.desij.gui;

import java.awt.event.ActionListener;

import javax.swing.*;

public class STGEditorNavigationPopUp extends JPopupMenu {
	private static final long serialVersionUID = 2993881479268447533L;

	public STGEditorNavigationPopUp(ActionListener listener) {
		super();
		setBorder(BorderFactory.createRaisedBevelBorder());
		
		newItem("Delete selected nodes", listener);
			
	}
	
	private void newItem(String text, ActionListener listener) {
		JMenuItem i = new JMenuItem(text);
		i.addActionListener(listener);
		add(i);
		
	}
	
	
	
}
