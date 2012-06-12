package net.strongdesign.balsa.hcexpressionparser.terms;

import java.util.Map;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;

public class HCTransitionTerm extends HCChannelTerm implements HCSTGGenerator {
	public String wire;
	public String direction;
	
	public HCTransitionTerm(HCChannelCountController c) {
		super(c);
	}
	
	@Override
	public HCTerm expand(ExpansionType type) {
		if (type==ExpansionType.UP) return this;
		return null;
	}
	
	@Override
	public String toString() {
		int cnt = instanceNumber;
		if (cnt>0) {
			return channel+cnt+wire+direction;
		}
		return channel+wire+direction;
	}

	@Override
	public void generateSTG(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace) {
		
		Integer signal = stg.getSignalNumber(wire+channel);
		Signature sg = Signature.INPUT;
		
		if (wire.equals("r")&&sig.isActive(channel)||
			wire.equals("a")&&!sig.isActive(channel)
				) {
			sg = Signature.OUTPUT;
		}
				
		stg.setSignature(signal, sg);
		
		EdgeDirection ed = EdgeDirection.DONT_CARE;
		if (direction.equals("+")) ed=EdgeDirection.UP;
		if (direction.equals("-")) ed=EdgeDirection.DOWN;
		
		SignalEdge se = new SignalEdge(signal, ed);
		
		Transition t = stg.addTransition(se);
		inPlace.setChildValue(t, 1);
		t.setChildValue(outPlace, 1);
		
	}

}
