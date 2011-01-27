package net.strongdesign.stg;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import junit.framework.Assert;

import net.strongdesign.stg.STGUtil.Reason;
import net.strongdesign.stg.traversal.ConditionFactory;

public class TransitionQueueWithTreeMap implements TransitionQueue {

	static class Key
	{
		boolean isContractible;
		int cost;
		int id;
	}
	
	static class KeyComparator implements Comparator<Key>
	{
		@Override
		public int compare(Key k1, Key k2) {
			if(k1.isContractible != k2.isContractible)
				return k1.isContractible ? -1 : 1;
			if(k1.cost != k2.cost)
				return k1.cost - k2.cost;
			return k1.id - k2.id;
		}
	}
	
	private final TreeMap<Key, Transition> contract = new TreeMap<Key, Transition>(new KeyComparator());
	private final HashMap<Transition, Key> oldKeys = new HashMap<Transition, Key>();
	private int contractible_count = 0;
	
	private final STG stg;
	
	private static Key constructKey(STG stg, Transition transition)
	{
		Key key = new Key();
		key.isContractible = STGUtil.isContractable(stg, transition) == Reason.OK;
		key.cost = (transition.getChildren().size()-1) * (transition.getParents().size()-1)-1;
		key.id = transition.getIdentifier();
		return key;
	}
	
	public TransitionQueueWithTreeMap(STG stg)
	{
		this.stg = stg;
		for(Transition t : stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)))
		{
			add(t);
		}
	}

	private void add(Transition t) {
		Key key = constructKey(stg, t);
		if(key.isContractible)
			contractible_count++;
		Assert.assertFalse(oldKeys.containsKey(t));
		Assert.assertFalse(contract.containsKey(key));
		contract.put(key, t);
		oldKeys.put(t, key);
	}
	
	@Override
	public Transition pop() {
		
		Entry<Key, Transition> firstEntry = contract.firstEntry();
		if(firstEntry == null)
			return null;
		Key firstKey = firstEntry.getKey();
		if(!firstKey.isContractible)
			return null;
		
		Transition first = contract.remove(firstKey);
		oldKeys.remove(first);
		return first;
	}

	@Override
	public void registerAffectedNodes(Collection<Node> nodes) {
		for(Node n : nodes)
		{
			if(n instanceof Transition)
			{
				Transition t = (Transition)n;
				if(remove(t))
					add(t);
			}
		}
	}

	private boolean remove(Transition t) {
		Key oldKey = oldKeys.remove(t);
		if(oldKey != null)
		{
			if(oldKey.isContractible)
				contractible_count--;
			if(t!=contract.remove(oldKey))
				Assert.fail();
			return true;
		}
		return false;
	}

	@Override
	public void removeNodes(Collection<Node> nodes) {
		for(Node node : nodes)
			if(node instanceof Transition)
				remove((Transition)node);
	}

	@Override
	public int size() {
		return contract.size();
	}

	@Override
	public int getContractibleTransitionsCount() {
		return contractible_count;
	}
}
