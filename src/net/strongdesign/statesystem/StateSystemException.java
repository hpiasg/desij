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

package net.strongdesign.statesystem;

/**
 * A semantic subclass of {@link Exception}.
 * 
 * <p>
 * <b>History: </b> <br>
 * 24.01.2005: Created <br>
 * 
 * <p>
 * 
 * @author Mark Sch�er
 */
public class StateSystemException extends Exception  {

	private static final long serialVersionUID = 260513272108831970L;

	public StateSystemException(String mes) {
        super(mes);
    }
    
    
}
