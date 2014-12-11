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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JDialog;

public class STGEditorAbout extends JDialog {
	private static final long serialVersionUID = 7659693520232299217L;
	private Image logo;
	
    public STGEditorAbout(STGEditorFrame frame) {
        super(frame, "About - JDesi", true);        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setSize(400, 300);

    	logo = Toolkit.getDefaultToolkit().getImage("/home/mark/jdesi-logo.gif");    	
    	
    }
    
    protected void paintComponent(Graphics g) {
    	g.drawLine(10, 10, 20, 20);
    	g.drawImage(logo, 0, 0, 400, 300, null);
    }
    

}
