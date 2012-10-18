package net.strongdesign.balsa.hcexpressionparser.terms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;


public class HCInfixOperator extends HCTerm implements HCSTGGenerator {

	public enum Operation {CHOICE, CONCUR, SEQUENCE, FOLLOW, SYNC, ENCLOSE, UNKNOWN;
	
			static public Operation fromString(String s) {
				if (s.equals("|")||s.equals("[]")) return CHOICE;
				if (s.equals("||")) return CONCUR;
				if (s.equals(";")) return SEQUENCE;
				if (s.equals(",")) return SYNC;
				if (s.equals(".")) return FOLLOW;
				if (s.equals(":")) return ENCLOSE;
				
				return UNKNOWN;
			}
			
			static public String toString(Operation op) {
				if (op==CHOICE) return "[]";
				if (op==CONCUR) return "||";
				if (op==SEQUENCE) return ";";
				if (op==FOLLOW) return ".";
				if (op==SYNC) return ",";
				if (op==ENCLOSE) return ":";
				
				return "?ERROR?";
			}
		};
		
	public Operation operation = null;
	public LinkedList<HCTerm> components = new LinkedList<HCTerm>();
	
	
	@Override
	public HCTerm expand(ExpansionType type, int scale, HCChannelSenseController sig, boolean oldChoice) throws Exception {
		HCInfixOperator ret = new HCInfixOperator();
		ret.operation = operation;
		
		if (ret.operation==Operation.SYNC) ret.operation = Operation.CONCUR;
		if (ret.operation==Operation.FOLLOW) ret.operation = Operation.SEQUENCE;
		if (ret.operation==Operation.ENCLOSE) ret.operation = Operation.SEQUENCE;
		
		
		if (operation==Operation.CONCUR) {
			
			if (type==ExpansionType.UP) {
				for (HCTerm t: components) {
					HCTerm expUp   = t.expand(ExpansionType.UP, scale, sig, oldChoice);
					HCTerm expDown = t.expand(ExpansionType.DOWN, scale, sig, oldChoice);
					
					if (expUp!=null&&expDown!=null) {
						HCInfixOperator in = new HCInfixOperator();
						in.operation = Operation.SEQUENCE;
						in.components.add(expUp);
						in.components.add(expDown);
						ret.components.add(in);
					} else if (expUp!=null) {
						ret.components.add(expUp);
					} else {
						throw new Exception("Unacceptable condition for expanding Concur");
					}
				}
				
				if (ret.components.size()==0) {
					throw new Exception("Error, Concur expansion with 0 components");
				}
				
			} else return null; // empty expansion
		} else if (operation==Operation.SEQUENCE) {
			int len = components.size();
			if (len==0) return null;
			
			if (type==ExpansionType.UP) {
				
				
				for (int i=0;i<len;i++) {
					HCTerm t = components.get(i);
					HCTerm expUp = t.expand(ExpansionType.UP, scale, sig, oldChoice);
					
					if (expUp!=null) {
						ret.components.add(expUp);
					}
					
					if (i!=len-1) {
						// add the down-phase as well
						HCTerm expDown = t.expand(ExpansionType.DOWN, scale, sig, oldChoice);
						if (expDown!=null) {
							ret.components.add(expDown);
						}
					}
					
				}
				
				if (ret.components.size()==0) {
					return null;
				}
				
			} else {
				return components.get(components.size()-1).expand(type, scale, sig, oldChoice);
			}
			
		} else if (operation==Operation.FOLLOW) {
			
			int len = components.size();
			for (int i=0;i<len;i++) {
				HCTerm t = components.get(i);
				
				HCTerm exp = t.expand(type, scale, sig, oldChoice);
				
				if (exp!=null) {
					ret.components.add(exp);
				}
				
			}
		} else if (operation==Operation.SYNC) {
			
			
			int len = components.size();
			for (int i=0;i<len;i++) {
				HCTerm t = components.get(i);
				
				HCTerm exp = t.expand(type, scale, sig, oldChoice);
				
				if (exp!=null) {
					ret.components.add(exp);
				}
				
			}
		} else if (operation==Operation.CHOICE) {
			if (oldChoice) {
				// old type of expanding choice
				int len = components.size();
				for (int i=0;i<len;i++) {
					HCTerm t = components.get(i);
					
					HCTerm exp = t.expand(type, scale, sig, oldChoice);
					
					if (exp!=null) {
						ret.components.add(exp);
					}
					
				}
				
			} else {
				// new type of expanding choice
				
				if (type==ExpansionType.UP) {
					for (HCTerm t: components) {
						HCTerm expUp   = t.expand(ExpansionType.UP, scale, sig, oldChoice);
						HCTerm expDown = t.expand(ExpansionType.DOWN, scale, sig, oldChoice);
						
						if (expUp!=null&&expDown!=null) {
							HCInfixOperator in = new HCInfixOperator();
							in.operation = Operation.SEQUENCE;
							in.components.add(expUp);
							in.components.add(expDown);
							ret.components.add(in);
						} else if (expUp!=null) {
							ret.components.add(expUp);
						} else {
							throw new Exception("Unacceptable condition for expanding Choice");
						}
					}
					
					if (ret.components.size()==0) {
						throw new Exception("Error, Choice expansion with 0 components");
					}
					
				} else return null; // empty expansion
				
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
	public void setInstanceNumber(int num, HCChannelSenseController sig) {
		
		for (HCTerm tt: components) {
			tt.setInstanceNumber(num, sig);
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
	public void generateSTGold(STG stg, HCChannelSenseController sig, Place inPlace, Place outPlace) {
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
				hc.generateSTGold(stg, sig, p1, p2);
				
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
				hc.generateSTGold(stg, sig, p1, p2);
			}
			
		} else if (operation == Operation.CHOICE) {
			
			int num;
			Transition t1;
			Transition t2;
			Place p1;
			Place p2;
			
			for (int i=0;i<len;i++) {
				
				
				num=stg.getHighestSignalNumber()+1;
				num=stg.getSignalNumber("choice");
				t1 = stg.addTransition(
						new SignalEdge(
								num, 
								EdgeDirection.DONT_CARE
								)
						);
				
				stg.setSignature(num, Signature.DUMMY);
				inPlace.setChildValue(t1, 1);
				
				num=stg.getHighestSignalNumber()+1;
				num=stg.getSignalNumber("choice");
				
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
				hc.generateSTGold(stg, sig, p1, p2);
			}
			
		} else {
			// unsupported component
			// TODO: throw something?
			return;
		}
		
	}
	
	@Override
	public void generateSTG(STG stg, HCChannelSenseController sig, Set<Place> inPlaces, Set<Place> outPlaces) {
		// all of the components at this stage have to be the STG generators
		
		int len=components.size();
		LinkedList< Set<Place> > fromSets = new LinkedList< Set<Place> >();
		LinkedList< Set<Place> > toSets = new LinkedList< Set<Place> >();
		
		for (int i=0;i<len;i++) {
			fromSets.add(new HashSet<Place>());
			toSets.add(new HashSet<Place>());
		}
		
		/* generate all the separate components */
		for (int i=0;i<len;i++) {
			HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
			hc.generateSTG(stg, sig, fromSets.get(i), toSets.get(i));
		}
		
		if (operation == Operation.SEQUENCE) {
			
			inPlaces.addAll(fromSets.get(0));
			
			for (int i=0;i<len-1;i++) {
				STG.cartesianProductBinding(stg, toSets.get(i), fromSets.get(i+1));
			}
			
			outPlaces.addAll(toSets.get(len-1));
			
			/* combine the sets sequentially */
			
		} else if (operation == Operation.CONCUR) {
			
			for (int i=0;i<len;i++) {
				inPlaces.addAll(fromSets.get(i));
				outPlaces.addAll(toSets.get(i));
			}
			
		} else if (operation == Operation.CHOICE) {
			
			for (int i=0;i<len;i++) {
				HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
				
				// for each loop component create an additional dummy transition
				if (hc instanceof HCLoopTerm) {
					
					int num;
					Transition t;
					Place np;
					
					num=stg.getHighestSignalNumber()+1;
					num=stg.getSignalNumber("choice_loop");
					t = stg.addTransition(
							new SignalEdge(
									num, 
									EdgeDirection.DONT_CARE
									)
							);
					
					stg.setSignature(num, Signature.DUMMY);
					np = stg.addPlace("p", 0);
					
					np.setChildValue(t, 1);
					// transition outputs into each of the loop's input places
					for (Place p: fromSets.get(i)) {
						t.setChildValue(p, 1);
					}
					
					// set the new place as the from place 
					fromSets.get(i).clear();
					fromSets.get(i).add(np);
				}
			}
			
			// now to find the final inPlaces and outPlaces, perform the Cartesian products
			Set<Place> cart=fromSets.get(0);
			for (int i=1;i<len;i++) {
				cart=STG.cartesianProductBinding(stg, cart, fromSets.get(i));
			}
			inPlaces.addAll(cart);
			
			cart=toSets.get(0);
			for (int i=1;i<len;i++) {
				cart=STG.cartesianProductBinding(stg, cart, toSets.get(i));
			}
			outPlaces.addAll(cart);
			
		} else {
			// unsupported component
			// TODO: throw something?
			return;
		}
		
	}
}
