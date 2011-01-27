package net.strongdesign.stg.traversal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.statesystem.decorator.Cache;
import net.strongdesign.stg.Marking;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGAdapterFactory;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.util.Pair;

/**
 * 
 * This class models the parallel composition of a collection of STGs as a 
 * @link net.strongdesign.statesystem.StateSystem<List<Marking>,Signaledge>.
 * It is checked for <i>computation interference</i> lazily, i.e. only for the states
 * where @link #getNextStates(List, SignalEdge) or @link #getNextStates(List, SignalEdge)
 * is actually called. If computation interference is found a ComputationInterferenceException is thrown
 * 
 */
public class STGParallelComposition implements StateSystem<List<Marking>, SignalEdge> {
	//In the comments the terms statesystem and STG are used interchangeble

	/**The statesystem which wraps the STGs whose parallel composition is modelled*/
	private List<StateSystem<Marking,SignalEdge>> stgs;

	/**The set of hidden signals*/
	private Set<Integer> hiddenSignals;

	/**Mapping from input signal names to the STGs which produce this signal, see also @link #getInputSTGs(String)*/
	private Map<Integer, Set<StateSystem<Marking,SignalEdge>>> inputSignals;

	/**Mapping from output signal names to the STGs which produce this signal*/
	private Map<Integer, StateSystem<Marking,SignalEdge>> outputSignals;

	private Map<Integer, String> signalNames = new HashMap<Integer, String>();

	private Map<Integer, Signature> signatures;

	/**
	 * Constructs a new instance from a collection of STGs 
	 * @param stgs
	 * @throws STGException
	 */
	public STGParallelComposition(Map<String,Integer> signalNumbers, Collection<STG> stgs, Collection<String> hidden) throws STGException {
		this.stgs = new LinkedList<StateSystem<Marking, SignalEdge>>();
		this.hiddenSignals = reassignSignals(signalNumbers, stgs, hidden);

		inputSignals = new HashMap<Integer, Set<StateSystem<Marking,SignalEdge>>>();
		outputSignals = new HashMap<Integer, StateSystem<Marking,SignalEdge>>();
		signatures = new HashMap<Integer, Signature>();


		for (STG stg : stgs) {
			//Cache the STG for better performance
			StateSystem<Marking,SignalEdge> cachedSTG = new Cache<Marking, SignalEdge>(STGAdapterFactory.getStateSystemAdapter(stg));
			this.stgs.add(cachedSTG);


			//analyse signatures, complain about forbidden properties
			for (Integer sig : stg.getSignals()) {
				switch (stg.getSignature(sig)) {
				case INPUT:
					if (hiddenSignals.contains(sig))
						throw new STGException("Cannot hide input signals: "+sig);
					getInputSTGs(sig).add(cachedSTG);

					Signature sign = signatures.get(sig);
					if (sign==null)
						signatures.put(sig, Signature.INPUT);					
					break;

				case  OUTPUT:
					if (outputSignals.put(sig, cachedSTG) != null)
						throw new STGException("Output is produced by more than one component: "+sig);

					if (hiddenSignals.contains(sig)) {
						signatures.put(sig, Signature.INTERNAL);
					}
					else {
						signatures.put(sig, Signature.OUTPUT);	
					}					
					break;

					//just for catching INTERNALS before default: throws an Exception
				case  INTERNAL: 
					signatures.put(sig, Signature.INTERNAL);
					break;

				default: throw new STGException("Not allowed signal type: "+stg.getSignature(sig));				
				}
			}
		}



	}

	/**
	 * Reassigns signal numbers for a collection of STGs such that a signal number is mapped to the same signal
	 * name in every STG. 
	 * @param signalNumbers A predefined mapping which must not be changed.
	 * @param stgs The collection of STGs.
	 * @param hidden The names of signals whose new signal numbers is returned.
	 * @return The new signals numbers of the signals in signalNames.
	 * @throws STGException
	 */
	private Set<Integer> reassignSignals(Map<String, Integer> signalNumbers, Collection<STG> stgs, Collection<String> hidden) throws STGException {

		// New mappings for signals not in signalNumbers
		Map<String, Integer> newSignalNumbers = new HashMap<String, Integer>(signalNumbers);

		int max = Integer.MIN_VALUE;
		Collection<Integer> usedSignalsNumbers = newSignalNumbers.values();
		for (Integer i : usedSignalsNumbers) {
			if (i > max) max = i;
		}


		Set<Integer> internalSignals = new HashSet<Integer>();
		// consider every STG ...
		for (STG stg : stgs) {
			// ... and every signal 
			for (String signalName : new HashSet<String>(stg.getSignalNames(stg.getSignals()))) {
				Integer signal = stg.getSignalNumber(signalName);

				if (stg.getSignature(signal) == Signature.INTERNAL) {
					// special handling for internal signals, every single one gets a unique number (withnin the || composition)
					++max;
					stg.reassignSignal(signalName, max);
					internalSignals.add(signal);
				}
				else {
					// is there a previous assigned signal number ?
					Integer assignedSignal = newSignalNumbers.get(signalName);
					
					if (assignedSignal == null) {
						//signal name is unknown
						if (usedSignalsNumbers.contains(signal) || internalSignals.contains(signal)) {
							// if the signal number is known, find a new one
							++max;
							stg.reassignSignal(signalName, max);
							newSignalNumbers.put(signalName, max);
						}
						else {
							// signal number also not known, store signal and update max
							newSignalNumbers.put(signalName, signal);
							if (signal > max) max = signal;
						}
					}
					else {				
						// reassign the number
						stg.reassignSignal(signalName, assignedSignal);
					}		
				}
			}			
		}	


		for (String signalName : newSignalNumbers.keySet()) {
			signalNames.put(newSignalNumbers.get(signalName), signalName);
		}

		// determine the signal numbers of the given signals
		Set<Integer> result = new HashSet<Integer>();

		for (String hiddenSignalName : hidden) {
			Integer hiddenSignalNumber = newSignalNumbers.get(hiddenSignalName);

			if (hiddenSignalNumber == null) {
				System.err.println("Warning! Unknown signal in hiding set: " + hiddenSignalName);
			}
			else {
				result.add(hiddenSignalNumber);
			}
		}

		return result;
	}


	public Signature getSignature(String signal) {
		return signatures.get(signal);
	}

	/**
	 * Returns the initial state in form of a list of markings, one for each component
	 */
	public List<Marking> getInitialState() {
		List<Marking> result = new LinkedList<Marking>();

		for (StateSystem<Marking, SignalEdge> stg : stgs)
			result.add(stg.getInitialState());

		return result;		
	}

	/**
	 * Convenience method for the access of inputSignals, entries for non existing keys/signal-names are 
	 * generated automatically. 
	 * @param signalName
	 * @return
	 */
	protected Set<StateSystem<Marking,SignalEdge>> getInputSTGs(Integer signalName) {
		Set<StateSystem<Marking,SignalEdge>> result = inputSignals.get(signalName);
		if (result==null) {
			result = new HashSet<StateSystem<Marking,SignalEdge>>();
			inputSignals.put(signalName, result);
		}
		return result;
	}

	public Set<SignalEdge> getEvents(List<Marking> state) throws ComputationInterferenceException {
		//Mapping from an STG in form of the corresponding statesystem to its activated signals
		Map<StateSystem<Marking,SignalEdge>, Set<SignalEdge>> activatedSignals =
			new HashMap<StateSystem<Marking,SignalEdge>, Set<SignalEdge>>();

		//The set of all activated signals for each component
		Set<SignalEdge> result = new HashSet<SignalEdge>();

		//fill the above variables
		Iterator<Marking> ns = state.iterator();
		for (StateSystem<Marking,SignalEdge> stg : stgs) {
			Marking curMarking = ns.next();
			activatedSignals.put(stg, stg.getEvents(curMarking));
			result.addAll(stg.getEvents(curMarking));
		}

		//The set of signals which are not activated in every component
		Set<SignalEdge> toRemove = new HashSet<SignalEdge>();

		//check for computation interference and synchronisation
		for (SignalEdge edge : result) {
			Integer signal = edge.getSignal();


			//activated as output?
			if (outputSignals.containsKey(signal) 
					&& ! activatedSignals.get(outputSignals.get(signal)).contains(edge)) {
				toRemove.add(edge);
				continue;
			}


			//signal activated as input in all components?
			boolean inputActivated = true;

			for (StateSystem<Marking,SignalEdge> sys : getInputSTGs(signal)) 
				inputActivated = inputActivated && activatedSignals.get(sys).contains(edge);

			//only signals which are activated in all listening components are activated at all
			if (inputSignals.containsKey(edge.getSignal()) && !inputActivated) {
				toRemove.add(edge);
				continue;
			}

			//check for ci
			if (	outputSignals.containsKey(edge.getSignal()) && 
					activatedSignals.get(outputSignals.get(signal)).contains(edge) &&
					! inputActivated	)
				throw new ComputationInterferenceException("Computation interference for signal edge "+edge+
						" under marking "+state);
		}

		result.removeAll(toRemove);

		return result;
	}

	public Set<List<Marking>> getNextStates(List<Marking> state, SignalEdge edge) {
		//the resulting possible markings
		Set<List<Marking>> result = new HashSet<List<Marking>>();

		//Iterator for the markings corrsponding to the STGs
		Iterator<Marking> itMarking;

		//hidden signals are considered internally as outputs
		if (hiddenSignals.contains(edge.getSignal())) 
			edge = new SignalEdge(edge.getSignal(), edge.getDirection());

		//contains the possible next markings for each component
		List<Set<Marking>> possibleNextMarkings = new LinkedList<Set<Marking>>();

		//dependent on the signature a different handling is needed 
		Signature sign = Signature.INTERNAL;
		if (outputSignals.containsKey(edge.getSignal()))
			sign = Signature.OUTPUT;
		else if (inputSignals.containsKey(edge.getSignal()))
			sign = Signature.INPUT;

		switch (sign) {
		case INPUT: 
			//build up possibleMarkings
			itMarking = state.iterator();
			for (StateSystem<Marking,SignalEdge> stg : stgs) {
				Marking curMarking = itMarking.next();
				Set<Marking> nextStates = stg.getNextStates(curMarking, edge);

				//component does not activate edge
				if (nextStates.isEmpty() ) {
					if ( getInputSTGs(edge.getSignal()).contains(stg)) 
						return new HashSet<List<Marking>>();

					//because it don't know about the signal -> current marking is wrapped in a set returned unchanged
					Set<Marking> nothing = new HashSet<Marking>();
					nothing.add(curMarking);				
					possibleNextMarkings.add(nothing);
				}
				else				
					possibleNextMarkings.add(nextStates);	
			}

			return getAllCombinations(possibleNextMarkings);

			//break; //!! don't forget to uncomment if the above return is deleted/modified

		case OUTPUT:
			//for detection of computation interference 
			boolean allInputsActivated = true;

			//build up possibleMarkings
			itMarking = state.iterator();
			for (StateSystem<Marking,SignalEdge> stg : stgs) {
				Marking curMarking = itMarking.next();
				Set<Marking> nextStates = stg.getNextStates(curMarking, edge);

				//component does not activate edge
				if (nextStates.isEmpty()) {
					Integer sigName = edge.getSignal();
					StateSystem<Marking, SignalEdge> prodSTG = outputSignals.get(sigName);

					//if output signals is not activated in producing component, it cannot fire at all
					if (prodSTG == null || prodSTG.equals(stg)) // (**) 
						return new HashSet<List<Marking>>();

					//if output is not activated as input in listening component we have computation interference 
					//this is catched later, since the produdcing component might still not activate it, see preceeding if-clause 
					if (getInputSTGs(sigName).contains(stg))	// (*) 
						allInputsActivated = false;	

					//if it is all ok, add the unchaged marking, these lines are useles if (*) was true at least once
					//either in (**) an empty set is returned or an exception is thrown later
					Set<Marking> nothing = new HashSet<Marking>();
					nothing.add(curMarking);				
					possibleNextMarkings.add(nothing);
				}
				//if edge is activated, all possible results are added
				else 
					possibleNextMarkings.add(nextStates);		

			}

			//Computation interference
			if (! allInputsActivated)
				throw new ComputationInterferenceException("Computation interference for signal edge "+edge+
						" under marking "+state);

			return getAllCombinations(possibleNextMarkings);

			//break; //!! don't forget to uncomment if the above return is deleted/modified

			//internal signals with the same name can be included in more than one component
			//all theese occurences are independent from each other and lead to different states/markings
		case INTERNAL: 
			//check for each stg and the corresponding marking in state ...
			itMarking = state.iterator();
			for (StateSystem<Marking,SignalEdge> sys : stgs) {
				Marking curMarking = itMarking.next();
				//...if the event is activated, and for all resulting markings/states (none if its not activated)
				//generate a new following state
				for (Marking m : sys.getNextStates(curMarking, edge)) {
					List<Marking> lm = new LinkedList<Marking>();
					//...which leaves all other sub markings unchanged
					Iterator<Marking> itMarking2 = state.iterator();
					for (StateSystem<Marking,SignalEdge> sys2 : stgs) {
						Marking curMarking2 = itMarking2.next();
						if (sys==sys2)
							lm.add(m);
						else
							lm.add(curMarking2);
					}
					result.add(lm);
				}
			}

			return result;
			//break;

		default: return new HashSet<List<Marking>>();
		}
	}


	/**
	 * This method takes a list of sets, each containing possible alternatives for a list entry
	 * and returns every possible list obtained from choosing one alternative for each entry.
	 * 
	 * <p><b>Example:</b><br>
	 * Given the list [{a}, {b,c}, {b}, {a,c}] the following set would be returned:<br>
	 * { [a, b, b, a], [a, c, b, a], [a, b, b, c], [a, c, b, c] }.
	 * 
	 * @param <A>
	 * @param alternatives
	 * @return
	 */
	protected static <A> Set<List<A>> getAllCombinations(List<Set<A>> alternatives) {
		//The result
		Set<List<A>> result = new HashSet<List<A>>();

		//data structure holding a triple (by nested Pairs) of the current non-deterministic alternative, 
		//the corresponding Set to get a new iterator and the iterator itself for getting the next alternative
		List<Pair<A,Pair<Set<A>,Iterator<A>>>> curAlternative = 
			new LinkedList<Pair<A,Pair<Set<A>,Iterator<A>>>>();

		//initialise this structure with the first element, the corresponding set (is not changed)
		//and an iterator pointing to the second element (if it exists)
		for (Set<A> sm : alternatives ) {
			Iterator<A> itA = sm.iterator();
			curAlternative.add(new Pair<A,Pair<Set<A>,Iterator<A>>>
			(itA.next(), new Pair<Set<A>, Iterator<A>>(sm, itA)));
		}

		/*
		 * The algorithm works as follows: all alternatives are enumerated by 'adding 1' to the first element of alternatives
		 * This means, if we have reached entry a of alternatives and carry is set true (as it is initially)
		 * for a the next possible entry is choosen. If there are no more it is started with the first and carry stays true, i.e. 
		 * the same procedure is done for the next element after a and so on.
		 * If carry is still true after the last element of alternatives, all results have been bene enumerated. 
		 */
		boolean carry=false;
		while (! carry) {
			List<A> curNext = new LinkedList<A>();
			carry = true;

			for (Pair<A, Pair<Set<A>,Iterator<A>>> cure : curAlternative) {
				curNext.add(cure.a);
				if (carry) {
					if (cure.b.b.hasNext()) {
						cure.a = cure.b.b.next();
						carry = false;
					}
					else {
						cure.b.b = cure.b.a.iterator();
						cure.a = cure.b.b.next();
					}
				}
			}

			result.add(curNext);
		} 

		return result;
	}

	/**
	 * Guess what!?
	 */
	public class ComputationInterferenceException extends RuntimeException {
		private static final long serialVersionUID = -5968375897347525250L;

		public ComputationInterferenceException(String mes) {
			super(mes);
		}
	}

	public Map<Integer, Signature> getSignature() {
		return signatures;
	}

	public String getSignalName(Integer signal) {
		return signalNames.get(signal);
	}
















}
