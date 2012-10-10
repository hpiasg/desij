package net.strongdesign.balsa.breezefile;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class BreezePartElement extends AbstractBreezeElement implements NamedBreezeElement {
	private static final long serialVersionUID = -2932159559337348649L;
	String name;
	
	LinkedList<Object> ports = new LinkedList<Object>();
	LinkedList<Object> attributes = new LinkedList<Object>();
	
	BreezeChannelListElement channels = null;
	BreezeComponentListElement components = null;
	
	
	public BreezeComponentListElement getComponentList() {
		return components;
	}
	
	LinkedList<Object> call_contexts = new LinkedList<Object>();
	
	@SuppressWarnings("unchecked")
	public BreezePartElement(LinkedList<Object> list) {
		Iterator<Object> it = list.iterator();
		it.next();
		
		this.name = (String)it.next();
		while (it.hasNext()) {
			Object cur = it.next();
			String symb = BreezeElementFactory.symbolOf(cur);
			
			if (symb.equals("ports"))				ports.addAll((Collection<? extends Object>) cur);
			
			else if (symb.equals("attributes"))		attributes.addAll((Collection<? extends Object>) cur);
			
			else if (symb.equals("channels"))		{
				
				channels = new BreezeChannelListElement((LinkedList<Object>)cur);
				
			}
			else if (symb.equals("components"))		{
				components = new BreezeComponentListElement((LinkedList<Object>) cur);
			}
			//else if (symb.equals("call-contexts"))	call_contexts.addAll((Collection<? extends Object>) cur);
			else this.add(cur);
		}
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void output() {
		System.out.printf("(breeze-part "+name);
		super.output(ports, 2, false, 2);
		
		super.output(attributes, 2, false, 1);
		
		channels.output();
		
		components.output();
		//super.output(call_contexts, 2, false, 2);
		super.output(this, 0, true, 3);
		
		System.out.printf("\n)");
	}

	
}
