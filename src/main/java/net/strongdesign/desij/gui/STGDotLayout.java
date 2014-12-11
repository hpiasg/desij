package net.strongdesign.desij.gui;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Set;
import java.util.Map.Entry;

import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;

public class STGDotLayout {
	
	
	private static final double zoom = 70; 
	
	static public void doLayout(STG stg) {
		doLayout(stg, null);
	}
	
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
			
			String str=null;
			
			double minx = 1000;
			double miny = 1000;
			
			while ( (str=bin.readLine())!=null) {
				String ss[] = str.split(" ");
				if (ss[0].equals("node")) {
					int id=Integer.valueOf(ss[1]);
					double x = Double.valueOf(ss[2])*zoom;
					double y = -Double.valueOf(ss[3])*zoom;
					
					minx = Math.min(minx, x);
					miny = Math.min(miny, y);
					
					if (g==null) {
						
						Node n = stg.getNode(id);
						
						Point point = new Point((int)x, (int)y);
						
						try {
							stg.setCoordinates(n, point);
						} catch (STGException e) {
							e.printStackTrace();
						}
						
					} else {
						g.setNodeLocationById(id, (int)x, (int)y);
					}
				}
			}
			
			pdot.waitFor();
			pdot.getErrorStream().close();
			pdot.getInputStream().close();
			
			// do the shift
			if (g==null) {
				double dx = -minx+50;
				double dy = -miny+50;
				
				for (Entry<Node, Point> en: stg.getCoordinates().entrySet()) {
					Node n = en.getKey();
					Point p = en.getValue();
					p.translate((int)dx, (int)dy);
					
				}
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
