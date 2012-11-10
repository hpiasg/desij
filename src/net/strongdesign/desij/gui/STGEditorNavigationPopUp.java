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
		add(listener.PETRIFY);
		add(listener.RELAX_INJECTIVE);

	}
	
}
