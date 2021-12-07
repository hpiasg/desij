

//package net.strongdesign.stg;
//
//public class Snippet {
//	/**
//	 * Contracts all dummy transitions of an STG.
//	 * --> strongly inspired by the method contract() of AbstractDecomposition 
//	 * uses Reordering as well
//	 * @param stg The stg.
//	 * @return The number of contracted dummies
//	 * @throws STGException If a dummy cannot be contracted.
//	 */
//	public static int removeDummies(STG stg) throws STGException {
//		// transitions subject to contraction, i.e. all Dummies --> ArrayList because of performance for Reordering
//		List<Transition> contract = 
//			new ArrayList<Transition>(stg.getTransitions(ConditionFactory.getSignatureOfCondition(Signature.DUMMY)));
//		
//		int contractions = 0;
//		int result = contract.size();
//		
//		 while (true) {
//			//used to detect an increase in the number of places
//			int nroPlaces = stg.getNumberOfPlaces();
//			int nroTransitions = stg.getNumberOfTransitions();
//			int actTransIndex = -1; // empty List from end to start, because of performance with ArrayList
//				
//				
//			for (int i = 0; i < contract.size(); i++)
//			{
//				if(isContractable(stg, contract.get(i)) == Reason.OK)
//				{
//					if (actTransIndex == -1)
//					{
//						actTransIndex = i;
//					}
//					if (CLW.instance.ORDER_DUMMY_TRANSITIONS.isEnabled()) { 
//						
//						// find smallest element in linear time --> makes no further updating of keys in a priority queue necessary
//						int actPre = contract.get(actTransIndex).getChildren().size();
//						int actPost = contract.get(actTransIndex).getParents().size();
//						int actCost = (actPre-1)*(actPost-1)-1;
//						int iCost = (contract.get(i).getChildren().size()-1) * (contract.get(i).getParents().size()-1)-1;
//						if(ConditionFactory.SAFE_CONTRACTABLE.fulfilled(contract.get(i)))
//						{
//							Assert.assertEquals(iCost, -1);
//						}
//						else
//						{
//							Assert.assertTrue(iCost > -1);
//						}
//						if (actCost > iCost)
//							actTransIndex = i;
//					}
//					else
//					{
//						break;
//					}
//				}
//			}
//			
//			if(actTransIndex != -1)
//			{
//				Transition actTransition = contract.remove(actTransIndex);
//				
//				stg.contract(actTransition);
//					
//				contractions++;
//				
//				DesiJ.logFile.debug("Contracted transition: " + actTransition.getString(Node.UNIQUE));
//			}
//			
//			if (
//					   CLW.instance.CHECK_RED_OFTEN.isEnabled()
//					|| actTransIndex == -1
//					|| contractions % 1 == 0
//					|| stg.getNumberOfPlaces() > CLW.instance.PLACE_INCREASE.getDoubleValue() * nroPlaces
//			) {
//				NodeRemover cleverRemover = null;
//				contract.removeAll(redDel(stg, cleverRemover));
//			}
//			
//			if(stg.getNumberOfPlaces() == nroPlaces && stg.getNumberOfTransitions() == nroTransitions)
//				break;
//		}
//	
//		result -= contract.size();
//		
//		return result;
//	}
//	
//}

