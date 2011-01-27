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
