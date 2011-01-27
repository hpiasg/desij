package net.strongdesign.desij.decomposition;

public enum DecompositionEvent {
	RED_PLACE_DEL 				("Deleted redundant place: ", 3, true),
	PLACE_INCREASE				("Place increase: started deletion of redundant places", 1, false),
	RED_TRANS_DEL 				("Deleted redundant transition: ", 2, true),
	TRANS_CON 					("Contracted transition: ", 3, true),
	TRANS_CON_AUTO_CONFLICT 	("Contracted transition (with new auto-conflict: ", 3, true),
	
	NEW_SIGNAL_SET				("Started new signal set ", 1, false),
	NEW_COMPONENT				("Started new component ", 1, false),
	BACKTRACKING 				("Backtracking. Added signal: ", 1, false),
	REMAINING_TRANSITIONS		("Not contractable transitions (all / syntactical): ",1, false),
	NEW_POSTPONE_TRY			("Try new round with postponed transitions ", 2, false),
	FINISHED 					("Finished decomposition ", 1, false),
	FINISHED_STATIC				("Finished decomposition, syntactically non-contractable dummies: ", 1, false),
	
	
	TREE_START					("Starting new tree decomposition ", 1, false),
	TREE_NEW_NODE				("Decomposing inner tree node ", 2, true),
	TREE_FINISHED_LEAF			("Finished tree leaf ", 1, true),
	TREE_SIGNAL_POSTPONED		("Moved signal to child nodes ", 2, false),
	TREE_CREATED				("Created Decomposition Tree ", 1, false),
	TREE_CREATED_VALUE			("Decomposition Tree: ", 2, false),
	
	
	AGGR_AGGRD_TREE				("Aggregated tree", 1, false),
	AGGR_FOUND_CANDIDATE		("Found candidate for aggregation. Value: ", 1, false),
	AGGR_APPROVED_CANDIDATE		("Approved candidate for aggregation. Value: ", 1, false),
	
	
 
	ONTHEFLY_FOUNDMAXNODE		("Found node with maximal subset: ", 2, false), 
	ONTHEFLY_TRYNEWNODE			("Try to contract signal for new node", 2, false),
	ONTHEFLY_NEWNODE			("Generated new node", 2, true), 
	
	LAZY_NEWSIGNAL				("Starting new Signal", 2, false),
	LAZY_BACKTRACKING			("Decomposition of signal failed, looking for causal signal", 1, false),
	LAZY_CAUSALFOUND			("Found causal signal", 1, false),
	LAZY_CAUSALNOTFOUND			("Current signal is not causal", 2, false),
	LAZY_REACHED_N				("Reached initial component, initial structural auto-conflict", 1, false), 
	
	SELF_TRIGGERING_FOUND 		("Found self-triggering place", 2, false), 
	SELF_TRIGGERING_REMOVED		("Self-triggering place removed", 2, true),
	SELF_TRIGGERING_NOT_REMOVED	("Self-triggering place not removed", 2, false), 
	
	CSC_CONFLICT				("Found CSC conflict in component", 1, false),
	CSC_NEW_CONFLICT			("Found new CSC conflict in component", 1, false),
	CSC_SOLVED					("Solved CSC conflict with known signals.", 1, false),
	CSC_SOLVED_ADD				("Solved CSC conflict with additional signals.", 1, false),
	CSC_IREDUCIBLE				("Found Irreducible CSC conflict.", 1, false),
	CSC_UP						("Going one node upwards in decomposition tree.", 2, false),
	CSC_REACHED_ROOT			("Reached root node of decomposition tree during CSC solving.", 1, false), 
	CSC_NOT_SOLVED				("Could not solve CSC", 1, false),
	CSC_LEVEL_EXCEEDED			("Exceeded backtracking level for CSC solving.", 1, false), 
	CSC_FINAL_REDUCTION			("Contracted signals. ", 1, false), 
	CSC_RECONTRACT				("Try to contract non CSC solving signals again. ", 1, false), 
	
	STAT_SERVER_STARTED			("Started statistics server", 2, false), 
	
	
	CONTRACTION_NOT_POSSIBLE_DUMMY ("Contraction not possible - no lambda transition ", 3, false),
	CONTRACTION_NOT_SECURE ("Contraction not possible - not secure ", 3, false),
	CONTRACTION_NOT_POSSIBLE_LOOP ("Contraction not possible - lies on a loop ", 3, false),
	CONTRACTION_NOT_POSSIBLE_ARC_WEIGHT ("Contraction not possible - arc weight >1 ", 3, false),
	CONTRACTION_NOT_POSSIBLE_NEW_AUTOCONFLICT ("Contraction not possible - new auto-conflict ", 3, false),
	CONTRACTION_NOT_POSSIBLE_DYNAMICALLY_UNSAFE ("Contraction not possible - dynamically unsafe ", 3, false),
	CONTRACTION_NOT_POSSIBLE_SYNTACTICALLY_UNSAFE ("Contraction not possible - statically unsafe ", 3, false), 
	
	
	STOPPED_FOR_BACKTRACKING ("First backtracking. Stopped.", 1, false), 
	
	
	;

	
	
	
	
	
	
	
	
//	CSC_IREDUCIBLE				("Found Irreducible CSC conflict.", 1, false),
	
	
	
	;
	

	
	
	
	
	public String toString(){
		return message;
	}
	
	public boolean writeSTG() {
		return writeFile;
	}
	
	
	private DecompositionEvent(String mes, int verboseLevel, boolean write) {
		message = mes;
		this.verboseLevel = verboseLevel;
		writeFile = write;
	}
	
	private String message;
	private int verboseLevel;
	private boolean writeFile;
	
	public int getVerboseLevel() {
		return verboseLevel;
	}
}
