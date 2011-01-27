/*
 * Created on 26.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.stg.traversal;
import net.strongdesign.stg.*;

/**
 * @author Mark Sch�fer
 *
 * Interface for Classes implementing an arbitrary operation
 * on some Objects
 *
 */
public interface Operation<T> {
	public void operation(T o) throws STGException;
}

