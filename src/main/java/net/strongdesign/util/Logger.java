

package net.strongdesign.util;


import java.io.*;
import java.util.Date;

/**
 * Slightly enhanced version of {@link java.io.FileWriter} for convenience
 * of logging
 *  
 * <p><b>History:</b><br>

 *  * 25.12.2004: Created<br>
 * 
 * <p>
 * @author Mark Sch�er 
 */
public class Logger  
{
    protected FileWriter fw;
    public Logger(String s) throws IOException {
        fw = new FileWriter(s);       
    }
    
    public void write(String mes) throws IOException {
	    fw.append("" + new Date() + mes + "\n");
    }
    
    public void append(String s) throws IOException{
        fw.append(s);
    }
    
    public void close() throws IOException {
        fw.close();
    }
    
    public void finalize() throws IOException {
        if (fw!=null)
            fw.close();
    }
}
