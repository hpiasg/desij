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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;

public class STGEditorSignalChooser extends JDialog implements MouseListener {
	private static final long serialVersionUID = 8847030119404539788L;

	private STG stg;
	
	private JList input, output, internal, dummy;
	private DefaultListModel inM, outM, intM, dumM;
	private Object[] selectedSignals=null;

	private JList sourceList, targetList;

	private STGEditorFrame frame;
	
	
	public STGEditorSignalChooser(String title, STG stg, STGEditorFrame frame) {
		super(frame, title);
		this.stg = stg;
		
		this.frame = frame;
		
		inM = new DefaultListModel();
		outM = new DefaultListModel();
		intM = new DefaultListModel();
		dumM = new DefaultListModel();
		
		
		for (Integer signal : stg.getSignals())
			switch (stg.getSignature(signal)) {
			case INPUT: 	inM.addElement(signal); break;
			case OUTPUT: 	outM.addElement(signal); break;
			case INTERNAL: 	intM.addElement(signal); break;
			case DUMMY: 	dumM.addElement(signal); break;
			}
		
		input = new JList(inM);
		output = new JList(outM);
		internal = new JList(intM);
		dummy = new JList(dumM);
		
		
		
		setLayout(new GridLayout(1,4));
		
		JScrollPane p0 = new JScrollPane(input);
		p0.setBorder(BorderFactory.createTitledBorder("Input"));
		add(p0);
		
		JScrollPane p1 = new JScrollPane(output);
		p1.setBorder(BorderFactory.createTitledBorder("Output"));
		add(p1);
		
		JScrollPane p2 = new JScrollPane(internal);
		p2.setBorder(BorderFactory.createTitledBorder("Internal"));
		add(p2);
		
		JScrollPane p3 = new JScrollPane(dummy);
		p3.setBorder(BorderFactory.createTitledBorder("Dummy"));
		add(p3);
		
		
		setSize(300, 200);
		
		input.addMouseListener(this);
		output.addMouseListener(this);
		internal.addMouseListener(this);
		dummy.addMouseListener(this);
		
		sortAllLists();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	}

	
	private void sortList(JList list) {
		List<String> elements = new LinkedList<String>();
		
		 
		for (int i=0; i<list.getModel().getSize(); ++i)
			elements.add((String) list.getModel().getElementAt(i));
		
		((DefaultListModel)list.getModel()).removeAllElements();
		Collections.sort(elements);
		for (String signal : elements)
			((DefaultListModel)list.getModel()).addElement(signal);
		
	}

	private void sortAllLists() {
		sortList(input);
		sortList(output);
		sortList(internal);
		sortList(dummy);
	}
	
	
	public void mouseClicked(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON3) return;
		
		sourceList = (JList) e.getComponent();
		selectedSignals = sourceList.getSelectedValues();
		
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON3) return;
		
		if (targetList == sourceList)  {
			System.out.println("fds");
			return;
		}
		
		Signature signature = Signature.ANY;
		if (targetList == input) signature = Signature.INPUT;
		if (targetList == output) signature = Signature.OUTPUT;
		if (targetList == internal) signature = Signature.INTERNAL;
		if (targetList == dummy) signature = Signature.DUMMY;
		
		
		for (Object signal : selectedSignals) {
			try {
				stg.setSignature((Integer)signal, signature);				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			((DefaultListModel)sourceList.getModel()).removeElement(signal);
			((DefaultListModel)targetList.getModel()).addElement(signal);
		}
		
		sortList(targetList);
		frame.updateSTG(stg);
		
	}

	public void mouseEntered(MouseEvent e) {
		targetList = (JList) e.getComponent();
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	
}
