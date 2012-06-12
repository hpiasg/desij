package net.strongdesign.desij.gui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import net.strongdesign.balsa.hcexpressionparser.ParseException;
import net.strongdesign.balsa.hcexpressionparser.HCExpressionParser;
import net.strongdesign.balsa.hcexpressionparser.terms.HCSTGGenerator;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm.ExpansionType;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public class STGGeneratorFrame  extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4446497586026898153L;
	
	public final STGEditorAction EXPAND = new STGEditorAction("Expand", 0, null, 0, this);
	public final STGEditorAction OKAY = new STGEditorAction("Generate", 0, null, 0, this);
	public final STGEditorAction CANCEL = new STGEditorAction("Cancel", 0, null, 0, this);
	
	
	JButton expandButton = new JButton("Expand");
	JButton generateButton = new JButton("Generate");
	JButton cancelButton = new JButton("Cancel");
	JTextArea inputText = new JTextArea();
	JTextArea outputText = new JTextArea();
	
	STGEditorFrame frame;
	
	STGGeneratorFrame(STGEditorFrame frame) {
		super();
		
		this.frame = frame;
		setTitle("STG Generator");
		
		setBounds(new Rectangle(150, 150, 500, 400));
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		
		JPanel bottomPanel = new JPanel();
		JSeparator separator = new JSeparator();
		
		add(topPanel, BorderLayout.NORTH);
		topPanel.add(separator, BorderLayout.SOUTH);
		
		add(outputText, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		topPanel.add(inputText);
		
		expandButton.addActionListener(this);
		topPanel.add(expandButton, BorderLayout.EAST);
		
		
		generateButton.addActionListener(this);
		bottomPanel.add(generateButton);
		
		cancelButton.addActionListener(this);
		bottomPanel.add(cancelButton);
		outputText.setEditable(false);
		
	}

	@Override
	public void actionPerformed(ActionEvent act) {
		Object source = act.getSource();
		
		// 
		if (source==expandButton) {
			
			// launch the parser routine
			HCExpressionParser parser = new HCExpressionParser(new StringReader(inputText.getText()));
			
			try {
				//parser.
				outputText.setText("");
				HCTerm t = parser.HCParser();
				String s;
	            if (t==null) {
	              	s="NULL term returned";
	            } else {
	    			HCTerm up   = t.expand(ExpansionType.UP);
	    			HCTerm down = t.expand(ExpansionType.DOWN);
	    			
		    		if (up!=null) s="UP: "+up.toString()+"\n";
		    		else s="UP: null\n";
		    		
		    		if (down!=null) s+="DOWN: "+down.toString();
		    		else s+="DOWN: null";
	    			
	    		}
				outputText.setText(s);
				
			} catch (ParseException e) {
				String s = outputText.getText();
				s+= "\n"+e.getMessage();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (source==generateButton) {
			// launch the parser routine
			HCExpressionParser parser = new HCExpressionParser(new StringReader(inputText.getText()));
			
			try {
				//parser.
				outputText.setText("");
				HCTerm t = parser.HCParser();
				String s;
				
	            if (t==null) {
	              	s="NULL term returned";
	            } else {
	    			HCTerm up   = t.expand(ExpansionType.UP);
	    			HCTerm down = t.expand(ExpansionType.DOWN);
	    			
	    			
		    		if (up!=null) s="UP: "+up.toString()+"\n";
		    		else s="UP: null\n";
		    		
		    		if (down!=null) s+="DOWN: "+down.toString();
		    		else s+="DOWN: null";
		    		
		    		
		    		
					STG stg = new STG();
					Place p = stg.addPlace("init", 1);
	    			
					((HCSTGGenerator)up).generateSTG(stg, parser, p, p);
					
					frame.addSTG(stg, "generated");
	    		}
				outputText.setText(s);
				
			} catch (ParseException e) {
				String s = outputText.getText();
				s+= "\n"+e.getMessage();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			

			
			setVisible(false);
		}
		
		if (source==cancelButton) {
			setVisible(false);
		}
	}

}
