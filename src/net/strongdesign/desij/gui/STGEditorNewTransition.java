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


import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import net.strongdesign.stg.STG;


public class STGEditorNewTransition extends JFrame {
	private static final long serialVersionUID = 4812285322844172159L;

	private STGEditor editor;
	private STG stg;
	
	
	public STGEditorNewTransition(STGEditor editor) {
		super("Add Transition - JDesi");
		
		this.editor = editor;
		stg = this.editor.getSTG();
		
		Collection<String> signals = stg.getSignalNames(stg.getSignals());
		
		Object[] l = signals.toArray();
		Arrays.sort(l);
		
		JList signalList = new JList(l);
		
		signalList.setCellRenderer(new SignalCellRenderer(stg));
		
		
		setLayout(new GridLayout(1, 2));
		
		add(new JScrollPane(signalList));
		
		JPanel panel = new JPanel();
		panel.add(new JRadioButton("+", false));
		panel.add(new JRadioButton("-", false));
		panel.add(new JButton("Add"));

			
		add(panel);
		
		setSize(200, 200);
		
		
		
	}
}
