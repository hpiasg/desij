
 
package net.strongdesign.stg.synthesis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.strongdesign.statesystem.StateSystem;
import net.strongdesign.stg.Marking;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.SignalState;
import net.strongdesign.stg.Signature;
import net.strongdesign.util.Pair;

/**
 * @author Dominic Wist
 * 
 * return value for getStateGraph()
 */
public class EncodedReachabilityGraph {
	
	private StateSystem<Marking, SignalEdge> stateSystem;
	private Map<Marking, SignalState> encoding;
	private STG stg;
	
	// global data for the BFS to reduce the call stack size during recursion
	private Set<Marking> bfsGoal;
	private Queue<Pair<List<SignalEdge>, Marking>> bfsFrontier; // queue of marking and the fired event before reaching this marking
	private Set<Marking> bfsVisited;
	private Map<Marking, List<SignalEdge>> bfsResult;
	
	public EncodedReachabilityGraph(StateSystem<Marking, SignalEdge> sys, Map<Marking, SignalState> encoding, STG stg) {
		this.stateSystem = sys;
		this.encoding = encoding;
		this.stg = stg;
	}
		
	
	public Set<Pair<List<SignalEdge>,List<SignalEdge>>> getCSCViolationTraces() throws STGException {
		
		Set<Pair<List<SignalEdge>, List<SignalEdge>>> result = new HashSet<Pair<List<SignalEdge>,List<SignalEdge>>>();
		Set<Set<Marking>> cscMarkings = getCSCConflictingMarkings();
		
		for (Set<Marking> conflictingMarkings : cscMarkings) {
			
			// getting the SHORTEST trace for each of the conflicting markings, but NOT ALL traces
			bfsGoal = new HashSet<Marking>(conflictingMarkings);
			bfsFrontier = new LinkedList<Pair<List<SignalEdge>,Marking>>();
			bfsFrontier.offer(new Pair<List<SignalEdge>, Marking>(new LinkedList<SignalEdge>(), stateSystem.getInitialState()));
			bfsVisited = new HashSet<Marking>();
			bfsResult = new HashMap<Marking, List<SignalEdge>>(conflictingMarkings.size());
			bfs(); // bfsResult contains the result
			
			// free bfsResult from interleaved traces resulting from concurrency
			deleteInterleavingTraces(); // !!!!! maybe not so good !!!!

			Set<List<SignalEdge>> historicTraces = new HashSet<List<SignalEdge>>();
			// bfsResult compare pair wise --> forget about one left trace, it should be a concurrent one
			buildResult: while (bfsResult.size() > 1) {
				
				Iterator<Marking> markingIter = bfsResult.keySet().iterator();
				Marking firstMarking = markingIter.next();
				Marking secondMarking;
				boolean inConflict = false;
				
				do {
					secondMarking = markingIter.next();
					inConflict = haveCSCViolation(
							new Pair<SignalState, Set<SignalEdge>>(encoding.get(firstMarking), stateSystem.getEvents(firstMarking)), 
							new Pair<SignalState, Set<SignalEdge>>(encoding.get(secondMarking), stateSystem.getEvents(secondMarking))
							);
				} while (!inConflict && markingIter.hasNext());
				
				if (!inConflict) {
					bfsResult.remove(firstMarking); // since it is not in CSC conflict with any marking
					continue;
				}
				
				List<SignalEdge> trace1 = bfsResult.remove(firstMarking);
				List<SignalEdge> trace2 = bfsResult.remove(secondMarking);
				
				// proof for type I conflict of trace1 and trace2
				List<SignalEdge> intersection = buildIntersection(trace1, trace2);
								
				if (trace1.size() == intersection.size() || trace2.size() == intersection.size()) {
					// We have a type I conflict!
					result.add(new Pair<List<SignalEdge>, List<SignalEdge>>(trace1, trace2));
				}
				else {
					// Do we have a concurrent conflict?
					
					// Is intersection as trace in bfsResult? Then we have type I conflict between intersection and trace2
					for (Marking m : bfsResult.keySet())
						if (equalTrace(bfsResult.get(m), intersection)) {
							firstMarking = m; // new firstMarking, ie. refuse old one
							trace1 = bfsResult.get(firstMarking); // new trace1, ie. refuse old one
							break; // now we have a new type I conflict
						}
					
					// Is intersection as trace in historicTraces? Then a type I conflict between intersection and traceX 
					// was already solved, ie. trace1 and trace2 are in concurrent conflict with traceX
					for (List<SignalEdge> oldTrace: historicTraces) 
						if (equalTrace(oldTrace, intersection)) 
							continue buildResult; // forget about trace1 and trace2
					
					// if it is not type I, it is type II conflict, but not a concurrent conflict
					result.add(new Pair<List<SignalEdge>, List<SignalEdge>>(trace1, trace2));
				}
				
				// Store! in order to avoid concurrent conflicts later
				historicTraces.add(trace1);
				historicTraces.add(trace2);
			}
		
		}
		
		return result;
	}
	
	private void deleteInterleavingTraces() {
		
		Set<Marking> markingsToDelete = new HashSet<Marking>();
		
		for (Marking m : bfsResult.keySet()) {
			List<SignalEdge> interleavingTrace = bfsResult.get(m);
			if (interleavingTrace.isEmpty()) continue;
			
			List<SignalEdge> interleavingTraceWorkingCopy = new LinkedList<SignalEdge>(bfsResult.get(m));
			
			for (List<SignalEdge> trace : bfsResult.values()) 
				if ( (trace != interleavingTrace) && (trace.size() <= interleavingTrace.size()) )
					interleavingTraceWorkingCopy.removeAll(trace);
			
			if (interleavingTraceWorkingCopy.isEmpty()) // interleavingTrace is completely covered by other traces
				markingsToDelete.add(m);
		}
		
		for (Marking m : markingsToDelete)
			bfsResult.remove(m);
		
	}


	/**
	 * Helper Rountine for getCSCViolationTraces()
	 * @param trace1
	 * @param trace2
	 * @return - whether trace1 and trace2 specify the same sequence of signal edges
	 */
	private boolean equalTrace(List<SignalEdge> trace1, List<SignalEdge> trace2) {
		
		if (trace1.size() != trace2.size())
			return false;
		
		Iterator<SignalEdge> trace1Iterator = trace1.iterator();
		Iterator<SignalEdge> trace2Iterator = trace2.iterator();
		
		while (trace1Iterator.hasNext()) { // then trace2Iterator as well ...
			if (trace1Iterator.next() != trace2Iterator.next())
				return false;
		}

		return true;
	}


	/**
	 * Helper Routine for getCSCViolationTraces()
	 * @param trace1
	 * @param trace2
	 * @return - the intersection!
	 */
	private List<SignalEdge> buildIntersection(List<SignalEdge> trace1, List<SignalEdge> trace2) {
		
		Iterator<SignalEdge> trace1Iterator = trace1.iterator();
		Iterator<SignalEdge> trace2Iterator = trace2.iterator();
		
		List<SignalEdge> intersection = new LinkedList<SignalEdge>();
		
		while (trace1Iterator.hasNext() && trace2Iterator.hasNext()) {
			SignalEdge nextElement = trace1Iterator.next();
			if ( nextElement == trace2Iterator.next() ) 
				intersection.add(nextElement);
			else 
				break; // common starting part of the traces has finshed
		}
						
		return intersection;
	}


	
	/**
	 * Helper Routine for getCSCViolationTraces()
	 * 
	 * @throws STGException - if there is indeterminism in the reachability graph or an activated event leads to no marking
	 */
	private void bfs() throws STGException {
		// Assert.assertTrue(bfsGoal != null && bfsFrontier != null && bfsVisited != null && bfsResult != null);
		if (bfsFrontier.isEmpty())
			throw new STGException("Reachable marking(s) " + bfsGoal.toString() + " not found");
		
		Pair<List<SignalEdge>, Marking> curMarking = bfsFrontier.poll();
		bfsVisited.add(curMarking.b);
		
		if (bfsGoal.remove(curMarking.b)) {
			bfsResult.put(curMarking.b, curMarking.a);
			if (bfsGoal.isEmpty()) return; // all shortest traces to conflicting marking are found
		}
		
		// expand the queue
		for (SignalEdge edge : stateSystem.getEvents(curMarking.b)) {
			Marking nextMarking = stateSystem.getNextStates(curMarking.b, edge).iterator().next(); // should be extacly one next State
			if ( !bfsVisited.contains(nextMarking) ) {
				List<SignalEdge> newTrace = new LinkedList<SignalEdge>(curMarking.a);
				if (edge != null) newTrace.add(edge);
				bfsFrontier.offer(new Pair<List<SignalEdge>, Marking>(newTrace, nextMarking));
			}
		}
		
		bfs();
	}
	
	public Set<Set<Marking>> getCSCConflictingMarkings() {
		
		Set<Set<Marking>> result = new HashSet<Set<Marking>>();
						
		Set<Marking> conflictingMarkings;
		for (Marking currentMarking : this.encoding.keySet()) {
			conflictingMarkings = null;
			
			for (Marking reachableMarking : this.encoding.keySet()) 
				if (currentMarking != reachableMarking && 
						haveCSCViolation(
								new Pair<SignalState, Set<SignalEdge>>(encoding.get(currentMarking), stateSystem.getEvents(currentMarking)),
								new Pair<SignalState, Set<SignalEdge>>(encoding.get(reachableMarking), stateSystem.getEvents(reachableMarking))
								) ) { // CSC violation?
					if (conflictingMarkings == null) {
						conflictingMarkings = new HashSet<Marking>();
						conflictingMarkings.add(currentMarking);
						conflictingMarkings.add(reachableMarking);
					}
					else {
						conflictingMarkings.add(reachableMarking);
						// maybe reachableMarking is only in conflict with currentMarking but not with different markings in conflictingMarkings?
						// but we will proof it later when giving back the traces ...
					}
				}
			
			if (conflictingMarkings != null) {
				// join all conflicting sets representing the same state code
				SignalState currentState = encoding.get(currentMarking);
				for (Set<Marking> differentConflictMarking : result) 
					if (encoding.get(differentConflictMarking.iterator().next()).equals(currentState)) { // differentConflictMarking cannot be empty
						conflictingMarkings.addAll(differentConflictMarking);
						result.remove(differentConflictMarking);
						break; // there can only be one set in result having the same statecode, because it is tested in every iteration
					}
				result.add(conflictingMarkings);		
			}
		}
				
		return result;
	}
	
	private boolean haveCSCViolation(Pair<SignalState, Set<SignalEdge>> state1, Pair<SignalState, Set<SignalEdge>> state2) {
		
		if (! state1.a.equals(state2.a)) return false; // different state codes --> no CSC violation
		
		// now we have a USC violation, i.e. state1 and state2 have the same state codes
		
		Set<SignalEdge> activatedEdgesState1 = new HashSet<SignalEdge>(state1.b);
		Set<SignalEdge> activatedEdgesState2 = new HashSet<SignalEdge>(state2.b);
		activatedEdgesState1.removeAll(state2.b);
		activatedEdgesState2.removeAll(state1.b);
		if (activatedEdgesState1.isEmpty() && activatedEdgesState2.isEmpty()) return false; // just a USC conflict
		
		for (SignalEdge currentEdge : activatedEdgesState1)
			if (stg.getSignature(currentEdge.getSignal()) == Signature.OUTPUT ||
					stg.getSignature(currentEdge.getSignal()) == Signature.INTERNAL)
				return true; // state1 activates an output or internal edge which is not activated by state2
		
		for (SignalEdge currentEdge : activatedEdgesState2)
			if (stg.getSignature(currentEdge.getSignal()) == Signature.OUTPUT ||
					stg.getSignature(currentEdge.getSignal()) == Signature.INTERNAL)
				return true; // state2 activates an output or internal edge which is not activated by state1
		
		return false; // no output or internal edge is activated in only one of these states --> no CSC conflict
	}

}
