

package net.strongdesign.balsa.breezefile;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import net.strongdesign.balsa.breezefile.xml.BreezeExpressions;
import net.strongdesign.balsa.breezefile.xml.Component;
import net.strongdesign.desij.CLW;

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
		boolean xmlresult = false;
	    if(!CLW.instance.BREEZEEXPRESSIONSFILE.getValue().equals("")) {
	        xmlresult = fillExpressionsXml();
	    } 
	    
	    if(!xmlresult) {
	        fillExpressionsDefault();
	    }
	}
	
	private static boolean fillExpressionsXml() { 
	    BreezeExpressions exp = BreezeExpressions.readIn(new File(CLW.instance.BREEZEEXPRESSIONSFILE.getValue()));
	    if(exp == null) {
	        System.out.print("Failed to read Breeze expressions from xml. Using defaults");
	        return false;
	    }
	    
	    fillExpressionsDefault(); // Use defaults for undefined components in xml
	    if(exp.getComponents() != null) {
    	    for(Component c : exp.getComponents()) {
    	        components.put(c.getName(), c.getExpression());
    	    }
	    }
	    
	    filled = true;
	    return true;
	}	
	
	private static void fillExpressionsDefault() {    
//		components.put("$BrzFetch", "active B,C\n#(A:(B.C))");
		//components.put("$BrzCallMux", "scaled A\nactive B\n#(#|(rA+;oA+;up(B);aA+;rA-;oA-;down(B);aA-))");
//		components.put("$BrzCase", "scaled B\nactive B\n#(rA+;up(#|(iB+;up(B);aA+;rA-;iB-;down(B)));aA-)");
		
		
		// high-level specifications
		components.put("$BrzSlice", "active B\n#(A:B)");
		components.put("$BrzAdapt", "active B\n#(A:B)");
		
		components.put("$BrzSplit", "active B,C\n#(A:(B,C))");
		components.put("$BrzFetch", "active B,C\n#(A:(B.C))");
		
		components.put("$BrzConcur", "scaled B\nactive B\n#(A:#||(B))");
		
		
		components.put("$BrzSequence",   "$BrzSequence:3");
//		components.put("$BrzSequence",   "scaled B\nactive B\n#(A:#;(B))");
//		components.put("$BrzSequence",   "scaled B,C\nactive B,C\n#(rA+.#.(rB+.aB+.rC+.rB-.aB-).aA+.rA-.#.(rC-).aA-)");
//		components.put("$BrzSequence",   "scaled B,C\nactive B,C\n#(rA+.#.(rB+.aB+.oC+.rB-.aB-).aA+.rA-.#.(oC-).aA-)");
		
		
		// TODO: what is the difference between this and BrzSequence?
		components.put("$BrzSequenceOptimised", "scaled B\nactive B\n#(A:#;(B))");
		
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
		components.put("$BrzEncode", "scaled A,C\nactive B,C,D\n#(#|(A:(rC.aD.rB.aB)))");
		
		
//		components.put("$BrzWireFork", "active B\nscaled B\nrA+; #||(rB+)");
		components.put("$BrzWireFork", "active B\nscaled B\n#(rA+;#||(rB+;~aB+);aA+;rA-;#||(rB-;aB-);aA-)");
		//
		
		components.put("$BrzLoop", "active B\n#(rA+;#(B);~aA+;rA-;aA-)");
		
		components.put("$BrzVariable", "scaled B\nactive D\n#(A:D)||(#||(#(B)))");
		
//		components.put("$BrzCase", "scaled B,D\nactive B,C,D,E\n#(rA+;rC+;((#|(aD+;up(B);aA+;rA-;rC-;aD-;down(B)))|(aE+;aA+;rA-;rC-;aE-));aA-)");
		
		//components.put("$BrzCaseFetch", "scaled C,F,G\nactive B,C,D,E,F,G,H\n"+
		//		"#(rA+;((D.B);rE+;#|(aF+;up(C.rG.aH);aA+;rA-;rE-;aF-;down(C.rG.aH));aA-))");
		components.put("$BrzCaseFetch", 
				"scaled C,F,G\n"
				+ "active B,C,D,E,F,G,H\n"
				+ "#(rA+;((up(B);up(D);cD+;down(D);down(B));rE+;#|(aF+;up(C.rG.aH);aA+;rA-;cD-;rE-;aF-;down(C.rG.aH));aA-))");
		
		
		components.put("$BrzHalt", "#(rA+;~aA+;rA-;aA-)");
		components.put("$BrzNullAdapt", "active A\n#(B:(up(A);down(A)))");
		
		// using synchronous product
		components.put("$BrzDecisionWait", "scaled B,C\nactive C\n#(#|(B:C)) || #(rA+;#|(up(C);aA+;rA-;down(C));aA-)");
		components.put("$BrzSynch", "active B\nscaled A\n#||(#(A:B))");
		components.put("$BrzSynchPull", "active B\nscaled A\n#||(#(A:B))");
		components.put("$BrzSynchPush", "active C\nscaled B\nscale 2\n#||(#(B:C)) || #(A:C)");
		components.put("$BrzPassivatorPush", "scaled A\n#(B) || (#||( #(rA+;aB+;aA+;rA-;aB-;aA-)))");
		
		components.put("$BrzArbiter", "active C,D,E,F\n#(A:(E.C))||#(B:(F.D))||#(aE | aF)||#(C | D)");
		
		components.put("$BrzWhile", "active B, C, R, F, T\n" +
				"#(rA+;up(B);rR+;#(aT+;down(B);rR-;aT-;C;up(B);rR+);"+
				"aF+;aA+;rA-;down(B);rR-;aF-;aA-)");
		
		// old
		components.put("$BrzFalseVariable",	"scaled C\nactive B\n#(A:(rB+;#||(#C);aB+;down(B)))");
		// SELEM, separated
//		components.put("$BrzFalseVariable",	"scaled C\nactive B\n#(A:(up(B);down(B)))||(#||(#(C)))");
		// TELEM, separated
//		components.put("$BrzFalseVariable",	"scaled C\nactive B\n#(rA+;rB+;aB+;aA+;((rB-;aB-)||rA-);aA-)||(#||(#(C)))");
		
		components.put("TELEM", "active B\n#(rA+;rB+;aB+;aA+;((rB-;aB-)||rA-);aA-)");
		components.put("TELEM_SCALED", "scaled B\nactive B\n#(rA+; #.(up(B)) ;aA+;rA-;aA-) || #||(#(B;aA-))");
		components.put("TELEM_SCALED_CSC", "scaled B\nactive B\n#(rA+; #;(up(B);cB+) ;aA+;rA-;aA-) || #||(#(cB+;down(B);cB-;aA-)) || #(#;(cB-);aA-) || #(rA+;cB+;rA-;cB-)");
		
		
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
