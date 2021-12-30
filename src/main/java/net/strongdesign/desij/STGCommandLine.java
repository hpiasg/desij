

package net.strongdesign.desij;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.strongdesign.desij.decomposition.partitioning.Partition;
import net.strongdesign.desij.decomposition.partitioning.PartitionComponent;
import net.strongdesign.stg.Node;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.STGFile;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.CommandLineTool;
import net.strongdesign.util.FileSupport;
import net.strongdesign.util.HelperApplications;


public class STGCommandLine extends CommandLineTool<STG> {
	
//	********************************************************************
	
	public STGCommandLine(STG stg, InputStream in, PrintStream out) {
		super(in, out, new MainMenu(stg), 
				MetaAction.EXIT, MetaAction.REDISPLAY, MetaAction.BACK, MetaAction.FORWARD, MetaAction.GOTO );
	}
	
	
//	********************************************************************
	
	protected static class MainMenu implements MenuEntry {
		private final STG stg;
		
		private static final String VIEW = "View";		
		private static final String CONTRACT = "Contract transition";		
		private static final String RED_PLACE = "Remove redundant place";		
		private static final String SIGNATURE = "Change signature";		
		private static final String INITIAL_COMP = "Initial component";		
		private static final String SAVE = "Save STG";		
		
		private static final Set<Object> choices = new LinkedHashSet<Object>();
		
		public MainMenu(STG stg) {
			super();
			this.stg = stg;
			
			choices.add(VIEW);
			choices.add(CONTRACT);
			choices.add(RED_PLACE);
			choices.add(INITIAL_COMP);
			choices.add(SAVE);
			choices.add(SIGNATURE);
			
		}
		
		public String toString() {
			return "STG Main Menu:";
		}
		
		public Set<Object> getActivatedActions() {
			return choices;
		}
		
		public MenuEntry performAction(Object action) throws CommandLineException {
			if (action == VIEW) {
				try {
					String gFile = File.createTempFile("desij-stg", ".g").getCanonicalPath();
					String psFile = gFile+".ps";
					
					Set<Node> emptySet = Collections.emptySet();
					FileSupport.saveToDisk(STGFile.convertToDot(stg, emptySet, ""), gFile);
					
					HelperApplications.startExternalTool(HelperApplications.DOT, 
							" -Tps " +
							HelperApplications.SECTION_START+gFile+HelperApplications.SECTION_END +
							" -o " +
							HelperApplications.SECTION_START+psFile+HelperApplications.SECTION_END ).waitFor();
					
					HelperApplications.startExternalTool(HelperApplications.GHOSTVIEW, 
							HelperApplications.SECTION_START+psFile+HelperApplications.SECTION_END);
				} catch (IOException e) {
					throw new CommandLineException(e.getMessage());
				} catch (InterruptedException e) {
					throw new CommandLineException(e.getMessage());
				}
			}
			else if (action == CONTRACT) 
				return new Contract(stg);
			else if (action == RED_PLACE) {
				return new RedPlace(stg);
			}
			else if (action == SIGNATURE) {
				return new ChangeSignatureSignal(stg);
			}
			else if (action == SAVE) {
				return new SaveSTG(stg);
			}
			else if (action == INITIAL_COMP) 
				return new InitialComponent(stg);
			
			
			
			return this;
		}

		public boolean freeFormAllowed() {
			return false;
		}	
	}
	
//	********************************************************************
	
	protected static class InitialComponent implements MenuEntry {
		private final STG stg;
		
		private Partition partition;
		
		private String desc = "Create initial partition:"; 
		
		public InitialComponent(STG stg) throws CommandLineException {
			this.stg = stg;
			try {
				partition = Partition.getFinestPartition(stg,null);
			} catch (STGException e) {
				throw new CommandLineException(e.getMessage());
			}
		}
		
		public String toString() {
			return desc;
		}
		
		public Collection<?> getActivatedActions() {
			return partition.getPartition();
		}
		
		@SuppressWarnings("unchecked")
		public MenuEntry performAction(Object action) {
			
			desc = "Created partition of " + action;
			return new MainMenu(Partition.getInitialComponent(stg,
					(PartitionComponent) action));
			
		}

		public boolean freeFormAllowed() {
			return false;
		}
		
	}
	
//	********************************************************************
	
	protected static class Contract implements MenuEntry {
		private final STG stg;
		private String desc = "Contract transition:"; 
		
		public Contract(STG stg) {
			this.stg = stg;
		}
		
		public String toString() {
			return desc;
		}
		
		public Collection<?> getActivatedActions() {
			Collection<Object> result = new LinkedList<Object>();
			result.addAll(stg.getTransitions(ConditionFactory.getContractableCondition(stg)));
			result.add("Main menu");
			
			return result;
		}
		
		public MenuEntry performAction(Object action)
		throws CommandLineException {
			if (action.equals("Main menu")) {
				desc = "Contracted nothing, returned to main menu";
				return new MainMenu(stg);
			}
				
			STG newSTG = stg.clone();
			try {
				//the list returned by getEqualTo contains exactly (hopefully :-) one element:
				//the corresponding transition to action in the cloned net
				newSTG.contract(newSTG.getTransitions(ConditionFactory.getEqualTo(action)).iterator().next());
				
			} catch (STGException e) {
				throw new CommandLineException(e.getMessage());
			}
			
			desc = "Contracted " + action;
			return new Contract(newSTG);
		}
		
		public boolean freeFormAllowed() {
			return false;
		}
		
	}
	
//	********************************************************************
	
	public static class SaveSTG implements MenuEntry {
		private STG stg;
		private static int number = 0;
		
		
		private String f1 = "./stg_"+number+".g";
		private String f2;
		
		private String desc = "Save STG:";
		
		public SaveSTG(STG stg) {
			this.stg = stg;
			f2 = System.getenv().get("HOME")+"/stg_"+number+".g";
		}
		
		public String toString() {
			return desc;
		}
		
		public Collection<?> getActivatedActions() {
			List<String> result = new LinkedList<String>();
			result.add(f1);
			result.add(f2);
			
			return result;
		}
		
		public MenuEntry performAction(Object action) throws CommandLineException {
			if (action.equals(f1) || action.equals(f2))
				++number;
			
			try {
				FileSupport.saveToDisk(STGFile.convertToG(stg), (String) action);
			} catch (IOException e) {
				throw new CommandLineException(e.getMessage());
			}
		
			desc = "Saved as " + action;
			return new MainMenu(stg);
		}
		
		public boolean freeFormAllowed() {
			return true;
		}
		
	}
	
//	********************************************************************
	
	protected static class RedPlace implements MenuEntry {
		private final STG stg;
		private String desc = "Remove redundant place";
		
		public RedPlace(STG stg) {
			this.stg = stg;
		}
		
		public String toString() {
			return desc;
		}
		
		public Collection<?> getActivatedActions() {
			List<Object> result = new LinkedList<Object>();
			result.addAll( stg.getPlaces(ConditionFactory.getRedundantPlaceCondition(stg)) );
			result.add("Main menu");
			
			return result;
			
		}
		
		public MenuEntry performAction(Object action) {
			
			if (action.equals("Main menu")) {
				desc = "Removed nothing, returned to main menu";
				return new MainMenu(stg);
			}
			
			
			desc = "Removed place "+action;
			
			STG newSTG = stg.clone();
			
			newSTG.removePlace( newSTG.getPlaces(ConditionFactory.getEqualTo(action)).iterator().next());
			return new RedPlace(newSTG);
		}
		
		public boolean freeFormAllowed() {
			return false;
		}
		
	}
	
//	********************************************************************
	
	protected static class ChangeSignatureSignal implements MenuEntry {
		
		private STG stg;
		private String desc = "Change signature - Chose Signal"; 
		
		public ChangeSignatureSignal(STG stg) {
			this.stg = stg;
		}
		
		public Collection<?> getActivatedActions() {
			return stg.getSignalNames(stg.getSignals());
		}
		
		public String toString() {
			return desc;
		}
		
		public MenuEntry performAction(Object action) {
			desc = "Change signature of signal "+action;
			return new ChangeSignature(stg, (String) action);
		}
		
		public boolean freeFormAllowed() {
			return false;
		}
		
	}

//	********************************************************************
	
	protected static class ChangeSignature implements MenuEntry {
		
		private STG stg;
		private String signal;
		
		private String desc = "Change signature:";
		
		
		public ChangeSignature(STG stg, String signal) {
			this.stg = stg;
			this.signal = signal;
		}
		
		public Collection<?> getActivatedActions() {
			Set<Signature> result = new LinkedHashSet<Signature>();
			
			for (Signature s : Signature.values())
				result.add(s);
			
			result.remove(stg.getSignature(stg.getSignalNumber(signal)));
			
			return result;
			
		}
		
		public String toString() {
			return desc; 
		}
		
		public MenuEntry performAction(Object action) {
			STG newSTG = stg.clone();
			
			newSTG.setSignature(newSTG.getSignalNumber(signal), (Signature) action);
			
			desc = "Changed signature to "+action;
			return new MainMenu(newSTG);
		}
		
		public boolean freeFormAllowed() {
			return false;
		}
		
	}
	
//	********************************************************************
//	********************************************************************
//	********************************************************************
//	********************************************************************
	
	
}
