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
		
		components.put("$BrzVariable", "scaled B\n#(A)||(#||(#(B)))");
		
		components.put("$BrzCallMux", "scaled A\nactive B\n#(#|(A:B))");
		components.put("$BrzCase", "scaled B\nactive B\n#(rA+;up(#|(up(B);aA+;rA-;down(B)));aA-)");
		components.put("$BrzConstant", "#(A)");
		
		
		components.put("$BrzBinaryFunc", "active B,C\n#(A:(B,C))");
		components.put("$BrzBinaryFuncConstR", "active B\n#(A:(B))");
		components.put("$BrzUnaryFunc", "active B\n#(A:(B))");
		
		components.put("$Halt", "rA+");
		
	}
}
