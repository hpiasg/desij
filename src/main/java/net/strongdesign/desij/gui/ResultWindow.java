

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
