package net.strongdesign.balsa;

/**
 * Copyright 2012-2014 Stanislavs Golubcovs
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

import java.util.HashMap;

public class BreezeNet implements Cloneable {
	public HashMap<Integer, Object> components = new HashMap<Integer, Object>();
	public int nextComponentID=0; // number for the next component, starts with 0 
	
	public HashMap<Integer, HSChannel> channels = new HashMap<Integer, HSChannel>();
	public int nextChannelID=1; // number for the next channel, starts with 1
	public BreezeNet() {
		
	}
}
