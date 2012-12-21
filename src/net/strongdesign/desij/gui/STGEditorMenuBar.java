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

import java.awt.event.*;

import javax.swing.*;

public class STGEditorMenuBar extends JMenuBar implements ActionListener  {
	private static final long serialVersionUID = -9095831294193194005L;
	
	private final STGEditorAction FILE = new STGEditorAction("File", KeyEvent.VK_F, null, 0, this);
	
	private final STGEditorAction LAYOUT_TYPES = new STGEditorAction("Layouts", KeyEvent.VK_L, null, 0, this);
	
	private final STGEditorAction EDIT = new STGEditorAction("Edit", KeyEvent.VK_E, null, 0, this);
	private final STGEditorAction VIEW = new STGEditorAction("View", KeyEvent.VK_V, null, 0, this);
	private final STGEditorAction PETRINET = new STGEditorAction("Petri net", KeyEvent.VK_P, null, 0, this);
	private final STGEditorAction STG = new STGEditorAction("STG", KeyEvent.VK_S, null, 0, this);
	
	private final STGEditorAction PARTITION = new STGEditorAction("Partition", 0, null, 0, this);
	
	private final STGEditorAction DECOMPOSITION = new STGEditorAction("Decomposition", KeyEvent.VK_D, null, 0, this);
	private final STGEditorAction HELP = new STGEditorAction("Help", KeyEvent.VK_H, null, 0, this);

	
	
	public STGEditorMenuBar(STGEditorFrame frame) {//, STGLayoutCache cache) {
		super();

		JMenu file = new JMenu(FILE);
		add(file);
		
		JMenu layouts = new JMenu(LAYOUT_TYPES);
		add(layouts);
		
		JMenu edit = new JMenu(EDIT);
		add(edit);
		JMenu view = new JMenu(VIEW);
		add(view);
		JMenu petrinet = new JMenu(PETRINET);
		add(petrinet);
		JMenu stg = new JMenu(STG);
		add(stg);
		JMenu decomposition = new JMenu(DECOMPOSITION);
		add(decomposition);
		JMenu help = new JMenu(HELP);
		add(help);
		
		file.add(frame.NEW);
		file.add(frame.OPEN);
		file.add(frame.SAVE);
		file.add(frame.SAVE_AS);
		file.add(frame.SAVE_AS_SVG);
		
		file.add(new JSeparator());
	//	file.add(editor.PRINT);
	//	file.add(editor.PRINT_VISIBLE);
		file.add(frame.EXIT);
		
		layouts.add(frame.LAYOUT1);
		layouts.add(frame.LAYOUT2);
		layouts.add(frame.LAYOUT3);
		layouts.add(frame.LAYOUT4);
		layouts.add(frame.LAYOUT5);
		layouts.add(frame.LAYOUT6);
		layouts.add(frame.LAYOUT7);
		layouts.add(frame.LAYOUT8);

	//	edit.add(editor.SELECT_ALL);
	//	edit.add(editor.SHRINK);
	//	edit.add(editor.ENLARGE);
		edit.add(frame.COPY_STG);
		file.add(new JSeparator());
		edit.add(frame.LAYOUT);
		
		
		view.add(frame.IS_SHORTHAND);
//		view.add(editor.ZOOM_IN);
//		view.add(editor.ZOOM_OUT);
//		view.add(editor.ROTATE_CW);
//		view.add(editor.ROTATE_ACW);
//		view.add(editor.SHADOWS);
//		view.add(editor.GRID);
//		view.add(editor.ALIGN_GRID);
//		view.add(editor.CYCLES);
		
		petrinet.add(frame.RG);
//		petrinet.add(editor.ADD_PLACE);
//		petrinet.add(editor.ADD_TRANSITION);
//		petrinet.add(editor.ADD_ARC_PLACE_TRANSITION);
//		petrinet.add(editor.ADD_ARC_TRANSITION_PLACE);
//		petrinet.add(editor.SUB_ARC_PLACE_TRANSITION);
//		petrinet.add(editor.SUB_ARC_TRANSITION_PLACE);

		
		stg.add(frame.SIGNAL_TYPE);
		stg.add(frame.GENERATE_STG);
		
		
		JMenu partition = new JMenu(PARTITION);
		decomposition.add(partition);
		
		partition.add(frame.FINEST_PARTITION);
		partition.add(frame.ROUGHEST_PARTITION);
		partition.add(frame.MULTISIGNAL_PARTITION);
		partition.add(frame.AVOIDCSC_PARTITION);
		partition.add(frame.REDUCECONC_PARTITION);
		partition.add(frame.LOCKED_PARTITION);
		partition.add(frame.BEST_PARTITION);
		
		decomposition.add(frame.REDUCE_SAFE);
		decomposition.add(frame.REDUCE_WITH_LP_SOLVER);
		decomposition.add(frame.REDUCE_UNSAFE);
		
		JMenu decomp = new JMenu(frame.DECOMPOSE); 
		decomposition.add(decomp);
		
		decomp.add(frame.DECO_BASIC);
		decomp.add(frame.DECO_SINGLE_SIG);
		decomp.add(frame.DECO_MULTI_SIG);
		decomp.add(frame.DECO_TREE);
		decomp.add(frame.DECO_CSC_AWARE);
		decomp.add(frame.DECO_ICSC_AWARE);
		
		
//		decomposition.add(frame.RESOLVE_INTERNAL);
		help.add(frame.ABOUT);
		
		
		/*
		JMenu edit = new JMenu("Edit");
		add(edit);
		newItem(edit, "Select all", 0);
		newItem(edit, "Invert selection", 0);
		newItem(edit, "Remove", 0);
		
		
//		**************************************
		
		JMenu deco = new JMenu("Decomposition");
		add(deco);
		newItem(deco, "Create Initial Partition", KeyEvent.VK_I);
		newItem(deco, "Reduce", 0);
		newItem(deco, "Enable Decomposition Operations", KeyEvent.VK_O);
		newItem(deco, "Enable Forced Deletion", KeyEvent.VK_D);		
		newItem(deco, "Change signal type", 0);		
		
		
//		**************************************
		JMenu pn = new JMenu("Petri net");
		add(pn);
		newItem(pn, "Rename places", 0);
		newItem(pn, "Create reachability graph", 0);
		
		
//		**************************************		
		JMenu layout = new JMenu("Layout");
		add(layout);
		newItem(layout, "Apply Spring Layout", KeyEvent.VK_Y);
        layout.add(new JSeparator());
		newItem(layout, "Zoom in", KeyEvent.VK_PLUS);
		newItem(layout, "Zoom out", KeyEvent.VK_MINUS);
        newItem(layout, "Show all Nodes", KeyEvent.VK_N);
        layout.add(new JSeparator());
        newItem(layout, "Shrink", KeyEvent.VK_C);
        newItem(layout, "Enlarge", KeyEvent.VK_V);
        
		
//		**************************************
		JMenu help = new JMenu("Help");
		
		add(deco);
		add(layout);
		add(help);
		
		*/
	}
	
	public void actionPerformed(ActionEvent e) {
	}
	
}
