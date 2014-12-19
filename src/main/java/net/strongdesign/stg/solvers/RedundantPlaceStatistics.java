package net.strongdesign.stg.solvers;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
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

import net.strongdesign.desij.CLW;

public class RedundantPlaceStatistics {
	static public long totalStructuralChecks; // how much we can find with simple structural checks in total
	static public long totalShortcutPlaces; // how many shortcut places were found
	static public long totalSharedPathSplits; // how many times shared path was split
	static public long totalMergePlaceSplits; // how many merge places were split
	
	static public long totalSetupMills;  // number of milliseconds used for setting up the task
	static public long totalSolverMills; // number of milliseconds solver was thinking (accumulates)
	static public long totalFound;       // total number of the redundants found
	static public long totalChecked;       // total number of the redundants found
	
	
	public static void reset() {
		System.out.println("RedundantPlaceStatistics.reset");
		totalFound = 0;
		totalSetupMills = 0;
		totalSolverMills = 0;
		
		totalStructuralChecks=0;
		totalShortcutPlaces=0; 
		totalSharedPathSplits=0;
		totalMergePlaceSplits=0;
		
	}
	
	public static void reportStatistics() {
		reportStatistics(null);
		
	}

	public static void reportStatistics(String fileName) {
		String prep = fileName;
		if (prep==null||prep.equals("")) prep=" ";
		else prep+=": ";
		
		System.out.println(prep+"Shared path splits: "+RedundantPlaceStatistics.totalSharedPathSplits);
		System.out.println(prep+"Merge-place splits: "+RedundantPlaceStatistics.totalMergePlaceSplits);
		
		System.out.println(prep+"Structural checks found: "+RedundantPlaceStatistics.totalStructuralChecks+
				" shortcut places:"+RedundantPlaceStatistics.totalShortcutPlaces);
		
		System.out.println(prep+"Solver found: "+ RedundantPlaceStatistics.totalFound +"/"+ RedundantPlaceStatistics.totalChecked+
				" on depth: "+CLW.instance.IPLACE_LP_SOLVER_DEPTH.getIntValue()+
				" running time: "+(double)RedundantPlaceStatistics.totalSolverMills/1000+" s"+
				" setup time: "+(double)RedundantPlaceStatistics.totalSetupMills/1000+" s"
				);
		
	}
	
}
