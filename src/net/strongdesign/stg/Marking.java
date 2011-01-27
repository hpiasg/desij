package net.strongdesign.stg;


import java.util.*;

/**
 * 
 * <p>
 * <b>History: </b> <br>
 * 24.01.2005: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�fer
 */
public class Marking  {
    protected Map<Place, Integer> marking;
    
    
    public Marking() {
        marking = new HashMap<Place, Integer>();
    }
    
    
    public void setMarking(Place p, Integer i) {
        marking.put(p, i);
    }
    
    public Integer getMarking(Place p) {
        Integer i = marking.get(p);
        
        if (p==null)
            return new Integer(0);
        else 
            return i;        
    }
    
    public int hashCode() {
        return marking.hashCode();
    }
    
    public String toString() {
        StringBuilder res = new StringBuilder();
        boolean emptyMarking = true;
        for (Place place : marking.keySet()) {
            Integer m = marking.get(place);
            if (m != 0) {
                emptyMarking = false;
                if (m == 1)
                	res.append(place.getString(Place.UNIQUE)+" ");
                else
                	res.append(place.getString(Place.UNIQUE)+"="+m +" ");
            }
        }
        
        if (emptyMarking)
            return ("empty marking");
        
        return res.toString();
    }
    

    @Override
    public boolean equals(Object state) {
        if (state==this) 
            return true;
        
        if (! (state instanceof Marking) )
            return false;
        
        Map<Place, Integer> m = ((Marking)state).marking;
        
 
        if ( m.keySet().size() != marking.keySet().size()   )
            return false;
        
        for (Place place : m.keySet()  )
            if (! m.get(place).equals(marking.get(place))   )
                return false;
        
        return true;
    }

}
