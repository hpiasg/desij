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
