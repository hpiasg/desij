/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011,2012 Mark Schaefer, Dominic Wist, Stanislavs Golubcovs
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

package net.strongdesign.balsa.breezefile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.strongdesign.balsa.breezeparser.BreezeParser;
import net.strongdesign.balsa.hcexpressionparser.HCExpressionParser;
import net.strongdesign.balsa.hcexpressionparser.ParseException;
import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator;
import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator.Operation;
import net.strongdesign.balsa.hcexpressionparser.terms.HCSTGGenerator;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm;
import net.strongdesign.balsa.hcexpressionparser.terms.HCTerm.ExpansionType;
import net.strongdesign.desij.CLW;
import net.strongdesign.desij.DesiJException;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.parser.GParser;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;
import net.strongdesign.util.StreamGobbler;

public class ComponentSTGFactory {
	
//	static public HashMap<String, String> componentMap;
	
	//  -G operation=breeze C:\workspace\breeze\Viterbi\HU.breeze
	@SuppressWarnings("unchecked")
	static public int getScale(LinkedList<Object> channels) {
		if (channels==null) return 4;
		
		int ret=0;
		for (Object c : channels ) {
			if (c instanceof LinkedList<?>) {
				ret=Math.max(((LinkedList<Integer>)c).size(), ret);
			} else
				ret=Math.max(1, ret);
		}
		
		return ret;
	}
	
	public static STG getSTGFromExpression(String expression, int scale) {
		HCExpressionParser parser = new HCExpressionParser(new StringReader(expression));
		STG stg = null;
		
		try {
			HCTerm t = parser.HCParser();
			
            if (t==null) {
            	return null;
            } else {
    			HCTerm up   = t.expand(ExpansionType.UP, scale, parser, false);
    			HCTerm down   = t.expand(ExpansionType.DOWN, scale, parser, false);
    			
    			HCInfixOperator ud = new HCInfixOperator();
    			
    			ud.components.add(up);
    			ud.components.add(down);
    			ud.operation = Operation.SEQUENCE;
    			
    			HCTerm tt = ud;
    			if (down==null) tt=up;
    			
    			stg = HCInfixOperator.generateComposedSTG(true, tt, parser, 
    					CLW.instance.ENFORCE_INJECTIVE_LABELLING.isEnabled());
    		}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return stg;
		
	}
	
	
	static public STG createSTGComponent(String compName, int compID, LinkedList<Object> parameters, LinkedList<Object> channels) {
		if (compName.startsWith("\"")) compName = compName.split("\"")[1];
		
		int scale = ComponentSTGFactory.getScale(channels);
		
		String expression = ComponentSTGExpressions.getExpression(compName);
		
		if (expression==null) return null;
		
		STG stg = null;
		
		if (expression.startsWith("$")) {
			stg = ComponentSTGInternalImplementations.getInternalImplementation(expression, scale);
		} else {
			stg = getSTGFromExpression(expression, scale);
		}
		
		if (stg==null) return null;
		
		if (CLW.instance.ENFORCE_INJECTIVE_LABELLING.isEnabled()) STGUtil.enforceInjectiveLabelling(stg);
		
		TreeMap<String, String> renaming = new TreeMap<String, String>();
		
		// do the renaming to the STG channel names
		byte cnt=0;
		int chanNum = channels.size();
		
		// for each STG signal determine its channel, assign appropriate name
		Map<String, Integer> signals = stg.getSignalNumbers();
		
		for (String sname: signals.keySet()) {
			
			// convert name to wire name, channel position, and index
			String wire = sname.substring(0,1);
			int channel = sname.charAt(1)-'A';
			int index = 0;
			
			if (sname.length()>2) {
				index = Integer.valueOf(sname.substring(2));
			}
			
			// is it a standard channel?
			// if yes, form new name with the channel ID
			
			if (channel<channels.size()&&(wire.equals("r")||wire.equals("a"))) {
				Object o = channels.get(channel);
				
				if (o instanceof LinkedList<?>) {
					@SuppressWarnings("unchecked")
					LinkedList<Integer> cl = (LinkedList<Integer>)o;
					int chanum = cl.get(index);
					renaming.put(sname, wire+(Integer)chanum);
				} else {
					// rename non-scalable channel
					renaming.put(sname, wire+(Integer)o);
				}
			} else { // if not, the numbering goes from the original name + component ID 
				renaming.put(sname, sname+"_"+compID);
			}
		}
		
//		for (Object o : channels) {
//			
//			if ( o instanceof LinkedList<?>) {
//				// rename scalable channel
//				@SuppressWarnings("unchecked")
//				LinkedList<Integer> cl = (LinkedList<Integer>)o;
//				for (int i=0;i<cl.size();i++) {
//					
//					String rFrom = ("r"+(char)('A'+cnt));
//					if (i>0) rFrom += i;
//					String aFrom = ("a"+(char)('A'+cnt));
//					if (i>0) aFrom += i;
//					
//					String input = (""+(char)('A'+cnt));
//					if (i>0) input += i;
//					String output = (""+(char)('A'+cnt));
//					if (i>0) output += i;
//					
//					String internal = (""+(char)('A'+cnt));
//					if (i>0) internal += i;
//					
//					renaming.put(rFrom, "r"+cl.get(i));
//					renaming.put(aFrom, "a"+cl.get(i));
//					
//					renaming.put("i"+input, "i"+compID+input);
//					renaming.put("o"+output, "o"+compID+output);
//					renaming.put("c"+internal, "c"+compID+internal);
//					
//				}
//				
//			} else if (o instanceof Integer) {
//				// rename non-scalable channel
//				renaming.put("r"+(char)('A'+cnt), "r"+(Integer)o);
//				renaming.put("a"+(char)('A'+cnt), "a"+(Integer)o);
//				
//				renaming.put("i"+(char)('A'+cnt), "i"+compID+(char)('A'+cnt));
//				renaming.put("o"+(char)('A'+cnt), "o"+compID+(char)('A'+cnt));
//				renaming.put("c"+(char)('A'+cnt), "c"+compID+(char)('A'+cnt));
//			}
//			
//			cnt++;
//		}
		
		try {
			stg.renameSignals(renaming);
		} catch (STGException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stg;
		
	}

	static public STG parallelComposition(LinkedList <STG> stgs, LinkedList<String> names) {
		// using the external pcomp tool
		
		try {
			// where the input
			int len = stgs.size();
			File in[] = new File[len];
			String files[] = new String[len];
			
//			String fnames="";
			
			File fnames = File.createTempFile("fnames", ".txt");
			FileWriter fw = new FileWriter(fnames);
			
			// for all selected nodes create temporary files and save corresponding STGs  
			for (int i=0;i<len;i++) {
				if (names==null) {
					in[i]= File.createTempFile("pcomp"+i+"_", ".g");
				} else {
					in[i]= File.createTempFile(names.get(i)+i+"_", ".g");
				}
				
				files[i]=in[i].getAbsolutePath();
				
				fw.write(files[i]+"\n");
			}
			
			fw.flush();
			fw.close();
			
			for (int i=0;i<len;i++) {
				FileSupport.saveToDisk(STGFile.convertToG(stgs.get(i)), files[i]);
			}
			
			String options=" -d ";
			
			if (CLW.instance.OPTIMIZED_PCOMP.isEnabled()) options+="-p ";
			
			Process pcomp = HelperApplications.startExternalTool(HelperApplications.PCOMP, options+" @"+fnames.getAbsolutePath());
			
			
			BufferedReader bin = new BufferedReader(new InputStreamReader(pcomp.getInputStream()));
			
			
			pcomp.getOutputStream().flush();
			pcomp.getOutputStream().close();
			
			
			OutputStream er = System.err;
			
			if (!CLW.instance.PUNF_MPSAT_GOBBLE.isEnabled()) er = null;
			
			StreamGobbler.createGobbler(pcomp.getErrorStream(), "pcomp-er", er);
			
			STG stg = null;
			
			try {
				stg= STGFile.readerToSTG(bin);
				
			} catch (STGException e) {
				e.printStackTrace();
			} catch (net.strongdesign.stg.parser.ParseException e) {
				e.printStackTrace();
			}
			
			pcomp.waitFor();
			
			pcomp.getErrorStream().close();
			pcomp.getInputStream().close();
			
			// delete all the created files
			for (int i=0;i<len;i++) {
				(new File(files[i])).delete();
			}
			
			fnames.delete();
			
			return stg;
		
		} catch (InterruptedException e) {
			throw new DesiJException("Error while running pcomp:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error while running pcomp: " + e.getMessage());
		}
		
	}
	
	public static Map<String, STG> breeze2stg() throws Exception {
		Map<String, STG> ret = new HashMap<String, STG>();
		
		for (String fileName : CLW.instance.getOtherParameters()) {
			ret.putAll(breeze2stg(fileName));
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, STG> breeze2stg(String fileName) throws Exception {
		Map<String, STG> ret = new HashMap<String, STG>();
		
		
		FileReader file = new FileReader(fileName);
		
		BreezeParser parser = new BreezeParser(file);
		
		for (Object item: (LinkedList<Object>)parser.ParseBreezeNet()) {
			AbstractBreezeElement ae = BreezeElementFactory.baseElement(item);
			// Go through all the breeze part elements, find all the component STGs 
			if (ae instanceof BreezePartElement) {
				BreezePartElement  bp = (BreezePartElement)ae;
				String bpname = bp.getName();
				
				STG mainSTG = null;
				TreeMap<Integer, BreezeComponentElement> tm = ((BreezePartElement)ae).getComponentList().getComponents();
				
				LinkedList <STG> stgs = new LinkedList<STG>();
				LinkedList <String> names = new LinkedList<String>();
				
				for (Entry <Integer, BreezeComponentElement> e : tm.entrySet()) {
					
					BreezeComponentElement be = e.getValue();
					STG stg = ComponentSTGFactory.createSTGComponent(e.getValue().getName(), e.getValue().getID(), be.parameters, be.channels);
					if(stg!=null) {
						stgs.add(stg);
						String name = e.getValue().getName();
						name=name.replaceAll("[\"$]", "");
						names.add(name);
					} else {
						if (! CLW.instance.SILENT.isEnabled()) {
							System.out.print("Component "+e.getValue().getName()+" not found\n");
						}
						
					}
				}
				
				mainSTG = ComponentSTGFactory.parallelComposition(stgs, names);
				ret.put(bpname, mainSTG);
			}
		}
			
		
		return ret;
	}
	
}
