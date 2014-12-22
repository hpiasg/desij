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

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class BitsetTree<Additional> {

	protected BitSet value;
	protected Additional additionalValue;
	protected Collection<BitsetTree<Additional>> subtree;
	
	
	public BitsetTree(BitSet value, Additional additionalValue) {
		this.value = value;
		this.additionalValue = additionalValue;
		subtree = new LinkedList<BitsetTree<Additional>>();		
	}
	
	
	public BitsetTree(BitSet value, Collection<BitsetTree<Additional>> newChilds) {
		this.value = value;
		this.subtree = newChilds;
		this.additionalValue = null;
	}


	public Additional getAdditionalValue() {
		return additionalValue;
	}
	
	public Collection<BitsetTree<Additional>> getSubtree() {
		return subtree;
	}
	public BitSet getValue() {
		return value;
	}
	
	
	
	public static <Entry> PresetTree<Entry, Object> buildTree(
    		Collection<Collection<Entry>> leafs,
    		int method) {
		
		Collection<Pair<Collection<Entry>, Object>> newLeafs = new LinkedList<Pair<Collection<Entry>,Object>>();
		
		for (Collection<Entry> e : leafs) {
			newLeafs.add(Pair.getPair(e, null));
		}
		
		return buildTreeLeafInfo(newLeafs, method);
	}
	
    public static <Entry, Additional> PresetTree<Entry, Additional> buildTreeLeafInfo(
    		Collection<Pair<Collection<Entry>, Additional>> leafs,
    		int method) {
    	
//    	Date start = new Date();
    	
    	Set<Entry> allEntries = new HashSet<Entry>();
    	for (Pair<Collection<Entry>,Additional> leaf : leafs) {
			allEntries.addAll(leaf.a);
		}
    	
   	
    	
    	Map<Integer, Entry> mIE = new HashMap<Integer, Entry>();
    	Map<Entry, Integer> mEI = new HashMap<Entry, Integer>();
    	int i = 0;
    	for (Entry e : allEntries) {
    		mIE.put(i, e);
    		mEI.put(e, i);
    		++i;    		
    	}
    	int size = i;
    	
    	LinkedList<BitsetTree<Additional>> treeLeafs = new LinkedList<BitsetTree<Additional>>();
    	for (Pair<Collection<Entry>, Additional> leaf : leafs) {
    		BitSet newBitSet = new BitSet(size);
    		for (Entry e : leaf.a) {
    			newBitSet.set(mEI.get(e));
    		}    		
    		
    		treeLeafs.add(new BitsetTree<Additional>(newBitSet, leaf.b));
    	}
    	
    	BitsetTree<Additional> root = null;
    	if (method == 0)
    		root = buildTreeTopDown(treeLeafs);
    	else if (method == 1)
    		root = buildTreePartialRandom(treeLeafs, 100);
    	
    	
//    	Date end = new Date();
    	
//    	System.out.println(end.getTime()-start.getTime()); // temporarily for debugging
    	
    	return convertToPresetTree(root, mIE);
    }
    
    
    public static <Entry, Additional> PresetTree<Entry, Additional> convertToPresetTree(BitsetTree<Additional> tree, Map<Integer, Entry> mIE) {
    	if (tree == null) return null;
    	
    	PresetTree<Entry, Additional> result = getTree(tree, mIE);
    	
    	for (BitsetTree<Additional> child : tree.getSubtree()) {
    		result.getSubtrees().add(convertToPresetTree(child, mIE));
    	}
    	
    	
    	return result;
    }
    
    
    private static <Entry, Additional> PresetTree<Entry, Additional> getTree(BitsetTree<Additional> tree, Map<Integer, Entry> mIE) {
    	ValueSet<Entry> valueSet = new ValueSet<Entry>();
    	BitSet bs = tree.getValue();
    	for(int j=bs.nextSetBit(0); j>=0; j=bs.nextSetBit(j+1)) {
    		valueSet.add(mIE.get(j));
    	}
    	return new PresetTree<Entry, Additional>(valueSet, new LinkedList<PresetTree<Entry, Additional>>(), tree.getAdditionalValue());
    }
    
    public static <Additional> BitsetTree<Additional> buildTreePartialRandom(List<BitsetTree<Additional>> leafs, int chunkSize) {
    	while (leafs.size() >= chunkSize) {
    		BitsetTree<Additional> sub = buildTreeTopDown(leafs.subList(0, chunkSize-1));
    		leafs.remove(0);
    		leafs.add(sub);	
    	}   	
    	
    	return buildTreeTopDown(leafs);
    }
    
    public static <Additional> BitsetTree<Additional> buildTreeTopDown(List<BitsetTree<Additional>> leafs) {
        	
        	/**
        	 * Stores all relevant details for the intersection of two nodes or leafs. 
        	 */
            class NodeIntersection implements Comparable<NodeIntersection> {
        		public int size;
        		public BitSet intersection;
        		public BitsetTree<Additional> a,b;
        		
        		public NodeIntersection(
        				BitsetTree<Additional> a, 
        				BitsetTree<Additional> b) {
        			
        			this.intersection 	= (BitSet) a.getValue().clone();
        			this.intersection.and(b.getValue());
        			
        			this.size			= intersection.cardinality();
        			this.a				= a;
        			this.b				= b;
        		}
        		
        		public NodeIntersection(BitSet intersection) {
        			this.intersection 	= intersection;
        			this.size			= intersection.cardinality();
        		}

        		public String toString () {
        			return "" + size + " - " + intersection + "\n";
        		}
        		
    			public int compareTo(NodeIntersection o) {
    				//!! this order means that elements with greater size are 'smaller' 
    				//we want to have multiple elements with the same size in a TreeSet
    				//so we must never return 0, it the sizes are equal the hashcodes are compared  
    				if (o.size != size)
    					return o.size - size;
    				
    				return o.hashCode() - hashCode();			
    			}
    			
        	}
            
            
            class AssociativeArray {
            	
            	protected Map<BitsetTree<Additional>, Map<BitsetTree<Additional>, NodeIntersection>> array =
                	new HashMap<BitsetTree<Additional>, Map<BitsetTree<Additional>, NodeIntersection>>();
            	
            	protected Map<BitsetTree<Additional>, Integer> keySet = new HashMap<BitsetTree<Additional>, Integer>();
            	
            	public NodeIntersection get(BitsetTree<Additional> a, BitsetTree<Additional> b) {
            		BitsetTree<Additional> aa,bb;
            		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
            		
            		Map<BitsetTree<Additional>, NodeIntersection> row = array.get(aa);
            		if (row == null) {
            			return null;
            		}
            		
            		return row.get(bb);
            	}
            	
            	public void put(BitsetTree<Additional> a, BitsetTree<Additional> b, NodeIntersection ni) {
            		BitsetTree<Additional> aa,bb;
            		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
            		
            		Map<BitsetTree<Additional>, NodeIntersection> row = array.get(aa);
            		if (row == null) {
            			row = new HashMap<BitsetTree<Additional>, NodeIntersection>();
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
            	
            	public NodeIntersection remove(BitsetTree<Additional> a, BitsetTree<Additional> b) {
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
            		
            		BitsetTree<Additional> aa,bb;
            		if (a.hashCode() > b.hashCode()) { aa = b; bb = a; } else { aa = a; bb = b; }
            		
            		Map<BitsetTree<Additional>, NodeIntersection> row = array.get(aa);
            		if (row != null) {
            			result = row.remove(bb);
            			if (row.size()==0) {
            				array.remove(aa);
            			}
            		}
            		
            		return result;

            	}
            	
            	public Set<BitsetTree<Additional>> keySet() {
            		return keySet.keySet();
            	}
            	
            }

          
            
            
            AssociativeArray main = new AssociativeArray();
           // NodeIntersection start = null;
            
            TreeSet<NodeIntersection> pq = new TreeSet<NodeIntersection>();
                    
            
//            int n=leafs.size()*leafs.size() / 2;
            //set up data structure initially
            for (BitsetTree<Additional> a : leafs )
            	for (BitsetTree<Additional> b : leafs ) {

            		if (a.hashCode() >= b.hashCode()) continue;
//            		System.out.println(n--);
            		
            		NodeIntersection newIntersection = new NodeIntersection(a, b); 
            		            	    
//            		add it into map
            		main.put(a, b, newIntersection);
            		
            		pq.add(newIntersection);
            	}
            
    	
            BitsetTree<Additional> result = null;
            
        	while (!pq.isEmpty() ) {
        		
//        		System.out.println(pq.size()); // temporarily for debugging!
        		NodeIntersection currentIntersection = pq.first();
        		pq.remove(currentIntersection);

        		
        		//	no intersection found, -> return DecoTree with one layer and empty root
            	if (currentIntersection.size == 0) {            		
            		Collection<BitsetTree<Additional>> newChilds = new LinkedList<BitsetTree<Additional>>();
            		newChilds.addAll(leafs);        		
            		BitsetTree<Additional> root = new BitsetTree<Additional>(new BitSet(), newChilds);
            		leafs.clear();
            		leafs.add(root);
    				return root;
            	}
       
            	
            	//find other nodes with the same 
            	
            	//otherwise generate common parent 
            	Collection<BitsetTree<Additional>> newChilds = new LinkedList<BitsetTree<Additional>>();
            	BitsetTree<Additional> a = currentIntersection.a;
            	BitsetTree<Additional> b = currentIntersection.b;
            	BitSet intersection = currentIntersection.intersection;
            	
            	newChilds.add(a);        	
    			newChilds.add(b);
            	        	
    			a.getValue().andNot(intersection);
            	b.getValue().andNot(intersection);
            	        	
            	BitsetTree<Additional> newTree = new BitsetTree<Additional>(intersection, newChilds);
            	
            	leafs.removeAll(newChilds);
            	leafs.add(newTree);		        		
            	result = newTree;
            
            	
            	
            	//update data structures
        		
            	List<BitsetTree<Additional>> remainingKeys =  new LinkedList<BitsetTree<Additional>>(main.keySet());
            	remainingKeys.remove(a);
            	remainingKeys.remove(b);
            	
            	main.remove(a, b);
            	
            	for (BitsetTree<Additional> node :  remainingKeys ) {
        			NodeIntersection iA = main.remove(node, a); 
        			NodeIntersection iB = main.remove(node, b);
        			
        			if (iA==null) {
        			}
        			pq.remove(iA);
        			pq.remove(iB);
        			
        			
        			BitSet inter = (BitSet) iA.intersection.clone();
        			inter.and(iB.intersection);
        			NodeIntersection newIntersection = new NodeIntersection(inter);
            		
        			newIntersection.a = node;
        			newIntersection.b = newTree;
        			
            		main.put(node, newTree, newIntersection);
            		
            		pq.add(newIntersection);

        		}
        	}

        	return result;
        	
        }
        
        	
  
    
    
   
    

}
