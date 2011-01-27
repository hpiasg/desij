package net.strongdesign.stg.synthesis;
import net.strongdesign.stg.*;
import net.strongdesign.stg.traversal.*;
import java.util.*;

public class IncidenceMatrix {
	/**The incidence matrix */
	private double[][] C;

	
	/**Maps each row/column number to a node */
	private Map<Integer, Transition> transitions;
	private Map<Integer, Place> places;
	
	/**Number of places, transitions resp.*/
	private int nrPlaces, nrTransitions;
	
	/**Constructs the incidence matrix from an STG*/
	public IncidenceMatrix(STG stg) {
		
		//Make an index of all nodes
		nrPlaces = 0;
		places = new HashMap<Integer, Place>();
		for (Place node : stg.getPlaces(ConditionFactory.ALL_PLACES)) 
			places.put(nrPlaces++, node);

		nrTransitions = 0;
		transitions = new HashMap<Integer, Transition>();
		for (Transition node : stg.getTransitions(ConditionFactory.ALL_TRANSITIONS)) 
			transitions.put(nrTransitions++, node);
		
		
		//Generate the matrix
		C = new double[nrPlaces][nrTransitions];
		
		for (int place = 0; place < nrPlaces; ++place) {
			Node currentPlace = places.get(place);		
			for (int transition = 0; transition < nrTransitions; ++transition) {
				Node currentTransition = transitions.get(transition);
				
				C[place][transition] = 	currentTransition.getChildValue(currentPlace) - 
										currentTransition.getParentValue(currentPlace);
			}	
		}
	}
	
	public int getRowCount() {
		return nrPlaces;
	}
	
	public int getColumnCount() {
		return nrTransitions;
	}
	
	public int getEntry(int rowIndex, int columnIndex) {
		return new Double(C[rowIndex][columnIndex]).intValue();
	}
	
	
	public Set<Place> getHeadPlaces() {		
		//Make a transposed copy of 
		double[][] K = new double[nrTransitions][nrPlaces];
		for (int place = 0; place < nrPlaces; ++place) 				
			for (int transition = 0; transition < nrTransitions; ++transition) 
				K[transition][place] = C[place][transition];
		
		//Generate upper triangle form of the transposed incidence matrix
		//At the same time determine all head places, i.e. the place with the first non-zero value in this matrix
		Set<Place> head = new HashSet<Place>();
		t: for (int currentTransition = 0; currentTransition < nrTransitions; ++currentTransition)
			for (int firstNotZero = currentTransition; firstNotZero < nrPlaces; firstNotZero++)
				if (K[currentTransition][firstNotZero] != 0) {
					head.add(places.get(firstNotZero));
					for (int targetTransition = currentTransition+1; targetTransition < nrTransitions; ++targetTransition)
						addLines(K, -K[targetTransition][firstNotZero]/K[currentTransition][firstNotZero], currentTransition, targetTransition);
					
					continue t;
				}
		
		return head;
	}
	
	private void addLines(double[][] matrix, double factor, int sourceLine, int targetLine) {
		for (int column = 0; column<matrix[0].length; ++column) 
			matrix[targetLine][column] += factor * matrix[sourceLine][column];
	}
}
