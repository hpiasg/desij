

/*
 * Created on 29.09.2004
 *
 */
package net.strongdesign.stg.traversal;


import java.util.*;
import net.strongdesign.stg.*;
import net.strongdesign.util.*;
/**
 * @author mark and dominic
 * 
 */
public abstract class GraphOperations {
	
	public static SortedSet<List<Node>> findSimplePaths(
			Node startNode,
			Node endNode,
			Condition<Node> abortPath) {
		
		SortedSet<List<Node>> resultingPaths = 
			new TreeSet<List<Node>>(new CollectionSizeComparator()); // return value
		List<Node> visitedNodes = new ArrayList<Node>();
		
		visitedNodes.add(startNode);
		if (startNode == endNode) {
			resultingPaths.add(visitedNodes);
		}
		else {
			for (Node childOfStart : startNode.getChildren())
				if (!visitedNodes.contains(childOfStart))
					dfsSimplePath(childOfStart, endNode, abortPath, visitedNodes, resultingPaths);
		}
		
		return resultingPaths;
	}
	
	private static void dfsSimplePath(
			Node node,
			Node endNode,
			Condition<Node> abortPath,
			List<Node> visitedNodes, 
			Set<List<Node>> results) {
		
		visitedNodes.add(node);
		
		if (node == endNode) {
			// add a shallow copy of visitedNodes to 'results' as a resulting simple path
			results.add(new ArrayList<Node>(visitedNodes)); // take a copy, visitedNodes cannot be null
			visitedNodes.remove(node); // removes last element
			return;
		}
		
		if (abortPath.fulfilled(node)) { // startNode will never be checked!
			visitedNodes.remove(node); // remove last element
			return;
		}
				
		for (Node child : node.getChildren()) 
			if (!visitedNodes.contains(child)) 
				dfsSimplePath(child, endNode, abortPath, visitedNodes, results);
		
		visitedNodes.remove(node); // removes last element		
	}
	
	public static Pair<Integer, List<Node>> findShortestPath (
			Node startNode, 
			Node endNode,
			Condition<Node> abortPath,
			Collector<Place, Integer> placeCost,
			Collector<Transition, Integer> transitionCost,		
			Collector<Pair<Node,Node>, Integer> arcCost) {
		
		TreeSet<Pair<Integer,List<Node>>> currentPaths = new TreeSet<Pair<Integer,List<Node>>>();
		
		List<Node> startList = new LinkedList<Node>();
		startList.add(startNode);
		currentPaths.add(new Pair<Integer,List<Node>>(new Integer(0), startList  ));
	
		
		while (currentPaths.size() > 0) {	
			Pair<Integer,List<Node>> actPath = currentPaths.first();
			currentPaths.remove(actPath);
			Integer cost = actPath.a;
			List<Node> path = actPath.b;
			
			Node lastNode = path.get(path.size()-1); 
			if (lastNode == endNode ) return actPath;
			
			if (abortPath.fulfilled(lastNode)) continue;
			
			for (Node newNode : lastNode.getChildren()) {
				List<Node> newPath = new LinkedList<Node>(path);
				if (newPath.contains(newNode)) continue;
				newPath.add(newNode);
				Integer nodeCost=null, newCost;
				Integer newarcCost = arcCost.operation(new Pair<Node,Node>(lastNode, newNode));
				
				if (newNode instanceof Place) 
					nodeCost =  placeCost.operation((Place)newNode);
				if (newNode instanceof Transition) 
					nodeCost =  transitionCost.operation((Transition)newNode);
				
				newCost = new Integer(cost.intValue()+nodeCost.intValue()+newarcCost.intValue());
				currentPaths.add(new Pair<Integer,List<Node>>(newCost, newPath));				
			}	
		}			
		
		return null;
	}
	
	
	public static List<Vector<Node>> getCyclesWith(Set<Node> nodes) {
		List<Vector<Node>> result = new LinkedList<Vector<Node>>();
		if (nodes.isEmpty())
			return result;


		Node finalNode = nodes.iterator().next();
		nodes.remove(finalNode);
		Stack<Node> path = new Stack<Node>();
		path.add(finalNode);
		
		getCyclesWith(path, nodes, result);
		
		nodes.add(finalNode);
		return result;
	}
	
	private static void getCyclesWith(Stack<Node> path, Set<Node> targets, List<Vector<Node>> cycles) {
		//PRECONDITION: path contains a regular path (witout loops)
		
		//follow each possible child
		for (Node child : path.peek().getChildren()) {
			//found an undesired cycle?
			//NO!
			if (! path.contains(child)) {
				//the start node is at first position of path, therefore in the best case a node of targtes was found
				
				//target found!, remove it and call with new target set
				if (targets.contains(child)) {
					targets.remove(child);
					path.push(child);
					getCyclesWith(path, targets, cycles);
					path.pop();
					targets.add(child);
				}				
				//no target found, this is the standard case
				else {
					path.push(child);
					getCyclesWith(path, targets, cycles);
					path.pop();
				}
			}
			//found a node of the path, is it the start node?
			else if (child == path.get(0)) //yes it is! the cycle was close 
				cycles.add( new Vector<Node>(path)  );
		}
	}
}
