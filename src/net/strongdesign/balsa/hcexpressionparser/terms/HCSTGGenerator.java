package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;

public interface HCSTGGenerator {
	void generateSTG(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace); 
}
