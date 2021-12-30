

package net.strongdesign.desij.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

public class STGEditorSwitchingAction extends STGEditorAction {
	private static final long serialVersionUID = -5927749605919852873L;

	private boolean state;
	private String enabled;
	private String disabled;
	
	public STGEditorSwitchingAction(String enabled, String disabled, boolean initialEnabled, int mnemonic, Character accelerator, int mask, ActionListener listener) {
		super(enabled, mnemonic, accelerator, mask, listener);
		this.enabled = enabled;
		this.disabled = disabled;
		this.state = initialEnabled;
		
		
		if (initialEnabled)
			putValue(Action.NAME, enabled);
		else
			putValue(Action.NAME, disabled);
	}
	
	public void actionPerformed(ActionEvent e) {
		state = ! state;
		if (state)
			putValue(Action.NAME, enabled);
		else
			putValue(Action.NAME, disabled);
		
		super.actionPerformed(e);
		
	}

}
