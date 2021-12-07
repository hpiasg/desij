

package net.strongdesign.util;



import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;



public class PresetTreeTest {

	public static void main(String[] args) {


		LinkedList<Collection<Integer>> c = new LinkedList<Collection<Integer>>();

		List<Integer> baseSet = new LinkedList<Integer>();

		
		final int SIGNALS = 1000;

		int max = 0;

		for (int i = 1; i<=SIGNALS; ++i)
			baseSet.add(i);


		for (int n=1; n<=SIGNALS/2; ++n) {
			ValueSet<Integer> ns = new ValueSet<Integer>();
			c.add(ns);

			for (Integer k : baseSet)
				if (Math.random()>=0.05) {
					ns.add(k);
				}
			max += ns.size();
		}

		System.out.println("Max:    " + max);

//		c = getTree1();
//		System.out.println(c);

//		PresetTree<Integer, Object> tree = PresetTree.buildTree(c, 0);

//		int size = tree.getSize();
//		System.out.println("Combined: " + size + " (" + ((double)size/(double)max) + ")");
//		LinkedList<Collection<Integer>> leafs = getLeafs(tree);
//		System.out.println(c.containsAll(leafs) && leafs.containsAll(c));


//		PresetTree<Integer, Object> treeR = PresetTree.buildTree(c, 1);
//		int sizeR = treeR.getSize();
//		System.out.println("Random: " + sizeR + " (" + ((double)sizeR/(double)max) + ")");
//		LinkedList<Collection<Integer>> leafsR = getLeafs(tree);
//		System.out.println(c.containsAll(leafsR) && leafsR.containsAll(c));


//		Date date = new Date();
//		PresetTree<Integer, Object> treeTD = PresetTree.buildTree(c, 2);
//		int sizeTD = treeTD.getSize();
//		System.out.println("Top-Down: " + sizeTD + " (" + ((double)sizeTD/(double)max) + ")");
//		LinkedList<Collection<Integer>> leafsTD = getLeafs(tree);
//		System.out.println(leafsTD);
//		System.out.println(c.containsAll(leafsTD) && leafsTD.containsAll(c));
////		System.out.println(treeTD);
//		System.out.println(new Date().getTime()-date.getTime());

		Date date1 = new Date();
		PresetTree<Integer, Object> treeBS = BitsetTree.buildTree(c, 0);
		int sizeBS = treeBS.getSize();
		System.out.println("Bitset: " + sizeBS + " (" + ((double)sizeBS/(double)max) + ")");
		LinkedList<Collection<Integer>> leafsBS = getLeafs(treeBS);
//		System.out.println(leafsBS);
		System.out.println(c.containsAll(leafsBS) && leafsBS.containsAll(c));
//		System.out.println(treeBS);
		System.out.println(new Date().getTime()-date1.getTime());



	}

	public static <Entry> LinkedList<Collection<Entry>> getLeafs(PresetTree<Entry, Object> tree) {
		LinkedList<Collection<Entry>> result = new LinkedList<Collection<Entry>>();
		getLeafs(tree, new HashSet<Entry>(), result);
		return result;
	}

	public static <Entry> void getLeafs(
			PresetTree<Entry, Object> tree, 
			Collection<Entry> elements,
			LinkedList<Collection<Entry>> result) {


		elements.addAll(tree.getValue());


		if (tree.getSubtrees().isEmpty()) {
			result.add(new HashSet<Entry>(elements));
		}
		else {
			for (PresetTree<Entry, Object> child : tree.getSubtrees()) {
				getLeafs(child, elements, result);
			}

		}		

		elements.removeAll(tree.getValue());

	}


	public static LinkedList<Collection<Integer>> getTree1() { 

		LinkedList<Collection<Integer>> c = new LinkedList<Collection<Integer>>();
		
		c.add(new ValueSet<Integer>(2));
		c.add(new ValueSet<Integer>(1,2,3,4));
		c.add(new ValueSet<Integer>(1,2,3,4));
		c.add(new ValueSet<Integer>(1,2,3,4));	
		
		
		return c;
	}


}
