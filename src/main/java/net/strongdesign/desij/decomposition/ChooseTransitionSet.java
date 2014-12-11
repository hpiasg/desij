/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

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
