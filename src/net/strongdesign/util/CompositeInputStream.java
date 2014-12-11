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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class CompositeInputStream extends InputStream {
	
	public static Logger log = Logger.getLogger(CompositeInputStream.class);

	List<InputStream> streams;
	Iterator<InputStream> fileIterator;	
	InputStream currentStream;
	
	List<String> names;
	Iterator<String> nameIterator;	
	String currentName;
	
	
	
	public CompositeInputStream(InputStream... inputStreams) {
		 streams = Arrays.asList(inputStreams);
		 fileIterator = streams.iterator();
		 
		 updateStream();			
	}
	
	public CompositeInputStream(List<InputStream> inputStreams, List<String> fileNames) {
		 streams = inputStreams;
		 fileIterator = streams.iterator();
		 
		 names = fileNames;
		 nameIterator = names.iterator();
		 
		 updateStream();			
	}

	public CompositeInputStream(File input, boolean recursive, String pattern) throws IOException {
		streams = new LinkedList<InputStream>();
		names = new LinkedList<String>();

		if (!input.isDirectory()) {
			streams.add(new FileInputStream(input));
			names.add(input.getCanonicalPath());
		}
		else {		
			descent(input, recursive, pattern);
		}

		fileIterator = streams.iterator();
		nameIterator = names.iterator();
						
		updateStream();
	}

	private void descent(File input, boolean recursive, String pattern) throws IOException {
		for (File f : input.listFiles()) {
			if (! f.getName().matches(pattern)) 
				continue;

			if (recursive && f.isDirectory())
				descent(f, recursive, pattern);

			streams.add(new FileInputStream(f));
			names.add(f.getCanonicalPath());
		}
	}

	
	private void updateStream() {
		if (fileIterator.hasNext()) {
			 currentStream = fileIterator.next();
			 currentName = nameIterator.next();
			 log.info(Log.print("Switched to stream :0", currentName));
		}
		 else {
			 currentStream = null;
			 log.info(Log.print("Finished last stream"));
		 }
	}
	
	@Override
	public int read() throws IOException {
		if (currentStream == null)
			return -1;
		
		int read = currentStream.read();
		if (read == -1) {
			updateStream();
			return read();			
		} 
		else {
			return read;	
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		
		for (InputStream is : streams) {
			is.close();
		}
	}

}
