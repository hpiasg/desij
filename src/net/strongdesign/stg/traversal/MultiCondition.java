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