/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

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
