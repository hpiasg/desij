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

package net.strongdesign.stg.traversal;

import java.util.*;

import net.strongdesign.stg.traversal.Condition;
/**
 * 
 * 
 * <p>
 * <b>History: </b> <br>
 * 03.10.2004: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�fer
 */
public  class MultiCondition<T> extends  AbstractCondition<T> {
	public static final int AND = 1;
	public static final int OR = 2;
	public static final int XOR = 3;

	protected List<Condition<? super T>> conditions;
	int mode;

	public MultiCondition(int mode) {
		this.mode = mode;
		this.conditions = new LinkedList<Condition<? super T>>();
	}
	

	public void addCondition(Condition<? super T> condition) {
		conditions.add(condition);
	}

	public boolean fulfilled(T o) {
		if (conditions.isEmpty())
			return true;

		if (mode == AND) {
			for (Condition<? super T> actCondition : conditions) 
				if (! actCondition.fulfilled(o)) return false;
			return true;
		}
		if (mode == OR) {
			for (Condition <? super T> actCondition : conditions)
				if (actCondition.fulfilled(o)) return true;
		}
		if (mode == XOR) {
			int res = 0;
			for (Condition <? super T> actCondition : conditions)
				if (actCondition.fulfilled(o)) ++res;
			if (res == 1)
				return true;				
		}

		return false;
	}
}