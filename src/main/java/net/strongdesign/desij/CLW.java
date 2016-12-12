/**
 * Copyright 2004-2013 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
 * Copyright (C) 2016 Norman Kluge
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

package net.strongdesign.desij;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import net.strongdesign.util.CommandLineWrapper;
import net.strongdesign.util.ParsingException;

/**
 * 
 * <p>
 * <b>History: </b> <br>
 * 24.01.2005: Created <br>
 * 11.04.2005: Changed into a subclass of {@link CommandLineWrapper}
 * <p>
 * 
 * @author Mark Schaefer
 */

public class CLW extends CommandLineWrapper {

    // *******************************************************************
	// General parameters
	// *******************************************************************
	
	@Help("\nGeneral options\n-------------------------------")
	public Integer GENERAL = MARKER;
	
	// *******************************************************************
	
	@Help("Shows this message.")
	public CommandLineOption HELP = 
		new CommandLineOption('h', "help", false);
	
	// *******************************************************************
	
	@Help("Shows the values of all command line options and parameters.")
	public CommandLineOption PARAMETER = 
		new CommandLineOption('V', "values", false);

	// *******************************************************************
	
	@Help("Start statistic server")
	public  CommandLineOption START_STAT_SERVER =
		new CommandLineOption('E', "stat-server", true);
	
	// *******************************************************************
	
	@Help("Suppresses the starting message.")
	public CommandLineOption SILENT = 
		new CommandLineOption('Z', "silent", false);

	//	 *******************************************************************
	
	@Help("Write detailed information to commandline, from 0: no information to 3: more than you want")
	public  CommandLineInteger VERBOSE = 
		new CommandLineInteger("verbose", 0, 3, 1, false);

	//	 *******************************************************************
	
	@Help("Show pcomp/punf/mpsat output")
	public  CommandLineOption PUNF_MPSAT_GOBBLE = 
		new CommandLineOption('m', "punf-mpsat-output", false);
	
	// *******************************************************************
	
	@Help("Working mode for JDesi:" +
			"\n\t- bisim <specification> <component>+: finds and prints an STG bisimulation between the STG in <specicfication>" +
				" and the parallel composition of the STGs in the <component> files." +
				"If an STG contains dummies, they are automatically contracted." +
			"\n\t- check <specification> <component>+: same as for bisim, but only check if a STG-bisimulation exists. " +
			"\n\t- killdummies: Contracts all dummy transitions." +
			"\n\t- killdummiesrelaxed: Contracts all dummy while also using relaxation of injective labelling" +
			"\n\t- reduceint: Removes overencoding, ie. if an STG satisfies CSC, then internal signals will be removed which are unnecessary to preserve CSC." +
			"\n\t- info:" +
			"\n\t- info1:" +
			"\n\t- info2:" +
			"\n\t- decompose:" +
			"\n\t- rg:" +
			"\n\t- convert:" +
			"\n\t- clone:" +
			"\n\t- reddel: Removed redundant places and save the result as <filename>.red.g" +	
			"\n\t- create: Create a predefined STG model. When using this the model parameter is mandantory." +	
			"\n\t- show: Converts the STG to a ps File and automatically displays it." +
			"\n\t- breeze: Convert given Breeze file *.breeze to a Balsa-STG")
			
	public  CommandLineParameter OPERATION = 
		new CommandLineParameter("operation", "cl,trace,bisim,killdummies,killdummiesrelaxed,reduceint,check,info,info1,reddel,info2,decompose,rg,convert,clone,create,show,breeze", "decompose", false);
	
	// *******************************************************************
	
	@Help("Start the graphical user interface")
	public  CommandLineOption GUI = 
		new CommandLineOption('G', "gui", false);
	
	// *******************************************************************
	
	@Help("Opens a command line for stg editing")
	public  CommandLineOption CL = 
		new CommandLineOption('e', "commandline", false );

	// *******************************************************************

	@Help(	"Several optimisations take place. Essentially the places are just numbered after" +
			"contraction. This makes the result more readable and accelarates the computations.")
	public  CommandLineOption PRODUCTIVE = 
		new CommandLineOption('v', "productive", true);
	
	
	// *******************************************************************
	// Decomposition options and parameters
	// *******************************************************************
	
	@Help("\nControl of the decomposition algorithm\n-------------------------------")
	public Integer DECOMPOSITION = MARKER;
	
	// *******************************************************************
	
	@Help("Decomposition algorithm")
	public  CommandLineParameter VERSION = 
	new CommandLineParameter("version", "csc-aware,irr-csc-aware,basic,lazy-single,lazy-multi,tree,breeze", "tree", false);
	
	// *******************************************************************

	@Help("Perfoms tree aggregation. Works only for tree decomposition and CSC-aware decomposition.")
	public  CommandLineOption AGGREGATION = 
		new CommandLineOption('a', "aggregation", false);
	
	// *******************************************************************

	@Help("")//xxx
	public  CommandLineOption LEAVE_DUMMIES = 
		new CommandLineOption('l', "leave-dummies", false);
	
	// *******************************************************************
	
	@Help("When CSC aware decomposition is enabled, this value determines the maximum number of nodes " +
			"for which it is tried to solve CSC with signal of the STG before CSC is solved with new signals.")
	public  CommandLineInteger CSC_BACKTRACKING_LEVEL = 
		new CommandLineInteger("max-csc-backtracking", -1, Integer.MAX_VALUE, 1, false);

	// *******************************************************************

	@Help("The maximum number of signals a component can have to perform component aggregation.")
	public  CommandLineDouble MAX_COMPONENT_SIZE = 
	new CommandLineDouble("mcs", 1, Integer.MAX_VALUE, 10, false);
	
	// *******************************************************************

	@Help("After solving CSC with known signals, the signals not destroying CSC cores" +
		  "are contracted again.")
	public  CommandLineOption TRY_RECONTRACT = 
	new CommandLineOption('#', "recontract", true);
	
	// *******************************************************************
	
	@Help("Stop decomposition if an autoconflict occurs")
	public  CommandLineOption STOP_WHEN_BACKTRACKING = 
		new CommandLineOption('A', "stop-when-backtracking", false);

	// *******************************************************************

	@Help("When true, structural autoconflicts are ignored.")
	public  CommandLineOption RISKY = 
		new CommandLineOption('Y', "risky", false);
	
	// *******************************************************************
	
	@Help("Which signals are treated different in case of an autoconflict (see conflict-strategy) " +
			"turns conservative to risky and vice versa")
	public  CommandLineParameter CONFLICT_SIGNAL_EXCEPTION = 
		new CommandLineParameter("conflict-signal-exception", "", "", false);
	
	// *******************************************************************
	
	@Help("Backtracking method")
	public  CommandLineParameter BACKTRACKING = 
		new CommandLineParameter("backtracking", "complete,one-step,last-conflict", "one-step", false);
	
	// *******************************************************************
	
	@Help( "Partition of output signals for components" +
			"\n\t- finest: total decomposition (one output per component)" +
			"\n\t- roughest: one component (i.e. specification without unnecessary signals)" +
			"\n\t- common-cause: common cause partition -- experimental partition for breeze files" +
			"\n\t- sw-heuristics: sandwich heuristics -- experimental partition for breeze files" +
			"\n\t- multisignaluse: merge components using the same signals" +
			"\n\t- avoidcsc: integrate outputs that avoid irreducible CSC conflicts in the components" +
			"\n\t- reduceconc: reduce concurrency between outputs for some component" +
			"\n\t- lockedsignals: generate one component if outputs are locked" +
			"\n\t- best: combines all heuristics via majority voting of the respective compatibility critera of two components" +
			"\n\t- file:<filename>: choose partition from file")
	public  CommandLineParameter PARTITION = 
		new CommandLineParameter("partition", "", "finest", false);
	
	// *******************************************************************
	
	@Help("Checks the given partition only for conflicts between outputs and not if it is complete.")
	@RelatedTo({ "PARTITION" })
	public  CommandLineOption ALLOW_INCOMPLETE_PARTITION = 
		new CommandLineOption('i', "incomplete-partition", false);
	
	// *******************************************************************
	
		
	@Help("Redundant transitions are removed during decomposition")
	@RelatedTo({ "REMOVE_REDUNDANT_TRANSITIONS" })
	public  CommandLineOption REMOVE_REDUNDANT_TRANSITIONS = 
		new CommandLineOption('T', "remove-redundant-transitions", true);

	// *******************************************************************
		
	@Help("Dummy transition contractions are ordered ascending by number of newly generated places")
	public  CommandLineOption ORDER_DUMMY_TRANSITIONS = 
		new CommandLineOption('o', "order-dummy-transitions", true);
	
	// *******************************************************************
	
	@Help("See option -o. Order is updated after every contraction")
	@NotEnabled
	public  CommandLineOption ADAPTIVE_REORDERING = 
	new CommandLineOption('r', "adaptive-reordering", true);

	// *******************************************************************
	
	@Help("If a transition contraction is not possible it is tried later.")
	public  CommandLineOption POSTPONE_CONTRACTIONS = 
		new CommandLineOption('p', "postpone-contractions", true);

	// *******************************************************************

	@Help("Consider transitions as non-contractable if their contraction leads to a self-triggering situation" +
			" and the respective places are not redundant.")
	@RelatedTo({ "REMOVE_REDUNDANT_PLACES", "FIND_REDUNDANT" })
	public CommandLineOption FORBID_SELFTRIGGERING =
		new CommandLineOption('s', "forbid-self-triggering", false);
	
	// *******************************************************************
	
	@Help("Perform only safeness preserving contractions.")
	public CommandLineOption SAFE_CONTRACTIONS =
		new CommandLineOption('f', "safe-contractions", true);

	
	// *******************************************************************

	@Help("Check safeness preserving contractions with unfolding.")
	public CommandLineOption SAFE_CONTRACTIONS_UNFOLDING =
		new CommandLineOption('x', "safe-contractions-unfolding", false);
	
	
	// *******************************************************************
	
	
	@Help("The method to choose a transition set for contraction")
	@NotEnabled
	public  CommandLineParameter TRANSITION_SET = 
		new CommandLineParameter("choose-transition-set", "", "any-complete-signal", false);

	// *******************************************************************
	
	@Help("The method used to generate the decomposition tree. The result of top-down method is usually\n" +
			"20 - 40 % better, but it takes a long time and much memory, e.g. to build a decomposition tree\n" +
			"for 300 signals about 512 MB are needed. The randomised algorithm is much faster and the memory\n" +
			"usage can be ignored but the results are worse which results in longer decomposition runtimes.")
	public  CommandLineParameter DECOMPOSITION_TREE = 
		new CommandLineParameter("deco-tree", "combined,top-down", "combined", false);

	
	@Help("The maximum size of an STG (#Transition + #Places) for which properties are checked on the unfolding.")
	public CommandLineInteger MAX_STG_SIZE_FOR_UNFOLDING = 
		new CommandLineInteger("max-unfolding-size", 0, Integer.MAX_VALUE, 20, false);

	
	@Help("Use the undo stack instead of cloning. (strongly recommended)")
	public CommandLineOption UNDO_STACK =
		new CommandLineOption('U', "undo-stack", true);
	
	
	
	// *******************************************************************
	// Output-Determinacy Options
	// *******************************************************************
	
	@Help("\nOutput-Determinacy Options\n-------------------------------")
	public Integer OutDet = MARKER;
	
	@Help("Dealing with specification dummies.")
	public CommandLineOption OD =
		new CommandLineOption('@', "out-det", true);
		
	// *******************************************************************
	
	// *******************************************************************
	// Options for redundant place handling
	// *******************************************************************
	
	@Help("\nHandling of Redundant places\n-------------------------------\n\n" +
			"When removal of redundant places is activated, it is checked for (in this order):\n" +
			" - Easy structural cases\n" +
			" - Loop only places\n" +
			" - Duplicate places\n" +
			" - Shortcut places\n" +
			" - Implicit places (with unfoldings)\n" +
			"The latter two possibilities are only checked if they are enabled (see below), the last one only if the STG" +
			" is small enough, see also parameter 'max-unfolding-size'")
	public Integer RED = MARKER;
	
	
	// *******************************************************************
	
	@Help("Redundant places are removed during decomposition")
	@RelatedTo({ "REMOVE_REDUNDANT_TRANSITIONS", "FIND_REDUNDANT" })
	public  CommandLineOption REMOVE_REDUNDANT_PLACES = 
		new CommandLineOption('P', "remove-redundant-places", true);

	// *******************************************************************
	
	@Help("Check also for shortcutplaces when looking for redundant places.")
	public  CommandLineOption SHORTCUTPLACE = 
		new CommandLineOption('u', "shortcutplace", true);	
	
	// *******************************************************************
	
	@Help("Check also for shortcutplaces when looking for redundant places.")
	public  CommandLineInteger SHORTCUTPLACE_LENGTH = 
		new CommandLineInteger("shortcut-length", 2, Integer.MAX_VALUE, Integer.MAX_VALUE, false);	

	// *******************************************************************	

	@Help("Use an unfolding to check for implicit places. When this option is enabled" +
	" every implicit places is found. You need punf/mpsat to be installed for this.")
	@RelatedTo("MAX_STG_SIZE_FOR_UNFOLDING")
	public  CommandLineOption RED_UNFOLDING = 
		new CommandLineOption('X', "red-unfolding", false);	

	//	*******************************************************************
	
	@Help("It is checked for redundant places (and transitions) before every contraction.")
	public  CommandLineOption CHECK_RED_OFTEN = 
		new CommandLineOption('O', "check-red-often", false);

	//	*******************************************************************
	
	@Help("If during the contraction of some transitions the number of places exceeds the original number " +
			"multiplied by this value, redundant places are deleted before it is proceeded with the contraction.")
	public CommandLineDouble PLACE_INCREASE =
		new CommandLineDouble("max-place-increase", 1, Double.MAX_VALUE, 1.1, false);

	// *******************************************************************
	// Internal Communication Options
	// *******************************************************************
	
	@Help("\nInternal Communication\n-------------------------------")
	public Integer INT = MARKER;
	
	// 	*******************************************************************
		
	@Help("Avoids irreducible CSC-conflicts by the insertion of internal communication signals" +
			" among the components.")
	public CommandLineOption AVOID_CONFLICTS =
		new CommandLineOption('k', "avoid-conflicts", false);

	// ********************************************************************
	
	@Help("The given strategy is used for introducing internal communication." +
			"\n\t- norecalc: without recalculation of the components" +
			"\n\t- mg: only applicable to marked graph sections of the specification" +
			"\n\t- general: applicable to STGs with general structure")
	public  CommandLineParameter INSERTION_STRATEGY = 
		new CommandLineParameter("insertion-strategy", "norecalc,mg,general", "general", false);

	// ********************************************************************
	
	@Help("Shows the components and the specification with highlighted irreducible" +
			" CSC conflicts as postscript documents.")
	public CommandLineOption SHOW_CONFLICTS =
		new CommandLineOption('z', "show-conflicts", false);
	
	// ********************************************************************
	
	@Help("The given strategy is specify which type of irreducible CSC conflict should be solved." +
			"\n\t- sst: structural self-triggers" +
			"\n\t- st: dynamic self-triggers" +
			"\n\t- general: general (dynamic) irreducible CSC conflicts (punf and mpsat are required)")
	public  CommandLineParameter CONFLICT_TYPE = 
		new CommandLineParameter("conflict-type", "sst,st,general", "general", false);

	// *******************************************************************
	// Synthesis Options
	// *******************************************************************
	
	@Help("\nSynthesis of Components\n-------------------------------")
	public Integer SYN = MARKER;
	
	// 	*******************************************************************
	
	@Help("When enabled DesiJ tries to synthesise the components.")
	public  CommandLineOption SYNTHESIS = 
		new CommandLineOption('y', "synthesis", false);
	
	//	*******************************************************************
			
	@Help("The file in which the equations are stored if synthesis is enabled.")
	public  CommandLineParameter EQUATIONS = 
		new CommandLineParameter("equations", "", "", false);
	
	//	 *******************************************************************
	
	@Help("The given tool is used for synthesis of the components." +
			"\n\t- mpsat: use the mpsat tool of Victor Khomenko" +
			"\n\t- petrify: use the petrify tool of Jordi Cortadella" +
			"\n\t- other: for your own tool, the tool name must be included then in syn-param")
	public  CommandLineParameter SYNTHESIS_TOOL = 
		new CommandLineParameter("syn-tool", "mpsat,petrify", "mpsat", false);
			
	// *******************************************************************
	
	@Help("Information (IP-Adress, login, password) about the server which is doing synthesis." + 
			"\n\t- Format: <IP-adress>:<username>:<password>")
	public  CommandLineParameter SERVER_INFO = 
		new CommandLineParameter("serverinfo", "", "", false);
	
	//	 *******************************************************************

	@Help("The following parameters are used when synthesising is enabled.")
	public  CommandLineParameter SYNTHESIS_CL =
		new CommandLineParameter("syn-param", "", "", false);
	
	
	
	
	// *******************************************************************
	// Various Options
	// *******************************************************************
	
	@Help("\nVarious options\n-------------------------------")
	public Integer VARIOUS = MARKER;

	@Help("Hidden signals")
	public  CommandLineParameter HIDDEN_SIGNALS = 
			new CommandLineParameter("hide", "", "", false);

	@Help("When the operation is create, this parameter defines the model. Available models are: art,seq,par,multipar"+
			"\n\t- for producing Breeze handshake component STGs, use appropriate component names: BrzCall, BrzFetch, ...")
	public CommandLineParameter MODEL = 
		new CommandLineParameter("model", "", "", false);
	
	@Help("When creating models out of handshake components, the internal handshakes are dummified, i.e. labelled with lambda.")
	public CommandLineOption DUMMIFY_INTERNALHANDSHAKES = 
		new CommandLineOption('D', "dummify-internal-handshakes", true);
	
	@Help("Produre Balsa-STG without dummy signals (the uncontracted dummies are recovered as internal signals)")
	public CommandLineOption RECOVER_BREEZE_DUMMY = 
		new CommandLineOption('d', "recover-breeze-dummy", true);
	
	@Help("When creating models out of handshake components, the components are generated with CSC.")
	public CommandLineOption HANDSHAKE_COMPONENT_CSC = 
		new CommandLineOption('c', "handshake-component-csc", false);
	
	@Help("When reducing a component satisfying CSC, the CSC property should be preserved (after contraction of all dummies).")
	public CommandLineOption CSC_PRESERVE = 
		new CommandLineOption('C', "preserve-csc", false);
	
	@Help("When using partitioning strategies, the solutions for linear programming problems (LP)" + 
			" are not restricted to integers (ILP).")
	public CommandLineOption NOILP = 
		new CommandLineOption('Q', "noilp", true);
	
	
	// *******************************************************************
	
	@Help("Use shared place optimization")
	public CommandLineOption SHARED_SHORTCUT_PLACE =
		new CommandLineOption('B', "shared-place-optimization", true);
	
	// *******************************************************************
	
	@Help("Enforce injective labelling, when generating breeze components")
	public CommandLineOption ENFORCE_INJECTIVE_LABELLING =
		new CommandLineOption('j', "injective-labelling", true);
	
	// *******************************************************************
	
	@Help("Use optimized parallel composition for breeze files")
	public CommandLineOption OPTIMIZED_PCOMP =
		new CommandLineOption('M', "optimized-pcomp", true);
	
	@Help("Lambdarize channel transitions when forming Balsa-STG")
	public CommandLineOption LAMBDARIZE_ON_PCOMP =
		new CommandLineOption('g', "lambdarize-on-pcomp", true);
	
	// *******************************************************************
	
	@Help("Use LP solver to find implicit places")
	public CommandLineOption USE_LP_SOLVE_FOR_IMPLICIT_PLACES =
		new CommandLineOption('t', "lp-solver", false);
	
	@Help("LP solver depth, when searching for implicit places (1 - small (fast), 10 - default (average), 0 - full depth(slow)).")
	public  CommandLineInteger IPLACE_LP_SOLVER_DEPTH = 
		new CommandLineInteger("lp-solver-depth", 0, Integer.MAX_VALUE, 10, false);	
	
	
	// *******************************************************************
	// Input / Output options
	// *******************************************************************
		
	@Help("\nInput / Output options\n-------------------------------")
	public Integer OUTPUT = MARKER;
	
// *******************************************************************
	
	@Help("Intermediate results are written")
	public  CommandLineOption KEEP_IDS = 
		new CommandLineOption('K', "keep-ids", false);
	
	// *******************************************************************
	
	@Help("Write a logfile")
	@RelatedTo({ "LOGFILE", "WRITE_INTERMEDIATE_RESULTS" })
	public  CommandLineOption WRITE_LOGFILE = 
		new CommandLineOption('L', "write-logfile", true);

	// *******************************************************************
	
	@Help("Name of the logfile.")
	public  CommandLineParameter LOGFILE = 
		new CommandLineParameter("logfile", "", "desij.logfile", false);

	// *******************************************************************
	
	@Help("Intermediate results are written")
	public  CommandLineOption WRITE_INTERMEDIATE_RESULTS = 
		new CommandLineOption('I', "intermediate-results", false);
	
	// *******************************************************************

	@Help("Fileformat of the written STGs.")
	public  CommandLineParameter FORMAT = 
		new CommandLineParameter("format", "g,ps,dot,svg", "g", false);
	
	// *******************************************************************
	
	@Help("When saving in *.g format implicit places are saved explicitly")
	public  CommandLineOption SAVE_ALL_PLACES = 
		new CommandLineOption('S', "save-all-places", false);
	
	// *******************************************************************
	
	@Help("Removes redundant places and transitions before an STG is saved. Not sensible " +
			"for decomposition.")
	public  CommandLineOption REMOVE_REDUNDANT_BEFORE_SAVE = 
		new CommandLineOption('b', "reddel-before-save", false);
	
	
	// *******************************************************************
	
	@Help("Writes the reachability graph of an STG instead of the STG itself.")
	public  CommandLineOption WRITE_RG = 
		new CommandLineOption('R', "reachability-graph", false);
	
	// *******************************************************************
	
	@Help("Label for output in graphic formats")
	public  CommandLineParameter LABEL = 
		new CommandLineParameter("label", "", "", false);
	
	// *******************************************************************
	
	@Help("Name of outputfile for operations different from decompose")
	public  CommandLineParameter OUTFILE = 
		new CommandLineParameter("outfile", "", "", false);
	
	// *******************************************************************
	
	@Help("Name of file with breeze expressions")
    public  CommandLineParameter BREEZEEXPRESSIONSFILE = 
        new CommandLineParameter("breezeexpressionsfile", "", "", false);
	
	@Help("Export individual STGs of HS-Components and undummyfied STG when creating Balsa-STG")
	public  CommandLineOption BREEZEEXPORTTEMP = 
    new CommandLineOption(null, "breeze-export-temp", false);
	
	// *******************************************************************
	
	@Help("Predefined working sets")
	public  CommandLineParameter PREDEF = 
		new CommandLineParameter("predef", "original,advanced", "", false);
	
	
	// *******************************************************************
	
	protected Collection<CommandLineOption> guiOptions = new LinkedList<CommandLineOption>();
	protected Collection<CommandLineParameter> guiParameters = new LinkedList<CommandLineParameter>();


	/**
	 * The singleton global instance
	 */
	public static CLW instance = null;

	
	

	
	
    public CLW(String[] args) throws ParsingException {
    	super(args);
    	
    	if (args.length == 0) {
    		showHelp();
    	}
    	else {
	    	registerAll();	
	        checkForValidity();
    	}
    } 
 
    
    protected void registerGUIOptions(CommandLineOption... options) {
    	super.registerOptions(options);
    	Collections.addAll(guiOptions, options);   	
    }
    
    protected void registerGUIParameters(CommandLineParameter... parameters) throws ParsingException {
    	super.registerParameter(parameters);
   		Collections.addAll(guiParameters, parameters);
    }


	@Override
	protected void checkForValidity() {
		
		if (HELP.isEnabled())
			return;
		
		setPredef();
		
		if (! GUI.isEnabled() && files.isEmpty() && ! "create".contains(OPERATION.getValue()))
			throw new DesiJException("You must specify at least one input file.");
		
		if (! REMOVE_REDUNDANT_PLACES.isEnabled())
			System.err.println("Warning: It is not recommended to disable deletion of redundant places. The results will" +
					"probably be very bad.");
		
		if (WRITE_INTERMEDIATE_RESULTS.isEnabled())
			System.err.println("Warning: Writing intermediate results may lead to a large number of new files.");
		
		if ( OUTFILE.getValue().equals("") && 
			"rg,convert,clone,killdummies,create".contains(OPERATION.getValue()))	
				throw new DesiJException("You must specify outfile=<filename> if operation="+OPERATION.getValue()+"."+OUTFILE.getValue());
			
		if (OPERATION.getValue().equals("create") && MODEL.getValue().equals(""))
			throw new DesiJException("You must specify an STG model when using operation create.");
		
		if ("bisim,check".contains(OPERATION.getValue())) {
			if (getOtherParameters().size() < 2) 
				throw new DesiJException("You must specify at least two STGs: specification first then the components");
		}
		
	}


	private void setPredef() {
		if (PREDEF.getValue().equals("original")) {
			CHECK_RED_OFTEN.setEnabled(true);
			FORBID_SELFTRIGGERING.setEnabled(false);
			OPERATION.setValue("decompose");
			ORDER_DUMMY_TRANSITIONS.setEnabled(true);
			RED_UNFOLDING.setEnabled(false);
			SAFE_CONTRACTIONS.setEnabled(false);
//			SHORTCUTPLACE.setEnabled(true);
			VERSION.setValue("basic");
		}
		else if (PREDEF.getValue().equals("advanced")) {
			CHECK_RED_OFTEN.setEnabled(false);
			FORBID_SELFTRIGGERING.setEnabled(true);
			SAFE_CONTRACTIONS.setEnabled(true);
			VERSION.setValue("csc-aware");
		}
		
	}
	

}
