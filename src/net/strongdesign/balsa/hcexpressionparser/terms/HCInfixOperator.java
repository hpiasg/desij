package net.strongdesign.balsa.hcexpressionparser.terms;

import java.util.LinkedList;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGException;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;


public class HCInfixOperator extends HCTerm implements HCSTGGenerator {

	public enum Operation {CHOICE, CONCUR, SEQUENCE, SYNC, ENCLOSE, UNKNOWN;
	
			static public Operation fromString(String s) {
				if (s.equals("|")||s.equals("[]")) return CHOICE;
				if (s.equals("||")) return CONCUR;
				if (s.equals(";")) return SEQUENCE;
				if (s.equals(",")) return SYNC;
				if (s.equals(":")) return ENCLOSE;
				
				return UNKNOWN;
			}
			
			static public String toString(Operation op) {
				if (op==CHOICE) return "[]";
				if (op==CONCUR) return "||";
				if (op==SEQUENCE) return ";";
				if (op==SYNC) return ",";
				if (op==ENCLOSE) return ":";
				
				return "?ERROR?";
			}
		};
		
	public Operation operation = null;
	public LinkedList<HCTerm> components = new LinkedList<HCTerm>();
	
	
	@Override
	public HCTerm expand(ExpansionType type) throws Exception {
		HCInfixOperator ret = new HCInfixOperator();
		ret.operation = operation;
		if (ret.operation==Operation.SYNC) ret.operation = Operation.CONCUR;
		if (ret.operation==Operation.ENCLOSE) ret.operation = Operation.SEQUENCE;
		
		
		if (operation==Operation.CONCUR) {
			
			if (type==ExpansionType.UP) {
				for (HCTerm t: components) {
					HCTerm expUp   = t.expand(ExpansionType.UP);
					HCTerm expDown = t.expand(ExpansionType.DOWN);
					
					if (expUp!=null&&expDown!=null) {
						HCInfixOperator in = new HCInfixOperator();
						in.operation = Operation.SEQUENCE;
						in.components.add(expUp);
						in.components.add(expDown);
						ret.components.add(in);
					} else if (expUp!=null) {
						ret.components.add(expUp);
					} else {
						throw new Exception("Unacceptable condition for expanding Concur or Choice");
					}
				}
				
				if (ret.components.size()==0) {
					throw new Exception("Error, Concur or Choice expansion with 0 components");
				}
				
			} else return null; // empty expansion
		} else if (operation==Operation.SEQUENCE) {
			int len = components.size();
			if (len==0) return null;
			
			if (type==ExpansionType.UP) {
				
				
				for (int i=0;i<len;i++) {
					HCTerm t = components.get(i);
					HCTerm expUp = t.expand(ExpansionType.UP);
					
					if (expUp!=null) {
						ret.components.add(expUp);
					}
					
					if (i!=len-1) {
						// add the down-phase as well
						HCTerm expDown = t.expand(ExpansionType.DOWN);
						if (expDown!=null) {
							ret.components.add(expDown);
						}
					}
					
				}
				
				if (ret.components.size()==0) {
					return null;
				}
				
			} else { 
				return components.get(components.size()-1).expand(type);
			}
			
		} else if (operation==Operation.SYNC) {
			
			
			int len = components.size();
			for (int i=0;i<len;i++) {
				HCTerm t = components.get(i);
				
				HCTerm exp = t.expand(type);
				
				if (exp!=null) {
					ret.components.add(exp);
				}
				
			}
			
			
		} else if (operation==Operation.CHOICE) {
			
			int len = components.size();
			for (int i=0;i<len;i++) {
				HCTerm t = components.get(i);
				
				HCTerm exp = t.expand(type);
				
				if (exp!=null) {
					ret.components.add(exp);
				}
				
			}
			
		} else {
			throw new Exception("Unimplemented expansion");
		}
		
		if (ret.components.size()==1) ret.components.get(0); 
		if (ret.components.size()==0) return null; 
		return ret;
	}

	@Override
	public String toString() {
		String ret = "";
		for (int i=0;i<components.size();i++) {
			if (i>0) {
				ret+=Operation.toString(operation);
			}
			HCTerm t = components.get(i);
			if (!(t instanceof HCInfixOperator) || (t instanceof HCInfixOperator && ((HCInfixOperator)t).operation == operation)) {
				ret+=t.toString();
			} else {
				ret+=" ("+t.toString()+") ";
			}
			
		}
		
		return ret;
	}

	@Override
	public int getMaxCount() {
		int mx = 0;
		for (HCTerm tt: components) {
			mx=Math.max(mx, tt.getMaxCount());
		}
		return mx;
	}


	@Override
	public void setInstanceNumber(int num) {
		
		for (HCTerm tt: components) {
			tt.setInstanceNumber(num);
		}
	}
	
	@Override
	public Object clone() {
		HCInfixOperator ret = (HCInfixOperator)super.clone();
		ret.components = new LinkedList<HCTerm>();
		for (HCTerm tt: components) {
			ret.components.add((HCTerm)tt.clone());
		}
		return ret;
	}

	@Override
	public void generateSTG(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace) {
		// all of the components at this stage have to be the STG generators
		int len=components.size();
		
		if (operation == Operation.SEQUENCE) {
			Place p1=inPlace;
			Place p2=null;
			for (int i=0;i<len;i++) {
				p2=outPlace;
				if (i<len-1) {
					p2=stg.addPlace("p", 0);
				}
				
				HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
				hc.generateSTG(stg, sig, p1, p2);
				
				p1=p2;
			}
		} else if (operation == Operation.CONCUR) {
			
			int num=stg.getSignalNumber("con");
			Transition t1 = stg.addTransition(
					new SignalEdge(
							num, 
							EdgeDirection.DONT_CARE
							)
					);
			
			stg.setSignature(num, Signature.DUMMY);
			inPlace.setChildValue(t1, 1);
			
			num=stg.getSignalNumber("con");
			
			Transition t2 = stg.addTransition(
					new SignalEdge(
							num, 
							EdgeDirection.DONT_CARE
							)
					);
			
			stg.setSignature(num, Signature.DUMMY);
			
			t2.setChildValue(outPlace, 1);
			
			Place p1;
			Place p2;
			
			for (int i=0;i<len;i++) {
				HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
				p1 = stg.addPlace("p", 0);
				p2 = stg.addPlace("p", 0);
				t1.setChildValue(p1, 1);
				p2.setChildValue(t2, 1);
				hc.generateSTG(stg, sig, p1, p2);
			}
			
		} else if (operation == Operation.CHOICE) {
			
			int num;
			Transition t1;
			Transition t2;
			Place p1;
			Place p2;
			
			for (int i=0;i<len;i++) {
				
				
				num=stg.getHighestSignalNumber()+1;
				num=stg.getSignalNumber("con"+num);
				t1 = stg.addTransition(
						new SignalEdge(
								num, 
								EdgeDirection.DONT_CARE
								)
						);
				
				stg.setSignature(num, Signature.DUMMY);
				inPlace.setChildValue(t1, 1);
				
				num=stg.getHighestSignalNumber()+1;
				num=stg.getSignalNumber("con"+num);
				
				t2 = stg.addTransition(
						new SignalEdge(
								num, 
								EdgeDirection.DONT_CARE
								)
						);
				
				stg.setSignature(num, Signature.DUMMY);
				
				t2.setChildValue(outPlace, 1);
				
				HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
				p1 = stg.addPlace("p", 0);
				p2 = stg.addPlace("p", 0);
				t1.setChildValue(p1, 1);
				p2.setChildValue(t2, 1);
				hc.generateSTG(stg, sig, p1, p2);
			}
			
		} else {
			// unsupported component
			// TODO: throw something?
			return;
		}
		
	}
	
}
