package net.strongdesign.balsa.hcexpressionparser.terms;

import java.util.Set;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public interface HCSTGGenerator {
	

	void generateSTGold(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace);
	
	void generateSTG(STG stg, HCChannelSenseController sig, Set<Place> inPlaces, Set<Place> outPlaces);
	
}
