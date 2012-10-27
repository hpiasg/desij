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
		components.put("$BrzFetch", "active B,C\n#(A:(B.C))");
		components.put("$BrzConcur", "scaled B\nactive B\n#(A:#||(B))");
		
		components.put("$BrzSequence", "scaled B\nactive B\n#(A:#;(B))");
		components.put("$BrzSequenceOptimised", "scaled B\nactive B\n#(A:#;(B))");
		
		components.put("$BrzFork", "active B\nscaled B\n#( A : #,B )");
		components.put("$BrzLoop", "active B\nrA+;#(B)");
		components.put("$BrzContinue", "#(A)");
		
		components.put("$BrzVariable", "scaled B\n#(rA+;oA+;iA+;aA+;rA-;oA-;iA-;aA-)||(#||(#(B)))");
		
		components.put("$BrzCall", "scaled A\nactive B\n#(#|(A:B))");
		
		components.put("$BrzCombine", "active B,C\n#(A:(B,C))");

		components.put("$BrzCombineEqual", "scaled B\nactive B\n#(A:#,(B))");
		
		components.put("$BrzDecisionWait", "scaled B,C\nactive C\n#(#|(B:C)) ** #(rA+;#|(up(C);aA+;rA-;down(C));aA-)");
		
		
		// need to study, whether some additional internal signals are needed
//		components.put("$BrzCallMux", "scaled A\nactive B\n#(#|(A:B))");
		
		components.put("$BrzCase", "scaled B\nactive B\n#(rA+;up(#|(iB+;up(B);aA+;rA-;iB-;down(B)));aA-)");
		
		components.put("$BrzCaseFetch", "scaled C\nactive B,C\n#(rA+;(B;#|(iC+;up(C);aA+;rA-;iC-;down(C));aA-))");
		
		components.put("$BrzConstant", "#(A)");
		
		components.put("$BrzEncode", "scaled A\nactive B\n#(#|(A:B))");
		
		components.put("$BrzBinaryFunc", "active B,C\n#(A:(B,C))");
		components.put("$BrzBinaryFuncConstR", "active B\n#(A:(B))");
		components.put("$BrzUnaryFunc", "active B\n#(A:(B))");
		
		components.put("$BrzHalt", "rA+");
		
		// the component is not scalable
		// and it is not clear, how to write an expression for that
		// simply give its STG specification
		components.put("$BrzWhile",
				".inputs aB aC iA iC rA\n"+
				".outputs aA rB rC \n"+
				".graph\n"+
				"p2 iA+ iC+\np1 rB+\naA+ rA-\nrA- rB-/2\n"+
				"aA- rA+\naB+ p2\naB- iC-\niC- rC+\n"+
				"aB-/2 iA-\niA- aA-\naC+ rC-\nrC- aC-\n"+
				"aC- p1\niA+ aA+\nrB- aB-\niC+ rB-\n"+
				"rC+ aC+\nrA+ p1\nrB-/2 aB-/2\nrB+ aB+\n"+
				".marking { <aA-,rA+> }\n.end");
		
		
	}
}
