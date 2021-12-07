

package net.strongdesign.desij.gui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;



public class STGEditorAction extends AbstractAction {
	private static final long serialVersionUID = 8469139804287394279L;

	public STGEditorAction(String text, int mnemonic, Character accelerator,  int mask, ActionListener listener) {
		super(text);
		this.text = text;
		this.listener = listener;
		
		if (mnemonic != 0)
			putValue(Action.MNEMONIC_KEY, mnemonic);
		
		if (accelerator != null)
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, mask));
		

	}
	
	public STGEditorAction(String text, int mnemonic, int mask, ActionListener listener) {
		super(text);
		this.text = text;
		this.listener = listener;
		
		if (mnemonic != 0) {
			
			putValue(Action.MNEMONIC_KEY, mnemonic);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(mnemonic, mask));
		}
		

	}
	
	private ActionListener listener;
	private String text;
	
	public String getText() {
		return text;
	}
	
	public void actionPerformed(ActionEvent e) {
		e.setSource(this);
		listener.actionPerformed(e);		
	}


}
