

package net.strongdesign.statesystem.simulation;

import java.util.HashSet;
import java.util.Set;

public class RelationElement<A,B> {
	public final A a;
	public final B b;
	
	public RelationElement(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		
		if (!(o instanceof RelationElement)) return false;
		
		RelationElement el = (RelationElement) o;
		
		return (a.equals(el.a) && b.equals(el.b));
	}
	
	public int hashCode() {
		return a.hashCode() + 97*b.hashCode();
	}
	
	public String toString() {
		return "("+a+","+b+")";
	}
	
	
	public static <A,B> Set<RelationElement<A,B>> getCrossProduct(Set<A> setA, Set<B> setB) {
		Set<RelationElement<A,B>> result = new HashSet<RelationElement<A,B>>();
		
		for (A a : setA)
			for (B b : setB)
				result.add(new RelationElement<A,B>(a,b));
		
		return result;
	}
	
	public static <A,B> Set<RelationElement<A,B>> getCrossProduct(Set<A> setA, B b) {
		Set<RelationElement<A,B>> result = new HashSet<RelationElement<A,B>>();
		
		for (A a : setA)
			result.add(new RelationElement<A,B>(a,b));
		
		return result;
	}
	
	
	
	public static <A,B> Set<RelationElement<A,B>> getCrossProduct(A a, Set<B> setB) {
		Set<RelationElement<A,B>> result = new HashSet<RelationElement<A,B>>();
		
		for (B b : setB)
				result.add(new RelationElement<A,B>(a,b));
		
		return result;
	}
	
	public static <A,B> Set<RelationElement<A,B>> getCrossProduct(A a, B b) {
		Set<RelationElement<A,B>> result = new HashSet<RelationElement<A,B>>();
		
		result.add(new RelationElement<A,B>(a,b));
		
		return result;
	}
	
	

}
