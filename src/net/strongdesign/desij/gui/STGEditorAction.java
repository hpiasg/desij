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
