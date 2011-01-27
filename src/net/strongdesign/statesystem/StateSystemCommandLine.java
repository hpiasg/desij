package net.strongdesign.statesystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class StateSystemCommandLine {
	public static <State,Event> void openCommandLine(StateSystem<State,Event> system) throws Throwable {
		//The current state of the system
		State currentState = system.getInitialState();
		
		//The reader for getting the users input
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		//Mapping from number to event
		Map<Integer,Event> eventChooser;
		
		//Mapping from number to state
		Map<Integer,State> stateChooser;
		
		//List of states for undoing
		LinkedList<State> states = new LinkedList<State>();
		states.add(currentState);
		
		eventLoop:
			while (true) {
				try {
					//Print current state and activated event
					System.out.println("\nCurrent state: " + currentState);
					
					//Print all possible events and assign them to a number
					eventChooser = new HashMap<Integer,Event>();
					int i=1;
					for (Event event : system.getEvents(currentState)) {
						System.out.println("("+ i +") '"+event+"'");
						eventChooser.put(i++, event);
					}
					
					//No events activated -> exit
					if (eventChooser.isEmpty()) {
						System.out.println("No event activated.");
					}
					
					stateLoop:
						while (true) {
							//Print the input query
							System.out.print("Choose an event, (r) for redisplay, (b) for going back or (q) for quit: ");
							
							String input = reader.readLine();
							
							//exit?
							if (input.matches("[qQ]")) {
								System.out.println("Exiting.");
								return;
							}
							
							//redisplay?
							if (input.matches("[rR]")) {
								continue eventLoop;
							}
							
							//undo?
							if (input.matches("[bB]")) {
								if (states.size()>1) {
									states.removeLast();
									currentState = states.getLast();
									continue eventLoop;
								}
								System.out.println("Cannot go back further.");
								continue stateLoop;
								
							}
							
							
							
							//Get the number
							Integer selection;
							try {
								selection = Integer.parseInt(input);
							}
							catch (NumberFormatException e) {
								System.out.println("Please enter a number between "+1+" and "+eventChooser.size());
								continue;
							}
							
							//out of range?
							if (selection < 1  ||  selection > eventChooser.size()) {
								System.out.println("Please enter a number between "+1+" and "+eventChooser.size());
								continue;
							}
							
							//choosen event				
							Event event =  eventChooser.get(selection);
							
							//determine next states 
							Set<State> nextStates = system.getNextStates(currentState, event);
							
							//deterministic? then choose next state automatically
							if (nextStates.size()==1) {
								currentState = nextStates.iterator().next();
								states.add(currentState);
								continue eventLoop;
							}
							
							//if not display all next states and assign number
							stateChooser = new HashMap<Integer,State>();
							i=1;
							for (State nextState : nextStates) {
								System.out.println("("+ i +") "+nextState);
								stateChooser.put(i++,nextState);
							}
							
							//get the coosen next state
							while (true) {
								System.out.print("Choose the next state for event '"+event+"'  (a) for abort or (q) for quit: ");
								
								input = reader.readLine();
								
								//quit?
								if (input.matches("[qQ]")) {
									System.out.println("Exiting.");
									return;
								}
								
								//abort? choose new event
								if (input.matches("[aA]")) {
									continue eventLoop;
								}
								
								//number entered?
								try {
									selection = Integer.parseInt(input);
								}
								catch (NumberFormatException e) {
									System.out.println("Please enter a number between "+1+" and "+stateChooser.size());
									continue;
								}
								
								//invalid?
								if (selection < 1  ||  selection > stateChooser.size()) {
									System.out.println("Please enter a number between "+1+" and "+stateChooser.size());
									continue;
								}
								
								//assign new state
								currentState = stateChooser.get(selection);
								states.add(currentState);
								continue eventLoop;
							}
						}
					
				}
				catch (Exception e)  {
					System.out.println("Error: "+e.getMessage());
					System.out.println("Going back to previous state.");
					if (states.size()>1) {
						states.removeLast();
						currentState = states.getLast();					
					}
					else {
						System.out.println("Cannot go back. Exiting.");
						return;
					}
				}
			}
	}
}
