package net.strongdesign.balsa.breezefile;



import java.util.Iterator;
import java.util.LinkedList;

public class BreezeElementFactory {
	
	public static String symbolOf(Object value) {
		if (value instanceof LinkedList<?>) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> ll = (LinkedList<Object>)value;
			Iterator<Object> it = ll.iterator();
			Object temp = it.next();
			
			if (!(temp instanceof String)) return "";
			
			if (((String)temp).charAt(0)=='"') return "";
			return (String)temp;
		}
		return "";
	}
	
	
	// for now we only work with the breeze-part element and its contents, the rest elements are not changed 
	
	public static AbstractBreezeElement baseElement(Object value) {
		if (value instanceof LinkedList<?>) {
			@SuppressWarnings("unchecked")
			LinkedList<Object> ll = (LinkedList<Object>)value;
			Iterator<Object> it = ll.iterator();
			String first = (String)it.next();
			
			if (first.equals("import")) {
				return new BreezeImport((String)it.next());
			}
			
			if (first.equals("breeze-part")) {
				return new BreezePartElement(ll);
			}
		}
		return new AbstractBreezeElement(value);
	}
	

}
