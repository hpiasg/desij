

package net.strongdesign.statesystem;

/**
 * A semantic subclass of {@link Exception}.
 * 
 * <p>
 * <b>History: </b> <br>
 * 24.01.2005: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�er
 */
public class StateSystemException extends Exception  {

	private static final long serialVersionUID = 260513272108831970L;

	public StateSystemException(String mes) {
        super(mes);
    }
    
    
}
