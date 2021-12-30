package net.strongdesign.balsa.breezefile;



import java.util.Iterator;
import java.util.LinkedList;

public class BreezeChannelElement extends AbstractBreezeElement {
	
	private static final long serialVersionUID = 1180548319980606751L;


	public enum ChannelType {
		SYNC("sync"), PUSH("push"), PULL("pull"), ERROR("error!");
		String ctype;
		
		ChannelType(String type) {
			ctype = type;
		}
		
		public String toString() {
			return ctype;
		}
	};
	
	ChannelType type;
	int width=0;
	
	BreezeChannelElement(LinkedList<Object> value) {
		Iterator<Object> it = value.iterator();
		
		String ctype = (String)it.next();
		// read type and width
		type = ChannelType.ERROR;
		if (ctype.equals("sync")) {
			type = ChannelType.SYNC;
		} else if (ctype.equals("push")) {
			type = ChannelType.PUSH;
			width = (Integer)it.next();
		} else if (ctype.equals("pull")) {
			type = ChannelType.PULL;
			width = (Integer)it.next();
		}
		
		//read the tail part
		while(it.hasNext()) {
			this.add(it.next());
		}
	}
	
	public void output() {
		System.out.print("("+type.toString());
		
		if (type!=ChannelType.SYNC) {
			System.out.print(" "+width);
		}
		
		output(this, 0, true, 0);
		
		System.out.print(")");
	}
	
}
