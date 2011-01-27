/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg;

/**
 * @author mark
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public enum Signature {
	INPUT 		("?", "input signal", ".inputs"),
	OUTPUT 		("!", "output signal", ".outputs"),
	INTERNAL	("*", "internal signal", ".internal"),
	DUMMY 		("#", "dummy transition", ".dummy"),	
	ANY 		("&", "any signature", null);	

	
	private String name;
	private String long_name;
	private String gFormatName;
	
	

	private Signature(String name, String long_name, String gFormatName) {
		this.name = name;
		this.long_name = long_name;
		this.gFormatName = gFormatName;
	}
	
	

	public String toString() {
		return name;
	}

	public String toLongString() {
		return long_name;
	}

	public String getGFormatName() {
		return gFormatName;
	}



}
