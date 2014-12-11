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

import javax.swing.table.*;


public class DesiResultTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -7388052535494349177L;
	protected String[] columnNames;
    protected Object[][] content;
    protected int rows;
    
    public DesiResultTableModel(String[] columnNames, String[] stgNames) {
        super();
        this.columnNames = columnNames;
        
        this.rows = stgNames.length;        
        content = new String[rows][4];

        
        int i=0;
        for (String actName: stgNames)
            setValueAt(actName, i++, 0);
        
        
    }
    
    
    public int getRowCount() {
        return rows;
    }
    
    public String getColumnName(int index) {
        return columnNames[index];
    }
    
    public int getColumnCount() {
        return 4;
    }
    
    public Object getValueAt(int row, int column)  {
        return content[row][column];
    }
    
    public void setValueAt(Object value, int row, int col) {
        content[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    
}
