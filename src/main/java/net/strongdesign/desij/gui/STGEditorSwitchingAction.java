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
