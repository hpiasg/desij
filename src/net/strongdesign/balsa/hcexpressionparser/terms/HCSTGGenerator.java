package net.strongdesign.balsa.hcexpressionparser.terms;

import java.util.Set;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public interface HCSTGGenerator {
	

	void generateSTGold(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace);
	
	// generateSTG always creates a new STG and returns it
	STG generateSTG(HCChannelSenseController sig, Set<Place> inPlaces, Set<Place> outPlaces, boolean enforceInj);
	
}
