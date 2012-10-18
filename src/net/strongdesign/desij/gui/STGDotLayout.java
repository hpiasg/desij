package net.strongdesign.desij.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Set;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGFile;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;

public class STGDotLayout {
	
	static public void doLayout(STG stg, STGGraphComponent g) {
		
		try {
			Process pdot = HelperApplications.startExternalTool(HelperApplications.DOT, " -Tplain");
			
			BufferedReader bin = new BufferedReader(new InputStreamReader(pdot.getInputStream()));
			
			Set<Node> emptySet = Collections.emptySet();
			String dotString = STGFile.convertToDot(stg, emptySet, "");
			
			StreamGobbler.createGobbler(pdot.getErrorStream(), "dot", System.err);
			
			OutputStreamWriter bout = new OutputStreamWriter(pdot.getOutputStream());
			
			bout.write(dotString);
			bout.flush();
			pdot.getOutputStream().close();
			
			// process each line, apply coordinates
			String str=null;
			while ( (str=bin.readLine())!=null) {
				String ss[] = str.split(" ");
				if (ss[0].equals("node")) {
					int id=Integer.valueOf(ss[1]);
					double x = Double.valueOf(ss[2]);
					double y = Double.valueOf(ss[3]);
					g.setNodeLocationById(id, (int)(x*75), (int)(-y*75));
				}
				
			}
			
			pdot.waitFor();
//			System.err.println("DOT Exit code: "+exitCode);
			
			pdot.getErrorStream().close();
			pdot.getInputStream().close();
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
