package net.strongdesign.balsa.hcexpressionparser.terms;



import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.strongdesign.balsa.hcexpressionparser.HCExpressionParser;
import net.strongdesign.stg.EdgeDirection;
import net.strongdesign.stg.Place;
import net.strongdesign.stg.STG;
import net.strongdesign.stg.STGUtil;
import net.strongdesign.stg.SignalEdge;
import net.strongdesign.stg.Signature;
import net.strongdesign.stg.Transition;
import net.strongdesign.stg.solvers.CSCSolver;


public class HCInfixOperator extends HCTerm implements HCSTGGenerator {

	public enum Operation {SYNCPROD, CHOICE, CONCUR, SEQUENCE, FOLLOW, SYNC, ENCLOSE, UNKNOWN;
	
			static public Operation fromString(String s) {
				if (s.equals("|")) return CHOICE;
				if (s.equals("||")) return CONCUR;
				
				if (s.equals("**")) return SYNCPROD;
				
				if (s.equals(";")) return SEQUENCE;
				if (s.equals(",")) return SYNC;
				if (s.equals(".")) return FOLLOW;
				if (s.equals(":")) return ENCLOSE;
				
				return UNKNOWN;
			}
			
			static public String toString(Operation op) {
				if (op==CHOICE) return "|";
				if (op==SYNCPROD) return "\n**";
				
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
						throw new Exception("Unacceptable condition for expanding ||");
					}
				}
				
				
				if (ret.components.size()==0) {
					throw new Exception("Error, || expansion with 0 components");
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
						
//						// try to add an internal signal, if it is a channel expansion
//						if (expUp!=null && t instanceof HCChannelTerm && !(t instanceof HCTransitionTerm)) {
//							HCChannelTerm cterm = (HCChannelTerm)t;
//							
//							HCTransitionTerm tterm = new HCTransitionTerm();
//							tterm.channel = cterm.channel;
//							tterm.instanceNumber = cterm.instanceNumber;
//							tterm.wire = "c";
//							tterm.direction = "+";
//							ret.components.add(tterm);
//						}
						
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
				
//				for (int i=0;i<len-1;i++) {
//					HCTerm t = components.get(i);
//					if (t instanceof HCChannelTerm && !(t instanceof HCTransitionTerm)) {
//						HCChannelTerm cterm = (HCChannelTerm)t;
//						
//						
//						HCTransitionTerm tterm = new HCTransitionTerm();
//						tterm.channel = cterm.channel;
//						tterm.instanceNumber = cterm.instanceNumber;
//						tterm.wire = "c";
//						tterm.direction = "-";
//						ret.components.add(tterm);
//					}
//				}
				
				HCTerm tt = components.get(components.size()-1).expand(type, scale, sig, oldChoice);
				
				if (tt!=null)
					ret.components.add(tt);
				
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
			
		} else if (operation==Operation.SYNCPROD) {
			
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
						return null;
					}
				}
				
			} else return null; // empty expansion
			
		} else {
			throw new Exception("Unimplemented expansion");
		}
		
		if (ret.components.size()==1) return ret.components.get(0);
		
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
							EdgeDirection.UNKNOWN
							)
					);
			
			stg.setSignature(num, Signature.DUMMY);
			inPlace.setChildValue(t1, 1);
			
			num=stg.getSignalNumber("con");
			
			Transition t2 = stg.addTransition(
					new SignalEdge(
							num, 
							EdgeDirection.UNKNOWN
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
								EdgeDirection.UNKNOWN
								)
						);
				
				stg.setSignature(num, Signature.DUMMY);
				inPlace.setChildValue(t1, 1);
				
				num=stg.getHighestSignalNumber()+1;
				num=stg.getSignalNumber("choice");
				
				t2 = stg.addTransition(
						new SignalEdge(
								num, 
								EdgeDirection.UNKNOWN
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
	
	
	// for now, the synchronous product is only defined at the top level of expression
	public static STG generateComposedSTG(boolean solveCSC, HCTerm exp, HCExpressionParser parser, boolean enforce) {
		
		
		if ( exp instanceof HCInfixOperator && ((HCInfixOperator)exp).operation == Operation.SYNCPROD) {
			
			HCInfixOperator io = (HCInfixOperator)exp;
			LinkedList<STG> stgs = new LinkedList<STG>();
			
			for (HCTerm term : io.components) {
				stgs.add(generateComposedSTG(solveCSC, term, parser, enforce));
			}
			
			return STGUtil.synchronousProduct(stgs, false);
			
		} else {
			
			STG stg = new STG();

			// do it the new way
			Set<Place> listIn = new HashSet<Place>();
			Set<Place> listOut = new HashSet<Place>();

			stg=((HCSTGGenerator) exp).generateSTG(parser, listIn,
					listOut, enforce, solveCSC);

			// add the initial token
			for (Place p : listIn) {
				p.setMarking(1);
			}
			
			
			
			// do solve CSC conflicts using Petrify
			if (solveCSC) {
				try {
					
					STG outstg = CSCSolver.solveCSCWithPetrify(stg);
					
					if (outstg!=null&&enforce) {
						STGUtil.enforceInjectiveLabelling(outstg); 
					}
					
					return outstg;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			return stg;
		}
		
	}
	
	
	@Override
	public STG generateSTG(HCChannelSenseController sig, Set<Place> inPlaces, Set<Place> outPlaces, boolean enforce, boolean solveCSC) {
		
		STG stg=null;
		
		// all of the components at this stage have to be the STG generators
		
		int len=components.size();
		LinkedList< Set<Place> > fromSets = new LinkedList< Set<Place> >();
		LinkedList< Set<Place> > toSets = new LinkedList< Set<Place> >();
		
		for (int i=0;i<len;i++) {
			fromSets.add(new HashSet<Place>());
			toSets.add(new HashSet<Place>());
		}
		
		
		/* generate all the separate components in separate STGs */
		STG s[] = new STG[len];
		
		for (int i=0;i<len;i++) {
			HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
			s[i] = hc.generateSTG(sig, fromSets.get(i), toSets.get(i), enforce, solveCSC);
		}
		
		/**
		 * the split-place components are: choice and a loop with an exit
		 * the merge-place component is loop
		 * now, if a split-place component is followed by a merge-place component,
		 * there must be an additional dummy preceding the merge-place component
		 */
		
		
		if (operation == Operation.SEQUENCE) {
			
			stg=s[0];
			Set<Place> curIn = fromSets.get(0);
			Set<Place> curOut = toSets.get(0);
			
			for (int i=1;i<len;i++) {
				
				Set<Place> newIn = new HashSet<Place>();
				Set<Place> newOut = new HashSet<Place>();
				
				STGCompositionOperations.sequentialComposition(
						stg, curIn, curOut,
						s[i], fromSets.get(i), toSets.get(i),
						newIn, newOut);
				curIn = newIn;
				curOut = newOut;
			}
			
			inPlaces.addAll(curIn);
			outPlaces.addAll(curOut);
			
		} else if (operation == Operation.CONCUR) {
			
			
			/* now form a synchronous product of them all,
			 * the input and output places are combined inside */
			stg = s[0];
			Set<Place> curIn = fromSets.get(0);
			Set<Place> curOut = toSets.get(0);
			
			if (enforce) {
				for (int i=0;i<len;i++) {
					STGUtil.enforceInjectiveLabelling(s[i]);
				}
			}
			
			for (int i=1;i<len;i++) {
				Set<Place> newIn = new HashSet<Place>();
				Set<Place> newOut = new HashSet<Place>();
				
				stg = STGCompositionOperations.synchronousProduct(
						stg, curIn, curOut,
						s[i], fromSets.get(i), toSets.get(i),
						false, newIn, newOut);
				
				curIn = newIn;
				curOut = newOut;
			}
			
			inPlaces.addAll(curIn);
			outPlaces.addAll(curOut);
			
		} else if (operation == Operation.CHOICE) {
			
			for (int i=0;i<len;i++) {
				HCSTGGenerator hc = (HCSTGGenerator)components.get(i);
				
				// for each loop component surround it with dummies
				if (hc instanceof HCLoopTerm) {
					
					int num;
					Transition t1, t2;
					Place np1, np2;
					
					// create new signals
					num=s[i].getSignalNumber("choice_loop");
					t1 = s[i].addTransition(
							new SignalEdge(
									num,
									EdgeDirection.UNKNOWN
									)
							);
					s[i].setSignature(num, Signature.DUMMY);
					
					num=s[i].getSignalNumber("after_loop");
					t2 = s[i].addTransition(
							new SignalEdge(
									num,
									EdgeDirection.UNKNOWN
									)
							);
					s[i].setSignature(num, Signature.DUMMY);
					
					// create new places
					np1 = s[i].addPlace("p", 0);
					np2 = s[i].addPlace("p", 0);
					
					np1.setChildValue(t1, 1);
					for (Place p: fromSets.get(i)) {
						t1.setChildValue(p, 1);
					}
					
					for (Place p: toSets.get(i)) {
						t2.setParentValue(p, 1);
					}
					np2.setParentValue(t2, 1);
					
					// set the new place as the from place 
					fromSets.get(i).clear();
					fromSets.get(i).add(np1);
					toSets.get(i).clear();
					toSets.get(i).add(np2);
				}
			}
			
			stg = s[0];
			
			Set<Place> curIn = fromSets.get(0);
			Set<Place> curOut = toSets.get(0);
			
			for (int i=1;i<len;i++) {
				Set<Place> newIn = new HashSet<Place>();
				Set<Place> newOut = new HashSet<Place>();
				
				stg = STGCompositionOperations.choiceComposition(
						stg, curIn, curOut,
						s[i], fromSets.get(i), toSets.get(i),
						false, newIn, newOut);
				
				curIn = newIn;
				curOut = newOut;
			}
			
			inPlaces.addAll(curIn);
			outPlaces.addAll(curOut);
			
			
			
		}
		
		return stg;
	}
}
