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

package net.strongdesign.stg.synthesis;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;
import net.strongdesign.util.StreamGobbler;

public class Unfolding {
	
	// Moved from DesiJ.java
	
	public static Pair<String,Integer> synthesiseSTGWithPunfMpsat(String fileName) throws ParseException, STGException {

		try {
			//where the CSC violating traces are saved
			File equations = File.createTempFile("desij", ".eq");
			File mpsatMCI = new File("mpsat.mci");
			mpsatMCI.delete();
			
			Process punf = HelperApplications.startExternalTool(HelperApplications.PUNF,  
					" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
					HelperApplications.SECTION_START+fileName+".mci"+HelperApplications.SECTION_END + 
					" " +
					HelperApplications.SECTION_START+fileName+HelperApplications.SECTION_END );
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(punf.getInputStream(), "punf", System.out);
				StreamGobbler.createGobbler(punf.getErrorStream(), "punf", System.err);
			}
			punf.waitFor();
			punf.getErrorStream().close();
			punf.getInputStream().close();
			punf.getOutputStream().close();
			
			// old parameter: " -R -! -f -p0 "
			Process mpsat1 = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -R -f -@ -p0 -cl " +
					HelperApplications.SECTION_START+fileName+".mci"+HelperApplications.SECTION_END);
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(mpsat1.getInputStream(), "mpsat", System.out);
				StreamGobbler.createGobbler(mpsat1.getErrorStream(), "mpsat", System.err);
			}
			mpsat1.waitFor();			
			mpsat1.getErrorStream().close();
			mpsat1.getInputStream().close();
			mpsat1.getOutputStream().close();
						
			
			int newSignals = 0;
			File mp = new File("mpsat.g");
			if (mp.exists()) {
				newSignals= STGFile.convertToSTG(
					FileSupport.loadFileFromDisk(mp.getName()),
					false).getSignals().size();
			}
			else {
				
			}
			// old paramters: " -E  mpsat.mci "
			Process mpsat = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -E -! -@ mpsat.mci " + 
					HelperApplications.SECTION_START+equations.getCanonicalPath()+HelperApplications.SECTION_END);
			
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(mpsat.getInputStream(), "mpsat", System.out);
				StreamGobbler.createGobbler(mpsat.getErrorStream(), "mpsat", System.err);
			}
			mpsat.waitFor();
			mpsat.getErrorStream().close();
			mpsat.getInputStream().close();
			mpsat.getOutputStream().close();
			
			
			
			new File(fileName + ".mci").delete();
			new File("mpsat.g").delete();
			mpsatMCI.delete();
			
			if (mpsat.exitValue() == 0)
				return Pair.getPair(
						FileSupport.loadFileFromDisk(equations.getCanonicalPath()),
						newSignals);
			else
				return null;
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving punf/mpsat:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error involving punf/mpsat: " + e.getMessage());
		}
		
		
	}
	
	
	
	
	// Copied from CSCAwareDecomposition
	
	/**
	 * Extract the CSC violation traces from an STG. Uses punf and mpsat, see above.
	 * @param stg
	 * @return
	 * @throws IOException
	 */
	public static Set<Pair<List<SignalEdge>, List<SignalEdge>>> getCSCViolationTraces(STG stg) throws IOException {
		//where the STG is saved
		File tmpSTG = File.createTempFile("desij", ".g");

		//where the unfolding is saved
		File tmpUNF = File.createTempFile("desij", ".unf");

		//where the CSC violating traces are saved
		File tmpCONF = File.createTempFile("desij", ".conf");

		//save the STG, generate the unfolding and extract CSC violating traces
		FileSupport.saveToDisk(STGFile.convertToG(stg, false), tmpSTG.getCanonicalPath());

		try {
			
			Process punf = HelperApplications.startExternalTool(HelperApplications.PUNF, 
					" -m"+HelperApplications.SECTION_START+"="+HelperApplications.SECTION_END +
					HelperApplications.SECTION_START+tmpUNF.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpSTG.getCanonicalPath()+HelperApplications.SECTION_END );
			
			OutputStream os = null;
			OutputStream es = null;
			
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				os = System.out;
				es = System.err;
			}
			
			StreamGobbler.createGobbler(punf.getInputStream(), "punf", os);
			StreamGobbler.createGobbler(punf.getErrorStream(), "punf-er", es);
			
			punf.waitFor();
			punf.getErrorStream().close();
			punf.getInputStream().close();
			punf.getOutputStream().close();
			

			Process mpsat = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -C -a " +
					HelperApplications.SECTION_START+tmpUNF.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+tmpCONF.getCanonicalPath()+HelperApplications.SECTION_END );
			
			
			StreamGobbler.createGobbler(mpsat.getInputStream(), "mpsat", os);
			StreamGobbler.createGobbler(mpsat.getErrorStream(), "mpsat-er", es);
			
			mpsat.waitFor();
			mpsat.getErrorStream().close();
			mpsat.getInputStream().close();
			mpsat.getOutputStream().close();
			
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving punf/mpsat.");
		}

		//parse the conflict traces
		String conflicts = FileSupport.loadFileFromDisk(tmpCONF.getCanonicalPath());
		

		//no CSC conflict
		if (conflicts.startsWith("NO"))
			return Collections.emptySet();    	

		//the final result with all traces
		Set<Pair<List<SignalEdge>, List<SignalEdge>>> result = new HashSet<Pair<List<SignalEdge>, List<SignalEdge>>>();

		//help variables containing the current traces
		List<SignalEdge> trace0 = new LinkedList<SignalEdge>();
		List<SignalEdge> trace1 = new LinkedList<SignalEdge>();


		//split the line and convert the entries
		int i=0;
		for (String line : conflicts.split("\n")) {
			if (line.endsWith("\r")) line = line.substring(0, line.length()-1); // cut "\r" for Windows machines
			if (line.startsWith("YES") || line.startsWith("_SEQ"))
				continue;

			if (i==0) {
				i=1;
				trace0 = getTrace(stg, line);
			} 
			else {
				i=0;
				trace1 = getTrace(stg, line);
				result.add(new Pair<List<SignalEdge>, List<SignalEdge>> (trace0, trace1));
			}
		}
		return result;
	}

	/**
	 * Converts a single trace given as String delivered by mpsat into an appropriate representation. 
	 * @param line
	 * @return
	 */
	private static List<SignalEdge> getTrace(STG stg, String line) {
		List<SignalEdge> result = new LinkedList<SignalEdge>();

		for (String edge : line.split(",")) {
			if (edge.startsWith("i") || edge.startsWith("I")) {
			} 
			else if (edge.startsWith("o") || edge.startsWith("O") ) {
			}
			else if (edge.startsWith("d") || edge.startsWith("D") ) {
			}
			else if (edge.matches("[ \t]*"))
				continue;
			else
				throw new DesiJException("Unknown signature in "+line);

			String sig = edge.replaceAll(".*\\.|/.*","");

			EdgeDirection direction;
			if (sig.endsWith("+"))
				direction = EdgeDirection.UP;
			else if (sig.endsWith("-"))
				direction = EdgeDirection.DOWN;
			else
				throw new DesiJException("Unknown direction in "+line);

			String signalName = sig.replaceAll("\\+|-|_" ,  "");
			Integer signal = Integer.parseInt(signalName);
			result.add(new SignalEdge(signal, direction));
		}

		return result;
	}
	

}
