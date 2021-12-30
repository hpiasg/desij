package net.strongdesign.stg.solvers;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import net.strongdesign.desij.CLW;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;

public class CSCSolver {

	public static STG petrifySTG(STG stgin, String options) throws IOException, InterruptedException {
		
		Process petrify = HelperApplications.startExternalTool(HelperApplications.PETRIFY,options);
		
		OutputStreamWriter osw = new OutputStreamWriter(petrify.getOutputStream());
		
		//bugfix for windows petrify which does not support coordinates
		stgin.clearCoordinates();
		osw.write(STGFile.convertToG(stgin));
		osw.flush();
		osw.close();
		
		BufferedReader bin = new BufferedReader(new InputStreamReader(petrify.getInputStream()));
		
		OutputStream er = System.err;
		if (!CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) er = null;
		
		StreamGobbler.createGobbler(petrify.getErrorStream(), "petrify-er", er);
		
		STG stg=null;
		
		try {
			stg= STGFile.readerToSTG(bin);
			
		} catch (STGException e) {
			e.printStackTrace();
		} catch (net.strongdesign.stg.parser.ParseException e) {
			e.printStackTrace();
		}
		
		petrify.waitFor();
		petrify.getErrorStream().close();
		petrify.getInputStream().close();
		
		return stg;
	}
	
	public static STG solveCSCWithPetrify(STG stgin) throws IOException, InterruptedException {
		return petrifySTG(stgin, "-csc");
	}
	
	public static STG solveCSCWithMpsat(STG stgin) throws IOException, InterruptedException  {
		
		try {
			//	where the STG is saved
			File dir = FileSupport.createTempDirectory("cscSolve_");
			
			File tmpSTG = File.createTempFile("desij", ".g", dir);
			
			//where the unfolding is saved
			File punfOut = File.createTempFile("desij", ".mci", dir);
			
			//save the STG, generate the unfolding and extract CSC violating traces
			FileSupport.saveToDisk(STGFile.convertToG(stgin), tmpSTG.getCanonicalPath());
			
			HelperApplications.startExternalTool(HelperApplications.PUNF, 
					"-t " + tmpSTG.getCanonicalPath() +" -m="+punfOut.getCanonicalPath(), dir).waitFor();
			
			File mpsatOut= new File(dir.getAbsolutePath()+File.separator+"mpsat.g");
			
			Process exec = HelperApplications.startExternalTool(HelperApplications.MPSAT, 
					" -R -f -cl "+punfOut.getCanonicalPath(), dir);
			
			if (CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) {
				StreamGobbler.createGobbler(exec.getInputStream(), "safe mpsat", System.out);
				StreamGobbler.createGobbler(exec.getErrorStream(), "safe mpsat-Error", System.out);
			} else {
				StreamGobbler.createGobbler(exec.getInputStream(), "safe mpsat", null);
				StreamGobbler.createGobbler(exec.getErrorStream(), "safe mpsat-Error", null);
			}
			
			exec.waitFor();
			exec.getErrorStream().close();
			exec.getInputStream().close();
			exec.getOutputStream().close();
			
			
			if (!mpsatOut.exists()) return null;
			String res = FileSupport.loadFileFromDisk(mpsatOut.getCanonicalPath());
			

			try {
				STG stg= STGFile.readerToSTG(new FileReader(mpsatOut));
				
				if (tmpSTG.exists()) tmpSTG.delete();
				if (punfOut.exists()) punfOut.delete();
				if (mpsatOut.exists()) mpsatOut.delete();
				return stg;
			} catch (STGException e) {
				e.printStackTrace();
			} catch (net.strongdesign.stg.parser.ParseException e) {
				e.printStackTrace();
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	

}
