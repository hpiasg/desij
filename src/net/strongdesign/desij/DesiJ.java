/**
 * Copyright 2004-2013 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

/*
 * Created on 25.09.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.strongdesign.desij;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.strongdesign.balsa.breezefile.ComponentSTGFactory;
import net.strongdesign.desij.decomposition.BasicDecomposition;
import net.strongdesign.desij.decomposition.BreezeDecomposition;
import net.strongdesign.desij.decomposition.LazyDecompositionMultiSignal;
import net.strongdesign.desij.decomposition.LazyDecompositionSingleSignal;
import net.strongdesign.desij.decomposition.avoidconflicts.ComponentAnalyser;
import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.tree.CscAwareDecomposition;
import net.strongdesign.desij.decomposition.tree.IrrCscAwareDecomposition;
import net.strongdesign.desij.decomposition.tree.TreeDecomposition;
import net.strongdesign.desij.gui.STGDotLayout;
import net.strongdesign.desij.gui.STGEditorFrame;
import net.strongdesign.desij.gui.STGTreeLayout;
import net.strongdesign.desij.net.DecompositionStatistics;
import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.statesystem.decorator.Cache;
import net.strongdesign.statesystem.decorator.StateEnumerator;
import net.strongdesign.statesystem.simulation.RelationElement;
import net.strongdesign.statesystem.simulation.RelationPropagator.PropagationException;
import net.strongdesign.statesystem.simulation.Simulation;
import net.strongdesign.stg.Marking;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGAdapterFactory;
import net.strongdesign.stg.STGCreator;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.export.SVGExport;
import net.strongdesign.stg.parser.ParseException;
import net.strongdesign.stg.parser.TokenMgrError;
import net.strongdesign.stg.solvers.RedundantPlaceSolverLP;
import net.strongdesign.stg.solvers.RedundantPlaceStatistics;
import net.strongdesign.stg.synthesis.StateGraph;
import net.strongdesign.stg.synthesis.Unfolding;
import net.strongdesign.stg.traversal.CollectorFactory;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.stg.traversal.STGBisimulationPropagator;
import net.strongdesign.stg.traversal.STGParallelComposition;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.Pair;
import net.strongdesign.util.ParsingException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

/**
 * @author M. Schaefer
 * @author D. Wist
 * @author S. Golubcovs
 */
public class DesiJ {

	/**
	 * The global logfile
	 */
	public static org.apache.log4j.Logger logFile = org.apache.log4j.Logger.getRootLogger();
	public static net.strongdesign.desij.Logger smallLogFile;
	

	/**
	 * The singleton statistics server.
	 */
	public static DecompositionStatistics stats = null;

	public static String name = "//localhost/DesiJStat";
	public static boolean server;


	public static void main(String[] args) {
		
		int exitCode = desiJMain(args);

		if (!CLW.instance.GUI.isEnabled())
			System.exit(exitCode);
	}

	
	
	
	/**
	 * The real main function of desiJ - abstracted from calling System.exit()
	 * It is suitable for calling desiJ's functionality from third party Java tools 
	 * as library call of DesiJMain(...), e.g. the Workcraft tool 
	 * @param args -- commandLine arguments for DesiJ (must be parsed)
	 * @return errorCode of the system execution - 0 means everything works fine
	 */
	public static int desiJMain(String[] args) {
		
		int exitCode = 0;

		try {
			CLW.instance = new CLW(args);
			if (args.length == 0) return exitCode;

			if (CLW.instance.PARAMETER.isEnabled())
				System.out.println(CLW.instance.showValues());


			System.setSecurityManager(null);

			server = CLW.instance.OPERATION.getValue().equals("decomposition") && CLW.instance.START_STAT_SERVER.isEnabled();
			try {
				stats = new DecompositionStatistics();
				if (server) {
					Naming.rebind(name, stats);
				}
			} catch (Exception e) {
				e.printStackTrace();
				exitCode = 1;
				return exitCode;
			}


			// prepare logging
			
			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
			logFile.setLevel( Level.ALL ); // configure the logging level
			if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
				// logFile = new Logger(CLW.instance.LOGFILE.getValue());
				smallLogFile = new net.strongdesign.desij.Logger(CLW.instance.LOGFILE.getValue()+".small");
				PatternLayout layout = new PatternLayout("%-5p %c: %m%n");
				FileAppender fileAppender = new FileAppender(layout, CLW.instance.LOGFILE.getValue(), false);
				logFile.addAppender(fileAppender);
				
				String ar="";
				for (String s : args) {
					ar += s + " ";
				}
				logFile.info("Command line: " + ar);
				logFile.debug(CLW.instance.showValues());
			}


			if (!CLW.instance.SILENT.isEnabled())
				System.out.println(
						"DesiJ - An STG Decomposer dedicated to the Synthesis of SI Circuits\n" + 
						"(c) 2004-2007 by Mark Schaefer, University of Augsburg\n"+
						"(c) 2008-2012 by Dominic Wist, Hasso-Plattner-Institut\n"+
						"(c) 2012-2013 by Stanislavs Golubcovs, University of Augsburg, stanislavs.golubcovs@informatik.uni-augsburg.de\n"+
				"DesiJ comes as is with no warranty. Use at your own risk.");

			Date start = new Date();

			if (CLW.instance.HELP.isEnabled())
				CLW.instance.showHelp();
			else if (CLW.instance.GUI.isEnabled()) 
				interactiveDecomposition();
			else if (CLW.instance.CL.isEnabled()) 
				commandLine();
			else if (CLW.instance.OPERATION.getValue().equals("breeze"))
				breeze();
			else if (CLW.instance.OPERATION.getValue().equals("decompose")) 
				decompose();
			else if (CLW.instance.OPERATION.getValue().equals("rg")) 
				rg();
			else if (CLW.instance.OPERATION.getValue().equals("convert")) 
				convert();
			else if (CLW.instance.OPERATION.getValue().equals("clone")) 
				cloneSTG();
			else if (CLW.instance.OPERATION.getValue().equals("show")) 
				show();
			else if (CLW.instance.OPERATION.getValue().equals("killdummies")) 
				killdummies();
			else if (CLW.instance.OPERATION.getValue().equals("killdummiesrelaxed")) 
				killDummiesRelaxed();
			else if (CLW.instance.OPERATION.getValue().equals("reduceint"))
				reduceinternals();
			else if (CLW.instance.OPERATION.getValue().equals("info")) 
				info(0);
			else if (CLW.instance.OPERATION.getValue().equals("info1"))  
				info(1);
			else if (CLW.instance.OPERATION.getValue().equals("info2"))  
				info(2);
			else if (CLW.instance.OPERATION.getValue().equals("check"))  
				checkCorrectness(false);
			else if (CLW.instance.OPERATION.getValue().equals("bisim"))  
				checkCorrectness(true);
			else if (CLW.instance.OPERATION.getValue().equals("reddel"))  
				removeRedundantPlaces();
			else if (CLW.instance.OPERATION.getValue().equals("create"))  
				createSTG();


		if (! CLW.instance.SILENT.isEnabled()) {
			System.out.println("Overall time used " + (new Date().getTime() - start.getTime())/1000.0 + " s" );
		}


		}
		catch (FileNotFoundException e) {
			System.err.println("Could not find file: " + e.getMessage());
			exitCode = 255;
		}
		catch (IOException e) {
			System.err.println("IO-Error: " + e.getMessage());
			e.printStackTrace();
			exitCode = 255;
		}
		catch (InterruptedException e) {
			System.err.println("Error while running dot: " + e.getMessage());
			exitCode = 255;
		}
		catch (STGException e) {
			System.err.println("Error during STG operation: " + e.getMessage());
			exitCode = 255;
		}
		catch (ParsingException e) {
			System.err.println("Invalid command line: " + e.getMessage());
			exitCode = 255;			
		}
		catch (ParseException e) {
			System.err.println("Could not parse input file: " + e.getMessage());
			exitCode = 255;			
		}
		catch (TokenMgrError e) {
			System.err.println("Could not parse input file: " + e.getMessage());
			exitCode = 255;			
		}
		catch (DesiJException e) {
			System.err.println(e.getMessage());
			exitCode = 255;			
		}

		catch (Exception e) 
		{
			System.err.println(" FATAL ERROR: \nPlease send this error message to the above email adress.");
			e.printStackTrace();
			exitCode = 255;
		}
		finally {
			try {
				if (server) Naming.unbind(name);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

			//close logfile
			if (args.length > 0 && CLW.instance!=null) {
				if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
					try {
						// logFile.close();
						if (smallLogFile!=null) smallLogFile.close();
					} catch (IOException e) {
						System.err.println("IO-Error: " + e.getMessage());
						exitCode = 255;
					}
				}

				/*if (!CLW.instance.GUI.isEnabled())
					return exitCode;*/

			}
		}
		return exitCode;
	}

	private static void breeze() throws Exception {
		
		Map<String, STG> bmap = ComponentSTGFactory.breeze2stg();
		
		for (Entry<String,STG> e: bmap.entrySet()) {
			String fname;
			
			if (CLW.instance.OUTFILE.equals("")) {
				fname = e.getKey()+".g";
			} else {
				
				fname=CLW.instance.OUTFILE.getValue();
				
				if (bmap.size()>1) {
					fname = e.getKey()+fname;
				}
			}
			
			FileSupport.saveToDisk(STGFile.convertToG(e.getValue()),fname);
		}
	}


	private static void createSTG() throws STGException, IOException {
		STG stg=null;
		if (CLW.instance.MODEL.getValue().startsWith("Brz")) {
			stg = ComponentSTGFactory.createSTGComponent(CLW.instance.MODEL.getValue(), 0, null, null);
		} else {
			stg = STGCreator.getPredefinedSTG(CLW.instance.MODEL.getValue());
		}

		if (CLW.instance.OUTFILE.getValue().equals("")) {
			System.out.print(STGFile.convertToG(stg));
		} else {
			FileSupport.saveToDisk(STGFile.convertToG(stg),CLW.instance.OUTFILE.getValue() );
		}
	}

	public static STG loadSTG(String fileName, boolean withCoordinates) throws ParseException, IOException, STGException {
		STG stg;
		try {
			stg = STGFile.convertToSTG(FileSupport.loadFileFromDisk(fileName+".g"), withCoordinates);
		}
		catch (FileNotFoundException e) {
			stg = STGFile.convertToSTG(FileSupport.loadFileFromDisk(fileName), withCoordinates);
		}

		return stg;
	}



	private static void commandLine() throws IOException,  ParseException, STGException {
		String fileName = CLW.instance.getOtherParameters().iterator().next();
		STG stg = loadSTG(fileName, true);


		System.out.println("\nOpen STG command line for "+fileName);
		new STGCommandLine(stg, System.in, System.out).start();

	}


	
	private static void killSTGDummies(STG stg, String fileName, boolean relaxed) throws IOException, STGException, ParseException {
		
		RedundantPlaceStatistics.reset();
		
		int dum1 = stg.getNumberOfDummies();
		int pl1 = stg.getNumberOfPlaces();

		STGUtil.removeDummiesBreeze(stg, relaxed, false);
		
		int dum2 = stg.getNumberOfDummies();
		System.out.println(fileName+": Dummies before: "+dum1+" after:"+dum2);
		
		int pl2 = stg.getNumberOfPlaces();
		
		System.out.println(fileName+": Places before: "+pl1+" after:"+pl2);
		RedundantPlaceStatistics.reportStatistics(fileName);
		
		String name = CLW.instance.OUTFILE.getValue().equals("")?"":CLW.instance.OUTFILE.getValue();
		
		if (name!=null&&!name.equals(""))
			FileSupport.saveToDisk(STGFile.convertToG(stg), name);
		
	}
	
	
	private static void killdummies() throws Exception {
		
//		for (String fileName : CLW.instance.getOtherParameters()) {
//
//			STG stg = loadSTG(fileName, true);
//
//
//			int dum1 = stg.getNumberOfDummies();
//			int pl1 = stg.getNumberOfPlaces();
//
//			STGUtil.removeDummies(stg);
//			int dum2 = stg.getNumberOfDummies();
//			System.out.println(fileName+": Dummies before: "+dum1+" after:"+dum2);
//			
//			int pl2 = stg.getNumberOfPlaces();
//			System.out.println(fileName+": Places before: "+pl1+" after:"+pl2);
//			
//			String name = CLW.instance.OUTFILE.getValue().equals("")?"":CLW.instance.OUTFILE.getValue();
//			reportStatistics(fileName);
//			
//			if (!name.equals("")) {
//				FileSupport.saveToDisk(STGFile.convertToG(stg), name);
//			}
//		}
		
		for (String fileName : CLW.instance.getOtherParameters()) {
			
			STG stg;
			if (fileName.endsWith("breeze")) {
				
				for (Entry<String,STG> e: ComponentSTGFactory.breeze2stg().entrySet()) {
					String fname = e.getKey()+".g";
					stg = e.getValue();
					killSTGDummies(stg, fname, false);
				}
				
			} else {
				
				stg = loadSTG(fileName, true);
				killSTGDummies(stg, fileName, false);
			}

		}
		
	}
	
	/**
	 * contracts dummy signals while also uses relaxation to achieve more contractions
	 * @throws Exception 
	 */
	private static void killDummiesRelaxed() throws Exception {
		for (String fileName : CLW.instance.getOtherParameters()) {
			
			STG stg;
			if (fileName.endsWith("breeze")) {
				
				for (Entry<String,STG> e: ComponentSTGFactory.breeze2stg().entrySet()) {
					String fname = e.getKey()+".g";
					stg = e.getValue();
					killSTGDummies(stg, fname, true);
				}
				
			} else {
				
				stg = loadSTG(fileName, true);
				killSTGDummies(stg, fileName, true);
			}

		}
	}
	
	private static void reduceinternals() throws ParseException, IOException, STGException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			STG stg = loadSTG(fileName, true);
			
			int n = STGUtil.removeInternalSignals(stg);
			
			if (n == -1) {
				throw new DesiJException("There was an error during the LP removal procedure.");
			}
			
			System.out.println(fileName + ": " + n + " internal signals removed");
			String name = CLW.instance.OUTFILE.getValue().equals("") ? 	fileName : CLW.instance.OUTFILE.getValue();
			FileSupport.saveToDisk(STGFile.convertToG(stg), name);
		}
	}

	private static void checkCorrectness(boolean printResult) 
	throws FileNotFoundException, STGException, IOException, ParseException {		

		Collection<String> files = CLW.instance.getOtherParameters();
		LinkedList<STG> stgs = new LinkedList<STG>();

		for (String fileName : files) {  
			STG stg = STGFile.convertToSTG(FileSupport.loadFileFromDisk(fileName), false);
			int n = STGUtil.removeDummies(stg);
			if (!stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)).isEmpty())
				throw new STGException("Cannot contract all dummy transitions in input file " + fileName);
			if (n>0)
				System.out.println("Contracted " + n + " dummy transitions in input file " + fileName);
			stgs.add(stg);
		}

		STG stg = stgs.poll();


		Set<String> hid = new HashSet<String>();
		for (String s : CLW.instance.HIDDEN_SIGNALS.getValue().split(",")) {
			if (s.length()>0) {
				hid.add(s);
			}
		}

		STGParallelComposition parallelComposition = new STGParallelComposition(stg.getSignalNumbers(), stgs, hid);		



		//check signature
		Map<Integer, Signature> signatureSpec = stg.getSignature();
		Map<Integer, Signature> signatureImpl = parallelComposition.getSignature();
		boolean signaturesMatch = true;

		for (Integer signal : signatureImpl.keySet()) {
			Signature sigImpl = signatureImpl.get(signal);
			if (sigImpl == Signature.INTERNAL) continue;

			Signature sigSpec = signatureSpec.get(signal);
			if (sigSpec == null) {
				System.out.println("Unknown signal in implementation: " + parallelComposition.getSignalName(signal));
				signaturesMatch = false;
			}
			else if (sigSpec != sigImpl) {
				System.out.println("Signatures do not match for signal " + parallelComposition.getSignalName(signal));
				signaturesMatch = false;
			}
		}

		if (!signaturesMatch) return;


		System.out.println("Building STG-bisimulation ...");



		if (printResult) {
			StateSystem<Marking, SignalEdge> spec = 
				new Cache<Marking,SignalEdge>(
						STGAdapterFactory.getStateSystemAdapter(stg) );

			StateSystem<List<Marking>,SignalEdge> pc = 	
				new Cache<List<Marking>,SignalEdge>(parallelComposition);

			try {
				STGBisimulationPropagator<Marking, List<Marking>> bisimulationPropagator = new STGBisimulationPropagator<Marking,List<Marking>>();
				bisimulationPropagator.setSignatures(stg.getSignature(), signatureImpl);

				Set<RelationElement<Marking, List<Marking>>> result = Simulation.findSimulation(spec, pc, bisimulationPropagator);
				if (result==null)
					System.out.println("Incorrect decomposition"); //CHECK does findSimulation terminate normally in this case??
				else {
					for (RelationElement<Marking, List<Marking>> relEl : result)
						System.out.println(relEl);
				}
			} catch (PropagationException e) {
				System.out.println("Incorrect decomposition");
			}
		}
		else {
			StateSystem<Integer, SignalEdge> spec = 
				new Cache<Integer,SignalEdge>(
						new StateEnumerator<Marking,SignalEdge>(
								STGAdapterFactory.getStateSystemAdapter(stg) ));

			StateSystem<Integer,SignalEdge> pc = 	
				new Cache<Integer,SignalEdge>(new StateEnumerator<List<Marking>,SignalEdge>(parallelComposition ));

			try {
				STGBisimulationPropagator<Integer, Integer> bisimulationPropagator = new STGBisimulationPropagator<Integer,Integer>();
				bisimulationPropagator.setSignatures(stg.getSignature(), signatureImpl);

				Set<RelationElement<Integer, Integer>> result = Simulation.findSimulation(spec, pc, bisimulationPropagator);
				if (result==null)
					System.out.println("Incorrect decomposition"); //CHECK does findSimulation terminate normally in this case??
				else
					System.out.println("Correct decomposition");
			} catch (PropagationException e) {
				System.out.println("Incorrect decomposition");
			}
		}
	}


	private static void show() 
	throws IOException, FileNotFoundException, ParseException, InterruptedException, STGException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			STG stg = loadSTG(fileName, false);

			stg.showPS();

		}
	}

	private static void removeRedundantPlaces() throws ParseException, IOException, STGException 
	{
		RedundantPlaceStatistics.reset();
		System.out.println("Removing redundant places ");
		for (String fileName : CLW.instance.getOtherParameters()) {

			STG stg = loadSTG(fileName, true);

			int pl1 = stg.getNumberOfPlaces();
			int dum1 = stg.getNumberOfDummies();
			
			STGUtil.removeRedundantPlaces(stg);
			
			int pl2 = stg.getNumberOfPlaces();
			int dum2 = stg.getNumberOfDummies();
			
			System.out.println(fileName+": Dummies before: "+dum1+" after:"+dum2);
			System.out.println(fileName+": Places before: "+pl1+" after:"+pl2);
			
			RedundantPlaceStatistics.reportStatistics(fileName);

			FileSupport.saveToDisk(STGFile.convertToG(stg), fileName + ".red.g");
			System.out.println("done");
			
		}
	}



	private static void info(int verbose) 
	throws IOException, FileNotFoundException, STGException, ParseException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			//load STG
			STG stg = loadSTG(fileName, false);

			System.out.println("\nFilename: " + fileName);

			int nroArcs = 0;
			for (Node node : stg.getNodes()) {
				nroArcs += node.getChildren().size();
			}


			System.out.println("#Places / #Transitions / #Arcs: " + stg.getNumberOfPlaces() +" / " + stg.getNumberOfTransitions() + " / " + nroArcs);

			int nroInput=0, nroOutput=0, nroInternal=0, nroDummy=0; 

			for (Integer signal : stg.getSignals()) {
				switch (stg.getSignature(signal)) {
				case INPUT: ++nroInput; break;
				case OUTPUT: ++nroOutput; break;
				case INTERNAL: ++nroInternal; break;
				case DUMMY: ++nroDummy; break;
				}
			}


			System.out.println("#Input / #Output / #Internal / #Dummy: " + nroInput + " / " + nroOutput + " / " + nroInternal + " / " + nroDummy);


//			change internals to outputs

			stg.changeSignature(Signature.INTERNAL, Signature.OUTPUT);

			//generate partition
			Partition partition;
			if (CLW.instance.PARTITION.getValue().equals("finest"))
				partition = Partition.getFinestPartition(stg, null); 
			else
				partition = Partition.fromString(stg, CLW.instance.PARTITION.getValue());

			System.out.println("#Components: " + partition.getPartition().size());



			if (verbose == 1) {
				System.out.println("#Markings / States: " + STGUtil.sizeOfReachabilityGraph(stg));
			}

			if (verbose == 2) {
				STG rg = STGUtil.generateReachabilityGraph(stg);

				System.out.println("#Markings / States: " + rg.getNumberOfPlaces());


				Set<Collection<Integer>> dynamicConflicts = rg.collectUniqueCollectionFromPlaces(
						ConditionFactory.ALL_PLACES, 
						CollectorFactory.getAutoConflictCollector());

				System.out.println("#Dynamic conflicts: " + dynamicConflicts);
				System.out.println("#Nro Dynamic conflicts: " + dynamicConflicts.size());
			}

		}
	}

	private static void cloneSTG() 
	throws IOException, FileNotFoundException, ParseException, STGException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			//load STG

			System.out.println("Cloning "+fileName + " to "+CLW.instance.OUTFILE.getValue());

			STG stg = loadSTG(fileName, true);

			String l = CLW.instance.LABEL.getValue();
			if (l.equals(""))
				l=CLW.instance.OUTFILE.getValue();

			STG b = stg.clone();

			FileSupport.saveToDisk(STGFile.convertToG(b), CLW.instance.OUTFILE.getValue() );
		}
	}

	private static void interactiveDecomposition() throws FileNotFoundException, IOException, ParsingException, ParseException, STGException  {   
		STGEditorFrame frame = new STGEditorFrame();
		//load breeze
		if (CLW.instance.OPERATION.getValue().equals("breeze")) {
			
			try {
				
				for (Entry<String,STG> e: ComponentSTGFactory.breeze2stg().entrySet()) {
					String fname = e.getKey();
					STG stg = e.getValue();
					frame.addSTG(stg, fname);
				}
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		} else {
			//load STG
			for (String fileName : CLW.instance.getOtherParameters()) {

				File f= new File(fileName);
				if (! f.exists() ) {
					f = new File(fileName+".g");
					if (! f.exists())
						throw new FileNotFoundException(fileName);          
				}       
				
//				String file = FileSupport.loadFileFromDisk(f.getAbsolutePath());
//				STG stg = STGEditorFile.convertToSTG(file, true);
//				STGEditorCoordinates coordinates = STGEditorFile.convertToCoordinates(file);
				
				frame.open(fileName);
				frame.getFileChooser().setCurrentDirectory(f);
			}
		}
		
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() { // callback for closing the GUI
			public void windowClosed(WindowEvent e) {
				System.exit(0); // interactive GUI-based decomposition should never be called from an external tool, eg. WorkCraft
			}
		});
	}


	private static void convert() 
	throws IOException, InterruptedException, FileNotFoundException, STGException, ParseException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			//load STG

			System.out.println("Converting "+fileName + " to "+CLW.instance.OUTFILE.getValue() + " as "+ CLW.instance.FORMAT.getValue());

			STG stg = loadSTG(fileName, true);

			String l = CLW.instance.LABEL.getValue();
			if (l.equals("")) {
				l=CLW.instance.OUTFILE.getValue();
			}
			if (CLW.instance.FORMAT.getValue().equals("dot")) {
				Set<Node> emptySet = Collections.emptySet();
				FileSupport.saveToDisk(STGFile.convertToDot(stg, emptySet, l),  CLW.instance.OUTFILE.getValue());
			}
			else if (CLW.instance.FORMAT.getValue().equals("ps")) {
				Set<Node> emptySet = Collections.emptySet();
				FileSupport.saveToDisk(STGFile.convertToDot(stg, emptySet, l), CLW.instance.OUTFILE.getValue()+".tmp");

				
				HelperApplications.startExternalTool(HelperApplications.DOT, 
						" -Tps "+
						HelperApplications.SECTION_START+CLW.instance.OUTFILE.getValue()+".tmp"+HelperApplications.SECTION_END+
						" -o " +
						HelperApplications.SECTION_START+CLW.instance.OUTFILE.getValue()+HelperApplications.SECTION_END ).waitFor();
				File fileToDelete = new File(CLW.instance.OUTFILE.getValue()+".tmp");
				if (fileToDelete.exists() && fileToDelete.isFile()) fileToDelete.delete();
				//Runtime.getRuntime().exec(HelperApplications.getApplicationPath("dot") + " -Tps "+CLW.instance.OUTFILE.getValue()+".tmp -o "+CLW.instance.OUTFILE.getValue()).waitFor();
				//Runtime.getRuntime().exec("rm " + CLW.instance.OUTFILE.getValue()+".tmp").waitFor();
			}
			else if (CLW.instance.FORMAT.getValue().equals("svg")) {
				if (stg.getCoordinates().size()==0) {
					STGTreeLayout.doLayout(stg, true);
					if (stg.getPlaces().size()<1000) {
						STGDotLayout.doLayout(stg);
					}
				}
				String svg = SVGExport.export(stg);
				FileSupport.saveToDisk(svg,  CLW.instance.OUTFILE.getValue());
			}
			else if (CLW.instance.FORMAT.getValue().equals("g")) {
				FileSupport.saveToDisk(STGFile.convertToG(stg), CLW.instance.OUTFILE.getValue());
			}
			else 
				throw  new STGException("Unknown file format: "+CLW.instance.FORMAT.getValue().equals("g"));

		}
	}

	private static void rg() throws Exception {
		for (String fileName : CLW.instance.getOtherParameters() ) {


			System.out.println("Generating reachability graph of "+fileName + ", saving as "+CLW.instance.OUTFILE.getValue() );

			STG stg = loadSTG(fileName, false);


			Date start = new Date();    
			STG res=STGUtil.generateReachabilityGraph(stg);
			Date end = new Date();
			double time = ((double)(end.getTime() - start.getTime()))/1000;

			FileSupport.saveToDisk(STGFile.convertToG(res), CLW.instance.OUTFILE.getValue());

			System.out.println(
					"Generated a RG with " + 
					res.getNumberOfPlaces() + " states and "+ 
					res.getNumberOfTransitions() +" arcs");
			System.out.println("Finished in "+time +" s");
		}
	}

	private static void decompose() throws IOException, STGException, ParseException {
		for (String fileName : CLW.instance.getOtherParameters()) {
			System.out.println("Decomposing "+fileName + " ...");


//			IMPLEMENT parametrize, logging

			//load STG
			logFile.info("Loading STG ...");			
			STG stg=loadSTG(fileName, false);
			logFile.info("... done");
			
			logFile.info(" "); // insert a blank line
			logFile.info("Specification's transition count: " + 
					stg.getTransitions(ConditionFactory.ALL_TRANSITIONS).size());
			logFile.info("Specification's place count: " + 
					stg.getPlaces(ConditionFactory.ALL_PLACES).size());
			logFile.info("Specification's input signal count: " +
					stg.getSignals(Signature.INPUT).size());
			logFile.info("Specification's output signal count: " +
					stg.getSignals(Signature.OUTPUT).size());
			logFile.info("Specification's dummy signal count: " +
					stg.getSignals(Signature.DUMMY).size());

			//and prepare for algorithm			
			if (CLW.instance.PRODUCTIVE.isEnabled()) {
				stg.simplifyLabels();
				logFile.info("Simplified places");
			}

			STGUtil.removeRedundantPlaces(stg);
			
			logFile.info("Removing initial dummies ...");
			int n = STGUtil.removeDummies(stg);
			logFile.info("Contracted " + n + " dummy transitions in the specification.");
			logFile.info(" ... done");
			
			if (CLW.instance.OD.isEnabled()) {
				// keep all left dummies (ie. their transition identifiers) as specifcation dummies in the stg
				// eigentlich gar nicht notwendig, weil der initiale stg ja immer erhalten bleibt (auch seine Dummies)
				// wir arbeiten ja immer nur auf einer Kopie!!!
			}
			else {
				// Are there still dummies in the specification?		
				if (!stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)).isEmpty()) {
					logFile.fatal("There are dummies in the specification left --> decomposition will be aborted!");
					throw new STGException("The specification has dummies!");
				}
					
			}


			// memorize internal signals and change signature to output,
			// they will be set back to internal after decomposition
			Set<Integer> internals = stg.collectUniqueCollectionFromTransitions(
					ConditionFactory.getSignatureOfCondition(Signature.INTERNAL),
					CollectorFactory.getSignalNameCollector());


			//change internals to outputs
			stg.setSignature(internals, Signature.OUTPUT);
			
			String filePrefix = new File("."+File.separator,fileName).getParent(); // for decomposition result names


			//generate partition
			Date start = new Date();
			
			Partition partition = null;
			String partitionString = CLW.instance.PARTITION.getValue();
			
			if (partitionString.equals("finest"))
				partition = Partition.getFinestPartition(stg,null);
			else if (partitionString.equals("roughest"))
				partition = Partition.getRoughestPartition(stg, null);
			else if (partitionString.equals("common-cause"))
				partition = Partition.getCommonCausePartition(stg);
			else if (partitionString.equals("sw-heuristics"))
				partition = Partition.getBreezePartition(stg);
			
			else if (partitionString.equals("multisignaluse"))
				partition = Partition.getMultipleSignalUsagePartition(stg);
			else if (partitionString.equals("avoidcsc"))
				partition = Partition.getCSCAvoidancePartition(stg);
			else if (partitionString.equals("reduceconc"))
				partition = Partition.getPartitionConcurrencyReduction(stg);
			else if (partitionString.equals("lockedsignals"))
				partition = Partition.getLockedSignalsPartition(stg);
			else if (partitionString.equals("best"))
				partition = Partition.getBestPartition(stg);
			else if (partitionString.startsWith("file:")) 
				partition = Partition.fromFile(partitionString.replaceFirst("file:", ""));
			else
				partition = Partition.fromString(stg, partitionString);

			Date endPartitioning = new Date();
						
			//start decomposition
			Collection<STG> components=null;
			
			final Integer BEFORE_ALL = new Integer(23);
			stg.addUndoMarker(BEFORE_ALL);
			if (CLW.instance.VERSION.getValue().equals("lazy-multi"))  
				components = new LazyDecompositionMultiSignal(filePrefix).decompose(stg, partition);

			else if (CLW.instance.VERSION.getValue().equals("lazy-single"))  
				components = new LazyDecompositionSingleSignal(filePrefix).decompose(stg, partition);

			else if (CLW.instance.VERSION.getValue().equals("basic")) 
				components = new BasicDecomposition(filePrefix).decompose(stg, partition);
			
			else if (CLW.instance.VERSION.getValue().equals("breeze")) 
				components = new BreezeDecomposition(filePrefix).decompose(stg, partition);

			else if (CLW.instance.VERSION.getValue().equals("tree")) 
				components = new TreeDecomposition(filePrefix).decompose(stg, partition);

			else if (CLW.instance.VERSION.getValue().equals("csc-aware"))
				components = new CscAwareDecomposition(filePrefix).decompose(stg, partition);
			
			else if (CLW.instance.VERSION.getValue().equals("irr-csc-aware"))
				components = new IrrCscAwareDecomposition(filePrefix).decompose(stg, partition);

			Date endDeco = new Date();

			// set internals back, but only those which are produced in a component and not used as input from
			// another component
			for (STG component : components) {
				for (Integer signal : component.getSignals())
					if (component.getSignature(signal) == Signature.OUTPUT
							&& internals.contains(signal))
						component.setSignature(signal, Signature.INTERNAL);
			}
			
			int newInternals = 0;
			
			if (CLW.instance.AVOID_CONFLICTS.isEnabled()) {
				
				ComponentAnalyser analyser = null;
				if (CLW.instance.INSERTION_STRATEGY.getValue().equals("norecalc"))
					analyser =
						new net.strongdesign.desij.decomposition.avoidconflicts.CAAvoidRecalculation(stg, components, filePrefix);
				else // strategy=mg OR strategy=general
					analyser =
						new net.strongdesign.desij.decomposition.avoidconflicts.CAGeneral(stg, components, filePrefix);
				
				boolean identificationResult = false; // if invalid CONFLICT_TYPE parameter value
				if (CLW.instance.CONFLICT_TYPE.getValue().endsWith("st"))
					identificationResult = analyser.identifyIrrCSCConflicts(true);
				else if (CLW.instance.CONFLICT_TYPE.getValue().equals("general"))
					identificationResult = analyser.identifyIrrCSCConflicts(false);
				
				if (identificationResult)
				{
					if (!analyser.avoidIrrCSCConflicts()) 
					{
						throw new STGException("At least one irreducible CSC conflict cannot be avoided!");
					}
					if (CLW.instance.SHOW_CONFLICTS.isEnabled()) 
						analyser.showWithConflicts();
					for (STG comp: components)
						analyser.refinePlaceHolderTransitions(comp);
				}
				
				newInternals += analyser.getNewInternals();
				
			}
			
			Date endIntCom = new Date();
			
			
			//write results
			DesiJ.logFile.info(""); // insert blank line

			List<Signature> o = new LinkedList<Signature>();
			o.add(Signature.OUTPUT);
			o.add(Signature.INTERNAL);

			StringBuilder equations = new StringBuilder();
			int nroSuccSynth = 0;
			for (STG component : components) {
		
				STGUtil.removeRedundantPlaces(component);
				StringBuilder signalNames = new StringBuilder();

				for (Integer s : component.collectUniqueCollectionFromTransitions(ConditionFactory.getSignatureOfCondition(o), 
						CollectorFactory.getSignalCollector()))
					signalNames.append(component.getSignalName(s));



				String componentName = signalNames.toString();
				componentName=componentName.substring(0,Math.min(componentName.length(), 50));
				
				componentName= fileName+ "__final_" +componentName+".g";
				
				if (CLW.instance.WRITE_RG.isEnabled()) {
					STG sgc  = STGUtil.generateReachabilityGraph(component);
					DesiJ.logFile.info(" - Final component is the reachability graph: " + componentName);
					FileSupport.saveToDisk(STGFile.convertToG(sgc), fileName+ "__final_rg_" +signalNames +".g");
				}
				else {
					
					FileSupport.saveToDisk(STGFile.convertToG(component), componentName);
				}
				

				if (CLW.instance.SYNTHESIS.isEnabled()) {
						System.out.println("Synthesising component: " + signalNames);
						equations.append("Component: " + signalNames+"\n");
											
						Pair<String,Integer>  synthesisResult = null;
						
						if (CLW.instance.SYNTHESIS_TOOL.getValue().equalsIgnoreCase("mpsat")) {
							if (component.getPlaces(ConditionFactory.getPlaceMarkingCondition(2)).isEmpty()) {
								synthesisResult = Unfolding.synthesiseSTGWithPunfMpsat(componentName);							
							}
						}
						else if (CLW.instance.SYNTHESIS_TOOL.getValue().equalsIgnoreCase("petrify")) {
							if (CLW.instance.SERVER_INFO.getValue().equals("")) {
								synthesisResult = StateGraph.synthesiseSTGWithPetrify(componentName);
							}
							else {
								String[] serverInfo = CLW.instance.SERVER_INFO.getValue().split(":");
								if (serverInfo[0].equalsIgnoreCase("localhost") || serverInfo[0].equals("127.0.0.1")) {
									// local call
									synthesisResult = StateGraph.synthesiseSTGWithPetrify(componentName);
								}
								else { // real remote call
									switch (serverInfo.length) {
										case 1:		synthesisResult = StateGraph.synthesiseSTGWithPetrify(
														componentName, serverInfo[0], "", ""); break;
										case 2:		synthesisResult = StateGraph.synthesiseSTGWithPetrify(
														componentName, serverInfo[0], serverInfo[1], ""); break;
										default:	synthesisResult = StateGraph.synthesiseSTGWithPetrify(
														componentName, serverInfo[0], serverInfo[1], serverInfo[2]);
									}
								}
								
							}
						}
						
						
						if (synthesisResult == null) 
							equations.append("SYNTHESIS NOT POSSIBLE (SNP)\n");
						else {
							int oldSignalCount = component.getSignals(Signature.INPUT).size();
							oldSignalCount += component.getSignals(Signature.OUTPUT).size();
							oldSignalCount += component.getSignals(Signature.INTERNAL).size();
							int newSignals = (synthesisResult.b - oldSignalCount);
							newInternals += newSignals;
							equations.append(	"Added " + 
												newSignals +
												" new internal signals.\n");
							equations.append( synthesisResult.a);
							++nroSuccSynth;
						}
						equations.append("\n");
					}
				
			}
			
			
			
			if (CLW.instance.SYNTHESIS.isEnabled()) {
				String equationFile = CLW.instance.EQUATIONS.getValue();
				if (equationFile.isEmpty())
					FileSupport.saveToDisk(equations.toString(), fileName+".equations");
				else
					FileSupport.saveToDisk(equations.toString(), equationFile);
			}
				
			Date end = new Date();

			//write time needed to logfile and console
			double time = ((double)(end.getTime() - start.getTime()))/1000;
			double partitionTime = ((double)(endPartitioning.getTime() - start.getTime()))/1000;
			double decoTime = ((double)(endDeco.getTime() - endPartitioning.getTime()))/1000;
			double intComTime = ((double)(endIntCom.getTime() - endDeco.getTime()))/1000;
			double synTime = ((double)(end.getTime() - endIntCom.getTime()))/1000;
			

			if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
				smallLogFile.append("(PAR) " 	+ partitionTime);
				smallLogFile.append("\n(DEC) " 	+ decoTime);
			}
			logFile.info(""); // blank line		
			logFile.info(" - Finished operation in " + time +" s");
			logFile.info(" - Partitioning time: " + partitionTime +" s");
			logFile.info(" - Decomposition time: " + decoTime +" s");
			
			if (CLW.instance.SYNTHESIS.isEnabled()) {
				if (CLW.instance.AVOID_CONFLICTS.isEnabled())
					logFile.info(" - Internal Communication insertion: " + intComTime + " s");
				logFile.info(" - Synthesis time: " + synTime +" s");
				logFile.info(" - Synthesis sucessful for: " + 
						nroSuccSynth +" of " + components.size() + " components");
				logFile.info(" - New internal signals added: " + newInternals);
				
				
				//also machine parsable
				if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
					smallLogFile.append("\n(IC) " 	+ intComTime);
					smallLogFile.append("\n(SYN) " 	+ synTime);
					smallLogFile.append("\n(SUC) "	+ nroSuccSynth +"/" + components.size());
					smallLogFile.append("\n(INT) "	+ newInternals + "\n");
				}

			}
			else if (CLW.instance.AVOID_CONFLICTS.isEnabled()) { // but not synthesis
				logFile.info(" - Internal Communication insertion: " + intComTime + " s");
				logFile.info(" - New internal signals added: " + newInternals);
				
				if (CLW.instance.WRITE_LOGFILE.isEnabled()) {
					smallLogFile.append("\n(IC) " 	+ intComTime);
					smallLogFile.append("\n(INT) "	+ newInternals + "\n");
				}
			}
			

			System.out.println("Finished operation in "+time +" s");
			System.out.println("Partitioning time: " + partitionTime +" s");
			System.out.println("Decomposition time: " + decoTime +" s");
			
			if (CLW.instance.AVOID_CONFLICTS.isEnabled())
				System.out.println("Internal Communication insertion: " + intComTime + " s");
			
			if (CLW.instance.SYNTHESIS.isEnabled()) {
				System.out.println("Synthesis time: " + synTime +" s");
				System.out.println("Synthesis sucessful for: " + 
						nroSuccSynth +" of " + components.size() + " components");
				System.out.println("New internal signals added: " + newInternals);

			}


		}	
	}

}





