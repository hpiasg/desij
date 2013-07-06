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
		components.put("$BrzUnaryFunc", "active B,D\n#(A:((B).D))");
		components.put("$BrzConstant", "#(A)");
		components.put("$BrzBinaryFunc", "active B,C,D\n#(A:((B,C).D))");
		components.put("$BrzBinaryFuncConstR", "active B,D\n#(A:(B.D))");
		
		// low-level specifications
		components.put("$BrzCallMux", "scaled A,S\nactive B,S,D\n#(#|(rA+;rS+;aD+;up(B);aA+;rA-;rS-;aD-;down(B);aA-))");
		components.put("$BrzCallDemux", "scaled A\nactive B,S,D\n#(#|(A:(rS.aD.B)))");
		
		//components.put("$BrzEncode", "scaled A\nactive B\n#(#|(rA+;(oA+||rB+);aB+;aA+;rA-;rB-;aB-;oA-;aA-))");
		components.put("$BrzEncode", "scaled A,C\nactive B,C,D,E\n#(#|(A:(rC.aD.rB.aB.rE)))");
		
		
		components.put("$BrzWireFork", "active B\nscaled B\nrA+; #||(rB+)");
		components.put("$BrzLoop", "active B\nrA+;#(B)");
		components.put("$BrzVariable", "scaled B\n#(rA+;oA+;iA+;aA+;rA-;oA-;iA-;aA-)||(#||(#(B)))");
		components.put("$BrzCase", "scaled B,D\nactive B,C,D\n#(rA+;rC+;#|(aD+;up(B);aA+;rA-;rC-;aD-;down(B));aA-)");
		
		components.put("$BrzCaseFetch", "scaled C,F,G\nactive B,C,D,E,F,G,H\n"+
				"#(rA+;((rD.B);rE+;#|(aF+;up(C.rG.aH);aA+;rA-;rE-;aF-;down(C.rG.aH));aA-))");
		
		components.put("$BrzHalt", "rA+");
		components.put("$BrzNullAdapt", "active A\n#(B:(up(A);down(A)))");
		
		// using synchronous product
		components.put("$BrzDecisionWait", "scaled B,C\nactive C\n#(#|(B:C)) || #(rA+;#|(up(C);aA+;rA-;down(C));aA-)");
		components.put("$BrzSynch", "active B\nscaled A\n#||(#(A:B))");
		components.put("$BrzSynchPull", "active B\nscaled A\n#||(#(A:B))");
		components.put("$BrzSynchPush", "active C\nscaled B\nscale 2\n#||(#(B:C)) || #(A:C)");
		components.put("$BrzPassivatorPush", "scaled A\n#(B) || (#||( #(rA+;aB+;aA+;rA-;aB-;aA-)))");
		
		components.put("$BrzArbiter", "active C,D,E,F\n#(A:(E.C))||#(B:(F.D))||#(aE | aF)||#(C | D)");
		
		components.put("$BrzWhile", "active B, C, R\n" +
				"#(rA+;up(B);rR+;#(aT+;down(B);rR-;aT-;C;up(B);rR+);"+
				"aF+;aA+;rA-;down(B);rR-;aF-;aA-)");
		
		
		components.put("$BrzFalseVariable",	"scaled C\nactive B\n#(A:(rB+;#||(#C);aB+;down(B)))");
		
		// components for the internal generators
//		components.put("$BrzActiveEagerFalseVariable", 
//				"scaled D\nactive B, C\n#(A:(B.#||(#(aD+;aD-))))||#(rA+;rC+;#||(#D);aC+;down(C);aA+;down(A))");
		
		components.put("$BrzActiveEagerFalseVariable", "$BrzActiveEagerFalseVariable:2"); 
		
		components.put("$BrzPassivator", "scaled A,B\n#||(#(A:aB))");
		
		
		// some other generated STG samples
		components.put("art", "$art:2,3");
		components.put("par", "$par:p1");
		components.put("seq", "$seq:s1");
		components.put("multipar", "$multipar:2");
		components.put("multiseq", "$multiseq:2");
		components.put("seqpartree", "$seqpartree:2");
		components.put("parseqtree", "$parseqtree:2");
		components.put("merge", "$merge:mrg");
		components.put("mix", "$mix:mx");
		components.put("multimix", "$multimix:2");
	/**/
	}
}
