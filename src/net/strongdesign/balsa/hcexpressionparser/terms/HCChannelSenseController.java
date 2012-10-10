package net.strongdesign.balsa.hcexpressionparser.terms;

import net.strongdesign.stg.Signature;

public interface HCChannelSenseController {
	boolean isActive(String name);
	void setActive(String name, boolean act);
	
	boolean isScaled(String name);
	void setScaled(String name, boolean act);
}
