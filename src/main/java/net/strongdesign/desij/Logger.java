

/*
 * Created on 25.12.2004
 *
 */
package net.strongdesign.desij;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Slightly enhanced version of {@link java.io.FileWriter} for convenience
 * of logging
 *  
 * <p><b>History:</b><br>

 *  * 25.12.2004: Created<br>
 * 
 * <p>
 * @author Mark Schaefer 
 */
public class Logger  
{
    private FileWriter fw;
    private long start;
    
    public Logger(String s) throws IOException {
        fw = new FileWriter(s);
        start = System.currentTimeMillis();
    }
    
    public void write(String mes) throws IOException {    	
	    fw.append(String.format("%1$07d : %2$s \n", (System.currentTimeMillis()-start), mes ));
	    fw.flush();
    }
    
    public void append(String s) throws IOException{
        fw.append(s);
        fw.flush();
    }
    
    public void close() throws IOException {
        fw.close();
    }
    
    public void finalize() throws IOException {
        if (fw!=null)
            fw.close();
    }
}
