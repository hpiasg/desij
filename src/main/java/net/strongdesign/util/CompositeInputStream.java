

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
