package net.strongdesign.balsa.breezefile;

import java.util.LinkedList;
import java.util.Map;

public class AbstractBreezeElement extends LinkedList<Object> {
	private static final long serialVersionUID = -6031936614723859366L;
	
	public AbstractBreezeElement() {
	}
	
	@SuppressWarnings("unchecked")
	public AbstractBreezeElement(Object value) {
		
		if (value instanceof LinkedList<?>) {
			this.addAll((LinkedList<Object>)value);
		} else {
			this.add(value);
		}
	}
	
	protected static void indent(int num) {
		for (int i=0;i<num;i++) {
			System.out.printf(" ");
		}
	}
	
	public void output() {
		output(this, 0, false, 5);
	}
	
	public void output(int level) {
		output(this, level, false, 5);
	}
	
	
	private boolean isBreezeCollection(Object value) {
		return value instanceof LinkedList<?>||value instanceof Map<?,?>; 
	}
	
	/**
	 * The function outputs breeze tree contents
	 * 
	 * @param value - an object to be printed
	 * @param level - a level of indent in the number of spaces 
	 * @param openedList - if true for a list, opening and closing brackets will not be printed
	 * @param doNewline - states, whether to output a "new line" for a list structure
	 * @return - returns true, if the component was a list
	 */
	@SuppressWarnings("unchecked")
	protected boolean output(Object value, int level, boolean openedList, int doNewline) {
		if (value==null) return false;
		
		if (isBreezeCollection(value)) {
			if (doNewline>0) {
				System.out.printf("\n");
				indent(level);
			}
			
			if (!openedList) {
				System.out.printf("(");
			}
			
			boolean has_list = false;
			boolean first=true;
			
			if (value instanceof Map<?, ?>) {
				
				for (java.util.Map.Entry<Integer, Object> entry: ((Map<Integer, Object>)value).entrySet()) {
					
					if (!first||openedList) System.out.printf(" ");
					has_list|=output(entry.getValue(), level+2, false, doNewline-1);
					System.out.printf(" ; "+entry.getKey());
					first=false;
				}
				
			} else {
				for (Object item: (LinkedList<Object>)value) {
					
					if (!first||openedList) System.out.printf(" ");
					has_list|=output(item, level+2, false, doNewline-1);
						
					first=false;
				}
			}
			
			if (!openedList) {
				if (has_list&&doNewline>1) {
					System.out.printf("\n"); indent(level); System.out.printf(")");
				} else {
					System.out.printf(")");
				}
			}
			return true;
			
		} else if (value instanceof Integer) {
			System.out.printf(""+value);
		} else if (value instanceof Boolean) {
			System.out.printf((((Boolean)value)?"#t":"#f"));
		} else if (value instanceof String) {
			System.out.printf(""+value);
		} else {
			System.out.printf("####error####("+value+")");
		}
		return false;
	}
	
}
