package net.strongdesign.balsa.breezefile;



import java.util.Iterator;
import java.util.LinkedList;

public class BreezeComponentElement extends AbstractBreezeElement implements NamedBreezeElement {

	
	private static final long serialVersionUID = -1344287802409223810L;
	
	boolean isDeclared = true;
	int ID;
	String symbol;
	String name;
	
	public LinkedList<Object> parameters = null;
	public LinkedList<Object> channels = null;
	
	@SuppressWarnings("unchecked")
	public BreezeComponentElement(LinkedList<Object> value, int ID) {
		
		Iterator<Object> it = value.iterator();
		symbol = (String)it.next();
		if (!symbol.equals("component")) isDeclared = false;
		
		name = (String)it.next();
		
		parameters =(LinkedList<Object>)it.next();
		channels   =(LinkedList<Object>)it.next();
		
		while (it.hasNext()) {
			Object cur = it.next(); 
			this.add(cur);
		}
		
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	
	public void output() {
		
		System.out.print("\n    ("+symbol+" "+name+" ");
		output(parameters, 0, false, 0); indent(1);
		output(channels, 0, false, 0);
		
		if (isDeclared) {
			// output parameters and channels
			output(this, 0, true, 0);
			System.out.print(")");
		} else {
			output(this, 4, true, 3);
			System.out.print("\n    )");
			
		}
		
	}
	

}
