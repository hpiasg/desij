package net.strongdesign.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.strongdesign.stg.traversal.ConditionFactory;
import net.strongdesign.util.Pair;

public class DuplicateTransitionRemover {

	static class TransitionKey
	{
		TransitionKey(Collection<Pair<Place, Integer>> preset, Collection<Pair<Place, Integer>> postset, SignalEdge label)
		{
			this.preset = new ArrayList<Pair<Place, Integer>>(preset);
			this.postset = new ArrayList<Pair<Place, Integer>>(postset);
			Comparator<Pair<Place, Integer>> comparator = 
				new Comparator<Pair<Place, Integer>>()
				{
					@Override
					public int compare(Pair<Place, Integer> p1, Pair<Place, Integer> p2) {
						return p1.a.getIdentifier() - p2.a.getIdentifier();
					}
				};
			Collections.sort(this.preset, comparator);
			Collections.sort(this.postset, comparator);
			this.label = label;
		}
		private final ArrayList<Pair<Place,Integer>> preset;
		private final ArrayList<Pair<Place,Integer>> postset;
		private final SignalEdge label;
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TransitionKey))
				return false;
			TransitionKey other = (TransitionKey)obj;
			
			return listEquals(preset, other.preset)
				&& listEquals(postset, other.postset)
				&& equals(label, other.label);
		}
		
		private boolean equals(Object o1, Object o2) {
			return o1==o2 || o1!=null && o1.equals(o2);
		}

		@Override
		public int hashCode() {
			Object[] keyComponents = new Object[3];
			keyComponents[0] = preset.toArray();
			keyComponents[1] = postset.toArray();
			keyComponents[2] = label;
			
			return Arrays.deepHashCode(keyComponents);
		}

		private boolean listEquals(List<Pair<Place,Integer>> list1, List<Pair<Place,Integer>> list2) {
			if(list1.size() != list2.size())
				return false;
			for(int i=0;i<list1.size();i++)
				if(list1.get(i) != list2.get(i))
					return false;
			return true;
		}
	}
	
	private static Pair<Collection<Pair<Place, Integer>>,Collection<Pair<Place, Integer>>> getIncidentArcsInfo(Transition t)
	{
		Collection<Pair<Place, Integer>> preset = new ArrayList<Pair<Place, Integer>>();
		Collection<Pair<Place, Integer>> postset = new ArrayList<Pair<Place, Integer>>();
		for(Node n : t.getNeighbours())
		{
			int cv = t.getChildValue(n);
			int pv = t.getParentValue(n);
			if(cv>0)
				postset.add(Pair.getPair((Place)n, cv));
			if(pv>0)
				preset.add(Pair.getPair((Place)n, pv));
		}
		return Pair.getPair(preset, postset);
	}

	public static Collection<? extends Transition> removeDuplicateTransitions(STG stg, NodeRemover remover) {
		Set<Transition> result = new HashSet<Transition>();
		HashMap<TransitionKey, Transition> map = new HashMap<TransitionKey, Transition>();
		
		for(Transition t : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS))
		{
			boolean isDummy = stg.getSignature(t.getLabel().getSignal()) == Signature.DUMMY;
			Pair<Collection<Pair<Place, Integer>>, Collection<Pair<Place, Integer>>> incidentArcsInfo = getIncidentArcsInfo(t);
			TransitionKey key = new TransitionKey(incidentArcsInfo.a, incidentArcsInfo.b, isDummy?null:t.getLabel());
			if(map.containsKey(key))
			{
				result.add(t);
				remover.removeTransition(t);
			}
			else
				map.put(key, t);
		}
		return result;
	}


}
