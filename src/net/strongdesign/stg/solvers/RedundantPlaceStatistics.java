package net.strongdesign.stg.solvers;

public class RedundantPlaceStatistics {
	static public long totalStructuralChecks; // how much we can find with simple structural checks in total
	static public long totalShortcutPlaces; // how many shortcut places were found
	static public long totalSharedPathSplits; // how many times shared path was split
	static public long totalMergePlaceSplits; // how many merge places were split
	
	static public long totalSetupMills;  // number of milliseconds used for setting up the task
	static public long totalSolverMills; // number of milliseconds solver was thinking (accumulates)
	static public long totalFound;       // total number of the redundants found
	static public long totalChecked;       // total number of the redundants found
	
	
	public static void Reset() {
		
		totalFound = 0;
		totalSetupMills = 0;
		totalSolverMills = 0;
		
		totalStructuralChecks=0;
		totalShortcutPlaces=0; 
		totalSharedPathSplits=0;
		totalMergePlaceSplits=0;
		
	}
	
	
}
