/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011, 2012 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import net.strongdesign.balsa.breezefile.ComponentSTGExpressions;
import net.strongdesign.balsa.breezefile.ComponentSTGFactory;
import net.strongdesign.balsa.breezefile.ComponentSTGInternalImplementations;
import net.strongdesign.balsa.hcexpressionparser.HCExpressionParser;
import net.strongdesign.balsa.hcexpressionparser.ParseException;
import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator;
import net.strongdesign.balsa.hcexpressionparser.terms.HCLoopTerm;
import net.strongdesign.balsa.hcexpressionparser.terms.HCSTGGenerator;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm;
import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm.ExpansionType;
import net.strongdesign.desij.CLW;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.STGInOutParameter;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.parser.GParser;

public class STGGeneratorFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4446497586026898153L;

	public final STGEditorAction EXPAND = new STGEditorAction("Expand", 0,
			null, 0, this);
	public final STGEditorAction OKAY = new STGEditorAction("Generate", 0,
			null, 0, this);
	public final STGEditorAction CANCEL = new STGEditorAction("Cancel", 0,
			null, 0, this);

	JComboBox presetList;
	JButton expandButton = new JButton("Expand");
	JButton generateButton = new JButton("Generate");
	JButton cancelButton = new JButton("Cancel");
	JTextArea inputText = new JTextArea();
	JTextArea outputText = new JTextArea();
	JCheckBox reduceSTG = new JCheckBox();
	JCheckBox originalChoiceExp = new JCheckBox();
	
	JCheckBox solveCSC = new JCheckBox();
	
	JCheckBox enforceInjectiveLabelling = new JCheckBox();

	STGEditorFrame frame;

	TreeMap<String, String> mp = new TreeMap<String, String>();

	STGGeneratorFrame(STGEditorFrame frame) {
		super();
		
		this.frame = frame;
		setTitle("STG Generator");
		
		// some samples for the JComboBox
		mp.put(" <Select template>", "");
		
		for (Entry<String, String> e: ComponentSTGExpressions.getExpressions().entrySet()) {
			if (e.getValue().contains("scaled")) {
				mp.put(e.getKey(), "scale 4\n"+e.getValue());
			} else {
				mp.put(e.getKey(), e.getValue());
			}
		}
		
		mp.put("WireFork", "active B\nscaled B\nscale 3\n#[ A : #,(B) ]");
		
		mp.put("Sync component", "#(A:B:C)");

		mp.put("Call (inconsistent)", "active B\nscale 2\nscaled A\n#(  #| (A:B))");

		mp.put("CaseFetch",
				"active B,C\nscaled C\nscale 4\n#(rA+;B;#|(up(C);aA+;rA-;down(C));aA-)");


		presetList = new JComboBox(mp.keySet().toArray());
		presetList.addActionListener(this);

		setBounds(new Rectangle(150, 150, 850, 400));
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		JPanel bottomPanel = new JPanel();
		JSeparator separator = new JSeparator();

		add(topPanel, BorderLayout.NORTH);

		topPanel.add(separator, BorderLayout.SOUTH);
		topPanel.add(presetList, BorderLayout.NORTH);

		JScrollPane spane = new JScrollPane();

		spane.getViewport().add(outputText, BorderLayout.CENTER);

		add(spane, BorderLayout.CENTER);

		add(bottomPanel, BorderLayout.SOUTH);
		
		generateButton.addActionListener(this);
		bottomPanel.add(generateButton);

		cancelButton.addActionListener(this);
		bottomPanel.add(cancelButton);
		

		topPanel.add(inputText);

		expandButton.addActionListener(this);
		topPanel.add(expandButton, BorderLayout.EAST);

		reduceSTG.setText("Reduce dummy transitions");
		reduceSTG.setSelected(false);
		bottomPanel.add(reduceSTG);
		
		originalChoiceExp.setText("Use original choice expansion");
		originalChoiceExp.setSelected(false);
		bottomPanel.add(originalChoiceExp);

		solveCSC.setText("Solve CSC using Petrify");
		
		solveCSC.setSelected(CLW.instance.HANDSHAKE_COMPONENT_CSC.isEnabled());
		
		bottomPanel.add(solveCSC);
		
		enforceInjectiveLabelling.setText("Injective labelling");
		enforceInjectiveLabelling.setSelected(CLW.instance.ENFORCE_INJECTIVE_LABELLING.isEnabled());
		bottomPanel.add(enforceInjectiveLabelling);

		outputText.setEditable(false);

		Font fnt = new Font("Monospaced", Font.PLAIN, 14);

		inputText.setFont(fnt);
		outputText.setFont(fnt);

	}

	@Override
	public void actionPerformed(ActionEvent act) {
		Object source = act.getSource();

		if (source == presetList) {
			JComboBox cb = (JComboBox) act.getSource();
			String name = (String) cb.getSelectedItem();
			inputText.setText(mp.get(name));
		}
		
		String input = inputText.getText();
		boolean internalImplementation = input.startsWith("$");

		//
		if (source == expandButton && !internalImplementation) {

			// launch the parser routine
			HCExpressionParser parser = new HCExpressionParser(
					new StringReader(inputText.getText()));
			
			try {
				// parser
				outputText.setText("");
				HCTerm t = parser.HCParser();
				String s;
				
				if (t == null) {
					s = "NULL term returned";
				} else {
					HCTerm up = t
							.expand(ExpansionType.UP, parser.scale, parser, originalChoiceExp.isSelected());
					HCTerm down = t.expand(ExpansionType.DOWN, parser.scale,
							parser, originalChoiceExp.isSelected());

					if (up != null)
						s = "UP: " + up.toString() + "\n";
					else
						s = "UP: null\n";

					if (down != null)
						s += "DOWN: " + down.toString();
					else
						s += "DOWN: null";

				}
				outputText.setText(s);

			} catch (ParseException e) {
				String s = outputText.getText();
				s += "\n" + e.getMessage();
				e.printStackTrace();
			} catch (Exception e) {
				String s = outputText.getText();
				s += "\n" + e.getMessage();
				e.printStackTrace();
			}
		}

		if (source == generateButton) {
			
			STG stg = null;
			
			if (!internalImplementation) {
				
				// launch the parser routine
				HCExpressionParser parser = new HCExpressionParser(
						new StringReader(inputText.getText()));
				
				try {
					// parser.
					outputText.setText("");
					HCTerm t = parser.HCParser();
					String s;

					if (t == null) {
						s = "NULL term returned";
					} else {
						
						
						HCTerm up = t
								.expand(ExpansionType.UP, parser.scale, parser, originalChoiceExp.isSelected());
						HCTerm down = t.expand(ExpansionType.DOWN, parser.scale,
								parser, originalChoiceExp.isSelected());

						if (up != null)
							s = "UP: " + up.toString() + "\n";
						else
							s = "UP: null\n";

						if (down != null)
							s += "DOWN : " + down.toString();
						else
							s += "DOWN: null";
						
						HCInfixOperator ud = new HCInfixOperator();
						ud.components.add(up);
						ud.components.add(down);
						ud.operation=Operation.SEQUENCE;
						
						HCTerm tt = ud;
						if (down==null) tt = up;
						
						// the final result is the sequential composition of up expansion and
						// down expansion
						stg = HCInfixOperator.generateComposedSTG(solveCSC.isSelected(), tt, 
								parser, enforceInjectiveLabelling.isSelected());
						
					}

					outputText.setText(s);

				} catch (ParseException e) {
					String s = outputText.getText();
					s += "\n" + e.getMessage();
					e.printStackTrace();
				} catch (Exception e) {
					String s = outputText.getText();
					s += "\n" + e.getMessage();
					e.printStackTrace();
				}
			} else {
				
				// create STG from the internal implementation
				stg = ComponentSTGInternalImplementations.getInternalImplementation(inputText.getText(), 0);
				
			}
			
			if (stg!=null) {
				if (enforceInjectiveLabelling.isSelected()) {
					STGUtil.enforceInjectiveLabelling(stg);
				}

				if (reduceSTG.isSelected()) {
					STGUtil.reduceSTG(stg);
				}

				frame.addSTG(stg, "generated");
				frame.setLayout(3);
			}
			
			setVisible(false);
		}

		if (source == cancelButton) {
			setVisible(false);
		}
	}

}
