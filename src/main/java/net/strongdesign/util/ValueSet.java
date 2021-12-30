

package net.strongdesign.util;


 


import java.util.Collection;
import java.util.HashSet;



/**
 * TreeNode, node class in DecompositionTree
 * @author Rui
 */

public class ValueSet<E> extends HashSet<E> implements Cloneable{
    
    private static final long serialVersionUID = -7955228093725323564L;


	/**
     * Constructor of TreeNode
     */
    public ValueSet() {
    }
    
    /**
     * Constructor of TreeNode
     * @param c a elements set
     */
    public ValueSet(Collection<E> c) {
        super();
        //this.clear();
        this.addAll(c);
    }
    
    
    public ValueSet(E... c) {
        super();
        //this.clear();
        for (E e : c)
        	this.add(e);
    }
    
    
    
    
    /**
     *erzeuge ein Klone von diese TreeNode
     *@return richtig Clone von dieseTreeNode
     */
    public ValueSet<E> clone() {
        ValueSet<E> tn = new ValueSet<E>();
        for(E e:this)
            tn.add(e);
        return tn;
    }
    
    
    /**
     * compute subtraction of this TreeNode and treeNode
     * @param treeNode another TreeNode
     * @return a TreeNode build von remain elements
     */
    public  ValueSet<E> subtraction(ValueSet<E> treeNode) {
    	ValueSet<E> result = clone();
    	result.removeAll(treeNode);
    	
    	return result;
    }
    
    /**
     * computer combination of elements from this TreeNode und trn
     * @param treeNode
     * @return
     */
    public  ValueSet<E> combination(ValueSet<E> trn) {
    	ValueSet<E> result = clone();
    	result.addAll(trn);
    	
    	return result;
    }

    public  ValueSet<E> combination(E trn) {
    	ValueSet<E> result = clone();
    	result.add(trn);
    	
    	return result;
    }
    
    
    /**
     * compute subtraction of two TreeNode
     * @param B TreeNode
     * @param I TreeNode
     * @return a new TreeNode build from remain elements of B subtract I
     */
    public static <E> ValueSet<E> subtraction(ValueSet<E> B, ValueSet<E> I) {
        return B.subtraction(I);
    }
    
    /**
     * compute intersection of two TreeNode
     * @param B TreeNode
     * @param I TreeNode
     * @return a new TreeNode build from all intersect elements of B and I
     */
    public static <E> ValueSet<E> intersect(ValueSet<E> A, ValueSet<E> B) {
        return A.intersect(B);
    }
    
    /**
     * compute combination of two TreeNode
     * @param A TreeNode
     * @param B TreeNode
     * @return a new TreeNode build from all elements of A and B
     */
    public static <E> ValueSet<E> combination(ValueSet<E> A, ValueSet<E> B) {
        return A.combination(B);
    }
    
    
    /**
     * compute intersection of this Node and TreeNode B
     * @param B TreeNode
     * @return a new TreeNode build from all intersect elements of current Node and B
     */
    public  ValueSet<E> intersect(ValueSet<E> treeNode) {
    	ValueSet<E> result = clone();
    	result.retainAll(treeNode);
    	
    	return result;
    }
    
}
