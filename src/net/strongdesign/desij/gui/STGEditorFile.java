package net.strongdesign.desij.gui;

import net.strongdesign.stg.STGFile;

public abstract class STGEditorFile extends STGFile {
	
//	
	public static STGEditorCoordinates convertToCoordinates(String file) {
//		STGEditorCoordinates result = new STGEditorCoordinates();
//		try {
//			BufferedReader reader = new BufferedReader(new StringReader(file));
//			
//			
//			
//			
//			boolean entered = false;
//			
//			while (true) {
//				String line = reader.readLine();
//				if (line == null)
//					break;
//				
//				if (!entered && line.trim().equals(".coordinates")) {
//					entered = true;
//					continue;
//				}
//				
//				if (!entered && line.trim().equals(".coordinates_end") || entered && line.trim().equals(".coordinates") ) {
//					throw new ParsingException("Invalid coordinate format");
//				}
//				
//				if (entered && line.trim().equals(".coordinates_end")) {
//					return result;
//				}
//				
//				
//				if (!entered)
//					continue;
//				
//				//Assume the current line denotes a coordinate
//				
//				line = line.trim();
//				
//				String[] c = line.replaceFirst("[\\w+-/]*", "").replaceFirst("<[\\w\\s+-/,]*>", "").trim().split(" ");
//				if (c.length != 2)
//					throw new ParsingException("2 coordinates expected: "+line);
//				Point p = new Point(Integer.parseInt(c[0]), Integer.parseInt(c[1]));
//				
//				String node = line.replaceAll("\\s\\d*", "").trim();				
//				
//				result.put(nodeMapping.get(node), p);
//			}
//		}
//		catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "Internal error while parsing coordinates", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		
		return null;
	}
//	
//	/**
//	 * Parses the coordinates given in file. The stg is needed as a reference for implicit places
//	 */
//	public static STGEditorCoordinates convertToCoordinates2(String file, STG stg) {
//		STGEditorCoordinates result = new STGEditorCoordinates();
//		try {
//			BufferedReader reader = new BufferedReader(new StringReader(file));
//			
//			
//			Collection<Transition> transitions = stg.getTransitions(ConditionFactory.ALL_TRANSITIONS);
//			
//			boolean entered = false;
//			
//			while (true) {
//				String line = reader.readLine();
//				if (line == null)
//					break;
//				
//				if (!entered && line.trim().equals(".coordinates")) {
//					entered = true;
//					continue;
//				}
//				
//				if (!entered && line.trim().equals(".coordinates_end") || entered && line.trim().equals(".coordinates") ) {
//					throw new ParsingException("Invalid coordinate format");
//				}
//				
//				if (entered && line.trim().equals(".coordinates_end")) {
//					return result;
//				}
//				
//				
//				if (!entered)
//					continue;
//				
//				//Assume the current line denotes a coordinate
//				
//				line = line.trim();
//				
//				String[] c = line.replaceFirst("[\\w+-/]*", "").replaceFirst("<[\\w\\s+-/,]*>", "").trim().split(" ");
//				if (c.length != 2)
//					throw new ParsingException("2 coordinates expected: "+line);
//				Point p = new Point(Integer.parseInt(c[0]), Integer.parseInt(c[1]));
//				
//				String node = line.replaceAll("\\s\\d*", "").trim();
//				
//				//a transition or numbered dummy place
//				if (node.matches("\\w*[+-](/\\d+)??") || node.matches("\\w*/\\d+") ) 
//					result.put(getTransition(node, transitions), p);
//				//an implicit place
//				else if (
//						node.matches("<\\w*[+-](/\\d+)??,\\w*[+-](/\\d+)??>") ||
//						node.matches("\\w*/\\d+,\\w*[+-](/\\d+)??>") ||
//						node.matches("<\\w*[+-](/\\d+)??,\\w*/\\d+>") ||
//						node.matches("<\\w*/\\d+,\\w*/\\d+") ) {
//					String[] pure = node.replaceAll("[<>]", "").split(",");
//					if (pure.length != 2)
//						throw new ParsingException("Unknown format: "+line);
//					Transition n1 = getTransition(pure[0], transitions);
//					Transition n2 = getTransition(pure[1], transitions);
//					
//					MultiCondition<Node> cond = new MultiCondition<Node>(MultiCondition.AND);
//					cond.addCondition(ConditionFactory.getChildOfCondition(n1));
//					cond.addCondition(ConditionFactory.getParentOfCondition(n2));
//					Collection<Place> pp = stg.getPlaces(cond);
//					if (pp.size()!=1)
//						throw new ParsingException("Double implicit place: "+line);
//					result.put(pp.iterator().next(), p);            
//				}
//				else {
//					//a place or unumbered dummy transition
//					Collection<Place> places = stg.getPlaces(ConditionFactory.getPlaceLabelOf(node));
//					if (places.size()!=1)
//						throw new ParsingException("Double or unknown place: "+line);
//					else if (places.size()==1)
//						result.put(places.iterator().next(), p);
//					else {
//						result.put(getTransition(node, transitions), p);                
//						
//					}
//				}
//			}
//			
//		}
//		catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "Internal error while parsing coordinates", "JDesi Error", JOptionPane.ERROR_MESSAGE);
//		}
//		
//		return result;
//	}
//	public static String convertToG(STG stg, boolean implicit, STGEditorCoordinates coordinates) {
////		collect all implicit places which have to made explicit
//		java.util.List<Place> explicit = new LinkedList<Place>();
//		java.util.List<Place> markedPlaces = stg.getPlaces(ConditionFactory.getMarkedGraphPlaceCondition());
//		while (! markedPlaces.isEmpty()) {
//			Place actPlace = markedPlaces.remove(0);
//			for (Place compPlace : markedPlaces)
//				if ( actPlace.getParents().equals(compPlace.getParents()) &&   
//						actPlace.getChildren().equals(compPlace.getChildren())) {
//					explicit.add(actPlace);
//					explicit.add(compPlace);
//				}
//		}
//		
//		StringBuilder result = new StringBuilder("\n.coordinates\n");
//		
//		Set<String> labels = new HashSet<String>();
//		for (Node node : stg.getNodes())
//			labels.add(node.getString(Node.UNIQUE));
//		
//		
//		for (Node node : coordinates.keySet()) {
//			if (! labels.contains(node.getString(Node.UNIQUE)))
//				continue;
//			
//			Point point = coordinates.get(node);
//			if (node instanceof Transition)
//				result.append(((Transition)node).getString(Node.UNIQUE)+" "+point.x+" "+point.y);
//			else if (node instanceof Place) {
//				Place p = (Place)node; 
//				if (implicit || !ConditionFactory.getMarkedGraphPlaceCondition().fulfilled(p) || ! explicit.contains(p) )
//					result.append((p.getString(Node.UNIQUE)));      
//				else
//					result.append( "<"+p.getParents().iterator().next().getString(Node.UNIQUE) +" , " + p.getChildren().iterator().next().getString(Node.UNIQUE) +">"  );
//				
//				result.append(" "+point.x+" "+point.y);
//			}
//			result.append("\n");
//		}
//		
//		result.append(".coordinates_end\n");
//		
//		
//		return result.toString();
//	}
//	
//	
//	/**Looks for a Transition which matches the given string*/
//	
//	private static Transition getTransition(String s, Collection<Transition> transitions) throws ParsingException {
//		int id;
//		String idS = s.replaceFirst("\\w*[+-]?(/)?", "");
//		if (idS.length()==0)
//			id=0;
//		else
//			id = Integer.parseInt(idS);
//		
//		EdgeDirection dir;
//		String dirS = s.replaceAll("[^+-]", "");
//		if (dirS.equals("+"))
//			dir = EdgeDirection.UP;
//		else if (dirS.equals("-"))
//			dir = EdgeDirection.DOWN;
//		else if (dirS.length()==0)
//			dir = EdgeDirection.UNKNOWN;
//		else
//			throw new ParsingException("Unknown direction: "+s);
//		String name = s.replaceAll("[+-]?(/\\d+)?", "");
//		
//		SignalEdge l = new SignalEdge(new String(name), dir);
//		
//		for (Transition t : STGOperations.getElements(transitions, ConditionFactory.getSignalEdgeOfCondition(l)))
//			if (t.getIdentifier()==id)
//				return t;
//		
//		throw new ParsingException("Unknown transition: "+s);     
//	}
//	
//	
	
}
