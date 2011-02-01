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

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ResultWindow extends JFrame {
	private static final long serialVersionUID = 5752708268549977036L;
	protected DesiResultTableModel tableData;
    protected JTable table;
    
    public ResultWindow(String title, DefaultListModel files) {
        super(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setSize(800,400);
        setBounds(300,300,800,400);
       
        
        String[] headings = {"STG", "Time (sec)", "# Comp.", "Components"};
        String[] fileNames = new String[files.getSize()];
        
        for (int i=0; i<files.getSize(); i++)
            fileNames[i] = ((File)files.get(i)).getAbsolutePath();
        
        tableData = new DesiResultTableModel(headings, fileNames);
        
        table = new JTable(tableData);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);

        
        JScrollPane scrollPane = new JScrollPane(table);
        table.setPreferredScrollableViewportSize(new Dimension(400, 400));        

        
        
        getContentPane().add(scrollPane);
          
     
        
        
        
    }
    
    
    public DesiResultTableModel getDataModel()  {
        return tableData;
    }


}
