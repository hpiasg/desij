package net.strongdesign.balsa.breezefile;

import java.util.LinkedList;

import net.strongdesign.balsa.components.HSComponent;
import net.strongdesign.stg.STG;

public class ComponentSTGFactory {
	
	static public STG createSTGComponent(String compName, LinkedList<Object> parameters, LinkedList<LinkedList<Integer> > channels) {
		STG ret = new STG();
		return ret;
	}

}
