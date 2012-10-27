package net.strongdesign.balsa.breezefile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.strongdesign.balsa.breezeparser.BreezeParser;
import net.strongdesign.balsa.hcexpressionparser.HCExpressionParser;
import net.strongdesign.balsa.hcexpressionparser.ParseException;
import net.strongdesign.balsa.hcexpressionparser.terms.HCInfixOperator;
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
	
	//  -G operation=breeze C:\RA\Documents\presentation\wc2\BalsaPlugin\bin\org\workcraft\testing\parsers\breeze\data\HU.breeze
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
	
	
	static public STG createSTGComponent(String compName, LinkedList<Object> parameters, LinkedList<Object> channels) {
		if (compName.startsWith("\"")) compName = compName.split("\"")[1];
		
		int scale = ComponentSTGFactory.getScale(channels);
		
		String expression = ComponentSTGExpressions.getExpression(compName);
		
		if (expression==null) return null;
		
		STG stg = null;
		
		if (expression.startsWith(".")) {
			
			// consider the input to be an STG in .g format
			try {
				GParser parser = new GParser(new StringReader(expression));
				stg = parser.STG();
			} catch (net.strongdesign.stg.parser.ParseException e) {
				e.printStackTrace();
			} catch (STGException e) {
				e.printStackTrace();
			}
			
		} else {
			
			HCExpressionParser parser = new HCExpressionParser(new StringReader(expression));
			stg = null;
			
			try {
				HCTerm t = parser.HCParser();
				
	            if (t==null) {
	            	return null;
	            } else {
	    			HCTerm up   = t.expand(ExpansionType.UP, scale, parser, false);
	    			
	    			stg = HCInfixOperator.generateComposedSTG(true, up, parser);
	    		}
				
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (stg==null) return null;
		
		if (CLW.instance.ENFORCE_INJECTIVE_LABELLING.isEnabled()) STGUtil.enforceInjectiveLabelling(stg);
		
		TreeMap<String, String> renaming = new TreeMap<String, String>();
		
		// do the renaming to the actual channel names
		byte cnt=0;
		for (Object o : channels) {
			if ( o instanceof LinkedList<?>) {
				// rename scalable channel
				@SuppressWarnings("unchecked")
				LinkedList<Integer> cl = (LinkedList<Integer>)o;
				for (int i=0;i<cl.size();i++) {
					
					String rFrom = ("r"+(char)('A'+cnt));
					if (i>0) rFrom += i;
					String aFrom = ("a"+(char)('A'+cnt));
					if (i>0) aFrom += i;
					
					String input = ("i"+(char)('A'+cnt));
					if (i>0) input += i;
					String output = ("o"+(char)('A'+cnt));
					if (i>0) output += i;
					
					renaming.put(rFrom, "r"+cl.get(i));
					renaming.put(aFrom, "a"+cl.get(i));
					renaming.put(input, "i"+cl.get(i));
					renaming.put(output, "o"+cl.get(i));
				}
			} else if (o instanceof Integer) {
				// rename non-scalable channel
				renaming.put("r"+(char)('A'+cnt), "r"+(Integer)o);
				renaming.put("a"+(char)('A'+cnt), "a"+(Integer)o);
				renaming.put("i"+(char)('A'+cnt), "i"+(Integer)o);
				renaming.put("o"+(char)('A'+cnt), "o"+(Integer)o);
			}
			cnt++;
		}
		
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
			String fnames="";
			
			// for all selected nodes create temporary files and save corresponding STGs  
			for (int i=0;i<len;i++) {
				if (names==null) {
					in[i]= File.createTempFile("pcomp"+i+"_", ".g");
				} else {
					in[i]= File.createTempFile(names.get(i)+i+"_", ".g");
				}
				
				files[i]=in[i].getAbsolutePath();
				if (i>0) fnames+=" ";
				fnames+=files[i];
			}
			
			for (int i=0;i<len;i++) {
				FileSupport.saveToDisk(STGFile.convertToG(stgs.get(i)), files[i]);
			}
			
			String options=" -d ";
			if (CLW.instance.OPTIMIZED_PCOMP.isEnabled()) options+="-p ";
			Process pcomp = HelperApplications.startExternalTool(HelperApplications.PCOMP, options+fnames);
			
			
			BufferedReader bin = new BufferedReader(new InputStreamReader(pcomp.getInputStream()));
			
			
			pcomp.getOutputStream().flush();
			pcomp.getOutputStream().close();
			
			StreamGobbler.createGobbler(pcomp.getErrorStream(), "pcomp", System.err);
			
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
			
			return stg;
		
		} catch (InterruptedException e) {
			throw new DesiJException("Error while running pcomp:  " + e.getMessage());
		} catch (IOException e) {
			throw new DesiJException("Error while running pcomp: " + e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static STG breeze2stg() throws Exception {
		
		for (String fileName : CLW.instance.getOtherParameters()) {
			
			FileReader file = new FileReader(fileName);
			
			BreezeParser parser = new BreezeParser(file);
			
			for (Object item: (LinkedList<Object>)parser.ParseBreezeNet()) {
				AbstractBreezeElement ae = BreezeElementFactory.baseElement(item);
				// Go through all the breeze part elements, find all the component STGs 
				if (ae instanceof BreezePartElement) {
					
					STG mainSTG = null;
					TreeMap<Integer, BreezeComponentElement> tm = ((BreezePartElement)ae).getComponentList().getComponents();
					
					LinkedList <STG> stgs = new LinkedList<STG>();
					LinkedList <String> names = new LinkedList<String>();
					
					for (Entry <Integer, BreezeComponentElement> e : tm.entrySet()) {
						
						BreezeComponentElement be = e.getValue();
						STG stg = ComponentSTGFactory.createSTGComponent(e.getValue().getName(), be.parameters, be.channels);
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
					return mainSTG;
				}
			}
			
		}
		return null;
	}
	
}
