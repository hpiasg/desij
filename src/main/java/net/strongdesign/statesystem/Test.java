

package net.strongdesign.statesystem;

import java.util.LinkedList;
import java.util.List;



public class Test {

	/**
	 * @param args
	 * @throws StateSystemException 
	 */
	public static void main(String[] args) throws StateSystemException {
		
		Automaton<Integer, String> a = new Automaton<Integer, String>();
		
		
		a.addState(1);
		a.addState(2);
		a.addState(3);
		a.addState(4);
		a.addState(5);
		a.addState(6);
		a.addState(7);
		
		
		a.addArc(1, "a", 2);
		a.addArc(1, "b", 3);
		a.addArc(2, "b", 4);
		a.addArc(3, "c", 2);
		a.addArc(3, "a", 5);
		a.addArc(4, "d", 1);
		a.addArc(5, "c", 4);
		a.addArc(5, "b", 6);
		a.addArc(6, "a", 1);
		a.addArc(6, "e", 7);
		
		a.setInitialState(1);
		
		
		
		List<String> trace = new LinkedList<String>();
		
		trace.add("b");
		trace.add("b");
		trace.add("e");
		
		
		
		System.out.println(trace);
		
		
		List<String> proj = StateSystems.inverseProjection(a, trace, new StateSystems.EventCondition<String>() {
		
			public boolean fulfilled(String event) {
				return !(event.matches("[b]"));
			}
		
		});
		
		System.out.println(proj);
		
		
		
		
		

	}

}
