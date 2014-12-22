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
