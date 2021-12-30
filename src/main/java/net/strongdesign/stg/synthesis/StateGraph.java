

package net.strongdesign.stg.synthesis;

import net.strongdesign.desij.DesiJException;
import net.strongdesign.statesystem.*;
import net.strongdesign.statesystem.decorator.Cache;
import net.strongdesign.stg.*;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class StateGraph {

	public static EncodedReachabilityGraph getSG(STG stg) throws SynthesisException {
				
		StateSystem<Marking, SignalEdge> sys = 
					new Cache<Marking, SignalEdge>(
							STGAdapterFactory.getStateSystemAdapter(stg));
		
		
		//Mapping from marking to the signalstate relative to the initial marking
		Map<Marking, SignalState> finished 	= new HashMap<Marking, SignalState>();
		Map<Marking, SignalState> border 	= new HashMap<Marking, SignalState>();
		
		//start with the intial marking and
		border.put( sys.getInitialState(), new SignalState(stg.getSignals(), SignalValue.ZERO)); // ZERO = neutral value, NOT logical low
		
		while (! border.isEmpty()) {
			//get next element to deal with
			Marking marking = border.keySet().iterator().next();
			SignalState state = border.get(marking);
			border.remove(marking);
			
			//get activated events and next states
			for (SignalEdge edge : sys.getEvents(marking)) {
				SignalState nextState;
				try {
					nextState = state.applySignalEdge(edge);
				} catch (IllegalArgumentException e) {
					throw new SynthesisException("STG is not consistent", stg);
				}
				
				//calculate next marking
				Set<Marking> markings = sys.getNextStates(marking, edge);
				if (markings.size()>1) throw new SynthesisException("Dynamic autoconflict", stg);	
				if (markings.size()<1) throw new SynthesisException("Internal error", stg);	
				Marking nextMarking = markings.iterator().next();
				
				//is this marking known already to finished or border?
				SignalState knownState = border.get(nextMarking);
				if (knownState == null) 	
					knownState = finished.get(nextMarking);
				
				//Yes, check if the STG is consistent
				if (knownState != null) { 
					if ( !knownState.equals(nextState) )
						throw new SynthesisException("STG is not consistent", stg);
				}
				//No, add it to border
				else {
					border.put(nextMarking, nextState);					
				}								
			}
			finished.put(marking, state);
		}
		
		//At this point finished contains all reachable markings with their corresponding signal changes relative to the initial marking
		
		//calculate initial marking
		SignalState initialMarking = new SignalState(stg.getSignals(), SignalValue.UNKNOWN);
		for (SignalState state : finished.values()) {
			for (Integer signal : stg.getSignals()) {
				SignalValue change = state.get(signal);
				
				switch (initialMarking.get(signal)) {
				case UNKNOWN:
					switch (change) {
					case PLUS: initialMarking.put(signal, SignalValue.LOW); break;
					case MINUS:initialMarking.put(signal, SignalValue.HIGH); break;
					}
					break;
				case LOW:
					if (change == SignalValue.MINUS)
						throw new SynthesisException("STG is not consistent", stg);
					break;
				case HIGH:
					if (change == SignalValue.PLUS)
						throw new SynthesisException("STG is not consistent", stg);
					break;
				}
			}
		}
		
		Map<Marking, SignalState> encoding = new HashMap<Marking, SignalState>(finished.size());
		
		for (Marking reachMarking : finished.keySet()) {
			encoding.put(reachMarking, initialMarking.applyChangeVector(finished.get(reachMarking)));
		}
				
		return new EncodedReachabilityGraph(sys, encoding, stg);
	}
	
	
//	public static Map<String, Integer> getInitialState(STG stg) {
//		
//		
//		Map<String, Integer> state  = new HashMap<String, Integer>();
//		
//
//		Set<Marking> border = new HashSet<Marking>();
//		border.add(stg.getMarking());
//		
//		
//		return state;
//	}
	

	// Moved from DesiJ.java
	
	public static Pair<String,Integer> synthesiseSTGWithPetrify(String fileName) throws ParseException, STGException {

		try {
			File equations = File.createTempFile("desij", ".eq");
			File generatedSTG = File.createTempFile("desij", ".g");
			
//			monotonic cover implementation with generalized C elements
//			Process petrify = Runtime.getRuntime().exec(HelperApplications.getApplicationPath("petrify") + 
//					" -gcm -eqn " + equations + " -o " +generatedSTG + " " + fileName);
//			complex gate implementation			
			Process petrify = HelperApplications.startExternalTool(HelperApplications.PETRIFY,  
					" -cg -eqn " + 
					HelperApplications.SECTION_START+equations.getCanonicalPath()+HelperApplications.SECTION_END + 
					" -o " + 
					HelperApplications.SECTION_START+generatedSTG.getCanonicalPath()+HelperApplications.SECTION_END + 
					" " + 
					HelperApplications.SECTION_START+fileName+HelperApplications.SECTION_END);
			petrify.waitFor();
			petrify.getErrorStream().close();
			petrify.getInputStream().close();
			petrify.getOutputStream().close();
			
			
			int newSignals = 0;
			String streamSTG = FileSupport.loadFileFromDisk(generatedSTG.getCanonicalPath());
			if (streamSTG != null && !streamSTG.equals("")) {
				STG tmpSTG = STGFile.convertToSTG(streamSTG, false);
				if (tmpSTG != null)
					newSignals= tmpSTG.getSignals().size();
			}
					
			if (petrify.exitValue() == 0) {
				String streamEq = FileSupport.loadFileFromDisk(equations.getCanonicalPath());
				return Pair.getPair(streamEq, newSignals);
			}
			else
				return null;
		}
		catch (InterruptedException e) {
			throw new DesiJException("Error involving petrify:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error involving petrify: " + e.getMessage());
		}
	}
	
	/**
	 * Logic synthesis using petrify on a remote server
	 * @param fileName
	 * @param host
	 * @return
	 * @throws IOException 
	 * @throws STGException 
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 */
	public static Pair<String,Integer> synthesiseSTGWithPetrify(String fileName, 
			String host, String username, String password) throws ParseException, STGException {
		
		try {
			Pair<File, File> cStgEq = HelperApplications.doSynthesisViaSsh(fileName, host, username, password);
		
			int newSignals = 0;
			String streamSTG = FileSupport.loadFileFromDisk(cStgEq.a);
			if (streamSTG != null && !streamSTG.equals("")) {
				STG tmpSTG = STGFile.convertToSTG(streamSTG, false);
				if (tmpSTG != null)
					newSignals= tmpSTG.getSignals().size();
			}
			
			String streamEq = FileSupport.loadFileFromDisk(cStgEq.b);
			if (streamEq != null && !streamEq.equals("")) {
				return Pair.getPair(streamEq, newSignals);
			}
			else 
				return null;
		} catch (Exception e) {
			throw new DesiJException("Error involving remote logic synthesis: " + e.getMessage());
		}
		
	}
	
}
