/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
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

package net.strongdesign.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CommandLineTool<State> {
	/**Where the output goes to.*/
	private PrintStream out;
	
	/**Where the input comes from.*/
	private BufferedReader reader;	
	
	/**The set of supported meta actions. Note: @link CommandLineTool supports all of them, this member contains the one which 
	 * should be supported by the derived classes.*/
	private Set<MetaAction> metaActions;
	
	/**The list of all encountered @link MenuEntry so far.*/
	private List<MenuEntry> menuEntries;
	
	/**The current @link MenuEntry.*/
	private int currentEntry;
	
	/**
	 * Actions of the underlying system can only throw this exception (except for @link RuntimeException, of course).
	 * Other exceptions thrown should be mapped to this exception. 
	 */
	protected static class CommandLineException extends Exception {
		private static final long serialVersionUID = 467920121525585557L;
		
		public CommandLineException(String mes) {
			super(mes);
		}
	}
	
	/**
	 * Actions supported by implementations of this interface are supported. 
	 */
	protected interface MenuEntry {
		public String toString();
		public Collection<?> getActivatedActions();
		public MenuEntry performAction(Object action) throws CommandLineException;
		public boolean freeFormAllowed();
	}
	

	
	/**
	 * All possible meta actions.
	 */
	protected enum MetaAction {
		BACK("back", "b"), 
		FORWARD("forward", "f"),
		GOTO("goto","g"), 
		EXIT("exit","q"), 
		REDISPLAY("redisplay", "r");
		
		private String mnemocic;
		private String name;
		
		private MetaAction(String name, String mnemonic) {
			this.name = name;
			this.mnemocic = mnemonic;
		}
		
		public String getMnemocic() {
			return mnemocic;
		}
		
		public String getName() {
			return name;
		}
	}
	
	
	public CommandLineTool(InputStream in, PrintStream out, MenuEntry startEntry, MetaAction... actions ) {
		this.out = out;
		reader = new BufferedReader(new InputStreamReader(in));
		
		
		menuEntries = new ArrayList<MenuEntry>();
		menuEntries.add(startEntry);
		currentEntry = 0;
		
		metaActions = new HashSet<MetaAction>();
		for (MetaAction c : actions)
			metaActions.add(c);
	}
	
	/**
	 * Opens the @link CommandLineTool for user input.
	 * @throws IOException
	 */
	public void start() throws IOException {
		mainLoop: 
		while (true) {
			
			//Whatever the current state wants to say to the user
			out.println("\n"+menuEntries.get(currentEntry).toString());
			
			//all possible actions in the current state
			Collection<?> activated = menuEntries.get(currentEntry).getActivatedActions();
			
			//for finding the action by its number
			Map<Integer,Object> numberActivated = new HashMap<Integer, Object>();
			
			//print all possible actions to out and number them, see above
			int i=1;
			for (Object ca : activated)  {
				out.println(""+ i + ") "+ca);
				numberActivated.put(i++, ca);
			}
			
			//prints the supported meta actions, they are activated by mnemonics
			for (MetaAction currentMetaAction : metaActions)
				out.print(currentMetaAction.getMnemocic()+") "+currentMetaAction.getName()+"  ");
			
			
			//ask for input
			out.print("\nChoice: ");
			String input = reader.readLine();
			
			//was a meta action entered?
			for (MetaAction currentMetaAction : metaActions) {
				if (input.equals(currentMetaAction.getMnemocic()) || input.equals(currentMetaAction.getName())) {
					switch (currentMetaAction) {
					
					//enough is enough, close command line
					case EXIT: return;
					
					//redisplay this state from the beginning
					case REDISPLAY: continue mainLoop;
					
					//goes one step back in the history
					case BACK: 
						if (currentEntry==0)
							out.println("Cannot go back further.");
						else {
							--currentEntry;
						}
						break;
						
						//goes one step forward in the history
					case FORWARD: 
						if (currentEntry==menuEntries.size()-1)
							out.println("Cannot go forward further.");
						else {
							++currentEntry;
						}
						break;
						
						//asks which state should be revisited
					case GOTO:
						while (true) {
							out.println("\nGoto:\n0) Current");
							int k=0;
							for (MenuEntry entry : menuEntries) {
								System.out.println(""+ ++k +") "+entry);
							}
							
							out.print("\nChoice: ");
							String input2 = reader.readLine();
							
							try {
								int c = Integer.parseInt(input2);
								if (c<0 || c>k) {
									out.println("Choice out of range.");
									continue;
								}
								if (c!=0)									
									currentEntry = c-1;
								continue mainLoop;
								
							}
							catch (NumberFormatException e) { }
							
						}
					}
					continue mainLoop;
					
				}
			} // for
			
			try {
				//was a number entered?
				try {
					Integer actionNumber = Integer.parseInt(input);
					
					//yes it was, but is it in range?
					if (numberActivated.containsKey(actionNumber)) 
						addNewEntry(menuEntries.get(currentEntry).performAction(numberActivated.get(actionNumber)));
					else
						out.println("Unknown number");
					
					continue mainLoop;
				}
				catch (NumberFormatException e)  {
					//no number entered
				}
				
				//was an action entered directly?
				for (Object action : numberActivated.values()) 
					if (input.equals(action.toString())) {						
						addNewEntry(menuEntries.get(currentEntry).performAction(action));
						continue mainLoop;
					}
			
			
			//pass the input directly to the menu entry if allowed
			if (menuEntries.get(currentEntry).freeFormAllowed()) {
				addNewEntry(menuEntries.get(currentEntry).performAction(input));
				continue mainLoop;
			}

			}
			catch (CommandLineException e) {
				out.println("Operation could not be performed: " + e.getMessage());
				continue mainLoop;
			}
			//action could not be determined
			out.println("Unknown action.");
		}
	}
	
	/**
	 * Add a new entry following the current one is history. Following entries are deleted.
	 * @param newEntry
	 */
	protected void addNewEntry(MenuEntry newEntry) {
		if (menuEntries.get(currentEntry) != newEntry) {
			menuEntries.add(++currentEntry, newEntry);
		}
		//delete from the end to improve runtime
		for (int index = menuEntries.size()-1; index > currentEntry; --index )
			menuEntries.remove(index);
	}
	
	
	
	
	
	
	
	
	
	
	
}
