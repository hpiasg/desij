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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.strongdesign.stg.STG;
import net.strongdesign.stg.Signature;

/**
 * Renders a cell e.g. for a JList containing a signal according to the common color scheme
 * @author Mark Schäfer
 *
 */
class SignalCellRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 6789391767248931424L;
	
	private STG stg;
	
	public SignalCellRenderer(STG stg) {
		//this.transition = transition;
		setOpaque(true);
		this.stg = stg;
	}
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setText(value.toString());					
		setBackground(isSelected ? Color.black : Color.white);
		setForeground(isSelected ? Color.white : Color.black);
		
		
		if (value instanceof Integer) {
			
			setText(((String)value));
			
			
			 Signature sig = stg.getSignature((Integer)value);
			 if (sig == Signature.INPUT) {
		         setBackground(isSelected ? Color.red : Color.white);
		         setForeground(isSelected ? Color.white : Color.red);
			 }
			 else if (sig == Signature.DUMMY) {
				 setBackground(isSelected ? Color.orange : Color.white);
				 setForeground(isSelected ? Color.white : Color.orange);
			 }
			 else if (sig == Signature.OUTPUT) {
				 setBackground(isSelected ? Color.blue : Color.white);
				 setForeground(isSelected ? Color.white : Color.blue);
			 }
			 else if (sig == Signature.INTERNAL) {
				 setBackground(isSelected ? Color.green : Color.white);
				 setForeground(isSelected ? Color.white : Color.green);
			 }
			 
		 }
		
		return this;
	}
	
}