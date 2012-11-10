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

import java.util.Map;
import java.util.TreeMap;

public class ComponentSTGExpressions {
	
	static boolean filled=false;
	static private Map<String, String> components = new TreeMap<String, String>(); 
	
	public static String getExpression(String name) {
		if (!filled) ComponentSTGExpressions.fillExpressions();
		if (ComponentSTGExpressions.components.containsKey(name)) {
			return ComponentSTGExpressions.components.get(name);
		}
		return null;
	}
	
	static public Map<String, String> getExpressions() {
		if (!filled) ComponentSTGExpressions.fillExpressions();
		return components;
	}
	
	private static void fillExpressions() {
		
//		components.put("$BrzFetch", "active B,C\n#(A:(B.C))");
		//components.put("$BrzCallMux", "scaled A\nactive B\n#(#|(rA+;oA+;up(B);aA+;rA-;oA-;down(B);aA-))");
//		components.put("$BrzCase", "scaled B\nactive B\n#(rA+;up(#|(iB+;up(B);aA+;rA-;iB-;down(B)));aA-)");
		
		
		// high-level specifications
		components.put("$BrzSlice", "active B\n#(A:B)");
		components.put("$BrzAdapt", "active B\n#(A:B)");
		
		components.put("$BrzSplit", "active B,C\n#(A:(B,C))");
		components.put("$BrzFetch", "active B,C\n#(A:(B.C))");
		
		components.put("$BrzConcur", "scaled B\nactive B\n#(A:#||(B))");
		
		components.put("$BrzSequence",          "scaled B\nactive B\n#(A:#;(B))");
		components.put("$BrzSequenceOptimised", "scaled B\nactive B\n#(A:#;(B))");// TODO: what is the difference?
		
		components.put("$BrzFork",       "active B\nscaled B\n#(A:#,(B))");
		components.put("$BrzForkPush",   "active B\nscaled B\n#(A:#,(B))");
		components.put("$BrzSplitEqual", "active B\nscaled B\n#(A:#,(B))");
		
		components.put("$BrzContinue", "#(A)");
		components.put("$BrzContinuePush", "#(A)");
		components.put("$BrzCall", "scaled A\nactive B\n#(#|(A:B))");
		components.put("$BrzCombine", "active B,C\n#(A:(B,C))");
		components.put("$BrzCombineEqual", "scaled B\nactive B\n#(A:#,(B))");
		components.put("$BrzUnaryFunc", "active B\n#(A:(B))");
		components.put("$BrzConstant", "#(A)");
		components.put("$BrzBinaryFunc", "active B,C\n#(A:(B,C))");
		components.put("$BrzBinaryFuncConstR", "active B\n#(A:(B))");
		
		// low-level specifications
		components.put("$BrzCallMux", "scaled A\nactive B\n#(#|(rA+;oA+;up(B);aA+;rA-;oA-;down(B);aA-))");
		components.put("$BrzEncode", "scaled A\nactive B\n#(#|(rA+;(oA+||rB+);aB+;aA+;rA-;rB-;aB-;oA-;aA-))");
		components.put("$BrzCallDemux", "scaled A\nactive B\n#(#|(rA+;oA+;up(B);aA+;rA-;oA-;down(B);aA-))");
		components.put("$BrzWireFork", "active B\nscaled B\nrA+; #||(rB+)");
		components.put("$BrzLoop", "active B\nrA+;#(B)");
		components.put("$BrzVariable", "scaled B\n#(rA+;oA+;iA+;aA+;rA-;oA-;iA-;aA-)||(#||(#(B)))");
		components.put("$BrzCase", "scaled B\nactive B\n#(rA+;up(#|(iB+;up(B);aA+;rA-;iB-;down(B)));aA-)");
		components.put("$BrzCaseFetch", "scaled C\nactive B,C\n#(rA+;(B;#|(iC+;up(C);aA+;rA-;iC-;down(C));aA-))");
		components.put("$BrzHalt", "rA+");
		components.put("$BrzNullAdapt", "active A\nA:(up(B);down(B))");
		
		// using synchronous product
		components.put("$BrzDecisionWait", "scaled B,C\nactive C\n#(#|(B:C)) ** #(rA+;#|(up(C);aA+;rA-;down(C));aA-)");
		components.put("$BrzSynch", "active B\nscaled A\n#**(#(A:B))");
		components.put("$BrzSynchPull", "active B\nscaled A\n#**(#(A:B))");
		components.put("$BrzSynchPush", "active C\nscaled B\nscale 2\n#**(#(B:C)) ** #(A:C)");
		components.put("$BrzPassivatorPush", "scaled A\n#(B) ** (#**( #(rA+;aB+;aA+;rA-;aB-;aA-)))");
		
		components.put("$BrzArbiter", "active C,D\n#(rA+;oA+;iA+;rC+;aC+;aA+;rA-;oA-;iA-;rC-;aC-;aA-)"
				+"**#(rB+;oB+;iB+;rD+;aD+;aB+;rB-;oB-;iB-;rD-;aD-;aB-)"
				+"**#((iA+;rC+;aC+;aA+;rA-;oA-;iA-)|(iB+;rD+;aD+;aB+;rB-;oB-;iB-))"
				+"**#((rC+;aC+;aA+;rA-;oA-;iA-;rC-;aC-)|(rD+;aD+;aB+;rB-;oB-;iB-;rD-;aD-))");
		
		
		

		// components for the internal generators
		components.put("$BrzActiveEagerFalseVariable", "$BrzActiveEagerFalseVariable:2");
		components.put("$BrzFalseVariable", "$BrzFalseVariable:2");
		components.put("$BrzWhile", "$BrzWhile");
		components.put("$BrzPassivator", "$BrzPassivator:2");
	/**/
	}
}
