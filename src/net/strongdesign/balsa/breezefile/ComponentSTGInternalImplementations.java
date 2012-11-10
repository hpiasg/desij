/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011,2012 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

package net.strongdesign.balsa.breezefile;

import java.io.StringReader;
import java.util.LinkedList;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.parser.GParser;

/**
 * This class provides internal implementation for some components 
 * that cannot be directly created with expressions
 *  
 * @author Stanislavs Golubcovs
 *
 */
public class ComponentSTGInternalImplementations {

	static public STG brzWhile() {
		STG stg = null;
		String expression = 
			".inputs aB aC iA iC rA\n"+
			".outputs aA rB rC \n"+
			".graph\n"+
			"p2 iA+ iC+\np1 rB+\naA+ rA-\nrA- rB-/2\n"+
			"aA- rA+\naB+ p2\naB- iC-\niC- rC+\n"+
			"aB-/2 iA-\niA- aA-\naC+ rC-\nrC- aC-\n"+
			"aC- p1\niA+ aA+\nrB- aB-\niC+ rB-\n"+
			"rC+ aC+\nrA+ p1\nrB-/2 aB-/2\nrB+ aB+\n"+
			".marking { <aA-,rA+> }\n.end";
		
		try {
			
			GParser parser = new GParser(new StringReader(expression));
			stg = parser.STG();
		} catch (net.strongdesign.stg.parser.ParseException e) {
			e.printStackTrace();
		} catch (STGException e) {
			e.printStackTrace();
		}
		return stg;
	}
	
	static public STG brzActiveEagerFalseVariable(int scale) {
		String expression = "active B,C\nscaled D\n#(A:(B.(up(C);down(C)))) || #||(#(D))";
		
		// initial model to work with
		STG stg = ComponentSTGFactory.getSTGFromExpression(expression, scale);
		
		Transition t1 = null, t2 = null;
		Place place = null;
		
		// find two transitions and remove place between rC+ -> aC+
		for (Place p: stg.getPlaces()) {
			// actions depend on the name of prior transition
			Transition t = (Transition)p.getParents().iterator().next();
			String name = stg.getSignalName(t.getLabel().getSignal());
			EdgeDirection ed = t.getLabel().getDirection();
			
			if (name.equals("rC")&&ed==EdgeDirection.UP) {
				
				t1 = t;
				t2 = (Transition)p.getChildren().iterator().next();
				place = p;
			}
			
		}
		
		// attach readers, make their markings empty
		for (Place p: stg.getPlaces()) {
			// actions depend on the name of prior transition
			Transition t = (Transition)p.getParents().iterator().next();
			String name = stg.getSignalName(t.getLabel().getSignal());
			EdgeDirection ed = t.getLabel().getDirection();
			
			if (name.startsWith("aD")&&ed==EdgeDirection.DOWN) {
				p.setParentValue(t1, 1);
				p.setChildValue(t2, 1);
				p.setMarking(0);
			}
		}
		
		stg.removePlace(place);
		
		return stg;
	}
	
	
	static public STG brzFalseVariable(int scale) {
		String expression = "active B\nscaled C\n#(A:(up(B);down(B)))||  #||(#(C))";
		
		// initial model to work with
		STG stg = ComponentSTGFactory.getSTGFromExpression(expression, scale);
		
		Transition t1 = null, t2 = null;
		Place place = null;
		
		// find two transitions and remove place between rC+ -> aC+
		for (Place p: stg.getPlaces()) {
			// actions depend on the name of prior transition
			Transition t = (Transition)p.getParents().iterator().next();
			String name = stg.getSignalName(t.getLabel().getSignal());
			EdgeDirection ed = t.getLabel().getDirection();
			
			if (name.equals("rB")&&ed==EdgeDirection.UP) {
				
				t1 = t;
				t2 = (Transition)p.getChildren().iterator().next();
				place = p;
			}
			
		}
		
		// attach readers, make their markings empty
		for (Place p: stg.getPlaces()) {
			// actions depend on the name of prior transition
			Transition t = (Transition)p.getParents().iterator().next();
			String name = stg.getSignalName(t.getLabel().getSignal());
			EdgeDirection ed = t.getLabel().getDirection();
			
			if (name.startsWith("aC")&&ed==EdgeDirection.DOWN) {
				p.setParentValue(t1, 1);
				p.setChildValue(t2, 1);
				p.setMarking(0);
			}
		}
		
		stg.removePlace(place);
		
		return stg;
	}
	

	static public STG brzPassivator(int scale) {
		
		
		String expression = "scaled A#||(rA+);oA+;#||(aA+;rA-);oA-;#||(aA-)";
		
		// initial model to work with
		STG stg = ComponentSTGFactory.getSTGFromExpression(expression, scale);
		
		
		LinkedList<Place> toDelete = new LinkedList<Place>();
		
		for (Place p1: stg.getPlaces()) {
			if (!p1.getParents().isEmpty()) continue;
			for (Place p2: stg.getPlaces()) {
				if (!p2.getChildren().isEmpty()) continue;
				
				Transition t1 = (Transition)p1.getChildren().iterator().next();
				String name1 = stg.getSignalName(t1.getLabel().getSignal());
				
				Transition t2 = (Transition)p2.getParents().iterator().next();
				String name2 = stg.getSignalName(t2.getLabel().getSignal());
				
				if (!name1.substring(1).equals(name2.substring(1))) continue;
				
				t2.setChildValue(p1, 1);
				toDelete.add(p2);
				
			}
		}
		
		
		for (Place p: toDelete) stg.removePlace(p);
		
		return stg;
	}
	
	
	static public STG brzNastyPassivator(int scale) {
		
		
		String expression = "scaled A#||(rA+);oA+;#||(aA+;rA-);oA-;#||(aA-)";
		
		// initial model to work with
		STG stg = ComponentSTGFactory.getSTGFromExpression(expression, scale);
		
		
		LinkedList<Place> toDelete = new LinkedList<Place>();
		
		for (Place p1: stg.getPlaces()) {
			if (!p1.getParents().isEmpty()) continue;
			for (Place p2: stg.getPlaces()) {
				if (!p2.getChildren().isEmpty()) continue;
				
				Transition t1 = (Transition)p1.getChildren().iterator().next();
				String name1 = stg.getSignalName(t1.getLabel().getSignal());
				
				Transition t2 = (Transition)p2.getParents().iterator().next();
				String name2 = stg.getSignalName(t2.getLabel().getSignal());
				
				if (!name1.substring(1).equals(name2.substring(1))) continue;
				
				t2.setChildValue(p1, 1);
				toDelete.add(p2);
				
			}
		}
		
		
		for (Place p: toDelete) stg.removePlace(p);
		
		return stg;
	}
	
	
	/**
	 * Finds internal implementation for the component
	 * scale == 0 means using scale given with component
	 * @param expression
	 * @param scale
	 * @return
	 */
	static public STG getInternalImplementation(String expression, int scale) {
		
		if (scale==0&&expression.contains(":")&&!expression.split(":")[1].equals("")) {
			scale = Integer.valueOf(expression.split(":")[1]);
		}
		
		if (expression.startsWith("$BrzWhile")) return brzWhile();
		
		if (expression.startsWith("$BrzActiveEagerFalseVariable")) return brzActiveEagerFalseVariable(scale);
		if (expression.startsWith("$BrzFalseVariable")) return brzFalseVariable(scale);
		
		if (expression.startsWith("$BrzPassivator")) return brzPassivator(scale);
		
		if (expression.startsWith("NastyPassivator")) return brzNastyPassivator(scale);
		
		return null;
	}
	
}
