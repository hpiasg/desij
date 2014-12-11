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


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;



/**
 * @author Rui
 */
public class PresetTree<Entry, Additional> {
    
    protected ValueSet<Entry> value;
    protected Additional additionalValue;
    protected Collection<PresetTree<Entry, Additional>> subtree;
    protected PresetTree<Entry, Additional> parent = null;
    
    
    /**
     *variante von DecompositionTree(TreeNode value, Collection<Decomposition> subtree)
     *@param value Wurzelnode f�r aktuellen Baum
     */
    public PresetTree(ValueSet<Entry> value) {
        this(value, new HashSet<PresetTree<Entry, Additional>>(), null);
    }
    
    
    public PresetTree(ValueSet<Entry> value, Collection<PresetTree<Entry, Additional>> subtree) {
        this(value, subtree, null);
    }
    
    
    public PresetTree(ValueSet<Entry> value, Collection<PresetTree<Entry, Additional>> subtree, Additional addO) {
        this.value=value;
        this.subtree = subtree;
        this.additionalValue = addO;
	}

    

	

	public PresetTree<Entry, Additional> getParent() {
		return parent;
	}


	public void setParent(PresetTree<Entry, Additional> parent) {
		this.parent = parent;
	}


    
   /**
    * Returns the number of leafs of this (sub)tree
    * @return
    */
    public int getNumberOfLeafs() {
    	//recursion fininshed in a leaf, returning 1    	
    	if (subtree.size() == 0)
    		return 1;
    	
    	//recursively add number of leafs of the child nodes
    	int result = 0;
    	for (PresetTree<Entry,Additional> tree : subtree)
    		result += tree.getNumberOfLeafs();
    	
    	return result;
    }
    
    /**
     * Returns the number of elements in all descandent nodes.
     * @return
     */
    public int getNumberOfElements() {
    	//recursion fininshed in a leaf, returning 1    	
    	if (subtree.size() == 0)
    		return value.size();
    	
    	//recursively add number of leafs of the child nodes
    	int result = 0;
    	for (PresetTree<Entry,Additional> tree : subtree)
    		result += tree.getNumberOfElements();
    	
    	return result;
    }
    
    public ValueSet<Entry> getValue() {
        return value;
    }
    
    
    public Additional getAdditionalValue() {
        return additionalValue;
    }
    
    public void setAdditionalValue(Additional addO) {
        additionalValue = addO;
    }
    
    
  
    public String toString() {
        StringBuilder res = new StringBuilder("\n");
        LinkedList<Boolean> tabs = new LinkedList<Boolean>();
        toString(this, tabs, res);
        
        return res.toString();
    }
    
    private void toString(PresetTree<Entry, Additional> tree, LinkedList<Boolean> tabs, StringBuilder res) {
    	int j=0;
    	for (Boolean tab : tabs) {
    		++j;
    		if (j==tabs.size()) {
        		if (tab) 
        			res.append(" ├─");
        		else
        			res.append(" └─"); 
    		}
    		else {
        		if (tab) 
        			res.append(" │ ");
        		else
        			res.append("   "); 
    		}
    	}    	
    	
    	res.append(tree.getValue().toString());
    	if (tree.additionalValue != null) res.append("("+ tree.additionalValue +")");
    	res.append("\n");
    	
    	tabs.addLast(true);
    	int i=0;
    	for (PresetTree<Entry, Additional> child : tree.getSubtrees()) {
    		++i;
    		if (i == tree.getSubtrees().size() && i!=1) {
    			tabs.removeLast();
    			tabs.addLast(false);
    		}

    		toString(child, tabs, res);
    	}
    	tabs.removeLast();
    }
    
    public Collection<PresetTree<Entry, Additional>> getSubtrees() {
        return subtree;
    }
 
    
    
    public int getSize() {
    	int result = value.size();
    	
    	for (PresetTree<Entry, Additional> child : subtree) {
    		result += child.getSize();
    	}
    	
    	return result;
    }
    
    
    /**
     * Builds a new 'optimal' preset tree with the bottom-up algorithm of V. Khomenko.
     * @param <Entry>
     * @param <Additional>
     * @param leafs The initial entries of the leafs. 
     * @return
     */
    public static <Entry, Additional> PresetTree<Entry, Additional> buildTree(Collection<Collection<Entry>> leafs, int method) {
    	
    	LinkedList<PresetTree<Entry, Additional>> leafNodes = new LinkedList<PresetTree<Entry, Additional>>();
    	
    	for (Collection<Entry> currentLeaf : leafs)
    		leafNodes.add(new PresetTree<Entry, Additional>(new ValueSet<Entry>(currentLeaf)));
    		
    	
    	switch (method) {
    	
    	case 0: return buildPresetTree(leafNodes);
    	case 1: return buildPresetTreeRandom(leafNodes); 
    	case 2: return buildPresetTreeBottomUp(leafNodes); 
    	}
    	
    	return null;
    	
    	
    	
    }

    
    
    
    public static <Entry, Additional> PresetTree<Entry, Additional> buildPresetTree(
    		LinkedList<PresetTree<Entry, Additional>> leafs) {
    	
    	while (leafs.size() > 50) {
    		buildPresetTreeBottomUp(leafs.subList(0, 49));    
    		PresetTree<Entry, Additional> head = leafs.poll();
    		leafs.addLast(head);
    		
    	}   	
    	
    	return buildPresetTreeBottomUp(leafs);
    }
    	
    	


    
    
    /**
     * Builds a new 'optimal' preset tree with the bottom-up algorithm of V. Khomenko.
     * @param <Entry>
     * @param <Additional>
     * @param leafs The initial entries of the leafs together with some additional information. 
     * @return
     */
    public static <Entry, Additional> PresetTree<Entry, Additional> buildTreeLeafInfo(
    		Collection<Pair<Collection<Entry>, Additional>> leafs, int method) {
    	
    	LinkedList<PresetTree<Entry, Additional>> leafNodes = new LinkedList<PresetTree<Entry, Additional>>();
    	
    	for (Pair<Collection<Entry>, Additional> currentLeaf : leafs) {
    		PresetTree<Entry, Additional> newTree =  new PresetTree<Entry, Additional>(new ValueSet<Entry>(currentLeaf.a));
    		newTree.setAdditionalValue(currentLeaf.b);
    		leafNodes.add(newTree);
    	}
    		
    	

    	switch (method) {
    	
    	case 0: return buildPresetTree(leafNodes);
    	case 1: return buildPresetTreeRandom(leafNodes); 
    	case 2: return buildPresetTreeBottomUp(leafNodes); 
    	}
    	
    	return null;
    }
 
	
    public static <Entry, Additional> PresetTree<Entry, Additional> buildPresetTreeRandom(
    		LinkedList<PresetTree<Entry, Additional>> leafs) {
    	
//    	Collections.shuffle(leafs);
    	
    	PresetTree<Entry, Additional> result = null;
    	
    	
    	int N=10;
    	
    	
    	while (leafs.size() >= 2) {

    		int max = -1;
    		ValueSet<Entry> intersection = null;
    		PresetTree<Entry, Additional> first = null, second = null;
    		
    		int i1 = 0;
    		for (Iterator<PresetTree<Entry, Additional>> it1 = leafs.iterator(); it1.hasNext() && i1<N ; ) {
    			++i1;
    			PresetTree<Entry, Additional> t1 = it1.next();
    			
    			int i2 = 0;
    			for (Iterator<PresetTree<Entry, Additional>> it2 = leafs.iterator(); it2.hasNext() && i2<N ; ) {
    				++i2;
    				PresetTree<Entry, Additional> t2 = it2.next();
    				
    				if (i1==i2) continue;
    					
    				
    				ValueSet<Entry> in = ValueSet.intersect(t1.value, t2.value);
    				if (in.size() > max) {
    					first = t1;
    					second = t2;
    					max = in.size();
    					intersection = in;
    				}
    			}
    		}
    		
    	
    		
    		leafs.remove(first);
    		leafs.remove(second);
    		
    		
    		first.value.removeAll(intersection);
    		second.value.removeAll(intersection);

    		PresetTree<Entry, Additional> newTree = new PresetTree<Entry, Additional>(intersection);
    		newTree.subtree.add(first);
    		newTree.subtree.add(second);

    		leafs.addLast(newTree);

    		result = newTree;
    		
    	}
    	
    	
    	return result;
    	
    }
    
    public static <Entry, Additional> PresetTree<Entry, Additional> buildPresetTreeBottomUp(
    		Collection<PresetTree<Entry, Additional>> leafs) {
    	
    	
    	
    	/**
    	 * Stores all relevant details for the intersection of two nodes or leafs. 
    	 */
        class NodeIntersection implements Comparable<NodeIntersection> {
    		public int size;
    		public ValueSet<Entry> intersection;
    		public PresetTree<Entry, Additional> a,b;
    		
    		public NodeIntersection(
    				PresetTree<Entry, Additional> a, 
    				PresetTree<Entry, Additional> b) {
    			
    			this.intersection 	= ValueSet.intersect(a.getValue(), b.getValue());
    			this.size			= intersection.size();
    			this.a				= a;
    			this.b				= b;
    		}
    		
    		public NodeIntersection(ValueSet<Entry> intersection) {
    			this.intersection 	= intersection;
    			this.size			= intersection.size();
    		}

    		public String toString () {
    			return "" + size + " - " + intersection + "\n";
    		}
    		
			public int compareTo(NodeIntersection o) {
				//!! this order means that elements with greater size are 'smaller' 
				//we want to have multiple elements with the same size in a TreeSet
				//so we may never return 0 
				if (o.size != size)
					return o.size - size;
				
				return o.hashCode() - hashCode();			
			}
			
    	}
        
        
        class AssociativeArray {
        	
        
        	
        	protected Map<PresetTree<Entry, Additional>, Map<PresetTree<Entry, Additional>, NodeIntersection>> array =
            	new HashMap<PresetTree<Entry, Additional>, Map<PresetTree<Entry, Additional>, NodeIntersection>>();
        	
        	protected Map<PresetTree<Entry, Additional>, Integer> keySet = new HashMap<PresetTree<Entry, Additional>, Integer>();
        	
        	public NodeIntersection get(PresetTree<Entry, Additional> a, PresetTree<Entry, Additional> b) {
        		PresetTree<Entry, Additional> aa,bb;
        		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
        		
        		Map<PresetTree<Entry, Additional>, NodeIntersection> row = array.get(aa);
        		if (row == null) {
        			return null;
        		}
        		
        		return row.get(bb);
        	}
        	
        	public void put(PresetTree<Entry, Additional> a, PresetTree<Entry, Additional> b, NodeIntersection ni) {
        		PresetTree<Entry, Additional> aa,bb;
        		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
        		
        		Map<PresetTree<Entry, Additional>, NodeIntersection> row = array.get(aa);
        		if (row == null) {
        			row = new HashMap<PresetTree<Entry, Additional>, NodeIntersection>();
        			array.put(aa, row);
        		}

        		if (row.put(bb, ni)==null) {

        			Integer vA = keySet.get(a);
        			if (vA==null) vA=0;
        			vA=vA+1;
        			keySet.put(a, vA);

        			Integer vB = keySet.get(b);
        			if (vB==null) vB=0;
        			vB=vB+1;
        			keySet.put(b, vB);
        		}
        	}
        	
        	public NodeIntersection remove(PresetTree<Entry, Additional> a, PresetTree<Entry, Additional> b) {
        		Integer vA = keySet.get(a);
        		if (vA!=null) {
        			vA=vA-1;
        			if (vA==0) keySet.remove(a);
        			else keySet.put(a, vA);
        		}
        		
        		Integer vB = keySet.get(b);
        		if (vB!=null) {
        			vB=vB-1;
        			if (vB==0) keySet.remove(b);
        			else keySet.put(b, vB);
        		}
        		
        		NodeIntersection result = null;
        		
        		PresetTree<Entry, Additional> aa,bb;
        		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
        		
        		Map<PresetTree<Entry, Additional>, NodeIntersection> row = array.get(aa);
        		if (row != null) {
        			result = row.remove(bb);
        			if (row.size()==0) {
        				array.remove(aa);
        			}
        		}
        		
        		return result;

        	}
        	
        	public Set<PresetTree<Entry, Additional>> keySet() {
        		return keySet.keySet();
        	}
        	
        }

      
        AssociativeArray main = new AssociativeArray();
       // NodeIntersection start = null;
        
        TreeSet<NodeIntersection> pq = new TreeSet<NodeIntersection>();
                
        
 
        //set up data structure initially
        for (PresetTree<Entry, Additional> a : leafs )
        	for (PresetTree<Entry, Additional> b : leafs ) {

        		if (a.hashCode() >= b.hashCode()) continue;
        		
        		NodeIntersection newIntersection = new NodeIntersection(a, b); 
        	
//        		add it into map
        		main.put(a, b, newIntersection);
        		
        		pq.add(newIntersection);
        		
        		
        		/*
        		//sort it into priority queue
        		int size = newIntersection.size;
        		NodeIntersection current = start;
        		NodeIntersection last = null;

        		while (current != null && current.size > size) {
        			last = current;
        			current = current.next;
        		}

        		if (current!=null) {
        			newIntersection.next = current;
        			current.previous = newIntersection;
        		} 
        		if (last!=null){
        			newIntersection.previous = last;
        			last.next = newIntersection;
        		}
        		else {
        			start = newIntersection;
        		}
        		
        		*/
        		
        		
        	}
        
    
            	
        PresetTree<Entry, Additional> result = null;
        
        
    	while (!pq.isEmpty() ) {

    		NodeIntersection currentIntersection = pq.first();
    		pq.remove(currentIntersection);
//    		start = currentIntersection.next;
//    		if (start != null) {
//    			start.previous = null;
//    		}
    		
    		//	no intersection found, -> return DecoTree with one layer and empty root
        	if (currentIntersection.size == 0) {
        		
        		Collection<PresetTree<Entry, Additional>> newChilds = new LinkedList<PresetTree<Entry, Additional>>();
        		newChilds.addAll(leafs);        		
        		PresetTree<Entry, Additional> root = new PresetTree<Entry, Additional>(new ValueSet<Entry>(), newChilds);
        		leafs.clear();
        		leafs.add(root);
				return root;
        	}
        		
        	
        	//otherwise generate common parent 
        	Collection<PresetTree<Entry, Additional>> newChilds = new LinkedList<PresetTree<Entry, Additional>>();
        	PresetTree<Entry, Additional> a = currentIntersection.a;
        	PresetTree<Entry, Additional> b = currentIntersection.b;
        	ValueSet<Entry> intersection = currentIntersection.intersection;
        	
        	newChilds.add(a);        	
			newChilds.add(b);
        	        	
			a.getValue().removeAll(intersection);
        	b.getValue().removeAll(intersection);
        	        	
        	PresetTree<Entry, Additional> newTree = new PresetTree<Entry, Additional>(intersection, newChilds);
        	
        	leafs.removeAll(newChilds);
        	leafs.add(newTree);		
    		
        	result = newTree;
        
        	
        	
        	//update data structures
    		
        	List<PresetTree<Entry, Additional>> remainingKeys =  new LinkedList<PresetTree<Entry, Additional>>(main.keySet());
        	remainingKeys.remove(a);
        	remainingKeys.remove(b);
        	
        	main.remove(a, b);
        	
        	for (PresetTree<Entry, Additional> node :  remainingKeys ) {
    			NodeIntersection iA = main.remove(node, a); 
    			NodeIntersection iB = main.remove(node, b);
    			
    			if (iA==null) {
    			}
    			pq.remove(iA);
    			pq.remove(iB);
    			
    			
    			NodeIntersection newIntersection = new NodeIntersection(ValueSet.intersect(iA.intersection, iB.intersection));
        		

    			newIntersection.a = node;
    			newIntersection.b = newTree;
    			
        		main.put(node, newTree, newIntersection);
        		
        		pq.add(newIntersection);
        		
//        		//sort it into priority queue
//        		int size = newIntersection.size;
//        		NodeIntersection current = start;
//        		NodeIntersection last = null;
//
//        		while (current != null && current.size > size) {
//        			last = current;
//        			current = current.next;
//        		}
//
//        		if (current!=null) {
//        			newIntersection.next = current;
//        			current.previous = newIntersection;
//        		} 
//        		if (last!=null){
//        			newIntersection.previous = last;
//        			last.next = newIntersection;
//        		}
//        		else {
//        			start = newIntersection;
//        		}
    		}
    	}
    	

    	return result;
    	
    }
    
    
    
   
    
	

}
