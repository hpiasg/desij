/**
 * Copyright 200-2013 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public class StreamGobbler extends Thread
{
	private InputStream is;
	private String type;
	private PrintStream ops;

    protected StreamGobbler(InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        
        this.ops = null;
        
        if (redirect!=null)
        	this.ops = new PrintStream(redirect);
    }
  
    
    public static StreamGobbler createGobbler(InputStream is, String type, OutputStream redirect) {
    	StreamGobbler newGobbler = new StreamGobbler(is, type, redirect);
    	newGobbler.start();
    	return newGobbler;
    }
    
    public void run()
    {
    	try
    	{    		
    		InputStreamReader isr = new InputStreamReader(is);
    		BufferedReader br = new BufferedReader(isr);
    		String line=null;
    		
    		while ( (line = br.readLine()) != null) {
    			if (ops!=null) {
        			if (type==null||type.equals("")) {
            			ops.println(line);    
        			} else {
            			ops.println(type + "> " + line);    
        			}
    			}
    		}
    		
    	} catch (IOException ioe) {
    		ioe.printStackTrace();  
    	}
    }
}
