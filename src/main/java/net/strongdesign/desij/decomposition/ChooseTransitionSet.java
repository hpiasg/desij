

/*
 * Created on 04.10.2004
 *
 */
package net.strongdesign.desij.decomposition;

import java.util.*;
import net.strongdesign.stg.*;

/**
 * An interface for implementing a subproblem of the decomposition algorithm.
 * It has to be choosen sometimes which transitions should be contracted; an
 * implementation of ChooseTransitionSet should make this decision.
 * 
 * todo Implement more implementing classes with better heuristics
 * @author Mark Sch�fer
 *
 */
public interface ChooseTransitionSet {
    public List<Transition> getTransitions(STG stg);
}
